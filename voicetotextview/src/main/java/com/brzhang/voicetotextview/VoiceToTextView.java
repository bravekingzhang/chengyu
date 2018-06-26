package com.brzhang.voicetotextview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.aai.exception.ClientException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VoiceToTextView extends ConstraintLayout {

    private RxPermissions rxPermissions;
    private boolean isAAIstarted;
    private TextView text;
    private FloatingActionButton btn;
    private Context mContext;

    private int appid;
    private int projectid;
    private String secretId = "";
    private String secretKey = "";

    public VoiceToTextView setActivity(Activity activity) {
        rxPermissions = new RxPermissions(activity);
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
    public void build() throws ClientException {
        AAIHelper.getInstance().init(mContext, this.appid, projectid, secretId, secretKey);
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
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showASrText();
                textRecognition();
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAsrText();
                stopAAI();
            }
        });
    }

    @SuppressLint("CheckResult")
    private void textRecognition() {
        if (isAAIstarted) {
            return;
        }
        isAAIstarted = true;
        rxPermissions.requestEachCombined(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            startAAI();
                        } else {
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        isAAIstarted = false;
                        hideAsrText();
                    }
                });
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
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        text.setText(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
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
