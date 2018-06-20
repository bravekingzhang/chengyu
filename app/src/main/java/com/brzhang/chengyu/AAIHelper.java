package com.brzhang.chengyu;

import android.support.annotation.WorkerThread;

import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.AbsCredentialProvider;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;

import java.util.concurrent.Future;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class AAIHelper {

    private static AAIHelper mInstance;

    private final AAIClient aaiClient;

    private final static int appid = 124124124;
    private final static int projectid = 21112312;
    private final String secretId = "XXX";

    // 初始化语音识别请求。
    private final AudioRecognizeRequest audioRecognizeRequest;

    private AAIHelper() throws ClientException {
        // 为了方便用户测试，sdk提供了本地签名，但是为了secretKey的安全性，正式环境下请自行在第三方服务器上生成签名。
        AbsCredentialProvider credentialProvider = new LocalCredentialProvider("your secretKey");
        aaiClient = new AAIClient(App.getContext(), appid, projectid, secretId, credentialProvider);
        audioRecognizeRequest = new AudioRecognizeRequest.Builder()
                .pcmAudioDataSource(new AudioRecordDataSource()) // 设置语音源为麦克风输入
                .build();
    }

    public static AAIHelper newInstance() throws ClientException {
        if (mInstance == null) {
            synchronized (AAIHelper.class) {
                if (mInstance == null) {
                    mInstance = new AAIHelper();
                }
            }
        }
        return mInstance;
    }

    @WorkerThread
    public Flowable<String> startAAI() {
        return Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(final FlowableEmitter<String> emitter) {
                if (aaiClient != null) {
                    aaiClient.startAudioRecognize(audioRecognizeRequest, new AudioRecognizeResultListener() {
                        @Override
                        public void onSliceSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult audioRecognizeResult, int i) {

                        }

                        @Override
                        public void onSegmentSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult audioRecognizeResult, int i) {

                        }

                        @Override
                        public void onSuccess(AudioRecognizeRequest audioRecognizeRequest, String s) {
                            emitter.onNext(s);
                            emitter.onComplete();
                        }

                        @Override
                        public void onFailure(AudioRecognizeRequest audioRecognizeRequest, ClientException clientException, ServerException serverException) {
                            if (clientException != null) {
                                emitter.onError(clientException);
                            } else if (serverException != null) {
                                emitter.onError(serverException);
                            } else {
                                emitter.onError(new Exception("未知错误"));
                            }

                        }
                    });
                } else {
                    emitter.onError(new Exception("AAIClient 初始化失败"));
                }

            }
        }, BackpressureStrategy.BUFFER);

    }
}
