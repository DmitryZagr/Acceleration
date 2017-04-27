package com.devteam.acceleration.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devteam.acceleration.R;

import java.util.List;

public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyMessageRecyclerViewAdapter.ViewHolder> {

    private final List<MessageData.MessageModel> mValues;
    private final MessageFragment.OnListFragmentInteractionListener mListener;

    public MyMessageRecyclerViewAdapter(List<MessageData.MessageModel> items,
                                        MessageFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MessageData.INCOMING_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_incoming_message, parent, false);
        } else if (viewType == MessageData.OUTGOING_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_outgoing_message, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position).getType();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mMessage = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getContent());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onMessageFragmentInteraction(holder.mMessage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public Integer id;
//        public final Integer margin;
//        public final Integer distance;
        public final TextView mContentView;
        public MessageData.MessageModel mMessage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            id = 0;
            mContentView = (TextView) view.findViewById(R.id.content);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
//            margin = params.leftMargin;
//            distance = params.topMargin;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
