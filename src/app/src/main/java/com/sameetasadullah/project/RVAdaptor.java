package com.sameetasadullah.project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RVAdaptor extends RecyclerView.Adapter<RVAdaptor.ViewHolder> {
    Context context;
    List<String> namesOfSusApps;
    List<ApplicationInfo> susApps;

    public RVAdaptor(Context context, List<String> namesOfSusApps, List<ApplicationInfo> susApps) {
        this.context = context;
        this.namesOfSusApps = namesOfSusApps;
        this.susApps = susApps;
    }

    @NonNull
    @Override
    public RVAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(context).inflate(R.layout.row, null, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdaptor.ViewHolder holder, int position) {
        holder.name.setText(namesOfSusApps.get(position));
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, trackApp.class);
                intent.putExtra("susAppPackage", susApps.get(holder.getAdapterPosition()).packageName);
                intent.putExtra("susAppName", namesOfSusApps.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return namesOfSusApps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            button = itemView.findViewById(R.id.button);
        }
    }
}

