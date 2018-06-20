package com.brzhang.chengyu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.aai.exception.ClientException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    TextView btn, text;

    RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn);
        text = findViewById(R.id.text);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textRecognition();
            }
        });

        rxPermissions = new RxPermissions(this); // where this is an Activity instance
    }

    @SuppressLint("CheckResult")
    private void textRecognition() {
        rxPermissions
                .requestEach(Manifest.permission.READ_PHONE_STATE)
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
