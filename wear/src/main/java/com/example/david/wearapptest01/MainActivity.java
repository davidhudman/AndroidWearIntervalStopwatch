package com.example.david.wearapptest01;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.KeyEvent;
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

import org.w3c.dom.Text;

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
    private float countdownTick = (float) 12;
    private float levelOfAccuracy = (float) .01;

    public MediaPlayer bellSound;
    public AudioManager am;
    public AudioManager.OnAudioFocusChangeListener afChangeListener;
    Vibrator vibrator;
    long[] vibrationPattern = {0, 500, 250, 500, 250, 500};
    //-1 - don't repeat
    final int indexInPatternToRepeat = -1;
    public boolean isVibrateEnabled = true;
    public boolean isBeepEnabled = true;

    public static final String ALERT_FREQUENCY = "com.example.david.IntervalTimerSimplest.ALERT_FREQUENCY";
    public static final String TIMER_DATA_STRINGS = "Interval,CountdownLength";
    public String TIMER_DATA_DATA;
    public Button startBeepAndChrono;

    public LinearLayout beepTimeSelect;
    public NumberPickerCustom minutePickerInterval1, secondPickerInterval1,
            millisecondPickerInterval1, minutePickerInterval2,
            secondPickerInterval2, millisecondPickerInterval2;

    // Dev Testing variables
    public boolean isDeveloperTestingEnabled = true;
    public TextView devTestFieldText;
    public int gestureCounter = 0;

    @Override /* KeyEvent.Callback */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
                return moveToNextItem();
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                return moveToPreviousItem();
            default:
                devTestFieldText.setText("Other Gesture " + gestureCounter++);
                // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
                return super.onKeyDown(keyCode, event);
        }
    }

    /** Shows the next item in the custom list. */
    private boolean moveToNextItem() {
        boolean handled = false;
        devTestFieldText.setText("NextItem Gesture " + gestureCounter++);
        // Return true if handled successfully, otherwise return false.
        return handled;
    }

    /** Shows the previous item in the custom list. */
    private boolean moveToPreviousItem() {
        boolean handled = false;
        devTestFieldText.setText("PrevItem Gesture " + gestureCounter++);
        // Return true if handled successfully, otherwise return false.
        return handled;
    }

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

        // Developer Testing Variables
        devTestFieldText = (TextView) findViewById(R.id.devTestField);


        beepTimeSelect.setVisibility(View.GONE);


        chronometer.stop();
        lapChrono.stop();

        // receiveDataFromPreviousActivity();

        isStartBeepButtonAvailable();

        setupBeepingComponents();

        startBeepAndChrono.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // switch vibrate mode
                if (isVibrateEnabled)  {    // then we need to disable vibrate
                    startBeepAndChrono.setText("Start Beep & Chrono");
                }
                else {                      // then we need to enable Vibrate
                    startBeepAndChrono.setText("Start VibBeep & Chrono");

                }
                isVibrateEnabled = !isVibrateEnabled;
                return true;
            }
        });

        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDeveloperComponents();
            }
        });
    }

    void toggleDeveloperComponents() {
        if (isDeveloperTestingEnabled) {    // then we need to disable developer mode
            devTestFieldText.setVisibility(View.GONE);
        }
        else {                              // then we need to enable developer mode
            devTestFieldText.setVisibility(View.VISIBLE);
        }

        isDeveloperTestingEnabled = !isDeveloperTestingEnabled;

    }

    // Determines whether UI should display button for the user to start the beeping
    void isStartBeepButtonAvailable() {
        float limit = 3;
        if (countdownTick >= limit ) {
            startBeepAndChrono.setVisibility(View.VISIBLE);
        } else if (countdownTick < limit && countdownTick >= 0) {
            // alert the user that their time is too small
            startBeepAndChrono.setVisibility(View.GONE);
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

        // These two values represent the original state of the beep timer
        beepTimer.setWasCancelled(false);
        beepTimer.setWasStarted(false);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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
        if (isBeepEnabled) {
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

        if (isVibrateEnabled) {
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
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

    public void showOrHideChangeBeeper(View view) {
        if (beepTimeSelect.getVisibility() == View.VISIBLE) {
            beepTimeSelect.setVisibility(View.GONE);
        }
        else {
            beepTimeSelect.setVisibility(View.VISIBLE);
        }
    }

    public void raiseMusicVolume(View view) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+1, 0);
    }

    public void lowerMusicVolume(View view) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)-1, 0);
    }

    public void changeBeeper(View view) {
        float interval = (
                (((minutePickerInterval1.getValue() * 10) + minutePickerInterval2.getValue() ) * 60)
                        + ((secondPickerInterval1.getValue() * 10) + secondPickerInterval2.getValue())
                        + (float) (((millisecondPickerInterval1.getValue() * 10) + millisecondPickerInterval2.getValue()) * levelOfAccuracy));
        countdownTick = interval;
        long newInterval = (long) (interval * 1000);       // this will need to be changed to something the user defines
        // beepTimer.stop();
        beepTimer.setInterval(newInterval);
        beepTimeSelect.setVisibility(View.GONE);

        isStartBeepButtonAvailable();
    }

    public void restartBeeper(View view) {
        beepTimer.restart();
        chronometer.start();
        lapChrono.start();

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
