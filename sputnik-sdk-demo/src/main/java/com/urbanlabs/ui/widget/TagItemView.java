package com.urbanlabs.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.urbanlabs.R;

/**
 * Created by kirill on 2/2/14.
 */
public class TagItemView extends TextView {
    public TagItemView(Context context) {
        super(context);
        setTextAppearance(getContext(), R.style.tagItem);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public TagItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextAppearance(getContext(), R.style.tagItem);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TagItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTextAppearance(getContext(), R.style.tagItem);
    }

    /**
     *
     * @param tag
     */
    public void setTag(String tag) {
        setText(tag);
    }
}
