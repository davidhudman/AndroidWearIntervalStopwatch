package com.example.david.wearapptest01;

/**
 * Created by david on 8/24/2017.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.NumberPicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)//For backward-compability
public class NumberPickerCustom extends NumberPicker {

    public NumberPickerCustom(Context context) {
        super(context);
    }

    public NumberPickerCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttributeSet(attrs);
    }

    public NumberPickerCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttributeSet(attrs);
    }

    private void processAttributeSet(AttributeSet attrs) {
        //This method reads the parameters given in the xml file and sets the properties according to it
        this.setMinValue(attrs.getAttributeIntValue(null, "min", 0));
        this.setMaxValue(attrs.getAttributeIntValue(null, "max", 0));
        this.setValue(attrs.getAttributeIntValue(null, "value", 0));
        this.setWrapSelectorWheel(true);
    }
}

