package com.urbanlabs.sdk.dict;

/**
 * Created by kirill on 2/7/14.
 */
public class TagDictItem {
    private int id_ = -1;
    private String originalOsm_;
    private int parent_ = -1;
    private String text_;
    private boolean hidden_ = false;

    public int getId() {
        return id_;
    }

    public void setId(int id) {
        this.id_ = id;
    }

    public int getParent() {
        return parent_;
    }

    public void setParent(int parent) {
        this.parent_ = parent;
    }

    public String getText() {
        return text_;
    }

    public void setText(String text) {
        this.text_ = text;
    }

    public boolean isHidden() {
        return hidden_;
    }

    public void setHidden(boolean hidden) {
        this.hidden_ = hidden;
    }

    public String getOriginalOsm() {
        return originalOsm_;
    }

    public void setOriginalOsm(String originalOsm) {
        this.originalOsm_ = originalOsm;
    }
}
