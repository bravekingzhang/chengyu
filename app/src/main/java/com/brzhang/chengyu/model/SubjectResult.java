
package com.brzhang.chengyu.model;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * 网络数据
 */
public class SubjectResult {

    @Json(name = "data")
    private List<Subject> mData;
    @Json(name = "version")
    private int mVersion;

    public List<Subject> getData() {
        return mData;
    }

    public int getVersion() {
        return mVersion;
    }

    public static class Builder {

        private List<Subject> mData;
        private int mVersion;

        public SubjectResult.Builder withData(List<Subject> data) {
            mData = data;
            return this;
        }

        public SubjectResult.Builder withVersion(int version) {
            mVersion = version;
            return this;
        }

        public SubjectResult build() {
            SubjectResult data = new SubjectResult();
            data.mData = mData;
            data.mVersion = mVersion;
            return data;
        }

    }

}
