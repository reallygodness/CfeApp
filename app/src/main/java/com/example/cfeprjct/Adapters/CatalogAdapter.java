package com.example.cfeprjct.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cfeprjct.R;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.ViewHolder> {

    private List<CatalogItem> items = new ArrayList<>();

    public void setItems(List<CatalogItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatalogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_catalog_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogAdapter.ViewHolder holder, int position) {
        CatalogItem item = items.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.priceTextView.setText(item.getPrice() + " ₽");

        // Загружаем картинку по URL (Glide):
        String url = item.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, priceTextView;
        ImageView imageView;
        Button addButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView       = itemView.findViewById(R.id.itemTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            priceTextView       = itemView.findViewById(R.id.itemPriceTextView);
            imageView           = itemView.findViewById(R.id.itemImageView);
            addButton           = itemView.findViewById(R.id.addButton);
        }
    }
}

