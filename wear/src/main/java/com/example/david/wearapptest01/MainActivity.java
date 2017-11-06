package com.example.david.wearapptest01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.Toast;

import java.text.DecimalFormat;

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
    public preciseCountdown alertTimer;
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
    public boolean isNextSplitStartingBeeper = false;
    public int alertFrequencyLowerLimit = 3; // the user needs to enter an alert frequency of at least this many seconds

    public static final String ALERT_FREQUENCY = "com.example.david.IntervalTimerSimplest.ALERT_FREQUENCY";
    public static final String TIMER_DATA_STRINGS = "Interval,CountdownLength";
    public String TIMER_DATA_DATA;
    public Button startBeepAndChrono;
    public ImageButton startAlertsButton;

    public LinearLayout beepTimeSelect, clearAllSplitsLayout;
    public NumberPickerCustom minutePickerInterval1, secondPickerInterval1,
            millisecondPickerInterval1, minutePickerInterval2,
            secondPickerInterval2, millisecondPickerInterval2;

    // Dev Testing variables
    public boolean isDeveloperTestingEnabled = false;
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
        // loadDeveloperComponents();

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        lapChrono = (Chronometer) findViewById(R.id.lapChrono);
        textView3 = (TextView) findViewById(R.id.textView3);
        startBeepAndChrono = (Button) findViewById(R.id.startBeepAndChrono);
        startAlertsButton = (ImageButton) findViewById(R.id.startAlertsButton);
        splitsView = (TextView) findViewById(R.id.splitsView);
        beepTimeSelect = (LinearLayout) findViewById(R.id.beepTimeSelect);
        clearAllSplitsLayout = (LinearLayout) findViewById(R.id.clearAllSplits);

        // variables related to picking the numbers
        minutePickerInterval1 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval1);
        secondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval1);
        millisecondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval1);
        minutePickerInterval2 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval2);
        secondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval2);
        millisecondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval2);

        // Developer Testing Variables
        devTestFieldText = (TextView) findViewById(R.id.devTestField);
        loadDeveloperComponents();


        beepTimeSelect.setVisibility(View.GONE);


        // get saved data
        chronometerRestore();

        // receiveDataFromPreviousActivity();

        isStartBeepButtonAvailable();

        setupBeepingComponents();

        startBeepAndChrono.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // do something
                return true;
            }
        });

        startAlertsButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                changeBeeper(null);
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

    public void backupSplitData() {
        // deliver message to Android handset - making a GET request with the data to a website is not an option because Android Wear before 2.0 does not support the watch directly accessing the Internet
    }

    public void chronometerRestore() {
        splitsView.setText(getSavedSharedPreferences("splitsText"));
        // get data from last stopwatch if it's status was started
        if (getSavedSharedPreferences("isStartedCumulative") == "true") {

            chronometer.setBase(Long.valueOf(getSavedSharedPreferences("startTimeCumulative")));
            chronometer.start();
            // chronometer.setPauseTime(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative")));

            lapChrono.setBase(Long.valueOf(getSavedSharedPreferences("startTimeLap")));
            lapChrono.start();
            lap = Integer.valueOf(getSavedSharedPreferences("lap"));

        }
        // if the timer was paused
        else if (getSavedSharedPreferences("isPausedCumulative") == "true") {

            lap = Integer.valueOf(getSavedSharedPreferences("lap"));

            if (lap == 0) {
                // make sure both timers are reset to their previous values
                chronometer.setBase(Long.valueOf(getSavedSharedPreferences("startTimeCumulative")));
                lapChrono.setBase(Long.valueOf(getSavedSharedPreferences("startTimeLap")));

                chronometer.stop(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative")));
                lapChrono.stop(Long.valueOf(getSavedSharedPreferences("pauseTimeLap")));

                chronometer.setText(chronometer.getText(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative"))));
                lapChrono.setText(chronometer.getText(Long.valueOf(getSavedSharedPreferences("pauseTimeLap"))));
            } else {    // when there is already a lap split and it is paused currently - this code block seems to mess up lapChrono for some reason and it counts from 172 minutes (not sure if up or down)
                // make sure both timers are reset to their previous values
                chronometer.setBase(Long.valueOf(getSavedSharedPreferences("startTimeCumulative")));
                lapChrono.setBase(Long.valueOf(getSavedSharedPreferences("startTimeCumulative")));

                chronometer.stop(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative")));
                lapChrono.stop(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative")));

                chronometer.setText(chronometer.getText(Long.valueOf(getSavedSharedPreferences("pauseTimeCumulative"))));
                lapChrono.setText(lapChrono.getText(Long.valueOf(getSavedSharedPreferences("pauseTimeLap"))));

                // something here fixes it...
                //chronometer.getSplit(lap);
                lapChrono.restart();
                lapChrono.setBase(Long.valueOf(getSavedSharedPreferences("lastSplitLap")));
                lapChrono.stop();
                //lapChrono.setBase(chronometer.getLastSplit());
            }

//            chronometer.setText("Paused");
//            lapChrono.setText("Paused");
        }
        // if the timer was not started or paused
        else {
            chronometer.stop();
            lapChrono.stop();
        }
    }

    public void chronometerSnapshot() {
        updateSharedPreferences("splitsText", splitsView.getText().toString());

        updateSharedPreferences("startTimeCumulative", String.valueOf(chronometer.getBase()));
        updateSharedPreferences("pauseTimeCumulative", String.valueOf(chronometer.getPauseTime()));
        updateSharedPreferences("isStartedCumulative", String.valueOf(chronometer.isRunning()));
        updateSharedPreferences("isPausedCumulative", String.valueOf(chronometer.isPaused()));
        updateSharedPreferences("lastSplitCumulative", String.valueOf(chronometer.getLastSplit()));

        updateSharedPreferences("startTimeLap", String.valueOf(lapChrono.getBase()));
        updateSharedPreferences("pauseTimeLap", String.valueOf(lapChrono.getPauseTime()));
        updateSharedPreferences("isStartedLap", String.valueOf(lapChrono.isRunning()));
        updateSharedPreferences("isPausedLap", String.valueOf(lapChrono.isPaused()));
        updateSharedPreferences("lastSplitLap", String.valueOf(lapChrono.getLastSplit()));

        updateSharedPreferences("lap", String.valueOf(lap));
    }

    public void switchToNextAlertMode(View view) {
        Context context = getApplicationContext();
        CharSequence toastText = "ERROR: Text not set.";
        int duration = Toast.LENGTH_SHORT;

        if (isVibrateEnabled && isBeepEnabled)  {           // then we need to disable vibrate but leave beeping
            isVibrateEnabled = false;
            isBeepEnabled = true;
            toastText = "Vibration off.\nBeeping on.";

        } else if (!isVibrateEnabled && isBeepEnabled) {    // then we need to disable beep but enable vibrate
            isVibrateEnabled = true;
            isBeepEnabled = false;
            toastText = "Vibration on.\nBeeping off.";

        } else if (isVibrateEnabled && !isBeepEnabled) {    // then we need to disable both
            isVibrateEnabled = false;
            isBeepEnabled = false;
            toastText = "Vibration off.\nBeeping off.";

        } else if (!isVibrateEnabled && !isBeepEnabled) {   // then we need to enable both
            isVibrateEnabled = true;
            isBeepEnabled = true;
            toastText = "Vibration on.\nBeeping on.";

        }

        Toast.makeText(context, toastText, duration).show();
    }

    void loadDeveloperComponents() {
        if (isDeveloperTestingEnabled) {    // then we need to disable developer mode
            devTestFieldText.setVisibility(View.VISIBLE);
        }
        else {                              // then we need to enable developer mode
            devTestFieldText.setVisibility(View.GONE);
        }
    }

    void toggleDeveloperComponents() {
        isDeveloperTestingEnabled = !isDeveloperTestingEnabled;
        Toast.makeText(getApplicationContext(), "Developer mode " + String.valueOf(isDeveloperTestingEnabled), Toast.LENGTH_SHORT).show();
        loadDeveloperComponents();
    }

    // Determines whether UI should display button for the user to start the beeping
    void isStartBeepButtonAvailable() {
        if (countdownTick >= alertFrequencyLowerLimit ) {
            startBeepAndChrono.setVisibility(View.VISIBLE);
        } else if (countdownTick < alertFrequencyLowerLimit && countdownTick >= 0) {
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
        alertTimer = new preciseCountdown((int) (countdownLen * 1000), (int) (countdownTick * 1000), 0) {
            @Override
            public void onTick(long millisUntilFinished) {
                playTheAlert();
            }

            @Override
            public void onFinished() {
                playTheAlert();
            }
        };

        // These two values represent the original state of the beep timer
        alertTimer.setWasCancelled(false);
        alertTimer.setWasStarted(false);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    protected void onDestroy() {
        super.onDestroy();
        alertTimer.stop();
        bellSound.release();
    }

    public void startBeeper() {
        if (countdownTick >= 2) {        // if the user entered a beep frequency 2 sec or greater, that's good. if not, we stop them.
            // we're good to go, continue
            alertTimer.setInterval((long)(countdownTick * 1000));       // this makes sure the value is up-to-date with what the user entered

            alertTimer.start();
        } else if (countdownTick >= 0 && countdownTick < 2) {
            return;
        } else if (countdownTick < 0) {     // the user asked for no beeping, so don't alert them, just don't beep
            return;
        }
    }

    public void playTheAlert() {
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
        if (!chronometer.isRunning() && !alertTimer.isWasStarted() && !alertTimer.isWasCancelled()) {
            startBeeper();
            chronometer.start();
            lapChrono.start();
            isNextSplitStartingBeeper = false;
            // startCountdown.setText("STOP BEEP");
        }
    }

    public void chronometerClick(View view) {
        chronometerClicked();
    }

    public void chronometerClicked() {
        if (chronometer.isRunning()) {
            String tempSplitText = "Lap" + threeDigits.format(lap) + " - " + chronometer.getSplit(lap) + "\n" + splitsView.getText().toString();
            if (lap == 0) {
                splitsView.setText(tempSplitText);  // add everything that was already there, too. We could make this one just add the new stuff, but I like making sure that no old data is lost in case it hasn't been backed up by the user yet.
            } else {
                splitsView.setText(tempSplitText);  // add everything that was already there, too
            }
            lap++;
            lapChrono.restart();
            lapChrono.setBase(chronometer.getLastSplit());

            if (isNextSplitStartingBeeper) {
                restartBeeper(null);
                isNextSplitStartingBeeper = false;
            }

            developerToast("if statement start");
        }
        else {  // starting from zero or paused
            long tempElapsedTime = SystemClock.elapsedRealtime();

            if (!chronometer.isPaused()) {  // if it's not paused, we need to make sure that we're going to get a new base time
                chronometer.setBase(tempElapsedTime);
                lapChrono.setBase(tempElapsedTime);
                chronometer.start();
                lapChrono.start();
            } else {
                chronometer.start();
                lapChrono.start();
            }

            if (lap == 0) {
                lapChrono.setBase(chronometer.getmBase());
            }
            else {
                lapChrono.setBase(chronometer.getLastSplit());
                developerToast("else statement start");
            }
        }
        // save the data somewhere in case the app gets closed
        chronometerSnapshot();
    }

    public void developerToast(String message) {
        if (isDeveloperTestingEnabled) {
            userToast(message);
        }
    }

    public void userToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public String getSavedSharedPreferences(String field) {
        SharedPreferences sharePref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharePref.getString(field, "");
    }

    public void updateSharedPreferences(String field, String value) {
        // update stored data
        SharedPreferences sharePref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePref.edit();
        editor.putString(field, value);
        editor.apply();
    }

    public void eraseSharedPreferencesData(View view) {
        updateSharedPreferences("splitsText", "");
        updateSharedPreferences("isStartedCumulative", "false");
        updateSharedPreferences("isPausedCumulative", "false");
        updateSharedPreferences("lastSplitLap", "");
        updateSharedPreferences("lap", "0");
        lap = 0;

        chronometer.quit();
        lapChrono.quit();
        chronometer.setText("00:00.00");
        lapChrono.setText("00:00.00");

        // update the view
        splitsView.setText("");
        showOrHideClearAllSplits(view);
    }

    public void showOrHideClearAllSplits(View view) {
        if (clearAllSplitsLayout.getVisibility() == View.VISIBLE) {
            clearAllSplitsLayout.setVisibility(View.GONE);
        }
        else {
            clearAllSplitsLayout.setVisibility(View.VISIBLE);
        }

    }

    public void pauseChronoClick(View view) {
        pauseChrono();
    }

    public void pauseChrono() {
        if (chronometer.isRunning()) {
            long tempElapsedTime = SystemClock.elapsedRealtime();
            chronometer.stop(tempElapsedTime);
            if (lap > 0) {
                lapChrono.setBase(chronometer.getLastSplit());
            }
            lapChrono.stop(tempElapsedTime);
            chronometerSnapshot();
        }
    }

    public void stopBeeper(View view) {
        if (alertTimer.isWasStarted()) {
            alertTimer.stop();
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

        if (interval >= alertFrequencyLowerLimit) {
            countdownTick = interval;
            long newInterval = (long) (interval * 1000);       // this will need to be changed to something the user defines
            // alertTimer.stop();
            alertTimer.setInterval(newInterval);
            beepTimeSelect.setVisibility(View.GONE);

            isStartBeepButtonAvailable();
        }
        else {
            Context context = getApplicationContext();
            CharSequence text = "Frequency entered was less than " + alertFrequencyLowerLimit + " seconds. Please enter a frequency of at least " + alertFrequencyLowerLimit + " seconds.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        isNextSplitStartingBeeper = true;
        userToast("When the stopwatch is running, pressing the lap split button will start the alerts.");
    }

    public void restartBeeper(View view) {
        alertTimer.restart();
        chronometer.start();
        lapChrono.start();
        isNextSplitStartingBeeper = false;
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
