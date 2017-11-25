package com.urbanlabs.mapview;

import com.urbanlabs.mapview.layer.network.IUrlRewrite;

/**
 * Created with IntelliJ IDEA.
 * User: paprika
 * Date: 3/4/14
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SputnikSVGUrlRewrite implements IUrlRewrite {
    private static final String GET_TILES  = "gettiles?";
    private static StringBuilder builder_;

    // image loading settings
    private String url_;
    private String layerId_;
    private String mapName_;

    public SputnikSVGUrlRewrite(String url, String layerId, String mapName) {
        url_ = url;
        layerId_ = layerId;
        mapName_ = mapName;

        builder_ = new StringBuilder();
    }

    @Override
    public String buildURL(long x, long y, long z, boolean force) {
        builder_.setLength(0);
        builder_.append("x="); builder_.append(x);
        builder_.append("&y="); builder_.append(y);
        builder_.append("&z="); builder_.append(z);

        builder_.append("&mapname="+mapName_);
        builder_.append("&layerid="+layerId_);
        builder_.append("&ext=svg");

        if(force)
            builder_.append("&force=true");

        return url_+"/"+GET_TILES + builder_.toString();
    }
}