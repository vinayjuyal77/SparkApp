package com.app.spark.utils;


import androidx.recyclerview.widget.RecyclerView;

public interface SwipeListener {
    public void onLeftToRightSwipe(RecyclerView.ViewHolder viewHolder);
    public void onRightToLeftSwipe(RecyclerView.ViewHolder viewHolder);
}
