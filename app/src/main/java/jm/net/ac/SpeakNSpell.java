package jm.net.ac;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

public class SpeakNSpell  {

    private static final String TAG = "SpeakNSpell";

    public interface ResultListener  {
        public void onReady();
        public void onError(@CheckForNull String message);
    }

    public static enum State {
        IDLE,
        STARTING,
        STARTED
    }

    private TextToSpeech mTextToSpeech;
    private State mState = State.IDLE;
    private ResultListener mOnReadyListener;
    private final Map<String, ResultListener> mOnCompleteListeners = new HashMap<>();
    private final Context mContext;

    public SpeakNSpell(Context context){
        mContext  = context;
    }

    public State getState(){
        return mState;
    }

    public void start(ResultListener onReadyListener){
        if(mState != State.IDLE){
            Log.e(TAG, "State is " + mState.name());
            if(onReadyListener != null){
                onReadyListener.onError("State is not IDLE (" + mState.name() + ")") ;
            }
        }else {
            mOnReadyListener = onReadyListener;
            mTextToSpeech = new TextToSpeech(mContext, mOnInitListener);
            mState = State.STARTING;
        }
    }

    public void shutdown(){
        if(mTextToSpeech != null){
            mOnCompleteListeners.clear();
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    public void speak(String text, ResultListener onCompleteListener){
        if(text == null){
            throw  new NullPointerException("text must not be null");
        }
        text = prepareText(text);

        if(mState != State.STARTED){
            if(onCompleteListener != null){
               onCompleteListener.onError("Not started! (" + mState.name() + ")");
            }
            return;
        }
        final String id = String.valueOf(text.hashCode()) + System.currentTimeMillis();
        if(onCompleteListener != null){
            mOnCompleteListeners.put(id, onCompleteListener);
        }
        if(mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, speechParamsWithId(id));
        }
    }

    private String prepareText(String text) {
        // replace links with "link"
        text = text.replaceAll("http(s?)://\\S+", " link ");

        // replace "-" (popular in "Redditor here" comments with "dash"
        // so it's not pronounced as "minus"
        text = text.replaceAll("\\-", " dash ");

        // replace "#" with "hash tag" so it's not read as "number".
        text = text.replaceAll("\\#", " hash tag ");

        return text;
    }

    private HashMap<String, String> speechParamsWithId(String id){
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        return params;
    }

    private void callOnReadyError(String message){
        if(mOnReadyListener != null){
            mOnReadyListener.onError(message);
            mOnReadyListener = null;
        }
    }

    private void callOnReadyResult(){
        if(mOnReadyListener != null){
            mOnReadyListener.onReady();
            mOnReadyListener = null;
        }
    }

    private final TextToSpeech.OnInitListener mOnInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status == TextToSpeech.ERROR){
                mState = State.IDLE;
                callOnReadyError("init failed");
            }else{
                if( TextToSpeech.ERROR == mTextToSpeech.setLanguage(Locale.getDefault())){
                    mState = State.IDLE;
                    callOnReadyError("setLocal failed: " + Locale.getDefault());
                }else{
                    mState = State.STARTED;
                    mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
                    callOnReadyResult();
                }

            }
            if(mOnReadyListener != null){
                Log.e(TAG, "On ready listener not callled?");
            }
        }
    };

    private final UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Log.d(TAG, "Utterance started");
        }

        @Override
        public void onDone(String utteranceId) {
            final ResultListener listener = mOnCompleteListeners.remove(utteranceId);
            Log.d(TAG, "Utterance complete");
            if(listener != null){
                listener.onReady();
            }
        }

        @Override
        public void onError(String utteranceId) {
            final ResultListener listener = mOnCompleteListeners.remove(utteranceId);
            Log.e(TAG, "Utterance error!");
            if(listener != null){
                listener.onError("Utterance error");
            }
        }
    };
}
