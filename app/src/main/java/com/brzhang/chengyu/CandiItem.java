package com.brzhang.chengyu;

public class CandiItem {
    private String item;
    private boolean selected;

    public CandiItem(String item) {
        this.item = item;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getItem() {
        return item;
    }
}
