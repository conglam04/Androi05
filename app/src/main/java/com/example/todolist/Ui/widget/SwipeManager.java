package com.example.todolist.Ui.widget;

import java.lang.ref.WeakReference;

public class SwipeManager {
    private static SwipeManager instance;
    private WeakReference<com.example.todolist.Ui.widget.SwipeRevealLayout> openedSwipeLayout;

    private SwipeManager() {}

    public static SwipeManager getInstance() {
        if (instance == null) {
            instance = new SwipeManager();
        }
        return instance;
    }

    public void onSwipeOpened(com.example.todolist.Ui.widget.SwipeRevealLayout layout) {
        // Đóng layout cũ nếu có
        if (openedSwipeLayout != null) {
            com.example.todolist.Ui.widget.SwipeRevealLayout oldLayout = openedSwipeLayout.get();
            if (oldLayout != null && oldLayout != layout) {
                oldLayout.animateClose();
            }
        }
        // Lưu layout mới
        openedSwipeLayout = new WeakReference<>(layout);
    }

    public void onSwipeClosed(com.example.todolist.Ui.widget.SwipeRevealLayout layout) {
        if (openedSwipeLayout != null) {
            com.example.todolist.Ui.widget.SwipeRevealLayout currentLayout = openedSwipeLayout.get();
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