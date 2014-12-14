package jm.net.ac.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jm.net.ac.SimpleComment;

public class CommentExtractor extends BaseParser<SimpleComment[]> {
    @Override
    protected SimpleComment[] doParse(XmlPullParser document) throws IOException, XmlPullParserException {
        List<SimpleComment> comments = new ArrayList<>();
        int e = document.nextToken();
        String title = null;
        String text  = null;
        while(e != XmlPullParser.END_DOCUMENT){
            if(isStartTag(document, "title")){
                title = document.nextText();
            }else if(isStartTag(document, "content")){
                text = document.nextText();
            }
            if(title != null && text != null){
                comments.add(new SimpleComment(title, text));
                title = text = null;
            }
            e = document.nextToken();
        }
        return comments.toArray(new SimpleComment[comments.size()]);
    }
}
