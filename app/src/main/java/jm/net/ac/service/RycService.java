package jm.net.ac.service;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import javax.annotation.Nullable;

import jm.net.ac.YoutubeComment;

public class RycService extends AbstractService {
    private static final String SERVICE_URL = "http://www/randomyoutubecomment.com";
    private static final String TAG = "RycService";

    @Override
    protected @Nullable
    YoutubeComment fetchComment() throws IOException {
        Document doc = Jsoup.connect(SERVICE_URL).get();
        Element commentDiv = doc.getElementById("comment");
        if(commentDiv == null){
            Log.e(TAG, "No #comment element found!");
            return null;
        }
        YoutubeComment.Builder builder = new YoutubeComment.Builder();
        builder.text(commentDiv.text());
        Element authorDiv = commentDiv.nextElementSibling();
        if(authorDiv == null){
            Log.w(TAG, "No element found after div#comment");
        }else{
            builder.author(authorDiv.text());
        }
        return builder.build();
    }

}
