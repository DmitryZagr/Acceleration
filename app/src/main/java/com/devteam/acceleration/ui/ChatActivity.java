package com.devteam.acceleration.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devteam.acceleration.R;
import com.devteam.acceleration.jabber.AccelerationConnectionService;

public class ChatActivity extends AppCompatActivity
        implements AnswersFragment.OnListFragmentInteractionListener,
        MessageFragment.OnListFragmentInteractionListener {

    static {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .penaltyLog()
                .penaltyDeath()
                .build()
        );
    }

    private static final int CUSTOM_KEYBOARD_SHOW = 0;
    private static final int CUSTOM_KEYBOARD_HIDE = 1;

    private static int customKeyboardState = CUSTOM_KEYBOARD_HIDE;
    private static int prevCustomKeyboardState = customKeyboardState;
    private boolean softKeyboardShowed = false;
    private MessageFragment mMessages;
    private AnswersFragment mAnswers;
    private EditText requestField;
    private Button hideButton;
    private BroadcastReceiver chatBroadcastReceiver;
    //TODO : c этим надо чет сделать
    //
    public static final String bot = "user@192.168.43.98";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mMessages = (MessageFragment) getSupportFragmentManager().findFragmentById(R.id.messages_fragment);
        mAnswers = (AnswersFragment) getSupportFragmentManager().findFragmentById(R.id.answers_fragment);
        requestField = (EditText) findViewById(R.id.request_field);
        manageBottomLayout();
        hideButton = (Button) findViewById(R.id.action_hide);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customKeyboardState == CUSTOM_KEYBOARD_HIDE) {
                    customKeyboardState = CUSTOM_KEYBOARD_SHOW;
                } else if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
                    customKeyboardState = CUSTOM_KEYBOARD_HIDE;
                }
                prevCustomKeyboardState = customKeyboardState;
                manageBottomLayout();
            }
        });

        final LinearLayout rootView = (LinearLayout) findViewById(R.id.activity_chat);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int defDiff = 0;

            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (defDiff == 0) defDiff = heightDiff;
                if (heightDiff > defDiff) {
                    if (softKeyboardShowed == false) {
                        System.out.println("keyboard opened " + heightDiff);
                        softKeyboardShowed = true;
                        prevCustomKeyboardState = customKeyboardState;
                        customKeyboardState = CUSTOM_KEYBOARD_HIDE;
                        manageBottomLayout();
                    }
                }
                else {
                    if (softKeyboardShowed == true) {
                        softKeyboardShowed = false;
                        System.out.println("keyboard closed " + heightDiff + " = " + defDiff);
                        if (customKeyboardState != prevCustomKeyboardState) {
                            customKeyboardState = prevCustomKeyboardState;
                            System.out.println("Changed all back");
                            manageBottomLayout();
                        }
                    }
                }
            }
        });

        initBroadcastReceiver();
    }

    @Override
    public void onBackPressed() {
        if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
            customKeyboardState = CUSTOM_KEYBOARD_HIDE;
            prevCustomKeyboardState = customKeyboardState;
            manageBottomLayout();
        }
    }

    @Override
    protected void onDestroy() {
        if (chatBroadcastReceiver != null) {
            unregisterReceiver(chatBroadcastReceiver);
        }
        super.onDestroy();
    }


    private void manageBottomLayout() {
        if (customKeyboardState == CUSTOM_KEYBOARD_HIDE) {
            System.out.println("HIDE");
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAnswers.getView().getLayoutParams();
//            params.weight = 5.0f;
//            mAnswers.getView().setLayoutParams(params);
//            params = (LinearLayout.LayoutParams) mMessages.getView().getLayoutParams();
//            params.weight = 1.0f;
//            mMessages.getView().setLayoutParams(params);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .hide(mAnswers)
                    .commit();
            params.weight = 100.0f;
            mMessages.getView().setLayoutParams(params);
        } else if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
            System.out.println("SHOW");
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mMessages.getView().getLayoutParams();
            params.weight = 50.0f;
            mMessages.getView().setLayoutParams(params);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .show(mAnswers)
                    .commit();
//            params = (LinearLayout.LayoutParams) mAnswers.getView().getLayoutParams();
//            params.weight = 3.0f;
//            mAnswers.getView().setLayoutParams(params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public void onAnswersFragmentInteraction(AnswersData.AnswerModel item) {


        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni !=  null && ni.isConnected()) {
            Intent intent = new Intent(AccelerationConnectionService.SEND_MESSAGE);
            intent.putExtra(AccelerationConnectionService.MESSAGE_BODY, item.toString());
            intent.putExtra(AccelerationConnectionService.BUNDLE_TO, bot);
            sendBroadcast(intent);

            mMessages.addMessageAndUpdateList(item.toString(), MessageData.OUTGOING_MESSAGE);
        } else {
            Toast.makeText(this, "No network", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onMessageFragmentInteraction(MessageData.MessageModel item) {

    }

    private void initBroadcastReceiver() {

        chatBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(AccelerationConnectionService.NEW_MESSAGE)) {
                    String message = intent.getStringExtra(AccelerationConnectionService.MESSAGE_BODY);
                    if (message == null)
                        message = "";
                    String from = intent.getStringExtra(AccelerationConnectionService.BUNDLE_FROM_JID);
                    mMessages.addMessageAndUpdateList(from + ":\n" + message, MessageData.INCOMING_MESSAGE);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(AccelerationConnectionService.NEW_MESSAGE);
        registerReceiver(chatBroadcastReceiver, intentFilter);
    }

}
