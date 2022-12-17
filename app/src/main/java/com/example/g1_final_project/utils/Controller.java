package com.example.g1_final_project.utils;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fxn.stash.Stash;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class Controller {
    private static final String TAG = "Controller";

    public static boolean isAnimStarted = false;
    public static YoYo.YoYoString animation1;
    public static YoYo.YoYoString animation2;

    private static View target1;
    private static TextView timeTextView;

    private static void slideOut() {
        animation1 = YoYo.with(Techniques.SlideOutLeft)
                .duration(2000)
                .onEnd(animator -> {
                    if (isAnimStarted)
                        slideIn();
                })
                .playOn(target1);
    }

    private static void slideIn() {
        animation2 = YoYo.with(Techniques.SlideInRight)
                .duration(2000)
                .onEnd(animator -> {
                    if (isAnimStarted)
                        slideOut();
                })
                .playOn(target1);
    }

    public static void stopAnimation() {
        isAnimStarted = false;
        if (Controller.animation1 != null && Controller.animation2 != null) {
            Controller.animation1.stop(true);
            Controller.animation2.stop(true);
        }
    }

    public static void startAnimation(View target) {
        target1 = target;
        slideOut();
        isAnimStarted = true;
    }

    public static int lastHourInt = Stash.getInt(Constants.HOURS, 0);
    public static int lastMinutesInt = Stash.getInt(Constants.MINUTES, 0);

    public static Stopwatch stopwatch = new StopwatchBuilder()
            .startFormat("HH:MM")
            .onTick(time -> {
                if (Stash.getInt(Constants.MINUTES, 0) == 60)
                    Stash.put(Constants.MINUTES, 0);

                Log.d(TAG, "time: "+time);
                String[] values = time.toString().split(":");

                String hoursStr = values[0];
                String minutesStr = values[1];

                Log.d(TAG, "hoursStr: "+hoursStr);
                Log.d(TAG, "minutesStr: "+minutesStr);

                int newMinutesInt = Integer.parseInt(minutesStr);

                int finalMinutesInt = lastMinutesInt;
                if (newMinutesInt != lastMinutesInt){
                    // DIFFERENT MINUTES FROM BEFORE

                    newMinutesInt = newMinutesInt - lastMinutesInt;
                    lastMinutesInt = Integer.parseInt(minutesStr);

                    finalMinutesInt = newMinutesInt + Stash.getInt(Constants.MINUTES, 0);

                    Stash.put(Constants.MINUTES, finalMinutesInt);

                }
                int newHourInt = Integer.parseInt(hoursStr);

                int finalHourInt = lastHourInt;
                if (newHourInt != lastHourInt){
                    // DIFFERENT MINUTES FROM BEFORE

                    newHourInt = newHourInt - lastHourInt;
                    lastHourInt = Integer.parseInt(hoursStr);

                    finalHourInt = newHourInt + Stash.getInt(Constants.HOURS, 0);

                    Stash.put(Constants.HOURS, finalHourInt);

                }

                Log.d(TAG, "newHourInt: "+newHourInt);
                Log.d(TAG, "newMinutesInt: "+newMinutesInt);

                Stash.put(Constants.CURRENT_TIME, finalHourInt + " hrs " + finalMinutesInt + " mins");

                Log.d(TAG, "finalHourInt: "+finalHourInt);
                Log.d(TAG, "finalMinutesInt: "+finalMinutesInt);

                timeTextView.setText(finalHourInt + " hrs " + finalMinutesInt + " mins");
            })
            .build();

    public static void startStopWatch(TextView textView) {
        timeTextView = textView;
        Controller.stopwatch.start();
    }

    public static void stopStopWatch() {
        Controller.stopwatch.stop();
        Controller.stopwatch.reset();
    }

}
