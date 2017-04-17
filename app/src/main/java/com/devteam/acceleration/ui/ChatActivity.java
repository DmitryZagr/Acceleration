package com.devteam.acceleration.ui;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

    private MessageFragment mMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mMessages = (MessageFragment) getSupportFragmentManager().findFragmentById(R.id.messages_fragment);
    }

    @Override
    public void onAnswersFragmentInteraction(AnswersData.AnswerModel item) {
        mMessages.addMessage(item.toString(), MessageData.OUTGOING_MESSAGE);
    }

    @Override
    public void onMessageFragmentInteraction(MessageData.MessageModel item) {

    }
}
