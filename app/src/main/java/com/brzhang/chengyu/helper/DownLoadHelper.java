package com.brzhang.chengyu.helper;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.widget.Toast;

import com.brzhang.chengyu.R;
import com.brzhang.chengyu.model.Version;
import com.brzhang.chengyu.model.SubjectResult;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import eu.inloop.localmessagemanager.LocalMessageManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadHelper {

    private DownloadManager mDownloadManager;
    private static DownLoadHelper downLoadHelper;
    private DownloadReceiver mReceiver;
    private Activity activity;

    public static DownLoadHelper getInstance() {
        if (downLoadHelper == null) {
            synchronized (DownLoadHelper.class) {
                if (downLoadHelper == null) {
                    downLoadHelper = new DownLoadHelper();
                }
            }
        }
        return downLoadHelper;
    }

    public void init(Activity context) {
        activity = context;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        mReceiver = new DownloadReceiver();
        activity.registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 检查题库最新版本号是否比本地新
     */
    public Flowable<Boolean> checkVersison() {
        PrefHelper.getInstance().setVersion(0);
        final OkHttpClient client = new OkHttpClient();
        final Moshi moshi = new Moshi.Builder().build();

        final JsonAdapter<Version> versionJsonAdapter = moshi.adapter(Version.class);
        final Request request = new Request.Builder()
                .url("http://111.230.169.95:8888/version")
                .build();

        return Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    Version version = versionJsonAdapter.fromJson(response.body().source());
                    int versionNum = PrefHelper.getInstance().getVersion();
                    if (version.getVersion() > versionNum) {
                        emitter.onNext(true);
                    } else {
                        emitter.onNext(false);
                    }
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }

            }
        }, BackpressureStrategy.BUFFER);

    }

    /**
     * 下载题库
     */
    public void downLoadData() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://111.230.169.95:8888/data"));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("下载题库...");
        long id = mDownloadManager.enqueue(request);
        //保存下载id
        PrefHelper.getInstance().setRequestId(id);
    }

    public void release() {
        activity.unregisterReceiver(mReceiver);
    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                parseTImuData(context);
            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {

                Toast.makeText(context, "请稍等...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseTImuData(Context context) {
        Uri uri = mDownloadManager.getUriForDownloadedFile(PrefHelper.getInstance().getRequestId());
        try {
            String content = readContentFromFile(context, uri);
            SubjectResult subjectResult = new Moshi.Builder().build().adapter(SubjectResult.class).fromJson(content);
            if (subjectResult != null) {
                PrefHelper.getInstance().setVersion(subjectResult.getVersion());
                DBHelper.getInstance(activity).insertTimuList(subjectResult.getData());
                Toast.makeText(context, "题库更新成功", Toast.LENGTH_LONG).show();
                LocalMessageManager.getInstance().send(R.id.timu_download_finished);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件内容
     *
     * @param uri
     * @return
     */
    private String readContentFromFile(Context context, Uri uri) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(context.getContentResolver().openInputStream(uri))));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
