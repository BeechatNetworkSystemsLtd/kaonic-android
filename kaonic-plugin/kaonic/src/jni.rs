use std::sync::{Arc, Mutex};

use base64::prelude::BASE64_STANDARD;
use base64::Engine;
use jni::objects::{GlobalRef, JByteArray, JClass, JMethodID, JObject, JString, JValue};
use jni::signature::{Primitive, ReturnType};
use jni::sys::{jlong, jstring};
use jni::{JNIEnv, JavaVM};

use android_log;
use log::{self, LevelFilter};
use rand_core::OsRng;
use reticulum::destination::SingleInputDestination;
use reticulum::identity::PrivateIdentity;
use reticulum::iface::kaonic::kaonic_grpc::KaonicGrpc;
use reticulum::iface::tcp_client::TcpClient;
use tokio::runtime::Runtime;
use tokio::sync::mpsc::Sender;
use tokio_util::sync::CancellationToken;

use crate::event::Event;
use crate::model::CallAudioData;
use crate::{Messenger, MessengerCommand, Platform};

#[derive(Clone)]
struct KaonicJni {
    _context: GlobalRef,
    obj: GlobalRef,
    receive_method: JMethodID,
    start_audio_method: JMethodID,
    stop_audio_method: JMethodID,
    feed_audio_method: JMethodID,
    jvm: Arc<JavaVM>,
}

struct KaonicLib {
    jni: Arc<Mutex<KaonicJni>>,
    runtime: Arc<Runtime>,
    cancel: CancellationToken,
    cmd_send: Sender<Event>,
}

struct PlatformJni {
    jni: Arc<Mutex<KaonicJni>>,
}

impl Platform for PlatformJni {
    fn send_event(&mut self, event: &crate::event::Event) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        let json = serde_json::to_string_pretty(&event).expect("valid json string");

        let event_json_str = env.new_string(json).unwrap();

