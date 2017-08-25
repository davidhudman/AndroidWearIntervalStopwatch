package com.example.david.wearapptest01;

/**
 * Created by david on 8/24/2017.
 */


import java.util.Timer;
import java.util.TimerTask;

public class preciseCountdown extends Timer {
    private long totalTime, interval, delay;
    private TimerTask task;
    private long startTime = -1;
    private boolean restart = false, wasCancelled = false, wasStarted = false;

    public preciseCountdown(long totalTime, long interval) {
        this(totalTime, interval, 0);
    }

    public preciseCountdown(long totalTime, long interval, long delay) {
        super("PreciseCountdown", true);
        this.delay = delay;
        this.interval = interval;
        this.totalTime = totalTime;
        this.task = getTask(totalTime);
    }

    public boolean isWasStarted() {
        return wasStarted;
    }

    public boolean isWasCancelled() {
        return wasCancelled;
    }

    public void start() {
        wasStarted = true;
        this.scheduleAtFixedRate(task, delay, interval);
    }

    public void restart() {
        if(!wasStarted) {
            start();
        }
        else if(wasCancelled) {
            wasCancelled = false;
            this.task = getTask(totalTime);
            start();
        }
        else{
            this.restart = true;
        }
    }

    public void stop() {
        this.wasCancelled = true;
        this.task.cancel();
    }

    // Call this when there's no further use for this timer
    public void dispose(){
        cancel();
        purge();
    }

    private TimerTask getTask(final long totalTime) {
        return new TimerTask() {

            @Override
            public void run() {
                long timeLeft;
                if (startTime < 0 || restart) {
                    startTime = scheduledExecutionTime();
                    timeLeft = totalTime;
                    restart = false;
                } else {
                    timeLeft = totalTime - (scheduledExecutionTime() - startTime);

                    if (timeLeft <= 0) {
                        this.cancel();
                        startTime = -1;
                        onFinished();
                        return;
                    }
                }

                onTick(timeLeft);
            }
        };
    }

    public void setTotalTime(long total_Time) {
        totalTime = total_Time;
    }
    public long getTotalTime() {
        return totalTime;
    }
    public void setInterval(long _interval) {
        interval = _interval;
    }
    public long getInterval() {
        return interval;
    }
    public void setDelay(long _delay) {
        delay = _delay;
    }
    public long getDelay() {
        return delay;
    }

    public void onTick(long timeLeft) {

    }
    public void onFinished(){

    };

    // public abstract void onTick(long timeLeft);
    // public abstract void onFinished();
}


