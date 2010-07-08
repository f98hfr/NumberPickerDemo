package com.finurligt.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.finurligt.widget.NumberPicker;
import com.finurligt.widget.NumberPicker.NumberPickerValueChangedListener;

public class NumberPickerDemo extends Activity {
    
    private static final String LOG_TAG = NumberPickerDemo.class.getSimpleName();
    private NumberPicker mNumberPicker = null;
    
    private NumberPickerValueChangedListener mPickerChangedListener = 
        new NumberPickerValueChangedListener() {
            
            @Override
            public void valueChanged(int value) {                
                Log.d(LOG_TAG, "Picker value changed to: " + value);
            }
        };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Find the picker
        mNumberPicker = (NumberPicker)findViewById(R.id.my_picker);
        
        // Setup a callback listener (NumberPickerValueChangedListener)
        if(mNumberPicker != null) {
            mNumberPicker.addValueChangedListener(mPickerChangedListener);
        }
    }
}