package com.example.todolist.Ui.viewmodel;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FocusTimerViewModel extends ViewModel {

    private CountDownTimer timer;
    private final MutableLiveData<Long> remaining = new MutableLiveData<>(0L);
    private boolean isRunning = false;

    public LiveData<Long> getRemaining() { return remaining; }
    public boolean isRunning() { return isRunning; }

    public void start(long totalMillis) {
        if (isRunning) return;

        isRunning = true;

        timer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long ms) {
                remaining.setValue(ms);
            }

            @Override
            public void onFinish() {
                isRunning = false;
                remaining.setValue(0L);
            }
        }.start();
    }

    public void stop() {
        if (timer != null) timer.cancel();
        isRunning = false;
    }

    @Override
    protected void onCleared() {
        if (timer != null) timer.cancel();
    }
}
