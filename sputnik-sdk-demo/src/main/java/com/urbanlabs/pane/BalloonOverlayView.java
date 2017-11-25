package com.urbanlabs.pane;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.urbanlabs.R;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.dict.TagDictItem;
import com.urbanlabs.sdk.dict.TagDictionary;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.KV;
import com.urbanlabs.ui.widget.TagItemContainer;
import com.urbanlabs.ui.widget.TagItemView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A view representing a MapView marker information balloon.
 *
 * @author Jeff Gilfelt
 * https://github.com/jgilfelt/android-mapviewballoons
 *
 */
public class BalloonOverlayView extends FrameLayout {
    private final TagDictionary dict_;
    private LinearLayout layout;
    private TextView title;
    private TextView snippet;
    private TagItemContainer tagCont_;

    /**
     * Create a new BalloonOverlayView.
     *
     * @param context - The activity context.
     * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
     * when rendering this view.
     */
    public BalloonOverlayView(Context context, int balloonBottomOffset) {
        super(context);

        setPadding(10, 0, 10, balloonBottomOffset);
        layout = new LimitLinearLayout(context);
        layout.setVisibility(VISIBLE);
        setupView(context, layout);

        FrameLayout.LayoutParams params = 
            new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.NO_GRAVITY;

        addView(layout, params);
        dict_ = TagDictionary.getInstance(context);
    }

    /**
     * Inflate and initialize the BalloonOverlayView UI. Override this method
     * to provide a custom view/layout for the balloon.
     *
     * @param context - The activity context.
     * @param parent - The root layout into which you must inflate your view.
     */
    protected void setupView(Context context, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.balloon_overlay, parent);
        title = (TextView) v.findViewById(R.id.balloon_item_title);
        snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
        tagCont_ = (TagItemContainer) v.findViewById(R.id.tagsContainer);
    }

    /**
     * Sets the view data from a given overlay item.
     *
     * @param item - The overlay item containing the relevant view data.
     */
    public void setData(SearchResult item) {
        layout.setVisibility(VISIBLE);
        setBalloonData(item, layout);
    }

    /**
     * Sets the view data from a given overlay item. Override this method to create
     * your own data/view mappings.
     *
     * @param item - The overlay item containing the relevant view data.
     * @param parent - The parent layout for this BalloonOverlayView.
     */
    protected void setBalloonData(SearchResult item, ViewGroup parent) {
        if (item.hasName()) {
            title.setVisibility(VISIBLE);
            title.setText(item.getTag(TUtil.OSM_NAME));
        } else {
            title.setText("");
            title.setVisibility(GONE);
        }
        if(TUtil.hasAddress(item)) {
            snippet.setVisibility(VISIBLE);
            try {
                snippet.setText(TUtil.extractAddress(item));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            snippet.setText("");
            snippet.setVisibility(GONE);
        }
        List<KV> props = new ArrayList<>();
        Map<String, String> tags = item.getTags();
        for(Map.Entry<String, String > e : tags.entrySet()) {
            props.add(new KV(e.getKey(), e.getValue()));
        }
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<TagDictItem> useFul = dict_.matchUsefulTags(props);
        for(TagDictItem t:useFul) {
            View tagItemView = inflater.inflate(R.layout.tag_item, parent, false);
            TagItemView tagItem = (TagItemView)tagItemView.findViewById(R.id.tagItemElement);
            tagItem.setText(t.getText());
            tagCont_.addTag(tagItem);
        }
    }

    private class LimitLinearLayout extends LinearLayout {
        private static final int MAX_WIDTH_DP = 280;
        private final float SCALE = getContext().getResources().getDisplayMetrics().density;

        public LimitLinearLayout(Context context) {
            super(context);
        }

        public LimitLinearLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int adjustedMaxWidth = (int)(MAX_WIDTH_DP * SCALE + 0.5f);
            int adjustedWidth = Math.min(measuredWidth, adjustedMaxWidth);
            int adjustedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(adjustedWidth, mode);
            super.onMeasure(adjustedWidthMeasureSpec, heightMeasureSpec);
        }
    }
}