package com.township.manager;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public class ComplaintsAdapter extends RecyclerView.Adapter {

    ArrayList<Complaint> dataset;
    Boolean resolved;
    Context context;

    public ComplaintsAdapter(ArrayList<Complaint> dataset, Context context, Boolean resolved) {
        this.dataset = dataset;
        this.context = context;
        this.resolved = resolved;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_complaints, parent, false);

        final MyViewHolder viewHolder = new MyViewHolder(view);

        viewHolder.clickArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean complaintExpanded = viewHolder.complaintExpanded;
                if (complaintExpanded) {
                    viewHolder.expandButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    viewHolder.complaintResolveButton.setVisibility(View.GONE);
                    viewHolder.complaintDescriptionTextView.setVisibility(View.GONE);
                    viewHolder.complaintImageButton.setVisibility(View.GONE);
                    viewHolder.complaintExpanded = false;
                } else {
                    viewHolder.expandButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    viewHolder.complaintResolveButton.setVisibility(View.VISIBLE);
                    viewHolder.complaintDescriptionTextView.setVisibility(View.VISIBLE);
                    viewHolder.complaintImageButton.setVisibility(View.VISIBLE);
                    viewHolder.complaintExpanded = true;
                }
            }
        });

        if (!resolved) {
            viewHolder.complaintResolveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // query to resolve complaint goes here
                    // update local database and change UI accordingly
                }
            });
        } else {
            viewHolder.complaintResolveButton.setText("Resolved");
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        Complaint complaint = dataset.get(position);

        viewHolder.complaintTitle.setText(complaint.getTitle());
        viewHolder.residentNameTextView.setText(complaint.getFirstName() + " " + complaint.getLastName());
        viewHolder.residentApartmentTextView.setText(complaint.getWing() + "/" + complaint.getApartment());
        viewHolder.complaintDescriptionTextView.setText(complaint.getDescription());

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView expandButton;
        View clickArea;
        ImageButton complaintImageButton;
        Boolean complaintExpanded = false;
        TextView residentNameTextView, residentApartmentTextView, complaintTitle;
        MaterialTextView complaintDescriptionTextView;
        MaterialButton complaintResolveButton;
        ConstraintLayout constraintLayout;

        public MyViewHolder(View view) {
            super(view);
            expandButton = view.findViewById(R.id.complaint_expand_button);
            clickArea = view.findViewById(R.id.complaint_card_click_area);
            complaintImageButton = view.findViewById(R.id.complaint_image_button);
            residentNameTextView = view.findViewById(R.id.complaint_resident_name);
            residentApartmentTextView = view.findViewById(R.id.complaint_apartment);
            complaintTitle = view.findViewById(R.id.complaint_title);
            complaintDescriptionTextView = view.findViewById(R.id.complaint_description_textview);
            complaintResolveButton = view.findViewById(R.id.resolve_complaint_button);
            constraintLayout = view.findViewById(R.id.complaint_card_constraint_layout);
        }

    }

}
