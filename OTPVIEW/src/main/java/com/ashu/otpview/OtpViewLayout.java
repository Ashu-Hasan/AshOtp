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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boxWidth, boxHeight);
            if (i != 0) params.setMargins(boxSpacing, boxSpacing, boxSpacing, boxSpacing);

            editText.setSingleLine(true);
            editText.setIncludeFontPadding(false);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            editText.setLayoutParams(params);
            editText.setGravity(Gravity.CENTER);
            editText.setTextColor(textColor);
            editText.setBackgroundResource(boxBackground);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            editText.setEms(1);

            // ⭐ Apply shadow/elevation
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
                        getChildAt(index + 1).requestFocus();
                    }
                    if (getOtp().length() == otpLength && listener != null) {
                        listener.onOtpComplete(getOtp());
                    }
                }
            });

            editText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && editText.getText().toString().isEmpty() && index > 0) {
                    getChildAt(index - 1).requestFocus();
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
}
