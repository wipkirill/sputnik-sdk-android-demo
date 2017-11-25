package com.urbanlabs.pane;

import android.content.Context;
import com.urbanlabs.mapview.pane.BubbleMarker;

/**
 * Created with IntelliJ IDEA.
 * User: paprika
 * Date: 2/25/14
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class SputnikBubbleMarker extends BubbleMarker {
    public SputnikBubbleMarker(Context context) {
        super(context);
    }

    @Override
    public void onSingleTapUp() {
        ;
    }

    @Override
    public void onLongPress() {
        if(isHidden())
            showBubble();
        else
            hideBubble();
    }

    @Override
    public void onShowPress() {
        ;
    }

    @Override
    public void onDoubleTap() {
        removeBubbleMarker();
    }
}
