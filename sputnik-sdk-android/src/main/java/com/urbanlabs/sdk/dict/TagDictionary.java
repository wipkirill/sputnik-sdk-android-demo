package com.urbanlabs.sdk.dict;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.urbanlabs.sdk.util.KV;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TagDictionary {
    private static String TAG = "[TagDictionary]";
    private static TagDictionary instance_ = null;
    private static final String ns = null;
    private Map<String, TagDictItem> dict_ = new HashMap<>();
    private static String resourceFileName_ = "en_EN.xml";

    private TagDictionary() {

    }

    /**
     * Sets file name of string resource used for OSM tag translation
     * By default equals to "en_EN.xml". See assets/en_EN.xml for details.
     * Put your resource file to assets/ folder.
     * @param resourceFileName_
     */
    public static void setResourceFileName(String resourceFileName_) {
        TagDictionary.resourceFileName_ = resourceFileName_;
    }

    /**
     * Current resource file name
     * @return
     */
    public static String getResourceFileName() {
        return resourceFileName_;
    }

    /**
     *
     * @param context
     * @return
     */
    public static TagDictionary getInstance(Context context) {
        if(instance_ == null) {
            instance_ = new TagDictionary();
            try {
                instance_.parse(context.getAssets().open(resourceFileName_));
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance_;
    }

    /**
     *
     * @param in
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readData(parser);
        } finally {
            in.close();
        }
    }

    /**
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readData(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            parser.require(XmlPullParser.START_TAG, ns, "tags");
        } catch(Exception e) {
            Log.e(TAG, "Error while parsing resource " + resourceFileName_);
            return;
        }

        while(parser.next() != XmlPullParser.END_DOCUMENT) {
            String name = "";
            if(parser.getEventType() == XmlPullParser.START_TAG)
                name = parser.getName();
            else
                continue;
            if(name == null) {
                Log.e(TAG, "Reading name == null, skipping");
                continue;
            }
            TagDictItem item = new TagDictItem();
            item.setOriginalOsm(name);
            for(int i=0;i<parser.getAttributeCount();++i) {
                if(parser.getAttributeName(i).equals("id"))
                    item.setId(Integer.valueOf(parser.getAttributeValue(i)));
                if(parser.getAttributeName(i).equals("hidden")) {
                    if(parser.getAttributeValue(i).equals("true"))
                        item.setHidden(true);
                }
                if(parser.getAttributeName(i).equals("parent")) {
                    item.setParent(Integer.valueOf(parser.getAttributeValue(i)));
                }
            }

            parser.next();
            String text = "";
            if(parser.getEventType() == XmlPullParser.TEXT)
                 text = parser.getText();
            item.setText(text);
            parser.next();
            if(parser.getEventType() == XmlPullParser.END_TAG)
                dict_.put(name, item);
        }
        Log.d(TAG, "Read entries " + dict_.size());
    }

    /**
     *
     * @param objData
     * @return
     */
    public List<TagDictItem> matchUsefulTags(List<KV> objData) {
        List<TagDictItem> useful = new ArrayList<>();
        for(KV kv : objData) {
            if(dict_.containsKey(kv.getName())){
                if(!dict_.get(kv.getName()).isHidden())
                    useful.add(dict_.get(kv.getName()));
            }
            if(dict_.containsKey(kv.getValue()))
                useful.add(dict_.get(kv.getValue()));
        }
        return useful;
    }

    /**
     *
     * @param id
     * @return
     */
    public TagDictItem findTagById(int id) {
        for(TagDictItem v : dict_.values()) {
            if(v.getId() == id)
                return v;
        }
        return null;
    }

    /**
     *
     * @param originalOsm
     * @return
     */
    public TagDictItem getLocalizedTag(String originalOsm) {
        if(dict_.containsKey(originalOsm))
            return dict_.get(originalOsm);
        Log.v(TAG, "No tag found in the dictionary: "+originalOsm);
        return null;
    }
}
