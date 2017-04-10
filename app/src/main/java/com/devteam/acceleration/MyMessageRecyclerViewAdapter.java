package com.devteam.acceleration;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devteam.acceleration.MessageFragment.OnListFragmentInteractionListener;
import com.devteam.acceleration.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyMessageRecyclerViewAdapter.ViewHolder> {

    private static final String LONG_MESSAGE =
            "Lorem Ipsum - это текст-\"рыба\", " +
            "часто используемый в печати и вэб-дизайне. Lorem Ipsum является " +
            "стандартной \"рыбой\" для текстов на латинице с начала XVI века.";
    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyMessageRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
        holder.mId = Integer.parseInt(mValues.get(position).id);
//        System.out.println("Bind " + holder.mId);
        if (holder.mId % 2 == 0)
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.mContentView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.setMargins(holder.margin, holder.distance, 0, holder.distance);
            holder.mContentView.setLayoutParams(params);
        }
        else
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.mContentView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.setMargins(0, holder.distance, holder.margin, holder.distance);
            holder.mContentView.setLayoutParams(params);
        }
        holder.mContentView.setText(mValues.get(position).content + LONG_MESSAGE);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
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
//        public final View mContainer;
        public Integer mId;
        public final Integer margin;
        public final Integer distance;
//        public final TextView mIdView;
        public final TextView mContentView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mId = 0;
//            mIdView = (TextView) view.findViewById(R.id.id);
//            mContainer = (View) view.findViewById(R.id.container);
            mContentView = (TextView) view.findViewById(R.id.content);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mContentView.getLayoutParams();
            margin = params.leftMargin;
            distance = params.topMargin;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
