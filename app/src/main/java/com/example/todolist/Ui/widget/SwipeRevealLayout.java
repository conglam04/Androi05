package com.example.todolist.Ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeRevealLayout extends FrameLayout {
    private static final int INVALID_POINTER = -1;

    private View contentView;
    private View actionsView;
    private int activePointerId = INVALID_POINTER;
    private float initialX;
    private float lastX;
    private boolean isBeingDragged;
    private int touchSlop;
    private float maxSwipeDistance;
    private boolean isOpen = false;
    private OnSwipeListener swipeListener;

    public interface OnSwipeListener {
        void onOpened();
        void onClosed();
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.swipeListener = listener;
    }

    public SwipeRevealLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SwipeRevealLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeRevealLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() >= 2) {
            actionsView = getChildAt(0);
            contentView = getChildAt(1);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (actionsView != null) {
            maxSwipeDistance = actionsView.getMeasuredWidth();
            if (maxSwipeDistance == 0) {
                actionsView.measure(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                );
                maxSwipeDistance = actionsView.getMeasuredWidth();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                initialX = ev.getX();
                lastX = initialX;
                isBeingDragged = false;

                // Đóng các item khác khi touch vào item này
                SwipeManager.getInstance().closeAllOpenedItems();
                break;

            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex < 0) {
                    break;
                }

                final float x = ev.getX(pointerIndex);
                final float xDiff = x - initialX;

                // Intercept nếu swipe ngang (sang trái hoặc sang phải nếu đã mở)
                if (Math.abs(xDiff) > touchSlop) {
                    // Cho phép swipe sang trái khi đóng, hoặc sang phải khi đang mở
                    if ((xDiff < 0 && !isOpen) || (xDiff > 0 && isOpen)) {
                        isBeingDragged = true;
                        lastX = x;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER;
                isBeingDragged = false;
                break;
        }

        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                initialX = ev.getX();
                lastX = initialX;
                // Cancel any ongoing animation
                if (contentView != null) {
                    contentView.animate().cancel();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                float deltaX = x - lastX;
                lastX = x;

                if (contentView != null) {
                    float newTranslationX = contentView.getTranslationX() + deltaX;
                    // Giới hạn: không cho kéo sang phải (>0) và không quá maxSwipeDistance
                    newTranslationX = Math.max(-maxSwipeDistance, Math.min(0, newTranslationX));
                    contentView.setTranslationX(newTranslationX);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // QUAN TRỌNG: Luôn snap về vị trí mở hoặc đóng
                handleActionUp();
                activePointerId = INVALID_POINTER;
                isBeingDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * Xử lý khi user thả tay - snap về vị trí mở hoặc đóng
     */
    private void handleActionUp() {
        if (contentView == null) {
            return;
        }

        float currentTranslation = contentView.getTranslationX();

        // Nếu kéo quá nửa thì mở, không thì đóng
        if (Math.abs(currentTranslation) > maxSwipeDistance / 2) {
            animateOpen();
        } else {
            animateClose();
        }
    }

    private void animateOpen() {
        if (contentView != null) {
            // Cancel any ongoing animation
            contentView.animate().cancel();

            contentView.animate()
                    .translationX(-maxSwipeDistance)
                    .setDuration(200)
                    .withEndAction(() -> {
                        if (!isOpen) {
                            isOpen = true;
                            // Notify SwipeManager
                            SwipeManager.getInstance().onSwipeOpened(this);
                            // Notify listener
                            if (swipeListener != null) {
                                swipeListener.onOpened();
                            }
                        }
                    })
                    .start();
        }
    }

    public void animateClose() {
        if (contentView != null) {
            // Cancel any ongoing animation
            contentView.animate().cancel();

            contentView.animate()
                    .translationX(0)
                    .setDuration(10)
                    .withEndAction(() -> {
                        if (isOpen) {
                            isOpen = false;
                            // Notify SwipeManager
                            SwipeManager.getInstance().onSwipeClosed(this);
                            // Notify listener
                            if (swipeListener != null) {
                                swipeListener.onClosed();
                            }
                        }
                    })
                    .start();
        }
    }

    /**
     * Đóng ngay lập tức không có animation (dùng khi scroll)
     */
    public void closeImmediately() {
        if (contentView != null) {
            contentView.animate().cancel();
            contentView.setTranslationX(0);
            if (isOpen) {
                isOpen = false;
                SwipeManager.getInstance().onSwipeClosed(this);
                if (swipeListener != null) {
                    swipeListener.onClosed();
                }
            }
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        animateClose();
    }

    /**
     * Reset về trạng thái đóng (dùng khi recycle view holder)
     */
    public void reset() {
        if (contentView != null) {
            contentView.animate().cancel();
            contentView.setTranslationX(0);
            isOpen = false;
        }
    }
}