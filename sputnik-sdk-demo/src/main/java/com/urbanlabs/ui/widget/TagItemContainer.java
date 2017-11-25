package com.urbanlabs.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by kirill on 2/7/14.
 */
public class TagItemContainer extends LinearLayout {
    public TagItemContainer(Context context) {
        super(context);
    }

    public TagItemContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagItemContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addTag(TagItemView tag) {
        addView(tag);
    }
}
