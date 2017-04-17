package com.devteam.acceleration.jabber;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.devteam.acceleration.ui.LoginActivity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import static com.devteam.acceleration.jabber.AccelerationJabberConnection.ConnectionState;
import static com.devteam.acceleration.jabber.AccelerationJabberConnection.LoggedInState;

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

    public static final String UI_AUTHENTICATED = TAG + ".uiauthenticated";
    public static final String SEND_MESSAGE = TAG + ".sendmessage";
    public static final String MESSAGE_BODY = TAG + ".message_body";
    public static final String BUNDLE_TO = TAG + ".b_to";

    public static final String NEW_MESSAGE = TAG + ".newmessage";
    public static final String BUNDLE_FROM_JID = TAG + ".b_from";
    public static final String CONNECTION_EVENT = TAG + ".coonection.event";

    public static ConnectionState connectionState = ConnectionState.DISCONNECTED;
    public static LoggedInState loggedInState;


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
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        stop();
    }

    public synchronized void start() {
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

    public synchronized void stop() {
        Log.d(TAG, "stop()");
        active = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
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
            Intent intent = new Intent(CONNECTION_EVENT);
//            intent.setPackage(LoginActivity.class.getPackage().getName());
            getApplicationContext().sendBroadcast(intent);
            stopSelf();
        }
    }

}
