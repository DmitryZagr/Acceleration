package com.devteam.acceleration.ui;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import com.devteam.acceleration.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyMessageRecyclerViewAdapter.ViewHolder> {

    private final List<MessageData.MessageModel> mValues;
    private final MessageFragment.OnListFragmentInteractionListener mListener;
    private int lastItem = -1;

    private Format todayFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Format theDayBeforeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

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
        holder.mContentView.setText(holder.mMessage.getContent());
        if (DateUtils.isToday(holder.mMessage.getTime().getTime())) {
            holder.timeSign.setText(todayFormat.format(holder.mMessage.getTime()));
        }
        else {
            holder.timeSign.setText(theDayBeforeFormat.format(holder.mMessage.getTime()));
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onMessageFragmentInteraction(holder.mMessage);
                }
            }
        });
        if (holder.mMessage.getURL() != null) {
            holder.progress.setVisibility(View.VISIBLE);
            holder.picture.setVisibility(View.VISIBLE);
            Picasso.with(holder.mView.getContext())
                    .load(holder.mMessage.getURL())
                    .into(holder.picture, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            holder.progress.setVisibility(View.GONE);
                            Picasso.with(holder.mView.getContext())
                                    .load(R.mipmap.error)
                                    .into(holder.picture);
                        }
                    });
        }
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastItem)
        {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastItem = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(final ViewHolder holder) {
        holder.clearAnimation();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public Integer id;
        public final TextView mContentView;
        public MessageData.MessageModel mMessage;
        public ImageView picture;
        public ProgressBar progress;
        public TextView timeSign;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            id = 0;
            mContentView = (TextView) view.findViewById(R.id.content);
            picture = (ImageView) view.findViewById(R.id.picture);
            progress = (ProgressBar) view.findViewById(R.id.picture_progress);
            timeSign = (TextView) view.findViewById(R.id.time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void clearAnimation() {
            mView.clearAnimation();
        }
    }
}
