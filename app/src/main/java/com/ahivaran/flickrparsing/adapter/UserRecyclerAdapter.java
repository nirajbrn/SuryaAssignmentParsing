package com.ahivaran.flickrparsing.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahivaran.flickrparsing.R;
import com.ahivaran.flickrparsing.data.UserData;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ImageHolder>{
    private static final String TAG = UserRecyclerAdapter.class.getSimpleName();

    ArrayList<UserData> mImageDataList;
    private static Activity mActivity;
    public UserRecyclerAdapter(Activity activity, ArrayList<UserData> imageDataList) {
        this.mImageDataList = imageDataList;
        mActivity = activity;
    }

    public void setBillDataList(ArrayList<UserData> imageDataList){
        this.mImageDataList = imageDataList;
        notifyDataSetChanged();
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.email_details_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        UserData userData = mImageDataList.get(position);
        holder.fNameTextView.setText(userData.fName);
        holder.lNameTextView.setText(userData.lName);
        holder.emailTextView.setText(userData.emailId);
        String imageUrl = userData.imageUrl;
        Glide.with(mActivity)
                .load(imageUrl)
                .into(holder.userImageView);
    }

    @Override
    public int getItemCount() {
        return mImageDataList != null ? mImageDataList.size() : 0;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder{

        ImageView userImageView;
        TextView fNameTextView;
        TextView lNameTextView;
        TextView emailTextView;
        public ImageHolder(View itemView) {
            super(itemView);
            userImageView = (ImageView)itemView.findViewById(R.id.user_image);
            fNameTextView = (TextView)itemView.findViewById(R.id.first_name);
            lNameTextView = (TextView)itemView.findViewById(R.id.last_name);
            emailTextView = (TextView)itemView.findViewById(R.id.email_id);
        }

    }

}
