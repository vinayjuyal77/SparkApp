package com.app.spark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.spark.R;
import com.app.spark.models.CountryStateResponse;

import java.util.List;

public class ListAdapter extends ArrayAdapter<CountryStateResponse.Result> {

    private static class ViewHolder {
        private TextView itemView;
    }

    public ListAdapter(Context context, int textViewResourceId, List<CountryStateResponse.Result> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.item_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.tvName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CountryStateResponse.Result item = getItem(position);
        if (item != null) {

            viewHolder.itemView.setText(item.getName());
        }

        return convertView;
    }
}