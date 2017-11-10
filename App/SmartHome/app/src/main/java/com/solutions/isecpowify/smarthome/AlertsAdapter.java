package com.solutions.isecpowify.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mayank on 10/11/17.
 */

class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertHolder> {

    private Activity current;
    private ArrayList<Alert> alerts;
    private final LayoutInflater mInflater;

    AlertsAdapter(Activity curr, ArrayList<Alert> alerts) {
        current = curr;
        this.alerts = alerts;
        mInflater = LayoutInflater.from(current);
    }

    @Override
    public AlertHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.alert_cell, parent, false);
        return new AlertHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlertHolder holder, int position) {

        Alert a = alerts.get(position);

        holder.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.icon.setImageDrawable(current.getDrawable(a.getIconResource()));

        holder.title.setText(a.title);
        holder.message.setText(a.message);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Date alertTime = new Date(a.timestamp);
        holder.time.setText(sdf.format(alertTime));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    class AlertHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title,message,time;

        AlertHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.alertIcon);
            title = view.findViewById(R.id.alertTitle);
            message = view.findViewById(R.id.alertMessage);
            time = view.findViewById(R.id.alertTime);
        }
    }
}
