package com.ashu.otpview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;

public class OtpViewLayout extends LinearLayout {

    private int otpLength;
    private int boxSize;
    private int boxSpacing;
    private int textColor;
    private int boxBackground;
    private float textSize;
    private OtpCompleteListener listener;
    private int boxWidth;
    private int boxHeight;

    private float boxElevation;


    public OtpViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OtpView);

        otpLength = a.getInt(R.styleable.OtpView_otpLength, 4);

// If boxWidth or boxHeight not set, fall back to boxSize (for backward compatibility)
        int defaultSize = a.getDimensionPixelSize(R.styleable.OtpView_boxSize, 120);

        boxWidth = a.getDimensionPixelSize(R.styleable.OtpView_boxWidth, defaultSize);
        boxHeight = a.getDimensionPixelSize(R.styleable.OtpView_boxHeight, defaultSize);

        boxElevation = a.getDimension(R.styleable.OtpView_boxElevation, 0f); // default no shadow
        boxSpacing = a.getDimensionPixelSize(R.styleable.OtpView_boxSpacing, 20);
        textColor = a.getColor(R.styleable.OtpView_textColor, Color.BLACK);
        textSize = a.getDimension(R.styleable.OtpView_textSize, 18);
        boxBackground = a.getResourceId(R.styleable.OtpView_boxBackground, android.R.drawable.editbox_background);
        boxSize = a.getDimensionPixelSize(R.styleable.OtpView_boxSize, 120);
        boxSpacing = a.getDimensionPixelSize(R.styleable.OtpView_boxSpacing, 20);
        textColor = a.getColor(R.styleable.OtpView_textColor, Color.BLACK);
        textSize = a.getDimension(R.styleable.OtpView_textSize, 18);
        boxBackground = a.getResourceId(R.styleable.OtpView_boxBackground, android.R.drawable.editbox_background);

        a.recycle();

        initBoxes(context);
    }

    private void initBoxes(Context context) {
        for (int i = 0; i < otpLength; i++) {
            final EditText editText = new EditText(context);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(boxWidth, boxHeight);
            params.gravity = Gravity.CENTER_VERTICAL;

            if (i != 0) params.setMargins(boxSpacing, boxSpacing, boxSpacing, boxSpacing);

            editText.setSingleLine(true);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            editText.setGravity(Gravity.CENTER);
            editText.setTextColor(textColor);
            editText.setBackgroundResource(boxBackground);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            editText.setEms(1);

// ✅ Font-safe settings
            editText.setIncludeFontPadding(true);
            editText.setPadding(0, 0, 0, 0);
            editText.setLineSpacing(0f, 1.1f);

// ⭐ Elevation
            ViewCompat.setElevation(editText, boxElevation);


            final int index = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < otpLength - 1) {
                        EditText et = (EditText) getChildAt(index + 1);

                        et.setEnabled(true);
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                        et.setClickable(true);
                        et.setCursorVisible(true);

                        getChildAt(index + 1).requestFocus();
                        updateBoxStates(index + 1);

                    }
                    if (getOtp().length() == otpLength && listener != null) {
                        listener.onOtpComplete(getOtp());
                    }
                }
            });

            editText.setOnKeyListener((v, keyCode, event) -> {

                if (keyCode == android.view.KeyEvent.KEYCODE_DEL
                        && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {

                    // Case 1: Current box has value → just clear it
                    if (!editText.getText().toString().isEmpty()) {
                        editText.setText("");
                        return true;
                    }

                    // Case 2: Current box empty → go back & clear previous
                    if (index > 0) {
                        EditText et = (EditText) getChildAt(index - 1);

                        et.setText("");
                        et.setEnabled(true);
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                        et.setClickable(true);
                        et.setCursorVisible(true);

                        getChildAt(index - 1).requestFocus();
                        updateBoxStates(index - 1);      // move focus properly
                        return true;
                    }
                }
                return false;
            });


            addView(editText);
        }
    }


    public String getOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            EditText editText = (EditText) getChildAt(i);
            sb.append(editText.getText().toString());
        }
        return sb.toString();
    }

    public void setOtpCompleteListener(OtpCompleteListener listener) {
        this.listener = listener;
    }

    public interface OtpCompleteListener {
        void onOtpComplete(String otp);
    }

    public void setOtpReadOnly(boolean readOnly) {
        for (int i = 0; i < otpLength; i++) {
            EditText editText = (EditText) getChildAt(i);

            editText.setFocusable(!readOnly);
            editText.setClickable(!readOnly);
            editText.setCursorVisible(!readOnly);
            editText.setInputType(readOnly ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
        }
    }

    public void setOtp(String otp) {
        if (otp == null) return;

        for (int i = 0; i < otpLength; i++) {
            EditText editText = (EditText) getChildAt(i);

            if (i < otp.length()) {
                editText.setText(String.valueOf(otp.charAt(i)));
            } else {
                editText.setText("");
            }
        }
    }

    private void updateBoxStates(int activeIndex) {
        for (int i = 0; i < otpLength; i++) {
            if (i != activeIndex) {
                EditText et = (EditText) getChildAt(i);

                boolean isActive = (i == activeIndex);

                et.setEnabled(isActive);
                et.setFocusable(isActive);
                et.setFocusableInTouchMode(isActive);
                et.setClickable(isActive);
                et.setCursorVisible(isActive);

                if (isActive) {
                    et.setSelection(et.getText().length());
                }
            }
        }

        // 🔒 Request focus ONLY ONCE, safely
        EditText active = (EditText) getChildAt(activeIndex);
        active.post(active::requestFocus);
    }

}
