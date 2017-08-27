package com.example.david.wearapptest01;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DurationActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    // all variables related to picking the numbers
    private float levelOfAccuracy = (float) .01;
    public NumberPickerCustom minutePickerInterval1, secondPickerInterval1,
            millisecondPickerInterval1, minutePickerInterval2,
            secondPickerInterval2, millisecondPickerInterval2;
    public static final String ALERT_FREQUENCY = "com.example.david.IntervalTimerSimplest.ALERT_FREQUENCY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        // variables related to picking the numbers
        minutePickerInterval1 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval1);
        secondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval1);
        millisecondPickerInterval1 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval1);
        minutePickerInterval2 = (NumberPickerCustom) findViewById(R.id.minutePickerInterval2);
        secondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.secondPickerInterval2);
        millisecondPickerInterval2 = (NumberPickerCustom) findViewById(R.id.millisecondPickerInterval2);
    }

//    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
//        super.onEnterAmbient(ambientDetails);
//        updateDisplay();
//    }
//
//    @Override
//    public void onUpdateAmbient() {
//        super.onUpdateAmbient();
//        updateDisplay();
//    }
//
//    @Override
//    public void onExitAmbient() {
//        updateDisplay();
//        super.onExitAmbient();
//    }
//
//    private void updateDisplay() {
//        if (isAmbient()) {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
////            mTextView.setTextColor(getResources().getColor(android.R.color.white));
////            mClockView.setVisibility(View.VISIBLE);
////            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
//        } else {
//            mContainerView.setBackground(null);
////            mTextView.setTextColor(getResources().getColor(android.R.color.black));
////            mClockView.setVisibility(View.GONE);
//        }
//    }

    public void nextViewBeep(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        // EditText editText = (EditText) findViewById(R.id.editText);
        // String message = editText.getText().toString();
        float message = (
                (((minutePickerInterval1.getValue() * 10) + minutePickerInterval2.getValue() ) * 60)
                        + ((secondPickerInterval1.getValue() * 10) + secondPickerInterval2.getValue())
                        + (float) (((millisecondPickerInterval1.getValue() * 10) + millisecondPickerInterval2.getValue()) * levelOfAccuracy));
        intent.putExtra(ALERT_FREQUENCY, message);
        startActivity(intent);
    }

    public void nextViewNoBeep(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);

        float message = -1;         // anything less than 0 lets the next activity know that we don't need beeping
        intent.putExtra(ALERT_FREQUENCY, message);
        startActivity(intent);
    }
}
