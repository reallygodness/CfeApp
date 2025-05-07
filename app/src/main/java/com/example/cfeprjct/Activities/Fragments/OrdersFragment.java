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
import com.example.cfeprjct.DAOS.OrderDAO;
import com.example.cfeprjct.DAOS.OrderStatusDAO;
import com.example.cfeprjct.DAOS.OrderedDessertDAO;
import com.example.cfeprjct.DAOS.OrderedDishDAO;
import com.example.cfeprjct.DAOS.OrderedDrinkDAO;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.Entities.OrderedDessert;
import com.example.cfeprjct.Entities.OrderedDish;
import com.example.cfeprjct.Entities.OrderedDrink;
import com.example.cfeprjct.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class OrdersFragment extends Fragment {


    private AppDatabase db;
    private OrderDAO orderDAO;
    private OrderedDrinkDAO orderedDrinkDAO;
    private OrderedDishDAO orderedDishDAO;
    private OrderedDessertDAO orderedDessertDAO;
    private OrderStatusDAO orderStatusDAO;
    private FirebaseFirestore firestore;
    private String userId;

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        // 1) Инициализация
        db                 = AppDatabase.getInstance(requireContext());
        orderDAO           = db.orderDAO();
        orderedDrinkDAO    = db.orderedDrinkDAO();
        orderedDishDAO     = db.orderedDishDAO();
        orderedDessertDAO  = db.orderedDessertDAO();
        orderStatusDAO     = db.orderStatusDAO();
        firestore          = FirebaseFirestore.getInstance();
        userId             = AuthUtils.getLoggedInUserId(requireContext());

        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) Готовим справочник статусов и настраиваем адаптер
        Executors.newSingleThreadExecutor().execute(() -> {
            List<OrderStatus> statuses = orderStatusDAO.getAllStatuses();

            // Если в локальной БД нет заказов — грузим из Firestore
            List<Order> local = orderDAO.getAllSync();
            if (local.isEmpty()) {
                firestore.collection("orders")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(this::onOrdersFetchedFromCloud);
            }

            // 3) В UI-потоке создаём адаптер и наблюдаем LiveData
            requireActivity().runOnUiThread(() -> {
                orderAdapter = new OrderAdapter(requireContext(), statuses);
                rvOrders.setAdapter(orderAdapter);

                LiveData<List<Order>> live = orderDAO.getAllLiveOrders();
                live.observe(getViewLifecycleOwner(), orders -> {
                    orderAdapter.submitList(orders);
                });
            });
        });

        return view;
    }

    /**
     * Callback, когда заказы из Firestore получены:
     * сохраняем их и их позиции в локальную БД в фоновом потоке.
     */
    private void onOrdersFetchedFromCloud(QuerySnapshot snap) {
        for (DocumentSnapshot doc : snap.getDocuments()) {
            // Собираем сам Order
            Order o = new Order();
            o.setOrderId(doc.getLong("orderId").intValue());
            o.setUserId(doc.getString("userId"));
            o.setCreatedAt(doc.getLong("createdAt"));
            o.setTotalPrice(doc.getDouble("totalPrice").floatValue());
            o.setStatusId(doc.getLong("statusId").intValue());

            // Вставляем Order в фоне
            Executors.newSingleThreadExecutor().execute(() ->
                    orderDAO.insertOrder(o)
            );

            String orderDocId = doc.getId();

            // Позиции — напитки
            firestore.collection("orders")
                    .document(orderDocId)
                    .collection("drinks")
                    .get()
                    .addOnSuccessListener(drinkSnap -> {
                        for (DocumentSnapshot d : drinkSnap.getDocuments()) {
                            OrderedDrink od = new OrderedDrink();
                            od.setOrderedDrinkId(d.getLong("orderedDrinkId").intValue());
                            od.setOrderId(d.getLong("orderId").intValue());
                            od.setDrinkId(d.getLong("drinkId").intValue());
                            od.setQuantity(d.getLong("quantity").intValue());
                            od.setSize(d.getLong("size").intValue());
                            // вставка в фоне
                            Executors.newSingleThreadExecutor().execute(() ->
                                    orderedDrinkDAO.insert(od)
                            );
                        }
                    });

            // Позиции — блюда
            firestore.collection("orders")
                    .document(orderDocId)
                    .collection("dishes")
                    .get()
                    .addOnSuccessListener(dishSnap -> {
                        for (DocumentSnapshot d : dishSnap.getDocuments()) {
                            OrderedDish od = new OrderedDish();
                            od.setOrderedDishId(d.getLong("orderedDishId").intValue());
                            od.setOrderId(d.getLong("orderId").intValue());
                            od.setDishId(d.getLong("dishId").intValue());
                            od.setQuantity(d.getLong("quantity").intValue());
                            od.setSize(d.getLong("size").intValue());
                            Executors.newSingleThreadExecutor().execute(() ->
                                    orderedDishDAO.insert(od)
                            );
                        }
                    });

            // Позиции — десерты
            firestore.collection("orders")
                    .document(orderDocId)
                    .collection("desserts")
                    .get()
                    .addOnSuccessListener(desSnap -> {
                        for (DocumentSnapshot d : desSnap.getDocuments()) {
                            OrderedDessert od = new OrderedDessert();
                            od.setOrderedDessertId(d.getLong("orderedDessertId").intValue());
                            od.setOrderId(d.getLong("orderId").intValue());
                            od.setDessertId(d.getLong("dessertId").intValue());
                            od.setQuantity(d.getLong("quantity").intValue());
                            od.setSize(d.getLong("size").intValue());
                            Executors.newSingleThreadExecutor().execute(() ->
                                    orderedDessertDAO.insert(od)
                            );
                        }
                    });
        }
    }
}