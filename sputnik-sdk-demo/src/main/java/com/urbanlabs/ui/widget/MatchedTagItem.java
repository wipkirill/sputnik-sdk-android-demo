package com.urbanlabs.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import com.urbanlabs.sdk.dict.TagDictItem;

/**
 * Created by kirill on 2/14/14.
 */
public class MatchedTagItem extends Button {
    private TagDictItem data_;

    public MatchedTagItem(Context context) {
        super(context);
    }

    public MatchedTagItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MatchedTagItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TagDictItem getData() {
        return data_;
    }

    public void setData(TagDictItem data) {
        setText(data.getText());
        this.data_ = data;
    }
}
