package jm.net.ac;

import android.content.Context;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;

import jm.net.ac.parser.CommentExtractor;
import jm.net.ac.parser.CommentFeedUrlExtractor;

import static jm.net.ac.Util.*;

public class YouTubeCommentService {
    public static final String MOST_POPULAR_CATEGORY_FORMAT =
            "http://gdata.youtube.com/feeds/api/standardfeeds/most_popular_%s";

    public static final String[] DEFAULT_VIDEO_FEED_URLS = {
        "http://gdata.youtube.com/feeds/api/standardfeeds/most_popular"
    };
    private static final String TAG = YouTubeCommentService.class.getSimpleName();

    private final Set<URL> mVideoFeedUrls = new HashSet<>();
    private final Context mContext;


    public YouTubeCommentService(Context context) {
        mContext = notNull(context, "context");
        for(String s:DEFAULT_VIDEO_FEED_URLS){
            addVideoFeedUrl(s);
        }
    }

    public void addMostPopularCategory(String category){
        addVideoFeedUrl(String.format(MOST_POPULAR_CATEGORY_FORMAT, category));
    }

    public void addVideoFeedUrl(String url){
        addVideoFeedUrl(newUrl(url));
    }

    public void addVideoFeedUrl(URL url){
        mVideoFeedUrls.add(url);
    }

    public Collection<URL> getVideoFeedUrls(){
        return mVideoFeedUrls;
    }

    public @CheckForNull SimpleComment nextComment() throws IOException {
        URL videoFeedUrl = randomChoice(getVideoFeedUrls());
        CommentFeedUrlExtractor p = new CommentFeedUrlExtractor();
        URL[] commentFeedUrls = p.parse(videoFeedUrl);

        if(commentFeedUrls.length == 0){
            return null;
        }
        URL commentUrl = randomChoice(commentFeedUrls);
        CommentExtractor ce = new CommentExtractor();
        SimpleComment[] comments = ce.parse(commentUrl);
        if(comments.length == 0){
            return null;
        }
        return randomChoice(comments);
    }

    private String getDeveloperKey(){
        return mContext.getResources().getString(R.string.developer_key);
    }
}
