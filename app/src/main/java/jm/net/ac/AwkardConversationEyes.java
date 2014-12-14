package jm.net.ac;

import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

import javax.annotation.CheckForNull;


public class AwkardConversationEyes extends ActionBarActivity {
    private static final String TAG = "AwkwardConversationEyes";
    private final YouTubeCommentService mCommentService = new YouTubeCommentService(this);
    private TextView mCommentTextView;
    private Button   mMoarButton;
    private final SpeakNSpell mSpeakNSpell = new SpeakNSpell(this);
    private final Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awkard_conversation_eyes);

        findViews();
        mMoarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NextCommentTask().execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSpeakNSpell.getState() == SpeakNSpell.State.IDLE) {
            startSpeakNSpell();
        }else if(mSpeakNSpell.getState() == SpeakNSpell.State.STARTED){
            startSpeaking();;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSpeakNSpell.getState() != SpeakNSpell.State.IDLE){
            mSpeakNSpell.shutdown();
        }
    }

    private void startSpeakNSpell(){
        mSpeakNSpell.start(new SpeakNSpell.ResultListener() {
            @Override
            public void onReady() {
                Log.i(TAG, "SpeakNSpell ready!");
                startSpeaking();
            }

            @Override
            public void onError(@CheckForNull String message) {
                if(message == null){
                    message = "Unknown error!";
                }
                message = "TextToSpeech is not available: " + message;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startSpeaking(){
        if(mSpeakNSpell.getState() == SpeakNSpell.State.STARTED){
            new NextCommentTask().execute();
        }else{
            Log.e(TAG, "startSpeaking but state is " + mSpeakNSpell.getState().name());
        }
    }

    private void startSpeakingAfterDelay(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSpeaking();
            }
        }, 150);
    }

    private void speak(String text){
        if(mSpeakNSpell.getState() == SpeakNSpell.State.STARTED){
            mSpeakNSpell.speak(text, new SpeakNSpell.ResultListener() {
                @Override
                public void onReady() {
                    Log.d(TAG, "SPOKE!");
                    startSpeakingAfterDelay();
                }

                @Override
                public void onError(@CheckForNull String message) {
                    Log.e(TAG, message == null ? "Unknown error while speaking" : message + " while speaking");
                    startSpeakingAfterDelay();
                }
            });
        }else{
            Log.e(TAG, "SpeakNSpell not started, but speak was called!!!");
        }
    }

    private void findViews(){
        mCommentTextView = (TextView) findViewById(R.id.commentTextView);
        mMoarButton      = (Button) findViewById(R.id.moarButton);
    }

    private static class NextCommentResult {
        public final SimpleComment comment;
        public final Throwable error;
        public NextCommentResult(SimpleComment comment, Throwable error){
            this.comment = comment;
            this.error = error;
        }
    }

    private class NextCommentTask extends AsyncTask<Void, Void, NextCommentResult> {

        @Override
        protected void onPreExecute() {
            mMoarButton.setEnabled(false);
        }

        @Override
        protected NextCommentResult doInBackground(Void... params) {
            try {
                final SimpleComment comment = mCommentService.nextComment();
                if(comment == null){
                    return new NextCommentResult(null, new Exception("no comments found!"));
                }
                return new NextCommentResult(comment, null);
            } catch (IOException e) {
                return new NextCommentResult(null, e);
            }
        }

        @Override
        protected void onPostExecute(NextCommentResult result) {
            mMoarButton.setEnabled(true);
            if (result.error != null) {
                Log.e(TAG, "nextComment failed", result.error);
            } else {
                if(result.comment.text != null){
                    speak(result.comment.text);
                    Toast.makeText(getApplicationContext(), result.comment.text, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Oopps, I'll try again!", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
}
