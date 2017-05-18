package com.devteam.acceleration.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.devteam.acceleration.R;
import com.devteam.acceleration.jabber.db.JabberDbHelper;
import com.devteam.acceleration.jabber.executors.JabberChat;
import com.devteam.acceleration.jabber.executors.JabberDB;

import org.jivesoftware.smack.packet.Message;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MessageFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private MyMessageRecyclerViewAdapter MessagesAdapter;

    private RecyclerView recyclerView;

    private JabberDbHelper jabberDbHelper;

    private static boolean isFirstStart = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MessageFragment newInstance(int columnCount) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(jabberDbHelper == null) {
            isFirstStart = true;
        }

        jabberDbHelper = new JabberDbHelper(this.getContext());
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        initCallbackDB();

        if(isFirstStart) {
            JabberDB.getInstance().getHistory(jabberDbHelper.getReadableDatabase());
            isFirstStart = false;
        }

    }

    @Override
    public void onDestroy() {
        jabberDbHelper.close();
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MessagesAdapter = new MyMessageRecyclerViewAdapter(MessageData.items, mListener);
            recyclerView.setAdapter(MessagesAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addMessageAndUpdateList(String content, int type, String URL) {
        MessageData.MessageModel message = new MessageData.MessageModel(
                String.valueOf(MessageData.count),
                content,
                type,
                URL);
        JabberDB.getInstance().saveMessage(jabberDbHelper.getWritableDatabase(), message);
        MessageData.addItem(message);
        if(MessageData.count.intValue() > MessageData.MAX_COUNT) {
            JabberDB.getInstance().removeOldMessages(jabberDbHelper.getWritableDatabase(), MessageData.count.intValue() - MessageData.MAX_COUNT);
        }
        MessagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(MessageData.count.get() - 1);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMessageFragmentInteraction(MessageData.MessageModel item);
    }

    private void initCallbackDB() {
        JabberDB.getInstance().bindCallback(new JabberDB.CallbackDB() {
            @Override
            public void onCallbackDb(Exception error) {
                MessagesAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(MessageData.count.get() - 1);
            }
        });
    }

}
