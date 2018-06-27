package com.brzhang.voicetotextview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.aai.exception.ClientException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VoiceToTextView extends ConstraintLayout {

    private VoiceToTextListener listener;
    private RxPermissions rxPermissions;
    private boolean isAAIstarted;
    private TextView text;
    private FloatingActionButton btn;
    private Context mContext;
    private ProgressBar progressBar;

    private int appid;
    private int projectid;
    private String secretId = "";
    private String secretKey = "";
    private boolean mAAIHelperAvailable;

    public VoiceToTextView setActivity(Activity activity) {
        rxPermissions = new RxPermissions(activity);
        return this;
    }

    public VoiceToTextView setListener(VoiceToTextListener listener) {
        this.listener = listener;
        return this;
    }

    public VoiceToTextView setAppid(int appid) {
        this.appid = appid;
        return this;
    }

    public VoiceToTextView setProjectid(int projectid) {
        this.projectid = projectid;
        return this;
    }

    public VoiceToTextView setSecretId(String secretId) {
        this.secretId = secretId;
        return this;
    }

    public VoiceToTextView setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    /**
     * 初始化各项参数
     *
     * @throws ClientException
     */
    @SuppressLint("CheckResult")
    public void build() {
        rxPermissions.requestEachCombined(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            mAAIHelperAvailable = AAIHelper.getInstance().init(mContext, appid, projectid, secretId, secretKey);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }

    public VoiceToTextView(Context context) {
        this(context, null);
    }

    public VoiceToTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceToTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        View rootView = View.inflate(context, R.layout.voice_to_text_view_layout, this);
        text = rootView.findViewById(R.id.text);
        btn = rootView.findViewById(R.id.btn);
        progressBar = rootView.findViewById(R.id.progress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAAIHelperAvailable) {
                    showASrText();
                    textRecognition();
                }
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAsrText();
                stopAAI();
            }
        });
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (listener != null) {
                    listener.onText(s.toString());
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    private void textRecognition() {
        if (isAAIstarted) {
            return;
        }
        isAAIstarted = true;
        startAAI();
    }

    /**
     * 显示实时语音转文字的文本框
     */
    private void showASrText() {
        text.setText("");
        CircularAnim.show(text).triggerView(btn).go();
        CircularAnim.hide(btn).go();
    }

    /**
     * 隐藏实时语音转文字的文本框
     */
    private void hideAsrText() {
        Point triggerPoint = new Point(text.getRight() - btn.getWidth() / 2, text.getHeight() / 2);
        CircularAnim.hide(text).triggerPoint(triggerPoint).go();
        CircularAnim.show(btn).go();
    }

    @SuppressLint("CheckResult")
    private void startAAI() {
        AAIHelper.getInstance()
                .startAAI()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VoiceResult>() {
                    @Override
                    public void accept(VoiceResult result) {
                        if (result.stateRecordOn) {
                            progressBar.setVisibility(VISIBLE);
                            text.setText(result.text);
                        } else {
                            progressBar.setVisibility(GONE);
                            stopAAI();
                            hideAsrText();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        progressBar.setVisibility(GONE);
                        hideAsrText();
                        stopAAI();
                        showToast(throwable.toString());
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void stopAAI() {
        AAIHelper.getInstance().stopAAI().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    isAAIstarted = false;
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
            }
        });
        hideAsrText();
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

}
