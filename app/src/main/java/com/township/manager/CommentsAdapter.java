package com.township.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsAdapter extends RecyclerView.Adapter {

    ArrayList<ViewHolder> viewHolders = new ArrayList<>();
    ArrayList<Notice.Comment> dataset;
    Context context;

    public CommentsAdapter(ArrayList<Notice.Comment> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_notice_board_comments, parent, false);
        final CommentsAdapter.ViewHolder viewHolder = new CommentsAdapter.ViewHolder(view);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        Notice.Comment comment = dataset.get(position);
        viewHolder.name.setText(comment.getPosted_by_first_name() + " " + comment.getPosted_by_last_name());

        if (comment.getPosted_by_apartment() == null) {
            viewHolder.apartment.setText("(" + comment.getPosted_by_designation() + ")");
        } else {
            viewHolder.apartment.setText("(" + comment.getPosted_by_wing() + "-" + comment.getPosted_by_apartment() + ")");
        }
        viewHolder.content.setText(comment.getContent());

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void showSending(ViewHolder viewHolder) {
        Log.d("ui check sending", viewHolder.content.getText().toString());
        ImageView imageView = viewHolders.get(viewHolders.size() -1).statusIcon;
        imageView.setImageResource(R.drawable.ic_access_time_black_24dp);
        imageView.setVisibility(View.VISIBLE);
    }

    public void showSent(ViewHolder viewHolder) {
        Log.d("ui check sent", viewHolder.content.getText().toString());
        final ImageView imageView = viewHolders.get(viewHolders.size() -1).statusIcon;
        imageView.setImageResource(R.drawable.checked);
        imageView.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        }, 1500);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, apartment, content;
        ImageView image, statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.comment_name);
            apartment = itemView.findViewById(R.id.comment_apartment);
            content = itemView.findViewById(R.id.comment_content);
            image = itemView.findViewById(R.id.comment_image);
            statusIcon = itemView.findViewById(R.id.comment_status_icon);
        }
    }

}
