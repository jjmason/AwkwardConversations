package jm.net.ac.service;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import jm.net.ac.AwkwardException;
import jm.net.ac.YoutubeComment;

public abstract class AbstractService {
    private static final int QUEUE_SIZE = 25;

    private Thread mWorker;
    private final Lock mLock = new ReentrantLock();
    private final Condition mStarted = mLock.newCondition();
    private final BlockingQueue<YoutubeComment> mQueue = new ArrayBlockingQueue<YoutubeComment>(QUEUE_SIZE);
    private final Condition mStoppedCondition = mLock.newCondition();
    private volatile boolean mStopped = false;

    protected abstract @Nullable YoutubeComment fetchComment() throws IOException;


    public @Nullable YoutubeComment nextComment(){
        start();
        try {
            return mQueue.take();
        } catch (InterruptedException e) {
            throw new AwkwardException(e);
        }
    }

    public void stop(){
        final Lock lock = mLock;
        lock.lock();
        try {
            mStopped = true;
            try {
                mStoppedCondition.await();
            } catch (InterruptedException e) {
                throw new AwkwardException("Interrupted while stopping", e);
            }
        }finally{
            lock.unlock();
        }
    }

    public void start(){
        final Lock lock = mLock;
        lock.lock();
        try{
            if(mWorker == null){
                try {
                    startWorker();
                } catch (InterruptedException e) {
                    throw new AwkwardException(e);
                }
            }
        }finally{
            lock.unlock();
        }
    }

    private boolean stopped(){
        return mStopped;
    }

    private void startWorker() throws InterruptedException {
        mStopped = false;
        mWorker = new Thread(new Runnable() {
            @Override
            public void run() {
                work();
            }
        });
        mStarted.await();
    }


    private void work(){
       Log.i("Worker", "Starting...");
       final Lock lock = mLock;
       lock.lock();
       try{
           mStarted.signalAll();
       }finally{
           lock.unlock();
       }

       while(!stopped()){
           try {
               YoutubeComment comment = fetchComment();
               if(comment != null) {
                   mQueue.put(comment);
               }
           } catch (InterruptedException | IOException e) {
               throw new AwkwardException(e);
           }
       }

       Log.i("Worker", "Stopping");
       final Lock lock2 = mLock;
       lock2.lock();
       try{
           mStoppedCondition.signalAll();
       }finally{
           lock2.unlock();
       }
    }
}
