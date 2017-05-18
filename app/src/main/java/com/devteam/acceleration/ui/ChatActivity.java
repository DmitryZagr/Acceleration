package com.devteam.acceleration.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devteam.acceleration.R;
import com.devteam.acceleration.jabber.executors.JabberChat;
import com.devteam.acceleration.jabber.JabberParams;
import com.devteam.acceleration.jabber.executors.JabberDB;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.packet.Message;

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
    private AlertDialog logoutConfirm;
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
        final Button buttonSend = (Button) findViewById(R.id.action_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO send text routine
                String message = requestField.getText().toString();

                if (StringUtils.isNotBlank(message)) {
                    sendMessage(message);
                }
            }
        });
        hideButton = (Button) findViewById(R.id.action_hide);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (softKeyboardShowed) {
                    hideButtonMore();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else if (customKeyboardState == CUSTOM_KEYBOARD_HIDE) {
                    hideButtonLess();
                    customKeyboardState = CUSTOM_KEYBOARD_SHOW;
                } else if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
                    hideButtonMore();
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
                    if (!softKeyboardShowed) {
                        System.out.println("keyboard opened " + heightDiff);
                        hideButtonLess();
                        softKeyboardShowed = true;
                        prevCustomKeyboardState = customKeyboardState;
                        customKeyboardState = CUSTOM_KEYBOARD_HIDE;
                        manageBottomLayout();
                    }
                } else {
                    if (softKeyboardShowed) {
                        softKeyboardShowed = false;
                        System.out.println("keyboard closed " + heightDiff + " = " + defDiff);
                        if (customKeyboardState != prevCustomKeyboardState) {
                            customKeyboardState = prevCustomKeyboardState;
                            System.out.println("Changed all back");
                            manageBottomLayout();
                        } else hideButtonMore();
                    }
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO logout routine (comment intent for test!)
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        prefs.edit().putBoolean(JabberParams.LOGGED_IN, false).apply();
                        JabberChat.getJabberChat().disconnect();

                        final Intent logout = new Intent(ChatActivity.this, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(logout);
                    }
                })
                .setNegativeButton(R.string.no, null);
        logoutConfirm = builder.create();

        initCallbackChat();

//        jabberDbHelper = new JabberDbHelper(this);

    }

    private void hideButtonMore() {
        hideButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_expand_more_black_24dp, 0);
        hideButton.setText(R.string.show_keypad);
    }

    private void hideButtonLess() {
        hideButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_expand_less_black_24dp, 0);
        hideButton.setText(R.string.hide_keypad);
    }

    @Override
    public void onBackPressed() {
        if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
            customKeyboardState = CUSTOM_KEYBOARD_HIDE;
            prevCustomKeyboardState = customKeyboardState;
            manageBottomLayout();
        } else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        JabberChat.getJabberChat().unbindCallback();
//        jabberDbHelper.close();
        super.onDestroy();
    }


    private void manageBottomLayout() {
        if (customKeyboardState == CUSTOM_KEYBOARD_HIDE) {
            System.out.println("HIDE");

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .hide(mAnswers)
                    .commitNow();

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAnswers.getView().getLayoutParams();
            params.weight = 0.0f;
            mAnswers.getView().setLayoutParams(params);

            params = (LinearLayout.LayoutParams) mMessages.getView().getLayoutParams();
            params.weight = 100.0f;
            mMessages.getView().setLayoutParams(params);
        } else if (customKeyboardState == CUSTOM_KEYBOARD_SHOW) {
            System.out.println("SHOW");

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .show(mAnswers)
                    .commit();

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mMessages.getView().getLayoutParams();
            params.weight = 50.0f;
            mMessages.getView().setLayoutParams(params);

            params = (LinearLayout.LayoutParams) mAnswers.getView().getLayoutParams();
            params.weight = 50.0f;
            mAnswers.getView().setLayoutParams(params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public void onAnswersFragmentInteraction(AnswersData.AnswerModel item) {

        //TODO appearing messages for test, remove in production
//        mMessages.addMessageAndUpdateList(item.toString(), MessageData.OUTGOING_MESSAGE, null);
//        mMessages.addMessageAndUpdateList("Answer:", MessageData.INCOMING_MESSAGE, "http://i.imgur.com/DvpvklR.png");
//
//        JabberDB.getInstance().saveMessage(jabberDbHelper.getWritableDatabase(), item, MessageData.OUTGOING_MESSAGE);

        sendMessage(item.toString());
    }

    private void sendMessage(String item) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null && ni.isConnected()) {
            if (JabberChat.connectionState == JabberChat.ConnectionState.AUTHENTICATED) {
                JabberChat.getJabberChat().sendMessage(item, bot);
                mMessages.addMessageAndUpdateList(item, MessageData.OUTGOING_MESSAGE, null);
                requestField.setText("");
            } else {
                JabberChat.getJabberChat().loginToChat();
            }
        } else {
            Toast.makeText(this, "No network", Toast.LENGTH_LONG).show();
        }
    }


    private void initCallbackChat() {
        JabberChat.getJabberChat().bindCallback(new JabberChat.CallbackMessage() {
            @Override
            public void onCallback(Message message, Exception e) {
                if (e instanceof Exception) {
                    Toast.makeText(ChatActivity.this, "Server is not available", Toast.LENGTH_LONG).show();
                    return;
                } else if (message != null) {
//                    JabberDB.getInstance().saveMessage(jabberDbHelper.getWritableDatabase(), message, MessageData.INCOMING_MESSAGE);
                    mMessages.addMessageAndUpdateList(message.getBody(), MessageData.INCOMING_MESSAGE, null);
                    return;
                }

                if (requestField.getText().length() != 0) {
                    JabberChat.getJabberChat().sendMessage(requestField.getText().toString(), bot);
                    mMessages.addMessageAndUpdateList(requestField.getText().toString(), MessageData.OUTGOING_MESSAGE, null);
                    requestField.setText("");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutConfirm.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageFragmentInteraction(MessageData.MessageModel item) {

    }


}
