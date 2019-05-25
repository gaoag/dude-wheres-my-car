package com.example.parkour;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;


public class TimerPickupView extends LinearLayout {

    static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String FORMAT = "%02d:%02d:%02d";

    public TextView getTimeLeft() {
        return timeLeft;
    }

    public TextView getWalkingTime() {
        return walkingTime;
    }

    public TextView getPickupButton() {
        return pickupButton;
    }

    private TextView timeLeft;
    private TextView walkingTime;
    private Button pickupButton;

    public TimerPickupView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.timers_pickup_component_view, this);

        timeLeft = findViewById(R.id.timer);
        walkingTime = findViewById(R.id.walktime);
        pickupButton = findViewById(R.id.pickup);

    }

    public void startTimer(int timeLimit) {

        CountDownTimer mCountDownTimer = new CountDownTimer(3600000*timeLimit, 1000) {
            @Override
            public void onFinish() {
                timeLeft.setText(mSimpleDateFormat.format(0));
            }

            public void onTick(long millisUntilFinished) {
                timeLeft.setText("Remaining time: " + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                invalidate();
            }
        };

        mCountDownTimer.start();
    }

}
