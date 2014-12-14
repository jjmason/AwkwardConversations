package jm.net.ac.parser;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import static jm.net.ac.Util.*;

import jm.net.ac.AwkwardException;

public class CommentFeedUrlExtractor extends BaseParser<URL[]>{
    private static final String TAG = CommentFeedUrlExtractor.class.getSimpleName();

    @Override
    protected URL[] doParse(XmlPullParser document) throws IOException, XmlPullParserException {
        Collection<URL> result = new ArrayList<>();
        int tag = document.nextToken();
        boolean inComments = false;

        while(tag != XmlPullParser.END_DOCUMENT){
            if(inComments && isFeedLink(document)){
                String href = document.getAttributeValue(null, "href");
                if(href != null){
                    result.add(newUrl(href));
                }
            }

            if(isStartComments(document)){
                inComments = true;
            }

            if(isEndComments(document)){
                inComments = false;
            }

            tag = document.nextToken();
        }

        if(result.isEmpty()){
            //throw new AwkwardException("no gd:comments tag found in feed!");

        }

        return result.toArray(new URL[result.size()]);
    }

    private boolean isFeedLink(XmlPullParser p) throws XmlPullParserException {
        return isStartTag(p,"feedLink");
    }

    private boolean isStartComments(XmlPullParser p) throws XmlPullParserException {
        return isStartTag(p, "comments");
    }

    private boolean isEndComments(XmlPullParser p) throws XmlPullParserException {
        return isEndTag(p, "comments");
    }
}
