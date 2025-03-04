/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.settings.brightness;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.Utils;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.R;

/**
 * {@code FrameLayout} used to show and manipulate a {@link ToggleSeekBar}.
 *
 */
public class BrightnessSliderView extends LinearLayout {

    @NonNull
    private ToggleSeekBar mSlider;
    private ImageView mLeftIcon;
    private ImageView mRightIcon;
    private DispatchTouchEventListener mListener;
    private Gefingerpoken mOnInterceptListener;
    @Nullable
    private Drawable mProgressDrawable;
    private float mScale = 1f;

    public BrightnessSliderView(Context context) {
        this(context, null);
    }

    public BrightnessSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Inflated from quick_settings_brightness_dialog
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mSlider = requireViewById(R.id.slider);
        mSlider.setAccessibilityLabel(getContentDescription().toString());
        mLeftIcon = requireViewById(R.id.brightness_icon_left);
        mRightIcon = requireViewById(R.id.brightness_icon_right);

        // Finds the progress drawable. Assumes brightness_progress_drawable.xml
        try {
            LayerDrawable progress = (LayerDrawable) mSlider.getProgressDrawable();
            DrawableWrapper progressSlider = (DrawableWrapper) progress
                    .findDrawableByLayerId(android.R.id.progress);
            LayerDrawable actualProgressSlider = (LayerDrawable) progressSlider.getDrawable();
            mProgressDrawable = actualProgressSlider.findDrawableByLayerId(R.id.slider_foreground);
            updateStartEndIconTint();
        } catch (Exception e) {
            // Nothing to do, mProgressDrawable will be null.
        }
    }

    /**
     * Attaches a listener to relay touch events.
     * @param listener use {@code null} to remove listener
     */
    public void setOnDispatchTouchEventListener(
            DispatchTouchEventListener listener) {
        mListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mListener != null) {
            mListener.onDispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // We prevent disallowing on this view, but bubble it up to our parents.
        // We need interception to handle falsing.
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * Attaches a listener to the {@link ToggleSeekBar} in the view so changes can be observed
     * @param seekListener use {@code null} to remove listener
     */
    public void setOnSeekBarChangeListener(OnBrightnessSliderChangedListener seekListener) {
        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekListener != null)
                    seekListener.onProgressChanged(seekBar, progress, fromUser);
                updateStartEndIconTint();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (seekListener != null)
                    seekListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekListener != null)
                    seekListener.onStopTrackingTouch(seekBar);
            }
        });
    }

    private void updateStartEndIconTint() {
        // Not the best conversion, but at least it works
        // TODO: Make it more efficient
        int percentage = (int) ((Float.parseFloat(String.valueOf(getValue())) / Float.parseFloat(String.valueOf(getMax()))) * 100f);
        mLeftIcon.setImageTintList(
                Utils.getColorAttr(mLeftIcon.getContext(), percentage <= 10 ?
                        android.R.attr.textColorPrimary :
                        android.R.attr.textColorPrimaryInverse)
        );
        mRightIcon.setImageTintList(
                Utils.getColorAttr(mLeftIcon.getContext(), percentage <= 90 ?
                        android.R.attr.textColorPrimary :
                        android.R.attr.textColorPrimaryInverse)
        );
    }

    /**
     * Enforces admin rules for toggling auto-brightness and changing value of brightness
     * @param admin
     * @see ToggleSeekBar#setEnforcedAdmin
     */
    public void setEnforcedAdmin(RestrictedLockUtils.EnforcedAdmin admin) {
        mSlider.setEnabled(admin == null);
        mSlider.setEnforcedAdmin(admin);
    }

    /**
     * Enables or disables the slider
     * @param enable
     */
    public void enableSlider(boolean enable) {
        mSlider.setEnabled(enable);
    }

    /**
     * @return the maximum value of the {@link ToggleSeekBar}.
     */
    public int getMax() {
        return mSlider.getMax();
    }

    /**
     * Sets the maximum value of the {@link ToggleSeekBar}.
     * @param max
     */
    public void setMax(int max) {
        mSlider.setMax(max);
    }

    /**
     * Sets the current value of the {@link ToggleSeekBar}.
     * @param value
     */
    public void setValue(int value) {
        mSlider.setProgress(value);
    }

    /**
     * @return the current value of the {@link ToggleSeekBar}
     */
    public int getValue() {
        return mSlider.getProgress();
    }

    public void setOnInterceptListener(Gefingerpoken onInterceptListener) {
        mOnInterceptListener = onInterceptListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mOnInterceptListener != null) {
            return mOnInterceptListener.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applySliderScale();
    }

    /**
     * Sets the scale for the progress bar (for brightness_progress_drawable.xml)
     *
     * This will only scale the thick progress bar and not the icon inside
     *
     * Used in {@link com.android.systemui.qs.QSAnimator}.
     */
    public void setSliderScaleY(float scale) {
        if (scale != mScale) {
            mScale = scale;
            applySliderScale();
        }
    }

    private void applySliderScale() {
        if (mProgressDrawable != null) {
            final Rect r = mProgressDrawable.getBounds();
            int height = (int) (mProgressDrawable.getIntrinsicHeight() * mScale);
            int inset = (mProgressDrawable.getIntrinsicHeight() - height) / 2;
            mProgressDrawable.setBounds(r.left, inset, r.right, inset + height);
        }
    }

    public float getSliderScaleY() {
        return mScale;
    }

    /**
     * Interface to attach a listener for {@link View#dispatchTouchEvent}.
     */
    @FunctionalInterface
    interface DispatchTouchEventListener {
        boolean onDispatchTouchEvent(MotionEvent ev);
    }

    /**
     * Copy of the {@link SeekBar.OnSeekBarChangeListener}
     * Refactored to add custom methods in one place instead of copying them everywhere
     */
    interface OnBrightnessSliderChangedListener {
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(SeekBar seekBar);
        void onStopTrackingTouch(SeekBar seekBar);
    }
}

