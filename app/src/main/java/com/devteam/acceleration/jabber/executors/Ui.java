package com.devteam.acceleration.jabber.executors;

/**
 * Created by admin on 08.05.17.
 */

import android.os.Handler;
import android.os.Looper;

public class Ui {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void run(Runnable runnable) {
        HANDLER.post(runnable);
    }
}
