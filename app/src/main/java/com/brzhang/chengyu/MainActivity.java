package com.brzhang.chengyu;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brzhang.voicetotextview.VoiceToTextListener;
import com.brzhang.voicetotextview.VoiceToTextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VoiceToTextView mVoiceToTextView;

    private List<AppCompatTextView> mAnswerTextViews = new LinkedList<>();

    private RecyclerView mCandidates;

    private ListAdapter listAdapter;

    private List<CandiItem> mSelectedCandiItems = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVoiceToTextView = findViewById(R.id.voice_to_text_view);
        mAnswerTextViews.add((AppCompatTextView) findViewById(R.id.text1));
        mAnswerTextViews.add((AppCompatTextView) findViewById(R.id.text2));
        mAnswerTextViews.add((AppCompatTextView) findViewById(R.id.text3));
        mAnswerTextViews.add((AppCompatTextView) findViewById(R.id.text4));
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
        for (AppCompatTextView mAnswerTextView : mAnswerTextViews) {
            mAnswerTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() != null) {
                        CandiItem candiItem = (CandiItem) v.getTag();
                        candiItem.setSelected(false);
                        mSelectedCandiItems.remove(candiItem);
                        listAdapter.notifyItemChanged(candiItem.getIndex());
                        positionSelectedCandiItems();
                    }
                }
            });
        }
        List<CandiItem> list = new ArrayList<>();
        list.add(new CandiItem(0, "非"));
        list.add(new CandiItem(1, "非"));
        list.add(new CandiItem(2, "非"));
        list.add(new CandiItem(3, "非"));
        list.add(new CandiItem(4, "非"));
        list.add(new CandiItem(5, "非"));
        list.add(new CandiItem(6, "非"));
        list.add(new CandiItem(7, "非"));
        list.add(new CandiItem(8, "非"));
        list.add(new CandiItem(9, "非"));
        list.add(new CandiItem(10, "非"));
        list.add(new CandiItem(11, "非"));
        list.add(new CandiItem(12, "非"));
        list.add(new CandiItem(13, "非"));
        list.add(new CandiItem(14, "非"));
        list.add(new CandiItem(15, "非"));
        list.add(new CandiItem(16, "非"));
        list.add(new CandiItem(17, "非"));
        listAdapter = new ListAdapter(this, list);
        mCandidates.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        mVoiceToTextView
                .setActivity(this)
                .setAppid(1251114236)
                .setProjectid(1114271)
                .setSecretId("AKIDkHZiiUrLQGsFNIlShhS1KNFrDJ8hY3rP")
                .setSecretKey("QbWCdokQr3zf6HF0WnqkPo21kESQAett")
                .setListener(new VoiceToTextListener() {
                    @Override
                    public void onText(String text) {
                        textAutoSelected(text);
                    }
                })
                .build();
    }

    /**
     * 将语言转化出来的文本自动填充
     *
     * @param s
     */
    private void textAutoSelected(String s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        mSelectedCandiItems.clear();
        for (int i = 0; i < s.length() && i < 4; i++) {
            String word = String.valueOf(s.charAt(i));
            for (CandiItem candiItem : listAdapter.items) {
                if (!candiItem.isSelected() && candiItem.getItem().equals(word)) {
                    candiItem.setSelected(true);
                    mSelectedCandiItems.add(candiItem);
                    positionSelectedCandiItems();
                    listAdapter.notifyItemChanged(candiItem.getIndex());
                    break;
                }
            }
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
            CandiItem candiItem = items.get(position);
            if (!candiItem.isSelected()) {
                holder.textView.setText(candiItem.getItem());
            } else {
                holder.textView.setText("");
            }
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    CandiItem candiItem = items.get(position);
                    if (candiItem.isSelected() || candidateCount() >= 4) {
                        return;
                    } else {
                        candiItem.setSelected(true);
                        mSelectedCandiItems.add(candiItem);
                        positionSelectedCandiItems();
                        listAdapter.notifyItemChanged(candiItem.getIndex());
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }
    }

    /**
     * 用选到的词来填空
     */
    private void positionSelectedCandiItems() {
        for (AppCompatTextView mAnswerTextView : mAnswerTextViews) {
            mAnswerTextView.setText("");
            mAnswerTextView.setTag(null);
        }
        int index = 0;
        for (CandiItem mSelectedCandiItem : mSelectedCandiItems) {
            mAnswerTextViews.get(index).setText(mSelectedCandiItem.getItem());
            mAnswerTextViews.get(index).setTag(mSelectedCandiItem);
            index++;
        }
    }

    /**
     * 成语4个字
     *
     * @return
     */
    private int candidateCount() {
        return mSelectedCandiItems.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (AppCompatTextView) itemView;
        }
    }

}
