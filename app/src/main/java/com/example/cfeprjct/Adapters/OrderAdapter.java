package com.example.cfeprjct.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.Entities.CartItem;
import com.example.cfeprjct.Entities.Dessert;
import com.example.cfeprjct.Entities.Dish;
import com.example.cfeprjct.Entities.Drink;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.Entities.OrderedDessert;
import com.example.cfeprjct.Entities.OrderedDish;
import com.example.cfeprjct.Entities.OrderedDrink;
import com.example.cfeprjct.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class OrderAdapter extends ListAdapter<Order, OrderAdapter.VH> {

    private final LayoutInflater inflater;
    private final AppDatabase db;
    private final List<OrderStatus> statuses;

    public OrderAdapter(Context ctx, List<OrderStatus> statuses) {
        super(new DiffUtil.ItemCallback<Order>() {
            @Override
            public boolean areItemsTheSame(@NonNull Order a, @NonNull Order b) {
                return a.getOrderId() == b.getOrderId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Order a, @NonNull Order b) {
                return a.getCreatedAt() == b.getCreatedAt()
                        && a.getTotalPrice() == b.getTotalPrice()
                        && a.getStatusId() == b.getStatusId();
            }
        });
        this.inflater  = LayoutInflater.from(ctx);
        this.db        = AppDatabase.getInstance(ctx);
        this.statuses  = statuses;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Order o = getItem(pos);

        // Шапка заказа
        h.tvOrderId.setText("Заказ №" + o.getOrderId());
        h.tvDate   .setText(formatDate(o.getCreatedAt()));
        h.tvStatus .setText(statuses.get(o.getStatusId() - 1).getStatusName());
        h.tvPayment.setText("К оплате курьеру: " + (int)o.getTotalPrice() + " ₽");

        // Очищаем контейнер товаров
        h.itemsContainer.removeAllViews();

        // Загружаем позиции заказа в фоне и рендерим их
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CartItem> items = new ArrayList<>();

            // Напитки
            for (OrderedDrink od : db.orderedDrinkDAO().getByOrderId(o.getOrderId())) {
                Drink d = db.drinkDAO().getDrinkById(od.getDrinkId());
                CartItem ci = new CartItem();
                ci.setProductType("drink");
                ci.setTitle(d.getName());
                ci.setImageUrl(d.getImageUrl());
                ci.setQuantity(od.getQuantity());
                ci.setUnitPrice(db.priceListDAO().getLatestPriceForDrink(d.getDrinkId()));
                ci.setSize(od.getSize());
                items.add(ci);
            }
            // Блюда
            for (OrderedDish od : db.orderedDishDAO().getByOrderId(o.getOrderId())) {
                Dish d = db.dishDAO().getDishById(od.getDishId());
                CartItem ci = new CartItem();
                ci.setProductType("dish");
                ci.setTitle(d.getName());
                ci.setImageUrl(d.getImageUrl());
                ci.setQuantity(od.getQuantity());
                ci.setUnitPrice(db.priceListDAO().getLatestPriceForDish(d.getDishId()));
                ci.setSize(d.getSize());
                items.add(ci);
            }
            // Десерты
            for (OrderedDessert od : db.orderedDessertDAO().getByOrderId(o.getOrderId())) {
                Dessert d = db.dessertDAO().getById(od.getDessertId());
                CartItem ci = new CartItem();
                ci.setProductType("dessert");
                ci.setTitle(d.getName());
                ci.setImageUrl(d.getImageUrl());
                ci.setQuantity(od.getQuantity());
                ci.setUnitPrice(db.priceListDAO().getLatestPriceForDessert(d.getDessertId()));
                ci.setSize(d.getSize());
                items.add(ci);
            }

            // Добавляем views в UI-потоке
            h.itemView.post(() -> {
                for (CartItem ci : items) {
                    View iv = inflater.inflate(R.layout.item_cart, h.itemsContainer, false);
                    ImageView img    = iv.findViewById(R.id.itemImage);
                    TextView  title  = iv.findViewById(R.id.itemTitle);
                    TextView  size   = iv.findViewById(R.id.itemSize);
                    TextView  qty    = iv.findViewById(R.id.tvQuantity);
                    TextView  price  = iv.findViewById(R.id.itemPrice);

                    title.setText(ci.getTitle());
                    qty  .setText("×" + ci.getQuantity());
                    price.setText((int)(ci.getUnitPrice()*ci.getQuantity()) + " ₽");

                    // Отображаем объём/вес
                    if (ci.getSize() > 0) {
                        size.setVisibility(View.VISIBLE);
                        String unit = ci.getProductType().equals("drink") ? " ml" : " г";
                        size.setText(ci.getSize() + unit);
                    } else {
                        size.setVisibility(View.GONE);
                    }

                    Glide.with(img.getContext())
                            .load(ci.getImageUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .centerCrop()
                            .into(img);

                    h.itemsContainer.addView(iv);
                }
            });
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView      tvOrderId, tvDate, tvStatus, tvPayment;
        LinearLayout  itemsContainer;

        VH(View v) {
            super(v);
            tvOrderId      = v.findViewById(R.id.tvOrderId);
            tvDate         = v.findViewById(R.id.tvOrderDate);
            tvStatus       = v.findViewById(R.id.tvOrderStatus);
            itemsContainer = v.findViewById(R.id.itemsContainer);
            tvPayment      = v.findViewById(R.id.tvOrderPayment);
        }
    }

    private String formatDate(long millis) {
        return DateFormat.format("dd.MM.yy HH:mm", new Date(millis)).toString();
    }
}
