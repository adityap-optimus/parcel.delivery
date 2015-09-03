package com.aditya.hackerearth.porterapp.helper.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.aditya.hackerearth.porterapp.R;

public class OverlayMenu extends ViewGroup {

    private static final int ANIMATION_DURATION = 300;

    private int mAddButtonColorNormal;
    private int mAddButtonColorPressed;
    private boolean mAddButtonStrokeVisible;

    private int mButtonSpacing;
    private int mLabelsMargin;
    private int mLabelsVerticalOffset;
    private int mIcon;

    private boolean mExpanded;

    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private OverlayButton mIconButton;
    private int mMaxButtonWidth;
    private int mButtonsCount;

    private TouchDelegateGroup mTouchDelegateGroup;

    private OnExpandibleOverlayMenuUpdateListener mListener;

    public interface OnExpandibleOverlayMenuUpdateListener {
        void onMenuExpanded();

        void onMenuCollapsed();
    }

    public OverlayMenu(Context context) {
        this(context, null);
    }

    public OverlayMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OverlayMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mButtonSpacing = (int) (getResources().getDimension(R.dimen.ob_actions_spacing) - getResources().getDimension(R.dimen.ob_shadow_radius) - getResources().getDimension(R.dimen.ob_shadow_offset));
        mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.ob_labels_margin);
        mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.ob_shadow_offset);

        mTouchDelegateGroup = new TouchDelegateGroup(this);
        setTouchDelegate(mTouchDelegateGroup);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandibleOverlayMenu, 0, 0);
        mAddButtonColorNormal = attr.getColor(R.styleable.ExpandibleOverlayMenu_ob_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
        mAddButtonColorPressed = attr.getColor(R.styleable.ExpandibleOverlayMenu_ob_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
        mAddButtonStrokeVisible = true;
        mIcon = attr.getResourceId(R.styleable.ExpandibleOverlayMenu_ob_iconRef, R.drawable.ic_share);
        attr.recycle();
        createAddButton(context);
    }

    public void setOnExpandibleOverlayMenuUpdateListener(OnExpandibleOverlayMenuUpdateListener listener) {
        mListener = listener;
    }

    private void createAddButton(final Context context) {
        mIconButton = new OverlayButton(context) {
            @Override
            void updateBackground() {
                mColorNormal = mAddButtonColorNormal;
                mColorPressed = mAddButtonColorPressed;
                mStrokeVisible = mAddButtonStrokeVisible;
                super.updateBackground();
            }

            @Override
            Drawable getIconDrawable() {
                return context.getResources().getDrawable(mIcon);
            }
        };

        mIconButton.setId(R.id.ob_expand_menu_button);
        mIconButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mIconButton, super.generateDefaultLayoutParams());
        mButtonsCount++;
    }

    public void addButton(OverlayButton button) {
        addView(button, mButtonsCount - 1);
        mButtonsCount++;
    }

    public void removeButton(OverlayButton button) {
        removeView(button.getLabelView());
        removeView(button);
        button.setTag(R.id.ob_label, null);
        mButtonsCount--;
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;
        mMaxButtonWidth = 0;
        int maxLabelWidth = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }
            mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
            height += child.getMeasuredHeight();
            TextView label = (TextView) child.getTag(R.id.ob_label);
            if (label != null) {
                maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
            }

        }

        width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0);


        height += mButtonSpacing * (mButtonsCount - 1);
        height = adjustForOvershoot(height);

        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean expandUp = true;
        if (changed) {
            mTouchDelegateGroup.clearTouchDelegates();
        }
        int addButtonY = expandUp ? b - t - mIconButton.getMeasuredHeight() : 0;
        int buttonsHorizontalCenter = r - l - mMaxButtonWidth / 2;
        int addButtonLeft = buttonsHorizontalCenter - mIconButton.getMeasuredWidth() / 2;
        mIconButton.layout(addButtonLeft, addButtonY, addButtonLeft + mIconButton.getMeasuredWidth(), addButtonY + mIconButton.getMeasuredHeight());

        int labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin;
        int labelsXNearButton = buttonsHorizontalCenter - labelsOffset;

        int nextY = expandUp ?
                addButtonY - mButtonSpacing :
                addButtonY + mIconButton.getMeasuredHeight() + mButtonSpacing;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child == mIconButton || child.getVisibility() == GONE) continue;

            int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
            int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
            child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

            float collapsedTranslation = addButtonY - childY;
            float expandedTranslation = 0f;

            child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(mExpanded ? 1f : 0f);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
            params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
            params.setAnimationsTarget(child);

            View label = (View) child.getTag(R.id.ob_label);
            if (label != null) {
                int labelXAwayFromButton = labelsXNearButton - label.getMeasuredWidth();

                int labelLeft = labelXAwayFromButton;

                int labelRight = labelsXNearButton;

                int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

                Rect touchArea = new Rect(
                        Math.min(childX, labelLeft),
                        childY - mButtonSpacing / 2,
                        Math.max(childX + child.getMeasuredWidth(), labelRight),
                        childY + child.getMeasuredHeight() + mButtonSpacing / 2);
                mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

                label.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
                label.setAlpha(mExpanded ? 1f : 0f);

                LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
                labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                labelParams.setAnimationsTarget(label);
            }

            nextY = expandUp ?
                    childY - mButtonSpacing :
                    childY + child.getMeasuredHeight() + mButtonSpacing;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDir = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
        private boolean animationsSetToPlay;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            mExpandDir.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseDir.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);
            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);
            mCollapseDir.setProperty(View.TRANSLATION_Y);
            mExpandDir.setProperty(View.TRANSLATION_Y);
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDir.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDir.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                mCollapseAnimation.play(mCollapseAlpha);
                mCollapseAnimation.play(mCollapseDir);
                mExpandAnimation.play(mExpandAlpha);
                mExpandAnimation.play(mExpandDir);
                animationsSetToPlay = true;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mIconButton);
        mButtonsCount = getChildCount();
    }


    public void collapse() {
        collapse(false);
    }

    public void collapseImmediately() {
        collapse(true);
    }

    private void collapse(boolean immediately) {
        if (mExpanded) {
            mExpanded = false;
            mTouchDelegateGroup.setEnabled(false);
            mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mTouchDelegateGroup.setEnabled(true);
            mCollapseAnimation.cancel();
            mExpandAnimation.start();

            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mIconButton.setEnabled(enabled);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;
            mTouchDelegateGroup.setEnabled(mExpanded);
            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
