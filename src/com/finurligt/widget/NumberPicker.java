/** 
 * Copyright (c) 2010 Henrik Fredriksson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. 
 */

package com.finurligt.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.finurligt.demo.R;

public class NumberPicker extends View {

    private static final String LOG_TAG = NumberPicker.class.getSimpleName();
    
    private int mScreenWidth = 0;
    private int mMaxValue = 99;
    private int mMinValue = 0;
    private int mDefaultValue = 0;
    private int mCurrentValue = 0;
    private int mBackgroundColor = Color.BLACK;
    private int mTextColor = Color.WHITE;
    private boolean mBackgroundDim = false;
    private NumberPickerValueChangedListener mListener = null;

    public NumberPicker(Context context) {
        super(context);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker);
        mMaxValue = a.getInt(R.styleable.NumberPicker_max_value, 99);
        mMinValue = a.getInt(R.styleable.NumberPicker_min_value, 0);
        mCurrentValue = mDefaultValue = a.getInteger(R.styleable.NumberPicker_default_value, 0);
        mBackgroundColor = a.getColor(R.styleable.NumberPicker_background_color, Color.BLACK);
        mTextColor = a.getColor(R.styleable.NumberPicker_text_color, Color.WHITE);
        mBackgroundDim = a.getBoolean(R.styleable.NumberPicker_background_dim, false);
        a.recycle();

        // Check for invalid XML min and max values
        if(mMinValue > mMaxValue) {
            throw new IllegalArgumentException("min_value is greater than max_value, not invalid");
        }

        // Check for identical background and text color
        if(mBackgroundColor == mTextColor) {
            Log.w(LOG_TAG, "Background color and text color is equal => no text will be visible!");
        }

        // Check for default value outside of [min,max] range.
        // Default to min value in this case
        if(mDefaultValue < mMinValue || mDefaultValue > mMaxValue) {
            mCurrentValue = mDefaultValue = mMinValue;
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager winm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        winm.getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int h = getMeasuredHeight();
        int w = getMeasuredWidth();
        Paint paint = new Paint();

        paint.setColor(mBackgroundColor);
        canvas.drawRect(1, 1, w-1, h-1, paint);
        paint.setColor(mTextColor);
        int textHeight = (int)(0.6f*h);
        paint.setTextSize(textHeight);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        String str = String.format("%02d", mCurrentValue);
        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        int height = bounds.height();
        canvas.drawText(str, w/2, h/2 + height/2, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureWidth(int wms) {
        int size = MeasureSpec.getSize(wms);
        int mode = MeasureSpec.getMode(wms);

        // Unless specified, a NumberPicker wants to occupy
        // 20% of the screen width
        final int wantedWidth = (int)(0.2f * (float)mScreenWidth);
        int finalWidth = 0;

        switch(mode) {
            case MeasureSpec.AT_MOST:
            {
                if(wantedWidth > size) {
                    finalWidth = size;
                }
                else {
                    finalWidth = wantedWidth;
                }
            }
            break;
            case MeasureSpec.EXACTLY:
            {
                finalWidth = size;
            }
            break;
            case MeasureSpec.UNSPECIFIED:
            default:
            {
                // Use predefined wantedWidth
                finalWidth = wantedWidth;
            }
        }

        return finalWidth;
    }

    private int measureHeight(int hms) {
        int size = MeasureSpec.getSize(hms);
        int mode = MeasureSpec.getMode(hms);

        // Unless specified, a NumberPicker wants to occupy
        // 20% of the screen width
        final int wantedHeight = (int)(0.2f * (float)mScreenWidth);
        int finalHeight = 0;

        switch(mode) {
            case MeasureSpec.AT_MOST:
            {
                if(wantedHeight > size) {
                    finalHeight = size;
                }
                else {
                    finalHeight = wantedHeight;
                }
            }
            break;
            case MeasureSpec.EXACTLY:
            {
                finalHeight = size;
            }
            break;
            case MeasureSpec.UNSPECIFIED:
            default:
            {
                // Use predefined wantedWidth
                finalHeight = wantedHeight;
            }
        }

        return finalHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {        
        final PickerDialog md = new PickerDialog(getContext(), 
                                                 R.style.NumberPickerDialog, 
                                                 mMinValue, 
                                                 mMaxValue,
                                                 mCurrentValue);

        md.show();

        md.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int x = md.getSelectedValue();
                if(x != -1) {
                    setValue(x, true);
                }
            }
        });

        if(mBackgroundDim) {
            WindowManager.LayoutParams lp = md.getWindow().getAttributes();
            lp.dimAmount = 0.5f;
            md.getWindow().setAttributes(lp);
            md.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        
        return super.onTouchEvent(event); 
    }

    public void addValueChangedListener(NumberPickerValueChangedListener npcl) {
        mListener = npcl;
    }

    /**
     * Set the value of the NumberPicker.
     * 
     * @param value     The value to set
     * @param notify    True if a possibly registered NumberPickerValueChangedListener
     *                  should be notified, false if not. For a client it might be good
     *                  to control this behavior.
     */
    public void setValue(int value, boolean notify) {
        mCurrentValue = value;
        // Notify listener
        if(mListener != null && notify) {
            mListener.valueChanged(value);
        }
        
        // Redraw with new value
        invalidate();
    }

    /**
     * Get the current value of the NumberPicker
     * @return  The current value
     */
    public int getValue() {
        return mCurrentValue;
    }

    /**
     * Get the max value of the NumberPicker
     * @return  The max value
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * Get the min value of the NumberPicker
     * @return  The min value
     */
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * Private dialog class that displays a listview (xml layout in
     * picker_item.xml)
     */
    private class PickerDialog extends Dialog {
        private int mSelectedValue = -1;
        private int mCurrentSelection = 0;
        private int mMinValue = 0;
        private int mMaxValue = 0;

        public PickerDialog(Context context, int theme, int min, int max, int current) {
            super(context, theme);
            mCurrentSelection = current;
            mMinValue = min;
            mMaxValue = max;
        }

        public int getSelectedValue() {
            return mSelectedValue;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final Context context = getContext();

            setContentView(R.layout.picker_dialog_content);
            ListView numberList = (ListView)findViewById(R.id.picker_list);
            numberList.setAdapter(new BaseAdapter() {

                @Override
                public int getCount() {
                    return (mMaxValue - mMinValue) + 1;
                }

                @Override
                public Object getItem(int arg0) {
                    return null;
                }

                @Override
                public long getItemId(int arg0) {
                    return 0;
                }

                @Override
                public View getView(int pos, View convert, ViewGroup parent) {
                    TextView retView = null;
                    
                    if(convert == null) {
                        retView = new TextView(context);
                        retView.setTextSize(30);
                        retView.setTextColor(Color.WHITE);
                        retView.setShadowLayer(2, 0, 0, Color.BLACK);
                    }
                    else {
                       retView = (TextView)convert;
                    }
                    
                    String str = String.format("%02d", pos + mMinValue);
                    retView.setText(str);
                    return retView;
                }
                
            });
                   
            numberList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedValue = position + mMinValue;
                    dismiss();
                }
            });

            numberList.setSelection(mCurrentSelection);
        }
    } // end PickerDialog

    /**
     * Public interface to implement by clients that wish to be notified
     * when the picker value changes. Register your class (that implements 
     * this interface) by using the addValueChangedListener method.     
     */
    public interface NumberPickerValueChangedListener {
        public void valueChanged(int value);
    }
}
