package com.brzhang.voicetotextview;

/**
 * state 1,0
 * text 语音识别出来的文字
 */
public class VoiceResult {
    boolean stateRecordOn;
    String text;

    public VoiceResult(boolean stateRecordOn, String text) {
        this.stateRecordOn = stateRecordOn;
        this.text = text;
    }

    @Override
    public String toString() {
        return "VoiceResult{" +
                "stateRecordOn=" + stateRecordOn +
                ", text='" + text + '\'' +
                '}';
    }
}