        let arguments = [JValue::Object(&event_json_str).as_jni()];

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.receive_method,
                ReturnType::Primitive(Primitive::Void),
                &arguments[..],
            )
            .unwrap()
        };
    }

    fn start_audio(&mut self) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.start_audio_method,
                ReturnType::Primitive(Primitive::Void),
                &[],
            )
            .unwrap()
        };
    }

    fn stop_audio(&mut self) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.stop_audio_method,
                ReturnType::Primitive(Primitive::Void),
                &[],
            )
            .unwrap()
        };
    }

    fn feed_audio(&mut self, audio_data: &[u8]) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        let byte_array = env.new_byte_array(audio_data.len() as i32).unwrap();

        let buffer: &[i8] = unsafe { std::mem::transmute(audio_data) };

        env.set_byte_array_region(&byte_array, 0, buffer)
            .expect("byte array with data");

        let arguments = [JValue::Object(&byte_array).as_jni()];

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.feed_audio_method,
                ReturnType::Primitive(Primitive::Void),
                &arguments[..],
            )
            .unwrap()
        };
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_libraryInit(_env: JNIEnv) {
    android_log::init("kaonic").unwrap();
    log::set_max_level(LevelFilter::Debug);
    log::info!("kaonic library initialized");
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeInit(
    mut env: JNIEnv,
    obj: JObject,
    context: JObject,
) -> jlong {
    let runtime = Arc::new(
        tokio::runtime::Builder::new_multi_thread()
            .worker_threads(24)
            .enable_all()
            .build()
            .expect("tokio runtime"),
    );

    let jni = {
        let jvm = env.get_java_vm().expect("failed to get JavaVM");
        let jvm = Arc::new(jvm);

        let obj = env
            .new_global_ref(obj)
            .expect("Failed to create global ref");

        let class = env.get_object_class(obj.clone()).expect("object class");

        let receive_method = env
            .get_method_id(&class, "receive", "(Ljava/lang/String;)V")
            .expect("event method");

        let start_audio_method = env
            .get_method_id(&class, "startAudio", "()V")
            .expect("start audio method");

        let stop_audio_method = env
            .get_method_id(&class, "stopAudio", "()V")
            .expect("stop audio method");

        let feed_audio_method = env
            .get_method_id(&class, "feedAudio", "([B)V")
            .expect("feed audio method");

        KaonicJni {
            _context: env
                .new_global_ref(context)
                .expect("Failed to create global ref"),
            obj,
            receive_method,
            start_audio_method,
            stop_audio_method,
            feed_audio_method,
            jvm,
        }
    };

    let (cmd_send, _) = tokio::sync::mpsc::channel(1);
    let lib = Box::new(KaonicLib {
        jni: Arc::new(Mutex::new(jni)),
        runtime,
        cancel: CancellationToken::new(),
        cmd_send,
    });

    Box::into_raw(lib) as jlong
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeTransmit(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    event: JString,
) {
    let lib = unsafe { &*(ptr as *const KaonicLib) };

    let event_str: String = match env.get_string(&event) {
        Ok(jstr) => jstr.into(),
        Err(_) => "{}".into(),
    };

    let event = serde_json::from_str::<Event>(&event_str);
    if let Ok(event) = event {
        let _ = lib.cmd_send.blocking_send(event);
    } else if let Err(err) = event {
        log::error!("can't parse event {} '{}'", err, event_str);
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeDestroy(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    // Safety: ptr must be a valid pointer created by nativeInit
    unsafe {
        let _state = Box::from_raw(ptr as *mut KaonicLib);
        // Box will be dropped here, cleaning up our state
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeStart(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    identity: JString,
) {
    // Safety: ptr must be a valid pointer created by nativeInit
    let lib = unsafe { &mut *(ptr as *mut KaonicLib) };

    lib.cancel.cancel();
    lib.cancel = CancellationToken::new();

    let (cmd_send, cmd_recv) = tokio::sync::mpsc::channel(1);
    lib.cmd_send = cmd_send;

    // Convert JString to Rust String
    let identity_hex: String = match env.get_string(&identity) {
        Ok(jstr) => jstr.into(),
        Err(_) => {
            eprintln!("Failed to convert JString to Rust String");
            return;
        }
    };

    // Convert hex string into PrivateIdentity
    match PrivateIdentity::new_from_hex_string(&identity_hex) {
        Ok(identity) => {
            lib.runtime.spawn(messenger_task(
                identity,
                cmd_recv,
                lib.jni.clone(),
                lib.cancel.clone(),
            ));
        }
        Err(_) => log::error!("can't create private identity"),
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeSendAudio(
    env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    data: JByteArray,
) {
    let lib = unsafe { &mut *(ptr as *mut KaonicLib) };

    let data: Vec<u8> = match env.convert_byte_array(data) {
        Ok(bytes) => bytes,
        Err(_) => vec![],
    };

    let call_audio_data = CallAudioData {
        address: "".into(),
        call_id: "".into(),
        data: BASE64_STANDARD.encode(data),
    };

    let _ = lib
        .cmd_send
        .blocking_send(Event::CallAudioData(call_audio_data));
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_libsource_KaonicLib_nativeGenerate(
    env: JNIEnv,
    _obj: JObject,
    _ptr: jlong,
) -> jstring {
    // Generate new identity
    let identity = PrivateIdentity::new_from_rand(OsRng);

    let _destination = SingleInputDestination::new(
        identity.clone(),
        Messenger::<PlatformJni>::destination_name(),
    );

    let secret = identity.to_hex_string();

    env.new_string(&secret).unwrap().into_raw()
}

async fn messenger_task(
    identity: PrivateIdentity,
    mut cmd_rx: tokio::sync::mpsc::Receiver<Event>,
    jni: Arc<Mutex<KaonicJni>>,
    cancel: CancellationToken,
) {
    let messenger = Messenger::new(identity, "messenger", PlatformJni { jni });

    // Setup all interfaces
    {
        // messenger
        //     .iface_manager()
        //     .await
        //     .lock()
        //     .await
        //     .spawn(TcpClient::new("192.168.1.134:4242"), TcpClient::spawn);

        messenger.iface_manager().await.lock().await.spawn(
            KaonicGrpc::new(
            format!("http://{}", "192.168.10.1:8080"),
                reticulum::iface::kaonic::RadioModule::RadioA,
            ),
            KaonicGrpc::spawn,
        );
    }

    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },

            Some(cmd) = cmd_rx.recv() => {
                match cmd {
                    Event::Message(message) => {
                        messenger.send(MessengerCommand::SendMessage(message)).await;
                    }
                    Event::CallAudioData(call_audio_data) => {
                        messenger.send(MessengerCommand::CallAudioData(call_audio_data)).await;
                    },
                    Event::ContactFound(_)=>{},
                    Event::MessageAcknowledge(_)=>{},
                }
            },
        }
    }
}
