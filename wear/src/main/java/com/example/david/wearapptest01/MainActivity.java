package com.example.david.wearapptest01;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

import static com.example.david.wearapptest01.R.id.chronometer;
import static com.example.david.wearapptest01.R.id.lapChrono;
import static com.example.david.wearapptest01.R.id.textView3;

public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mClockView;
    private TextView textView3;

    public int lap = 0;
    DecimalFormat threeDigits = new DecimalFormat("000");
    public TextView splitsView;
    public Chronometer chronometer, lapChrono;
    public preciseCountdown beepTimer;
    private float countdownLen = 86400;
    private float countdownTick = (float) 18.75;
    private float levelOfAccuracy = (float) .01;

    public MediaPlayer bellSound;
    public AudioManager am;
    public AudioManager.OnAudioFocusChangeListener afChangeListener;

    public static final String ALERT_FREQUENCY = "com.example.david.IntervalTimerSimplest.ALERT_FREQUENCY";
    public static final String TIMER_DATA_STRINGS = "Interval,CountdownLength";
    public String TIMER_DATA_DATA;
    public Button startBeepAndChrono;

    public LinearLayout beepTimeSelect;
    public NumberPickerCustom minutePickerInterval1, secondPickerInterval1,
            millisecondPickerInterval1, minutePickerInterval2,
            secondPickerInterval2, millisecondPickerInterval2;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putLong("chronoBase", chronometer.getBase()); // i think a null base would mean that it was never started
        savedInstanceState.putBoolean("chronoIsPaused", chronometer.isPaused());
        savedInstanceState.putBoolean("chronoIsRunning", chronometer.isRunning());
        savedInstanceState.putLong("chronoPauseTime", chronometer.getPauseTime());
        // save splits

        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

