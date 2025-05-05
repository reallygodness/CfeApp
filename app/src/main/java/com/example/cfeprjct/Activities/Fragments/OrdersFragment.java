package com.example.cfeprjct.Activities.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfeprjct.Adapters.OrderAdapter;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.R;

import java.util.List;

public class OrdersFragment extends Fragment {
    private AppDatabase db;
    private OrderAdapter adapter;

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        db       = AppDatabase.getInstance(requireContext());
        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 1) Сразу инициализируем адаптер—он принимает Context и список статусов
        new Thread(() -> {
            List<OrderStatus> statuses = db.orderStatusDAO().getAllStatuses();
            requireActivity().runOnUiThread(() -> {
                orderAdapter = new OrderAdapter(requireContext(), statuses);
                rvOrders.setAdapter(orderAdapter);

                // 2) Подписываемся на live-список заказов
                db.orderDAO().getAllLiveOrders()
                        .observe(getViewLifecycleOwner(), orders -> {
                            // здесь orders — List<Order>, подходящий для submitList
                            orderAdapter.submitList(orders);
                        });
            });
        }).start();

        return view;
    }
}
