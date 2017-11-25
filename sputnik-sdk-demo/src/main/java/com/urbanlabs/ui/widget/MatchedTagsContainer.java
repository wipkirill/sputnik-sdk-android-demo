package com.urbanlabs.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.urbanlabs.R;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.dict.TagDictItem;
import com.urbanlabs.sdk.dict.TagDictionary;
import com.urbanlabs.sdk.response.TagList;
import com.urbanlabs.sdk.response.json.JsonHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kirill on 2/14/14.
 */
public class MatchedTagsContainer extends LinearLayout {
    private LayoutInflater inflater_;
    private String mapName_;
    private TagDictionary dict_;

    private Set<String> selectedTags = new HashSet<String>();
    private Set<String> availableTags = new HashSet<>();
    private LinearLayout aTags;
    private LinearLayout sTags;

    private Refreshable listener_;

    public void setListener(Refreshable listener) {
        listener_ = listener;
    }

    /**
     *
     * @param context
     */
    public MatchedTagsContainer(Context context) {
        super(context);
        init();
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public MatchedTagsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MatchedTagsContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    /**
     *
     */
    public void init() {
        inflater_ = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dict_ = TagDictionary.getInstance(getContext());
        setVisibility(LinearLayout.GONE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        aTags = (LinearLayout)findViewById(R.id.availableTags);
        sTags = (LinearLayout)findViewById(R.id.selectedTags);
    }

    /**
     *
     * @param term
     */
    public void refresh(String term) {
        Sputnik.matchTags(mapName_, term, new GetCallback<TagList>() {
            @Override
            public void done(TagList tagList, SputnikException e) {
                if(e == null) {
                    availableTags = tagList.getTags();
                    renderAvailableTags();
                } else {
                    Log.e("[MatchTagContainer]", "Failed to match tags: " + e.getMessage());
                }
            }
        });
    }

    /**
     *
     */
    public void renderAvailableTags() {
        aTags.removeAllViews();
        if(selectedTags.size() == 0 && availableTags.size() == 0)
            setVisibility(LinearLayout.GONE);
        else
            setVisibility(LinearLayout.VISIBLE);
        Object[] at = availableTags.toArray();
        for(int i = 0; i < at.length; ++i) {
            String tag = (String)at[i];
            if(selectedTags.contains(tag))
                continue;
            View tagItemView = inflater_.inflate(R.layout.match_tag_item, this, false);
            final MatchedTagItem tagItem = (MatchedTagItem)tagItemView.findViewById(R.id.tagButtonNotSelected);
            final TagDictItem data = dict_.getLocalizedTag(tag);
            if(data != null) {
                tagItem.setData(data);
                tagItem.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        addSelectedTag(data);
                        aTags.removeView(tagItem);
                        listener_.refreshSearchResults();
                    }
                });
                aTags.addView(tagItem);
            }
        }
    }

    private void addSelectedTag(final TagDictItem data) {
        if(selectedTags.contains(data.getOriginalOsm()))
            return;
        View tagItemView = inflater_.inflate(R.layout.match_tag_item_selected, this, false);
        final MatchedTagItem tagItem = (MatchedTagItem)tagItemView.findViewById(R.id.tagButtonSelected);
        tagItem.setData(data);
        selectedTags.add(data.getOriginalOsm());

        tagItem.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                selectedTags.remove(data.getOriginalOsm());
                sTags.removeView(tagItem);
                renderAvailableTags();
            }
        });
        sTags.addView(tagItem);
    }

    public Set<String> getSelectedTags() {
        return selectedTags;
    }

    public void setMapName(String mapName) {
        this.mapName_ = mapName;
    }

    /**
     *
     */
    public void clear() {
        availableTags.clear();
        selectedTags.clear();
        sTags.removeAllViews();
        aTags.removeAllViews();
        setVisibility(LinearLayout.GONE);
    }

    public interface Refreshable {
        void refreshSearchResults();
    }
}
