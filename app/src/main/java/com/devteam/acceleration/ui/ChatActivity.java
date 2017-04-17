package com.devteam.acceleration.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devteam.acceleration.R;

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

    private static final int CUSTOM_KEYBOARD_HIDDEN = 0;
    private static final int CUSTOM_KEYBOARD_SHOWED = 1;

    private static int customKeyboardState = CUSTOM_KEYBOARD_SHOWED;
    private MessageFragment mMessages;
    private AnswersFragment mAnswers;
    private EditText requestField;
    private Button hideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mMessages = (MessageFragment) getSupportFragmentManager().findFragmentById(R.id.messages_fragment);
        mAnswers = (AnswersFragment) getSupportFragmentManager().findFragmentById(R.id.answers_fragment);
        requestField = (EditText) findViewById(R.id.request_field);
        requestField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.println("Editing? - " + hasFocus);
            }
        });
        manageBottomLayout();
        hideButton = (Button) findViewById(R.id.action_hide);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customKeyboardState == CUSTOM_KEYBOARD_SHOWED) {
                    customKeyboardState = CUSTOM_KEYBOARD_HIDDEN;
                }
                else if (customKeyboardState == CUSTOM_KEYBOARD_HIDDEN) {
                    customKeyboardState = CUSTOM_KEYBOARD_SHOWED;
                }
                manageBottomLayout();
            }
        });
    }

    private void manageBottomLayout() {
        if (customKeyboardState == CUSTOM_KEYBOARD_SHOWED) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mMessages.getView().getLayoutParams();
            params.weight = 6.0f;
            mMessages.getView().setLayoutParams(params);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .hide(mAnswers)
                    .commit();
        }
        else if (customKeyboardState == CUSTOM_KEYBOARD_HIDDEN) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mMessages.getView().getLayoutParams();
            params.weight = 3.0f;
            mMessages.getView().setLayoutParams(params);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .show(mAnswers)
                    .commit();
        }
    }

    @Override
    public void onAnswersFragmentInteraction(AnswersData.AnswerModel item) {
        mMessages.addMessageAndUpdateList(item.toString(), MessageData.OUTGOING_MESSAGE);
    }

    @Override
    public void onMessageFragmentInteraction(MessageData.MessageModel item) {

    }
}
