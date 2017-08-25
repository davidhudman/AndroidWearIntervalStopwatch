package com.example.david.wearapptest01;

/**
 * Created by david on 8/24/2017.
 */


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Chronometer extends TextView {
    @SuppressWarnings("unused")

    private static final String TAG = "Chronometer";

    public interface OnChronometerTickListener {

        void onChronometerTick(Chronometer chronometer);
    }

    private long mBase;
    private long pauseTime;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private boolean isPaused = false;
    private OnChronometerTickListener mOnChronometerTickListener;

    private static final int TICK_WHAT = 2;

    private long timeElapsed;

    private long lastSplit = 0;

    public Chronometer(Context context) {
        this (context, null, 0);
    }

    public Chronometer(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public Chronometer(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        init();
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return mBase;
    }

    public void setOnChronometerTickListener(
            OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    public void start() {
        long tempElapsedTime = SystemClock.elapsedRealtime();

        if (isPaused) {
            mBase = tempElapsedTime - (pauseTime - mBase);
            lastSplit = tempElapsedTime - (pauseTime - lastSplit);
        }
        mStarted = true;
        isPaused = false;
        updateRunning();
    }

    public String getSplit() {
        updateRunning();

        long tempTimeElapsed = SystemClock.elapsedRealtime();

        String tempString;

        if (lastSplit == 0) {
            tempString = getTextTimeSinceLastSplit(tempTimeElapsed, mBase) + "-" + getText(tempTimeElapsed);    // try removing the first part of this and see if this line is ever even reached
        }
        else {
            tempString = getTextTimeSinceLastSplit(tempTimeElapsed, lastSplit) + "-" + getText(tempTimeElapsed);
        }

        lastSplit = tempTimeElapsed;

        return tempString;
    }

    public void stop() {
        isPaused = true;
        mStarted = false;
        mRunning = false;
        pauseTime = SystemClock.elapsedRealtime();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isRunning() {
        return mStarted;
    }


    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super .onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super .onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    public String getText(long now) {
        timeElapsed = now - mBase;

        DecimalFormat twoDigits = new DecimalFormat("00");
        DecimalFormat threeDigits = new DecimalFormat("000");

        int hours = (int)(timeElapsed / (3600 * 1000));
        int remaining = (int)(timeElapsed % (3600 * 1000));

        int minutes = (int)(remaining / (60 * 1000));
        remaining = (int)(remaining % (60 * 1000));

        int seconds = (int)(remaining / 1000);
        remaining = (int)(remaining % (1000));

        int milliseconds = (int)(((int)timeElapsed % 1000) / 1);

        String text = "";

        if (hours > 0) {
            text += twoDigits.format(hours) + ":";
        }

        text += twoDigits.format(minutes) + ":";
        text += twoDigits.format(seconds) + ".";
        // text += Integer.toString(milliseconds);
        text += threeDigits.format(milliseconds);

        return text;
    }

    public String getTextTimeSinceLastSplit(long now, long m_Base) {
        timeElapsed = now - m_Base;

        DecimalFormat twoDigits = new DecimalFormat("00");
        DecimalFormat threeDigits = new DecimalFormat("000");

        int hours = (int)(timeElapsed / (3600 * 1000));
        int remaining = (int)(timeElapsed % (3600 * 1000));

        int minutes = (int)(remaining / (60 * 1000));
        remaining = (int)(remaining % (60 * 1000));

        int seconds = (int)(remaining / 1000);
        remaining = (int)(remaining % (1000));

        int milliseconds = (int)(((int)timeElapsed % 1000) / 1);

        String text = "";

        if (hours > 0) {
            text += twoDigits.format(hours) + ":";
        }

        text += twoDigits.format(minutes) + ":";
        text += twoDigits.format(seconds) + ".";
        // text += Integer.toString(milliseconds);
        text += threeDigits.format(milliseconds);

        return text;
    }

    private synchronized void updateText(long now) {
        setText(getText(now));
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this , TICK_WHAT), 1);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

}

