package com.brzhang.voicetotextview;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.AbsCredentialProvider;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.listener.AudioRecognizeTimeoutListener;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencent.aai.model.type.AudioRecognizeConfiguration;
import com.tencent.aai.model.type.AudioRecognizeTemplate;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class AAIHelper {

    private static AAIHelper mInstance;

    private AAIClient aaiClient;

    // 初始化语音识别请求。
    private AudioRecognizeRequest audioRecognizeRequest;
    private AudioRecognizeConfiguration audioRecognizeConfiguration;

    private AAIHelper() {

    }

    /**
     * 初始化实时语音识别的相关参数
     *
     * @param context
     * @param appid
     * @param projectid
     * @param secretId
     * @param secretKey
     * @return true 初始化成功，false 失败
     */
    public boolean init(Context context, int appid, int projectid, String secretId, String secretKey) {
        // 为了方便用户测试，sdk提供了本地签名，但是为了secretKey的安全性，正式环境下请自行在第三方服务器上生成签名。
        AbsCredentialProvider credentialProvider = new LocalCredentialProvider(secretKey);
        try {
            aaiClient = new AAIClient(context, appid, projectid, secretId, credentialProvider);
        } catch (ClientException e) {
            e.printStackTrace();
            return false;
        }
        audioRecognizeRequest = new AudioRecognizeRequest.Builder()
                .pcmAudioDataSource(new AudioRecordDataSource()) // 设置语音源为麦克风输入
                .template(new AudioRecognizeTemplate(1, 0, 0)) // 设置自定义模板
                .build();

        audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                .enableAudioStartTimeout(true) // 是否使能起点超时停止录音
                .enableAudioEndTimeout(true) // 是否使能终点超时停止录音
                .enableSilentDetect(true) // 是否使能静音检测，true表示不检查静音部分
                .minAudioFlowSilenceTime(1000) // 语音流识别时的间隔时间
                .maxAudioFlowSilenceTime(1000) // 语音终点超时时间
                .maxAudioStartSilenceTime(1000) // 语音起点超时时间
                .minVolumeCallbackTime(80) // 音量回调时间
                .sensitive(2)
                .build();
        return true;
    }

    public static AAIHelper getInstance() {
        if (mInstance == null) {
            synchronized (AAIHelper.class) {
                if (mInstance == null) {
                    mInstance = new AAIHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 是否不再录音状态
     *
     * @return
     */
    public boolean isAudioRecognizeIdle() {
        return !aaiClient.isAudioRecognizeIdle();
    }

    @WorkerThread
    public Flowable<VoiceResult> startAAI() {
        if (aaiClient != null) {
            if (!aaiClient.isAudioRecognizeIdle()) {
                return Flowable.empty();
            }
        }
        return Flowable.create(new FlowableOnSubscribe<VoiceResult>() {
            @Override
            public void subscribe(final FlowableEmitter<VoiceResult> emitter) {
                if (aaiClient != null) {
                    aaiClient.startAudioRecognize(audioRecognizeRequest, new AudioRecognizeResultListener() {
                        @Override
                        public void onSliceSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult resultText, int i) {
                            if (!TextUtils.isEmpty(resultText.getText())) {
                                //语音分片的语音识别结果回调接口
                                emitter.onNext(new VoiceResult(true, resultText.getText()));
                            }
                        }

                        @Override
                        public void onSegmentSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult audioRecognizeResult, int i) {
                            if (!TextUtils.isEmpty(audioRecognizeResult.getText())) {
                                //语音流的语音识别结果回调接口
                                emitter.onNext(new VoiceResult(true, audioRecognizeResult.getText()));
                            }
                        }

                        @Override
                        public void onSuccess(AudioRecognizeRequest audioRecognizeRequest, String s) {
                            if (!TextUtils.isEmpty(s)) {
                                //返回所有的识别结果
                                emitter.onNext(new VoiceResult(true, s));
                            }
                        }

                        @Override
                        public void onFailure(AudioRecognizeRequest audioRecognizeRequest, ClientException clientException, ServerException serverException) {
                            if (emitter.isCancelled()) {
                                return;
                            }
                            if (clientException != null) {
                                emitter.onError(clientException);
                            } else if (serverException != null) {
                                emitter.onError(serverException);
                            } else {
                                emitter.onError(new Exception("未知错误"));
                            }

                        }
                    }, new AudioRecognizeStateListener() {
                        @Override
                        public void onStartRecord(AudioRecognizeRequest request) {
                            // 开始录音
                            Log.e("AAIHelper", "onStartRecord() called with: request = [" + request + "]");
                            emitter.onNext(new VoiceResult(true, ""));
                        }

                        @Override
                        public void onStopRecord(AudioRecognizeRequest request) {
                            // 结束录音
                            Log.e("AAIHelper", "onStopRecord() called with: request = [" + request + "]");
                            emitter.onNext(new VoiceResult(false, ""));
//                            emitter.onComplete();
                        }

                        @Override
                        public void onVoiceFlowStart(AudioRecognizeRequest request, int i) {
                            // 语音流开始
                        }

                        @Override
                        public void onVoiceFlowFinish(AudioRecognizeRequest audioRecognizeRequest, int i) {
                            // 语音流结束
                        }

                        @Override
                        public void onVoiceFlowStartRecognize(AudioRecognizeRequest audioRecognizeRequest, int i) {
                            // 语音流开始识别
                        }

                        @Override
                        public void onVoiceFlowFinishRecognize(AudioRecognizeRequest audioRecognizeRequest, int i) {
                            // 语音流结束识别
                        }

                        @Override
                        public void onVoiceVolume(AudioRecognizeRequest audioRecognizeRequest, int i) {
                            // 音量回调
                        }
                    }, new AudioRecognizeTimeoutListener() {
                        @Override
                        public void onFirstVoiceFlowTimeout(AudioRecognizeRequest audioRecognizeRequest) {
//                            emitter.onNext(new VoiceResult(false, ""));
//                            emitter.onComplete();
                        }

                        @Override
                        public void onNextVoiceFlowTimeout(AudioRecognizeRequest audioRecognizeRequest) {
//                            emitter.onNext(new VoiceResult(false, ""));
//                            emitter.onComplete();
                        }
                    }, audioRecognizeConfiguration);
                } else {
                    emitter.onError(new Exception("AAIClient 初始化失败"));
                }

            }
        }, BackpressureStrategy.BUFFER)
                //这里的发送速度太快了，优化下
                .throttleLast(500, TimeUnit.MILLISECONDS);

    }

    /**
     * 停止语音识别
     *
     * @return
     */
    public Flowable<Boolean> stopAAI() {
        final int requestId = audioRecognizeRequest.getRequestId();
        return Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                aaiClient.stopAudioRecognize(requestId);
                emitter.onNext(true);
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }
}
