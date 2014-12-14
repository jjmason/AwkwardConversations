package jm.net.ac;
import static jm.net.ac.Util.*;

public class SimpleComment {
    public final String title;
    public final String text;

    public SimpleComment(String title, String text){
        this.title = notNull(title, "title");
        this.text  = notNull(text, "text");
    }

    @Override
    public String toString(){
        return title + ": " + text;
    }

}
