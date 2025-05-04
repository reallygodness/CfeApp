package com.example.cfeprjct.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.DAOS.OrderStatusDAO;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderAdapter extends ListAdapter<Order, OrderAdapter.VH> {
    private final SimpleDateFormat dateFmt =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private final OrderStatusDAO statusDao;

    public OrderAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Order>() {
            @Override public boolean areItemsTheSame(@NonNull Order a, @NonNull Order b) {
                return a.getOrderId() == b.getOrderId();
            }
            @Override public boolean areContentsTheSame(@NonNull Order a, @NonNull Order b) {
                return a.getStatusId() == b.getStatusId()
                        && a.getTotalPrice() == b.getTotalPrice()
                        && a.getCreatedAt() == b.getCreatedAt();
            }
        });
        // получаем DAO для статусов
        statusDao = AppDatabase
                .getInstance(context.getApplicationContext())
                .orderStatusDAO();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order o = getItem(position);

        // Номер заказа
        holder.tvId.setText("Заказ #" + o.getOrderId());

        // Дата и время
        if (o.getCreatedAt() > 0) {
            holder.tvDate.setText(dateFmt.format(new Date(o.getCreatedAt())));
        } else {
            holder.tvDate.setText("");
        }

        // Статус из отдельной таблицы
        OrderStatus st = statusDao.getById(o.getStatusId());
        holder.tvStatus.setText(st != null ? st.getStatusName() : "");

        // Сумма
        holder.tvTotal.setText(String.format(Locale.getDefault(),
                "%d ₽", (int)o.getTotalPrice()));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvDate, tvStatus, tvTotal;
        VH(@NonNull View itemView) {
            super(itemView);
            tvId     = itemView.findViewById(R.id.itemOrderId);
            tvDate   = itemView.findViewById(R.id.itemOrderDate);
            tvStatus = itemView.findViewById(R.id.itemOrderStatus);
            tvTotal  = itemView.findViewById(R.id.itemOrderTotal);
        }
    }
}
