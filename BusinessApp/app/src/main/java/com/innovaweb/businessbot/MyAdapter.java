package com.innovaweb.businessbot;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ImageView itemIcon;
    private List<String> items;

    public MyAdapter(List<String> items) {
        this.items = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String itemText = items.get(position);
        TextView textView = (TextView) holder.itemView.findViewById(R.id.item_text);
        textView.setText(itemText);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View itemView) {
            super(itemView);
            itemIcon = (ImageView)itemView.findViewById(R.id.item_icon);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            if (position == 1) {
                Context context = view.getContext();
                Intent intent = new Intent(context, InventoryActivity2.class);
                context.startActivity(intent);
            } else if (position == 0) {
                Context context = view.getContext();
                Intent intent = new Intent(context, ConfigchatActivity.class);
                context.startActivity(intent);
            } else if (position == 2) {
                Context context = view.getContext();
                Intent intent = new Intent(context, RegisterSalesActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
