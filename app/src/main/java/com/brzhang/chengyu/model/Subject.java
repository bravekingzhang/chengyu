
package com.brzhang.chengyu.model;

import com.squareup.moshi.Json;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 题
 */
@Entity
public class Subject {

    @Json(name = "answer")
    private String mAnswer;
    @Json(name = "description")
    private String mDescription;
    @Json(name = "index")
    private int mIndex;
    @Json(name = "pic")
    private String mPic;
    @Json(name = "text")
    private String mText;

    @Generated(hash = 195771696)
    public Subject(String mAnswer, String mDescription, int mIndex, String mPic,
                   String mText) {
        this.mAnswer = mAnswer;
        this.mDescription = mDescription;
        this.mIndex = mIndex;
        this.mPic = mPic;
        this.mText = mText;
    }

    @Generated(hash = 1617906264)
    public Subject() {
    }

    public String getAnswer() {
        return mAnswer;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getPic() {
        return mPic;
    }

    public String getText() {
        return mText;
    }

    public static class Builder {

        private String mAnswer;
        private String mDescription;
        private int mIndex;
        private String mPic;
        private String mText;

        public Subject.Builder withAnswer(String answer) {
            mAnswer = answer;
            return this;
        }

        public Subject.Builder withDescription(String description) {
            mDescription = description;
            return this;
        }

        public Subject.Builder withIndex(int index) {
            mIndex = index;
            return this;
        }

        public Subject.Builder withPic(String pic) {
            mPic = pic;
            return this;
        }

        public Subject.Builder withText(String text) {
            mText = text;
            return this;
        }

        public Subject build() {
            Subject subject = new Subject();
            subject.mAnswer = mAnswer;
            subject.mDescription = mDescription;
            subject.mIndex = mIndex;
            subject.mPic = mPic;
            subject.mText = mText;
            return subject;
        }

    }

    @Override
    public String toString() {
        return "Subject{" +
                "mAnswer='" + mAnswer + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mIndex=" + mIndex +
                ", mPic='" + mPic + '\'' +
                ", mText='" + mText + '\'' +
                '}';
    }

    public String getMAnswer() {
        return this.mAnswer;
    }

    public void setMAnswer(String mAnswer) {
        this.mAnswer = mAnswer;
    }

    public String getMDescription() {
        return this.mDescription;
    }

    public void setMDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public int getMIndex() {
        return this.mIndex;
    }

    public void setMIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public String getMPic() {
        return this.mPic;
    }

    public void setMPic(String mPic) {
        this.mPic = mPic;
    }

    public String getMText() {
        return this.mText;
    }

    public void setMText(String mText) {
        this.mText = mText;
    }
}
