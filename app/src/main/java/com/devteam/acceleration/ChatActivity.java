package com.devteam.acceleration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.devteam.acceleration.dummy.DummyContent;

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