//        if (savedInstanceState.getLong("chronoBase") > 0) {     // we have data from the last session
//            if (savedInstanceState.getBoolean("chronoIsPaused")) {
//                chronometer.start();
//                chronometer.setBase(savedInstanceState.getLong("chronoBase"));
//                chronometer.stop(savedInstanceState.getLong("chronoPauseTime"));
//            }
//            else {
//                chronometer.start();
//                chronometer.setBase(savedInstanceState.getLong("chronoBase"));
//            }
//
//
//        } else {        // this is a brand new session with no previous data
//            chronometer.stop();
//            lapChrono.stop();
//        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        lapChrono = (Chronometer) findViewById(R.id.lapChrono);
        textView3 = (TextView) findViewById(R.id.textView3);
        startBeepAndChrono = (Button) findViewById(R.id.startBeepAndChrono);
        splitsView = (TextView) findViewById(R.id.splitsView);
        beepTimeSelect = (LinearLayout) findViewById(R.id.beepTimeSelect);

        // variables related to picking the numbers
        minutePickerInterval1 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval1);
        secondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval1);
        millisecondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval1);
        minutePickerInterval2 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval2);
        secondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval2);
        millisecondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval2);
        beepTimeSelect.setVisibility(View.GONE);


        chronometer.stop();
        lapChrono.stop();

        // receiveDataFromPreviousActivity();

        isStartBeepButtonAvailable();

        setupBeepingComponents();
    }

    // Determines whether UI should display button for the user to start the beeping
    void isStartBeepButtonAvailable() {
        float limit = 2;
        if (countdownTick >= limit ) {
            startBeepAndChrono.setVisibility(View.VISIBLE);
        } else if (countdownTick < limit && countdownTick >= 0) {
            startBeepAndChrono.setVisibility(View.VISIBLE);
            startBeepAndChrono.setText("Time must be at least " + limit + " sec");
        } else if (countdownTick < 0) {
            startBeepAndChrono.setVisibility(View.GONE);
        }
    }

    // handles the data that is sent from the previous activity - this is currently the beep frequency
    void receiveDataFromPreviousActivity() {
        // Get the Intent that started this activity and extract the string that will be the beep frequency
        Bundle intent = getIntent().getExtras();
        countdownTick = intent.getFloat(MainActivity.ALERT_FREQUENCY);
        String receivedMessage = Float.toString( countdownTick );
        textView3.setText("Data sent was: " + receivedMessage); // test purposes, delete when finished
    }

    // Creates the mp3, AudioManager, and audio focus change listeners that we'll need to play sound
    void setupBeepingComponents() {
        // assign mp3 for music to be played on each tick of the countdown
        bellSound = MediaPlayer.create(this, R.raw.bellsound1secexactly);

        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // screw that other app - they're not getting focus
                }
            }
        };

        // I think there's a risk that this could get fired if the tick time was close to the length of the alert sound. If the alert sound is much shorter, it shouldn't be a problem.
        bellSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                am.abandonAudioFocus(afChangeListener);
            }
        });

        // create countdown timer for the sound alerts
        beepTimer = new preciseCountdown((int) (countdownLen * 1000), (int) (countdownTick * 1000), 0) {
            @Override
            public void onTick(long millisUntilFinished) {
                playTheSound();
            }

            @Override
            public void onFinished() {
                playTheSound();
            }
        };

        beepTimer.start();
        beepTimer.stop();
    }

    protected void onDestroy() {
        super.onDestroy();
        beepTimer.stop();
        bellSound.release();
    }

    public void startBeeper() {
        if (countdownTick >= 2) {        // if the user entered a beep frequency 2 sec or greater, that's good. if not, we stop them.
            // we're good to go, continue
            beepTimer.setInterval((long)(countdownTick * 1000));       // this makes sure the value is up-to-date with what the user entered

            beepTimer.start();
        } else if (countdownTick >= 0 && countdownTick < 2) {                            // We should probably send the user an error message instead. if the user entered a beep frequency less than our specified limit, we won't start the beeping, but we will start the stopwatch.
            return;
        } else if (countdownTick < 0) {     // the user asked for no beeping, so don't alert them, just don't beep
            return;
        }
    }

    public void playTheSound() {
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback
            if (bellSound.isPlaying()) {
                bellSound.seekTo(0);
            } else {
                bellSound.start();
            }
        }
    }

    public void startBeepAndChrono(View view) {
        if (!chronometer.isRunning() && !beepTimer.isWasStarted() && !beepTimer.isWasCancelled()) {
            startBeeper();
            chronometer.start();
            lapChrono.start();
            // startCountdown.setText("STOP BEEP");
        }
    }

    public void chronometerClick(View view) {
        if (chronometer.isRunning()) {
            if (lap == 0) {
                splitsView.setText("Lap" + threeDigits.format(lap) + " - "
                        + chronometer.getSplit(lap)
                        + "\n" + splitsView.getText().toString());  // add everything that was already there, too
            } else {
                splitsView.setText("Lap" + threeDigits.format(lap) + " - "
                        + chronometer.getSplit(lap)
                        + "\n" + splitsView.getText().toString());  // add everything that was already there, too
            }
            lap++;
            lapChrono.restart();
            lapChrono.setBase(chronometer.getLastSplit());
        }
        else {
            long tempElapsedTime = SystemClock.elapsedRealtime();
            chronometer.start();
            lapChrono.start();

            if (lap == 0) {
                lapChrono.setBase(chronometer.getmBase());
            }
            else {
                lapChrono.setBase(chronometer.getLastSplit());
            }
        }

    }

    public void pauseChrono(View view) {
        if (chronometer.isRunning()) {
            chronometer.stop();
            lapChrono.stop(chronometer.getPauseTime());
        }
    }

    public void stopBeeper(View view) {
        if (beepTimer.isWasStarted()) {
            beepTimer.stop();
        }
    }

    public void showChangeBeeper(View view) {
        beepTimeSelect.setVisibility(View.VISIBLE);
    }

    public void changeBeeper(View view) {
        float interval = (
                (((minutePickerInterval1.getValue() * 10) + minutePickerInterval2.getValue() ) * 60)
                        + ((secondPickerInterval1.getValue() * 10) + secondPickerInterval2.getValue())
                        + (float) (((millisecondPickerInterval1.getValue() * 10) + millisecondPickerInterval2.getValue()) * levelOfAccuracy));
        long newInterval = (long) (interval * 1000);       // this will need to be changed to something the user defines
        beepTimer.stop();
        beepTimer.setInterval(newInterval);
        beepTimer.restart();
        beepTimeSelect.setVisibility(View.GONE);
    }

    public void nextView(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DurationActivity.class);
        float message = countdownTick;
        intent.putExtra(ALERT_FREQUENCY, message);
        startActivity(intent);
    }

    //    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
//        super.onEnterAmbient(ambientDetails);
//    }
//
//    @Override
//    public void onUpdateAmbient() {
//        super.onUpdateAmbient();
//    }
//
//    @Override
//    public void onExitAmbient() {
//        super.onExitAmbient();
//    }
}
