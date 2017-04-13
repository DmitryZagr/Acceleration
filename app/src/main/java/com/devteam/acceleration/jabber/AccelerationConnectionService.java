package com.devteam.acceleration.jabber;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by admin on 13.04.17.
 */

public class AccelerationConnectionService extends Service {
    private static final String TAG = AccelerationConnectionService.class.getSimpleName();

    private boolean active;//Stores whether or not the thread is active
    private Thread thread;
    private Handler handler;//We use this handler to post messages to
    //the background thread.
    private AccelerationJabberConnection connection;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        start();
        return Service.START_STICKY;
        //RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
//        stop();
    }

    public void start() {
        Log.d(TAG, " Service Start() function called.");
        if (!active) {
            active = true;
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        handler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();
                    }
                });
                thread.start();
            }
        }
    }


    private void initConnection() {
        Log.d(TAG, "initConnection()");
        if (connection == null) {
            connection = new AccelerationJabberConnection(this);
        }
        try {
            connection.connect();

        } catch (IOException | SmackException | XMPPException | InterruptedException e) {
            Log.d(TAG, "Something went wrong while connecting ,make sure the credentials are right and try again");
            e.printStackTrace();
            //Stop the service all together.
            stopSelf();
        }

    }

}
