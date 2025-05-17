use std::sync::{Arc, Mutex};

use jni::objects::{GlobalRef, JByteArray, JClass, JMethodID, JObject, JString, JValue};
use jni::signature::{Primitive, ReturnType};
use jni::sys::{jlong, jstring};
use jni::{JNIEnv, JavaVM};

use rand_core::OsRng;

use reticulum::destination::SingleInputDestination;
use reticulum::hash::AddressHash;
use reticulum::identity::PrivateIdentity;
use reticulum::iface::kaonic::kaonic_grpc::proto::configuration_request::PhyConfig;
use reticulum::iface::kaonic::kaonic_grpc::KaonicGrpc;
use reticulum::iface::kaonic::RadioConfig;
use reticulum::iface::tcp_client::TcpClient;

use serde::{Deserialize, Serialize};
use tokio::runtime::Runtime;
use tokio::sync::mpsc::Sender;
use tokio_util::sync::CancellationToken;

use android_log;
use log::{self, LevelFilter};

use crate::event::Event;
use crate::messenger::{Messenger, MessengerCommand, Platform};
use crate::model::{Connection, ContactData, FileChunk, MessengerError};

#[derive(Clone)]
struct KaonicJni {
    _context: GlobalRef,
    obj: GlobalRef,

    receive_method: JMethodID,
    start_audio_method: JMethodID,
    stop_audio_method: JMethodID,
    feed_audio_method: JMethodID,
    request_file_chunk_method: JMethodID,
    receive_file_chunk_method: JMethodID,

    jvm: Arc<JavaVM>,
}

#[derive(Serialize, Deserialize)]
struct MessengerStartConfig {
    contact: ContactData,
    connections: Vec<Connection>,
}

#[derive(Serialize, Deserialize)]
struct KaonicConfig {
    module: i32,
    freq: u32,
    channel: u32,
    channel_spacing: u32,
    tx_power: u32,
    msc: u32,
    opt: u32,
}

impl KaonicConfig {
    pub fn to_radio_config(&self) -> RadioConfig {
        RadioConfig {
            module: self.module,
            freq: self.freq,
            channel: self.channel,
            channel_spacing: self.channel_spacing,
            tx_power: self.tx_power,
            phy_config: Some(PhyConfig::Ofdm(
                reticulum::iface::kaonic::kaonic_grpc::proto::RadioPhyConfigOfdm {
                    mcs: self.msc,
                    opt: self.opt,
                },
            )),
        }
    }
}

struct KaonicLib {
    jni: Arc<Mutex<KaonicJni>>,
    runtime: Arc<Runtime>,
    cancel: CancellationToken,
    cmd_send: Sender<MessengerCommand>,
    kaonic_config_send: Sender<RadioConfig>,
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

    fn request_file_chunk(&mut self, address: &String, file_id: &String, chunk_size: usize) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        let address = env.new_string(address).expect("new address string");
        let file_id = env.new_string(file_id).expect("new id string");

