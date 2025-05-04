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
import com.example.cfeprjct.R;

import java.util.List;

public class OrdersFragment extends Fragment {
    private AppDatabase db;
    private OrderAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        RecyclerView rv = v.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Передаём Context в адаптер
        OrderAdapter adapter = new OrderAdapter(requireContext());
        rv.setAdapter(adapter);

        // Наблюдаем за списком заказов
        db = AppDatabase.getInstance(requireContext());
        String userId = AuthUtils.isLoggedIn(requireContext())
                ? AuthUtils.getLoggedInUserId(requireContext())
                : null;

        LiveData<List<Order>> liveOrders = db.orderDAO().getAllByUser(userId);
        liveOrders.observe(getViewLifecycleOwner(), orders -> {
            adapter.submitList(orders);
        });

        return v;
    }
}
