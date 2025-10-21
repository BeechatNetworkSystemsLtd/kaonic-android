#include <jni.h>
#include <gst/gst.h>
#include <gst/app/gstappsrc.h>
#include <gst/video/videooverlay.h>
#include <android/native_window_jni.h>
#include <string.h>

typedef struct {
    GstElement *pipeline;
    GstElement *appsrc;
    GstElement *decoder;
    GstElement *sink;
    GMainLoop *loop;
} ReceiverContext;

// Callback function for pad-added signal
static void on_pad_added(GstElement *src, GstPad *pad, gpointer data) {
    GstElement *sink = GST_ELEMENT(data);
    GstPad *sinkpad = gst_element_get_static_pad(sink, "sink");
    if (!gst_pad_is_linked(sinkpad)) {
        gst_pad_link(pad, sinkpad);
    }
    gst_object_unref(sinkpad);
}

JNIEXPORT jlong JNICALL
Java_network_beechat_kaonic_video_ReceiverPipelineManager_nativeInit(JNIEnv *env, jobject thiz, jobject surface) {
    gst_init(NULL, NULL);

    ReceiverContext *ctx = g_malloc0(sizeof(ReceiverContext));

    // Create pipeline elements
    ctx->appsrc = gst_element_factory_make("appsrc", "receiver_appsrc");
    ctx->decoder = gst_element_factory_make("decodebin", "decoder");
    ctx->sink = gst_element_factory_make("glimagesink", "video_sink");

    if (!ctx->appsrc || !ctx->decoder || !ctx->sink) {
        g_printerr("Failed to create GStreamer elements\n");
        return 0;
    }

    ctx->pipeline = gst_pipeline_new("receiver-pipeline");
    gst_bin_add_many(GST_BIN(ctx->pipeline), ctx->appsrc, ctx->decoder, ctx->sink, NULL);

    if (!gst_element_link(ctx->appsrc, ctx->decoder)) {
        g_printerr("Failed to link appsrc and decoder\n");
        return 0;
    }

    // Connect decodebin pad-added signal to link dynamically
    g_signal_connect(ctx->decoder, "pad-added", G_CALLBACK(on_pad_added), ctx->sink);

    // Set caps (optional, depends on stream type)
    GstCaps *caps = gst_caps_new_simple("video/mpegts",
                                        "systemstream", G_TYPE_BOOLEAN, TRUE,
                                        "packetsize", G_TYPE_INT, 188, NULL);
    gst_app_src_set_caps(GST_APP_SRC(ctx->appsrc), caps);
    gst_caps_unref(caps);

    // Configure appsrc properties
    g_object_set(ctx->appsrc,
                 "format", GST_FORMAT_TIME,
                 "stream-type", 0,
                 "is-live", TRUE,
                 "do-timestamp", TRUE,
                 NULL);

    // Set native surface
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    gst_video_overlay_set_window_handle(GST_VIDEO_OVERLAY(ctx->sink), (guintptr)window);
    ANativeWindow_release(window);

    // Set pipeline to playing
    gst_element_set_state(ctx->pipeline, GST_STATE_PLAYING);

    return (jlong)(intptr_t)ctx;
}

JNIEXPORT void JNICALL
Java_network_beechat_kaonic_video_ReceiverPipelineManager_nativePush(JNIEnv *env, jobject thiz, jlong handle, jbyteArray data, jint length) {
    ReceiverContext *ctx = (ReceiverContext *)(intptr_t)handle;
    if (!ctx || !ctx->appsrc || length <= 0) return;

    static GstClockTime pts = 0;
    static GstClockTime last_push_time = 0;
    const GstClockTime frame_duration = gst_util_uint64_scale_int(1, GST_SECOND, 30); // 30 fps

    GstClockTime now = gst_util_get_timestamp();
    GstClockTime delta = now - last_push_time;
    last_push_time = now;

    GstBuffer *buffer = gst_buffer_new_allocate(NULL, length, NULL);
    GstMapInfo map;
    gst_buffer_map(buffer, &map, GST_MAP_WRITE);
    (*env)->GetByteArrayRegion(env, data, 0, length, (jbyte *)map.data);
    gst_buffer_unmap(buffer, &map);

    // Assign fixed-step PTS/DTS to avoid jitter/skew
    GST_BUFFER_PTS(buffer) = pts;
    GST_BUFFER_DTS(buffer) = pts;
    GST_BUFFER_DURATION(buffer) = frame_duration;
    pts += frame_duration;

    GstFlowReturn ret = gst_app_src_push_buffer(GST_APP_SRC(ctx->appsrc), buffer);
    if (ret != GST_FLOW_OK) {
//        __android_log_print(ANDROID_LOG_ERROR, "GStreamerPush", "Failed to push buffer: %d", ret);
    }
}

JNIEXPORT void JNICALL
Java_network_beechat_kaonic_video_ReceiverPipelineManager_nativeStop(JNIEnv *env, jobject thiz, jlong handle) {
    ReceiverContext *ctx = (ReceiverContext *)(intptr_t)handle;
    if (!ctx) return;

    gst_element_set_state(ctx->pipeline, GST_STATE_NULL);
    gst_object_unref(ctx->pipeline);
    g_free(ctx);
}