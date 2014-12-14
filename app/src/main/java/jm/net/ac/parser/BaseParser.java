package jm.net.ac.parser;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import static jm.net.ac.Util.*;
import javax.annotation.Nonnull;

import jm.net.ac.AwkwardException;

/**
 * A simple synchronous Xml parser base class.  Returns class Parsed,
 * and subclasses must implement Parsed doParse(XmlStreamReader xml).
 */
public abstract class BaseParser<Parsed> {
    public static final String XMLNS_GD = "http://schemas.google.com/g/2005";
    private static final String TAG = BaseParser.class.getSimpleName();
    private boolean mNamespaceAware = true;

    public Parsed parse(String feedUrl) throws IOException {
        return parse(newUrl(feedUrl));
    }

    public Parsed parse(URL feedUrl) throws IOException {
        return parseContent(fetchContent(feedUrl));
    }

    private Parsed parseContent(String content){
        XmlPullParserFactory factory  = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            configureParserFactory(factory);
            XmlPullParser parser = factory.newPullParser();
            Log.i(TAG, content);
            InputStream is =  new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
            parser.setInput(is, null);
            return doParse(parser);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            throw new AwkwardException(e);
        }

    }

    public  @Nonnull Parsed parse(@Nonnull XmlPullParser parser){
        try {
            return doParse(parser);
        } catch (IOException | XmlPullParserException e) {
            throw new AwkwardException(e);
        }
    }

    private @Nonnull String fetchContent(@Nonnull URL feedUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(feedUrl)
                .build();
        Response res = client.newCall(request).execute();
        return res.body().string();
    }

    protected void configureParserFactory(XmlPullParserFactory factory){
        factory.setNamespaceAware(  isNamespaceAware() );
    }

    protected boolean isNamespaceAware(){
        return mNamespaceAware;
    }


    protected boolean isTag(XmlPullParser p, int tag, String xmlns, String name) throws XmlPullParserException {
        if(p.getEventType() != tag) {
            return false;
        }
        if(xmlns != null && !xmlns.equals(p.getNamespace())){
            return false;
        }
        if(name != null && !name.equals(p.getName())){
            return false;
        }
        return true;
    }

    protected boolean isStartTag(XmlPullParser p, String tag) throws XmlPullParserException {
        return isStartTag(p, null, tag);
    }

    protected boolean isStartTag(XmlPullParser p, String xmlns, String tag) throws XmlPullParserException {
        return isTag(p, XmlPullParser.START_TAG, xmlns, tag);
    }

    protected boolean isEndTag(XmlPullParser p, String name) throws XmlPullParserException {
        return isEndTag(p, null, name);
    }

    protected boolean isEndTag(XmlPullParser p, String xmlns, String name) throws XmlPullParserException {
        return isTag(p, XmlPullParser.END_TAG, xmlns, name);
    }

    protected abstract Parsed doParse(XmlPullParser document) throws IOException, XmlPullParserException;
}