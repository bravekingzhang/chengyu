package com.brzhang.chengyu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brzhang.chengyu.helper.DBHelper;
import com.brzhang.chengyu.helper.DownLoadHelper;
import com.brzhang.chengyu.helper.PrefHelper;
import com.brzhang.chengyu.model.CandiItem;
import com.brzhang.chengyu.model.Subject;
import com.brzhang.voicetotextview.VoiceToTextListener;
import com.brzhang.voicetotextview.VoiceToTextView;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import eu.inloop.localmessagemanager.LocalMessage;
import eu.inloop.localmessagemanager.LocalMessageCallback;
import eu.inloop.localmessagemanager.LocalMessageManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LocalMessageCallback {

    private ProgressBar progressBar;

    private ImageView mImage;

    private VoiceToTextView mVoiceToTextView;

    private List<AppCompatTextView> mAnswerTextViews = new LinkedList<>();

    private RecyclerView mCandidates;

    private ListAdapter listAdapter;

    private List<CandiItem> mSelectedCandiItems = new LinkedList<>();

    private String lastVoiceText;

    /**
     * 当前题目
     */
    private Subject currentSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImage = findViewById(R.id.image);
        progressBar = findViewById(R.id.progress_main);
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
        DownLoadHelper.getInstance().init(this);
        LocalMessageManager.getInstance().addListener(this);
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("CheckResult")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_check_version) {
            DownLoadHelper.getInstance()
                    .checkVersison()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean hasNewVersion) throws Exception {
                            if (hasNewVersion) {
                                DownLoadHelper.getInstance().downLoadData();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (BuildConfig.DEBUG) {
                                showToast(throwable.toString());
                            }
                        }
                    });
            return true;
        } else if (id == R.id.first) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadHelper.getInstance().release();
        LocalMessageManager.getInstance().removeListener(this);
    }

    private void initData() {
        int i = 0;
        for (AppCompatTextView mAnswerTextView : mAnswerTextViews) {
            mAnswerTextView.setText("");
            mAnswerTextView.setTag(null);
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
            i++;
            if (i == 4) {
                mAnswerTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        IsGuessed();
                    }
                });
            }
        }

        currentSubject = DBHelper.getInstance(this).get(PrefHelper.getInstance().getIndex());
        if (currentSubject != null) {
            Glide.with(this).load(currentSubject.getPic()).into(mImage);
            List<CandiItem> list = new ArrayList<>();
            i = 0;
            CandiItem candiItem;
            for (String s : currentSubject.getText().split(",")) {
                candiItem = new CandiItem(i, s);
                list.add(candiItem);
                i++;
            }
            listAdapter = new ListAdapter(this, list);
        } else {
            DownLoadHelper.getInstance().downLoadData();
            progressBar.setVisibility(View.VISIBLE);
            listAdapter = new ListAdapter(this, Collections.<CandiItem>emptyList());
        }
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

    private void IsGuessed() {
        String result = "";
        for (AppCompatTextView mAnswerTextView : mAnswerTextViews) {
            result = result.concat(mAnswerTextView.getText().toString());
        }
        if (result.equals(currentSubject.getAnswer())) {
            int currentIndex = PrefHelper.getInstance().getIndex();
            PrefHelper.getInstance().setIndex(++currentIndex);
            final DialogPlus dialog = DialogPlus.newDialog(this)
                    .setGravity(Gravity.CENTER)
                    .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.result_item_layout))
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        }
                    })
                    .setPadding(25, 25, 25, 25)
                    .setInAnimation(R.anim.abc_fade_in)
                    .setOutAnimation(R.anim.abc_fade_out)
                    .setCancelable(false)
                    .setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogPlus dialog) {
                            initData();
                        }
                    })
                    .create();
            dialog.show();
            ((TextView) dialog.getHolderView().findViewById(R.id.description)).setText(String.format("[%s]:%s", currentSubject.getAnswer(), currentSubject.getDescription()));
            dialog.getHolderView().findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

    }

    /**
     * 将语言转化出来的文本自动填充
     *
     * @param s
     */
    private void textAutoSelected(String s) {
        if (TextUtils.isEmpty(s) || TextUtils.equals(s, lastVoiceText)) {
            return;
        }
        Log.e("MainActivity", "textAutoSelected() called with: s = [" + s + "]");
        ListIterator<CandiItem> candiItemListIterator = mSelectedCandiItems.listIterator();
        while (candiItemListIterator.hasNext()) {
            candiItemListIterator.next().setSelected(false);
            candiItemListIterator.remove();
        }
        positionSelectedCandiItems();
        listAdapter.notifyDataSetChanged();
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
        lastVoiceText = s;
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if (localMessage.getId() == R.id.timu_download_finished) {
            progressBar.setVisibility(View.GONE);
            initData();
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

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }
}
