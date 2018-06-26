package com.brzhang.chengyu;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brzhang.voicetotextview.VoiceToTextView;
import com.tencent.aai.exception.ClientException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VoiceToTextView mVoiceToTextView;

    private AppCompatTextView mTextView1, mTextView2, mTextView3, mTextView4;

    private RecyclerView mCandidates;

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVoiceToTextView = findViewById(R.id.voice_to_text_view);
        mTextView1 = findViewById(R.id.text1);
        mTextView2 = findViewById(R.id.text2);
        mTextView3 = findViewById(R.id.text3);
        mTextView4 = findViewById(R.id.text4);
        mCandidates = findViewById(R.id.candidates);
        mCandidates.setLayoutManager(new GridLayoutManager(this, 6));
        mCandidates.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 5;
                outRect.right = 5;
                outRect.top = 5;
                outRect.bottom = 5;
            }
        });
        initData();
    }

    private void initData() {
        List<CandiItem> list = new ArrayList<>();
        listAdapter = new ListAdapter(this, list);
        mCandidates.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        try {
            mVoiceToTextView
                    .setActivity(this)
                    .setAppid(1251114236)
                    .setProjectid(1114271)
                    .setSecretId("AKIDkHZiiUrLQGsFNIlShhS1KNFrDJ8hY3rP")
                    .setSecretKey("QbWCdokQr3zf6HF0WnqkPo21kESQAett")
                    .build();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Activity activity;

        private List<CandiItem> items;

        public ListAdapter(Activity activity, List<CandiItem> items) {
            this.activity = activity;
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.candi_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // TODO: 2018/6/26 被使用了的状态
            holder.textView.setText(items.get(position).getItem());
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (AppCompatTextView) itemView;
        }
    }

}
