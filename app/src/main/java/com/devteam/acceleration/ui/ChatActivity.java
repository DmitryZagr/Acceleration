package com.devteam.acceleration.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.devteam.acceleration.R;
import com.devteam.acceleration.ui.dummy.DummyContent;

public class ChatActivity extends AppCompatActivity
        implements AnswersFragment.OnListFragmentInteractionListener,
        MessageFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    public void onAnswersFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onMessageFragmentInteraction(MessageData.MessageModel item) {

    }
}