        let arguments = [
            JValue::Object(&address).as_jni(),
            JValue::Object(&file_id).as_jni(),
            JValue::Int(chunk_size as i32).as_jni(),
        ];

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.request_file_chunk_method,
                ReturnType::Primitive(Primitive::Void),
                &arguments[..],
            )
            .expect("requests method call");
        };
    }

    fn receive_file_chunk(&mut self, address: &String, file_id: &String, data: &[u8]) {
        let jni = self.jni.lock().expect("jni locked");

        let mut env = jni
            .jvm
            .attach_current_thread_permanently()
            .expect("failed to attach thread");

        let address = env.new_string(address).expect("new address string");
        let file_id = env.new_string(file_id).expect("new id string");

        let byte_array = env.new_byte_array(data.len() as i32).unwrap();
        let buffer: &[i8] = unsafe { std::mem::transmute(data) };

        env.set_byte_array_region(&byte_array, 0, buffer)
            .expect("byte array with data");

        let arguments = [
            JValue::Object(&address).as_jni(),
            JValue::Object(&file_id).as_jni(),
            JValue::Object(&byte_array).as_jni(),
        ];

        unsafe {
            env.call_method_unchecked(
                &jni.obj,
                jni.receive_file_chunk_method,
                ReturnType::Primitive(Primitive::Void),
                &arguments[..],
            )
            .unwrap()
        };
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_libraryInit(_env: JNIEnv) {
    android_log::init("kaonic").unwrap();
    log::set_max_level(LevelFilter::Debug);
    log::info!("kaonic library initialized");
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeInit(
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

        let request_file_chunk_method = env
            .get_method_id(
                &class,
                "requestFileChunk",
                "(Ljava/lang/String;Ljava/lang/String;I)V",
            )
            .expect("request file chunk method");

        let receive_file_chunk_method = env
            .get_method_id(
                &class,
                "receiveFileChunk",
                "(Ljava/lang/String;Ljava/lang/String;[B)V",
            )
            .expect("receive file chunk method");

        KaonicJni {
            _context: env
                .new_global_ref(context)
                .expect("Failed to create global ref"),
            obj,
            receive_method,
            start_audio_method,
            stop_audio_method,
            feed_audio_method,
            request_file_chunk_method,
            receive_file_chunk_method,
            jvm,
        }
    };

    let (cmd_send, _) = tokio::sync::mpsc::channel(1);
    let (kaonic_config_send, _) = tokio::sync::mpsc::channel(1);
    let lib = Box::new(KaonicLib {
        jni: Arc::new(Mutex::new(jni)),
        runtime,
        cancel: CancellationToken::new(),
        cmd_send,
        kaonic_config_send,
    });

    Box::into_raw(lib) as jlong
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeSendEvent(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    event: JString,
) {
    let event = parse_json_param::<Event>(&mut env, &event);
    if let Ok(event) = event {
        let lib = unsafe { &*(ptr as *const KaonicLib) };
        match event {
            Event::Message(message) => {
                let _ = lib
                    .cmd_send
                    .blocking_send(MessengerCommand::SendMessage(message));
            }
            Event::FileStart(file_start) => {
                let _ = lib
                    .cmd_send
                    .blocking_send(MessengerCommand::SendFileStart(file_start));
            }
            Event::ChatCreate(chat) => {
                let _ = lib
                    .cmd_send
                    .blocking_send(MessengerCommand::ChatCreate(chat));
            }
            _ => {}
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeSendFileChunk(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    address: JString,
    id: JString,
    data: JByteArray,
) {
    let lib = unsafe { &*(ptr as *const KaonicLib) };

    let id: String = match env.get_string(&id) {
        Ok(jstr) => jstr.into(),
        Err(_) => "".into(),
    };

    let address: String = match env.get_string(&address) {
        Ok(jstr) => jstr.into(),
        Err(_) => "".into(),
    };

    let data: Vec<u8> = match env.convert_byte_array(data) {
        Ok(bytes) => bytes,
        Err(_) => vec![],
    };

    let file_chunk = FileChunk {
        address,
        id: AddressHash::new_from_rand(OsRng).to_hex_string(),
        file_id: id,
        chat_id: "".into(),
        data,
    };

    let _ = lib
        .cmd_send
        .blocking_send(MessengerCommand::SendFileChunk(file_chunk));
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeDestroy(
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
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeConfigure(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    config_json: JString,
) {
    // Safety: ptr must be a valid pointer created by nativeInit
    let lib = unsafe { &mut *(ptr as *mut KaonicLib) };

    let kaonic_config = parse_json_param::<KaonicConfig>(&mut env, &config_json)
        .expect("valid kaonic config");

    let _ = lib
        .kaonic_config_send
        .blocking_send(kaonic_config.to_radio_config());
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeStart(
    mut env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    identity: JString,
    start_config_json: JString,
) {
    // Safety: ptr must be a valid pointer created by nativeInit
    let lib = unsafe { &mut *(ptr as *mut KaonicLib) };

    lib.cancel.cancel();
    lib.cancel = CancellationToken::new();

    let (cmd_send, cmd_recv) = tokio::sync::mpsc::channel(1);
    lib.cmd_send = cmd_send;

    let (kaonic_config_send, kaonoc_config_recv) = tokio::sync::mpsc::channel(1);
    lib.kaonic_config_send = kaonic_config_send;

    let identity_hex: String = match env.get_string(&identity) {
        Ok(jstr) => jstr.into(),
        Err(_) => {
            log::error!("invalid secret for identity");
            return;
        }
    };

    let start_config = parse_json_param::<MessengerStartConfig>(&mut env, &start_config_json)
        .expect("valid start config");

    // Convert hex string into PrivateIdentity
    match PrivateIdentity::new_from_hex_string(&identity_hex) {
        Ok(identity) => {
            lib.runtime.spawn(messenger_task(
                identity,
                cmd_recv,
                kaonoc_config_recv,
                lib.jni.clone(),
                start_config,
                lib.cancel.clone(),
            ));
        }
        Err(_) => log::error!("can't create private identity"),
    }
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeSendAudio(
    env: JNIEnv,
    _obj: JObject,
    ptr: jlong,
    data: JByteArray,
) {
    let _lib = unsafe { &mut *(ptr as *mut KaonicLib) };

    let _data: Vec<u8> = match env.convert_byte_array(data) {
        Ok(bytes) => bytes,
        Err(_) => vec![],
    };
}

#[no_mangle]
pub extern "system" fn Java_network_beechat_kaonic_impl_KaonicLib_nativeGenerate(
    env: JNIEnv,
    _obj: JObject,
    _ptr: jlong,
) -> jstring {
    // Generate new identity
    let identity = PrivateIdentity::new_from_rand(OsRng);

    // TODO: create JSON output to provide generated address hash
    let _destination = SingleInputDestination::new(
        identity.clone(),
        Messenger::<PlatformJni>::destination_name(),
    );

    let secret = identity.to_hex_string();

    env.new_string(&secret).unwrap().into_raw()
}

async fn messenger_task(
    identity: PrivateIdentity,
    mut cmd_rx: tokio::sync::mpsc::Receiver<MessengerCommand>,
    mut kaonic_config_rx: tokio::sync::mpsc::Receiver<RadioConfig>,
    jni: Arc<Mutex<KaonicJni>>,
    config: MessengerStartConfig,
    cancel: CancellationToken,
) {
    log::info!(
        "kaonic: start messenger for contact '{}'",
        config.contact.name
    );

    let mut kaonic_config_rx = Some(kaonic_config_rx);

    let messenger = Messenger::new(identity, config.contact, "messenger", PlatformJni { jni });

    // Setup all interfaces
    for connection in &config.connections {
        match connection {
            Connection::TcpClient(info) => {
                log::debug!("> add tcp client interface: {} <", info.address);
                messenger
                    .iface_manager()
                    .await
                    .lock()
                    .await
                    .spawn(TcpClient::new(info.address.clone()), TcpClient::spawn);
            }
            Connection::KaonicClient(info) => {
                log::debug!("> add kaonic client interface: {} <", info.address);
                messenger.iface_manager().await.lock().await.spawn(
                    KaonicGrpc::new(
                        info.address.clone(),
                        RadioConfig::new_for_module(reticulum::iface::kaonic::RadioModule::RadioA),
                        kaonic_config_rx.take(),
                    ),
                    KaonicGrpc::spawn,
                );
            }
        }
    }

    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                break;
            },
            Some(cmd) = cmd_rx.recv() => {
                messenger.send(cmd).await;
            },
        }
    }
}

fn parse_json_param<T: serde::de::DeserializeOwned>(
    env: &mut JNIEnv,
    input_json: &JString,
) -> Result<T, MessengerError> {
    let input_json: String = match env.get_string(&input_json) {
        Ok(jstr) => jstr.into(),
        Err(_) => {
            return Err(MessengerError::SerdeError);
        }
    };

    let result = serde_json::from_str::<T>(&input_json);
    if let Err(err) = result {
        log::error!("incorrect input json: \"{}\"", err);
        return Err(MessengerError::SerdeError);
    }

    Ok(result.unwrap())
}
