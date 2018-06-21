package com.brzhang.chengyu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.aai.exception.ClientException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    TextView text;
    FloatingActionButton btn;

    RxPermissions rxPermissions;

    private boolean isAAIstarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn);
        text = findViewById(R.id.text);

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                textRecognition();
                return true;
            }
        });
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View pView, MotionEvent pEvent) {
                pView.onTouchEvent(pEvent);
                // We're only interested in when the button is released.
                if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                    // We're only interested in anything if our speak button is currently pressed.
                    // Do something when the button is released.
                    if (isAAIstarted) {
                        stopAAI();
                    }

                }
                return false;
            }
        });

        rxPermissions = new RxPermissions(this); // where this is an Activity instance
    }

    @SuppressLint("CheckResult")
    private void stopAAI() {
        try {
            AAIHelper.newInstance().stopAAI().subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) {
                    if (aBoolean) {
                        isAAIstarted = false;
                    }
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("CheckResult")
    private void textRecognition() {
        if (isAAIstarted) {
            return;
        }
        isAAIstarted = true;
        rxPermissions
                .requestEachCombined(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            startAAI();
                        } else {
                            Log.e("MainActivity", "accept() called with: permission = [" + permission + "]");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        isAAIstarted = false;
                        Log.e("MainActivity", "accept() called with: throwable = [" + throwable + "]");
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void startAAI() throws ClientException {
        AAIHelper.newInstance()
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
                        text.setText(throwable.toString());
                    }
                });
    }

}
