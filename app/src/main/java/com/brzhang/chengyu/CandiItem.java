package com.brzhang.chengyu;

public class CandiItem {
    private int     index;
    private String  item;
    private boolean selected;

    public CandiItem(int index, String item) {
        this.index = index;
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

    public int getIndex() {
        return index;
    }
}
