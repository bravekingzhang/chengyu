package com.brzhang.chengyu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
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

import com.airbnb.lottie.LottieAnimationView;
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
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements LocalMessageCallback {

    private ProgressBar progressBar;

    private ImageView mSubjectImage;

    private TextView mSubjectIndex;

    private VoiceToTextView mVoiceToTextView;

    /**
     * 四个填空题
     */
    private List<AppCompatTextView> mTextSpaceViews = new LinkedList<>();

    /**
     * 已选择到的文字
     */
    private List<CandiItem> mSelectedCandiItems = new LinkedList<>();

    /**
     * 一堆文字
     */
    private RecyclerView mCandidates;

    private ListAdapter mListAdapter;


    /**
     * 因为语音识别会连续不断像这边推送识别出的句子，因此，为了减少不必要的验证，做一个保存
     */
    private String mLastVoiceText;

    /**
     * 当前题目信息
     */
    private Subject mCurrentSubject;

    /**
     * 察并且缓存语言转换为文本，一个一个的处理
     * https://blog.csdn.net/u013366008/article/details/76088482
     * subject 的使用
     */
    private PublishSubject<String> pSubject;
    /**
     * 是否猜中的标记
     */
    private boolean isGuessed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSubjectImage = findViewById(R.id.image);
        mSubjectIndex = findViewById(R.id.subject_num);
        progressBar = findViewById(R.id.progress_main);
        mVoiceToTextView = findViewById(R.id.voice_to_text_view);
        mTextSpaceViews.add((AppCompatTextView) findViewById(R.id.text1));
        mTextSpaceViews.add((AppCompatTextView) findViewById(R.id.text2));
        mTextSpaceViews.add((AppCompatTextView) findViewById(R.id.text3));
        mTextSpaceViews.add((AppCompatTextView) findViewById(R.id.text4));
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
        initVoiceTextView();
        initPublishSubject();
        DownLoadHelper.getInstance().init(this);
        LocalMessageManager.getInstance().addListener(this);
        initData();
    }

    private void initData() {
        isGuessed = false;
        mVoiceToTextView.closeArs();
        init4TextSpace();
        setupQuestion();
    }

    /**
     * 初始化PublishSubject，为了接受并且处理识别出来的文本，注意，这里在异步线程中执行，为什么，请看后面
     */
    @SuppressLint("CheckResult")
    private void initPublishSubject() {
        pSubject = PublishSubject.create();
        pSubject.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e("MainActivity", "accept() called with: s = [" + s + "]");
                        textAutoSelected(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showToast(throwable.toString());
                    }
                });
    }

    /**
     * 初始化语音转文本控件，大哥们可以用自己的id和key，setSecretKey建议在服务器端生成，别像我这样写在这里，不安全
     */
    private void initVoiceTextView() {
        mVoiceToTextView
                .setActivity(this)
                .setAppid(1251114236)
                .setProjectid(1114271)
                .setSecretId("AKIDkHZiiUrLQGsFNIlShhS1KNFrDJ8hY3rP")
                .setSecretKey("QbWCdokQr3zf6HF0WnqkPo21kESQAett")
                .setListener(new VoiceToTextListener() {
                    @Override
                    public void onText(String text) {
                        if (!TextUtils.isEmpty(text) && !TextUtils.equals(text, mLastVoiceText)) {
                            pSubject.onNext(text);
                        }
                    }
                })
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_check_version) {
            checkVersionIfNeedDownSubject();
            return true;
        } else if (id == R.id.action_achievement) {
            showAchievement();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 检查更新，一旦有新版本，启动任务更新，这里使用DownloadManager来下载，方便一会扩展较大的文件
     */
    @SuppressLint("CheckResult")
    private void checkVersionIfNeedDownSubject() {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadHelper.getInstance().release();
        LocalMessageManager.getInstance().removeListener(this);
    }


    /**
     * 显示题目
     */
    @SuppressLint("DefaultLocale")
    private void setupQuestion() {
        int i = 0;
        if (isPassAllSubject()) {
            showAchievement();
            return;
        }
        mCurrentSubject = DBHelper.getInstance(this).get(PrefHelper.getInstance().getIndex());
        if (mCurrentSubject != null) {
            mSubjectIndex.setText(String.format("%d关", PrefHelper.getInstance().getIndex()));
            Glide.with(this).load(mCurrentSubject.getPic()).into(mSubjectImage);
            List<CandiItem> list = new ArrayList<>();
            CandiItem candiItem;
            for (String s : mCurrentSubject.getText().split(",")) {
                candiItem = new CandiItem(i, s);
                list.add(candiItem);
                i++;
            }
            mListAdapter = new ListAdapter(this, list);
        } else {
            DownLoadHelper.getInstance().downLoadData();
            progressBar.setVisibility(View.VISIBLE);
            for (AppCompatTextView mAnswerTextView : mTextSpaceViews) {
                mAnswerTextView.setVisibility(View.GONE);
            }
            mListAdapter = new ListAdapter(this, Collections.<CandiItem>emptyList());
        }
        mCandidates.setAdapter(mListAdapter);
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * 是否已经通关
     *
     * @return
     */
    private boolean isPassAllSubject() {
        return PrefHelper.getInstance().getIndex() != 1 && PrefHelper.getInstance().getIndex() > DBHelper.getInstance(this).count();
    }

    /**
     * 初始化4个填空及待选文字的状态
     */
    private void init4TextSpace() {
        int i = 0;
        for (AppCompatTextView mAnswerTextView : mTextSpaceViews) {
            mAnswerTextView.setVisibility(View.VISIBLE);
            mAnswerTextView.setText("");
            mAnswerTextView.setTag(null);
            mAnswerTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() != null) {
                        CandiItem candiItem = (CandiItem) v.getTag();
                        candiItem.setSelected(false);
                        mSelectedCandiItems.remove(candiItem);
                        mListAdapter.notifyItemChanged(candiItem.getIndex());
                        positionSelectedCandiItems();
                    }
                }
            });
            i++;
            if (i == 3 || i == 4) {
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
    }

    private void IsGuessed() {
        String result = "";
        for (AppCompatTextView mAnswerTextView : mTextSpaceViews) {
            result = result.concat(mAnswerTextView.getText().toString());
        }
        /**
         * 降低游戏难度，说对3个以上的字就算对啦，因为有人发音不标准
         */
        if (result.length() >= 3 && (mCurrentSubject.getAnswer().startsWith(result) || mCurrentSubject.getAnswer().endsWith(result))) {
            isGuessed = true;
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
            ((TextView) dialog.getHolderView().findViewById(R.id.description)).setText(String.format("[%s]:%s", mCurrentSubject.getAnswer(), mCurrentSubject.getDescription()));
            dialog.getHolderView().findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

    }

    /**
     * 显示成就
     */
    @SuppressLint("DefaultLocale")
    private void showAchievement() {
        boolean isPassAllTheSubject = isPassAllSubject();
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setGravity(Gravity.CENTER)
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.user_achievement_layout))
                .setCancelable(true)
                .create();
        if (isPassAllTheSubject) {
            LottieAnimationView animationView = dialog.getHeaderView().findViewById(R.id.animation_view);
            animationView.setAnimation(R.raw.done);
            animationView.playAnimation();
            TextView description = dialog.getHolderView().findViewById(R.id.description);
            description.setTextColor(getResources().getColor(R.color.colorAccent));
            description.setText("恭喜通关666");
        } else {
            ((TextView) dialog.getHolderView().findViewById(R.id.description)).setText(String.format("你已通关%d，总关数%d", PrefHelper.getInstance().getIndex(), DBHelper.getInstance(this).count()));
        }
        dialog.show();
    }


    /**
     * 将语言转化出来的文本自动填充,
     * <p>
     * 这里通过答案来反向一个字一个字的查找语言转换的文本中是否全部包含了所需的字，
     * <p>
     * 因为需要一个字一个字的填入的效果，因此，需要每个字的填入需要一定的延时，但是我们不能lock主线程，因此
     * <p>
     * 只能想办法丢在异步线程，但是这样子如果不加控制，将会出现多个线程发送消息，操控UI线程的ui控件，填字将会混乱不堪
     * <p>
     * 因此，我们想到了，可以将这些tobePosition进行buffer起来，一个一个的来处理,因此rxjava在这里使用是一种比较好的选择
     *
     * @param tobePosition
     */
    @WorkerThread
    private void textAutoSelected(String tobePosition) throws InterruptedException {
        if (isGuessed) {
            return;
        }
        ListIterator<CandiItem> candiItemListIterator = mSelectedCandiItems.listIterator();
        while (candiItemListIterator.hasNext()) {
            candiItemListIterator.next().setSelected(false);
            candiItemListIterator.remove();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                positionSelectedCandiItems();
                mListAdapter.notifyDataSetChanged();
            }
        });
        for (int i = 0; i < mCurrentSubject.getAnswer().length() && i < 4; i++) {
            String word = String.valueOf(mCurrentSubject.getAnswer().charAt(i));
            if (tobePosition.contains(word)) {
                for (final CandiItem candiItem : mListAdapter.items) {
                    if (!candiItem.isSelected() && candiItem.getItem().equals(word)) {
                        candiItem.setSelected(true);
                        mSelectedCandiItems.add(candiItem);
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                positionSelectedCandiItems();
                                mListAdapter.notifyItemChanged(candiItem.getIndex());
                            }
                        });
                        break;
                    }
                }
            } else {
                mSelectedCandiItems.add(new CandiItem(i, ""));
            }
        }
        mLastVoiceText = tobePosition;
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
                        mListAdapter.notifyItemChanged(candiItem.getIndex());
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
        for (AppCompatTextView mAnswerTextView : mTextSpaceViews) {
            mAnswerTextView.setText("");
            mAnswerTextView.setTag(null);
        }
        int index = 0;
        for (CandiItem mSelectedCandiItem : mSelectedCandiItems) {
            mTextSpaceViews.get(index).setText(mSelectedCandiItem.getItem());
            mTextSpaceViews.get(index).setTag(mSelectedCandiItem);
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
