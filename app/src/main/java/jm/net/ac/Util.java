package jm.net.ac;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;

public class Util {
    private  Util(){}

    public static final Random random = new Random();

    public static <T> T notNull(T object, String name){
        if(object == null){
            throw new NullPointerException(
                    (name == null ? "value" : name) + " must not be null!");
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    public static <T> T randomChoice(Collection<T> collection){
        return (T) randomChoice(collection.toArray());
    }

    public static <T> T randomChoice(T[] options){
        int i = random.nextInt(options.length);
        return options[i];
    }

    public static URL newUrl(String url){
        try{
            return new URL(url);
        }catch(MalformedURLException e){
            throw new RuntimeException(e);
        }
    }
}
