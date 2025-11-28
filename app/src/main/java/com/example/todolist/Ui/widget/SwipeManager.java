package com.example.todolist.Ui.widget;

import java.lang.ref.WeakReference;

public class SwipeManager {
    private static SwipeManager instance;
    private WeakReference<SwipeRevealLayout> openedSwipeLayout;

    private SwipeManager() {}

    public static SwipeManager getInstance() {
        if (instance == null) {
            instance = new SwipeManager();
        }
        return instance;
    }

    public void onSwipeOpened(SwipeRevealLayout layout) {
        // Đóng layout cũ nếu có
        if (openedSwipeLayout != null) {
            SwipeRevealLayout oldLayout = openedSwipeLayout.get();
            if (oldLayout != null && oldLayout != layout) {
                oldLayout.animateClose();
            }
        }
        // Lưu layout mới
        openedSwipeLayout = new WeakReference<>(layout);
    }

    public void onSwipeClosed(SwipeRevealLayout layout) {
        if (openedSwipeLayout != null) {
            SwipeRevealLayout currentLayout = openedSwipeLayout.get();
            if (currentLayout == layout) {
                openedSwipeLayout = null;
            }
        }
    }

    public void closeAllOpenedItems() {
        if (openedSwipeLayout != null) {
            SwipeRevealLayout layout = openedSwipeLayout.get();
            if (layout != null) {
                layout.animateClose();
            }
            openedSwipeLayout = null;
        }
    }

    public boolean hasOpenedItem() {
        return openedSwipeLayout != null && openedSwipeLayout.get() != null;
    }
}