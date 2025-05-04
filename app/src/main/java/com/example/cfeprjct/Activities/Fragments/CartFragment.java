package com.example.cfeprjct.Activities.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfeprjct.Activities.MainActivity;
import com.example.cfeprjct.Adapters.CartAdapter;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.DAOS.AddressDAO;
import com.example.cfeprjct.DAOS.OrderDAO;
import com.example.cfeprjct.DAOS.OrderStatusDAO;
import com.example.cfeprjct.DAOS.OrderedDessertDAO;
import com.example.cfeprjct.DAOS.OrderedDishDAO;
import com.example.cfeprjct.DAOS.OrderedDrinkDAO;
import com.example.cfeprjct.Entities.Address;
import com.example.cfeprjct.Entities.CartItem;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.Entities.OrderedDessert;
import com.example.cfeprjct.Entities.OrderedDish;
import com.example.cfeprjct.Entities.OrderedDrink;
import com.example.cfeprjct.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {
    private static final int REQ_LOC = 1001;

    private AppDatabase db;
    private AddressDAO addressDAO;
    private OrderDAO orderDAO;
    private OrderedDrinkDAO orderedDrinkDAO;
    private OrderedDishDAO orderedDishDAO;
    private OrderedDessertDAO orderedDessertDAO;
    private OrderStatusDAO orderStatusDAO;
    private FirebaseFirestore firestore;
    private FusedLocationProviderClient fusedLocationClient;

    private CartAdapter cartAdapter;
    private RecyclerView rvCart;
    private TextView tvTotal;
    private TextView tvAddress;
    private ImageView btnEditAddress;
    private MaterialButton btnCheckout;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // 1) Инициализируем БД, DAO и сервисы
        db = AppDatabase.getInstance(requireContext());
        addressDAO         = db.addressDAO();
        orderDAO           = db.orderDAO();
        orderedDrinkDAO    = db.orderedDrinkDAO();
        orderedDishDAO     = db.orderedDishDAO();
        orderedDessertDAO  = db.orderedDessertDAO();
        orderStatusDAO     = db.orderStatusDAO();
        firestore          = FirebaseFirestore.getInstance();
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(requireContext());

        // 2) Привязываем view
        rvCart          = view.findViewById(R.id.rvCart);
        tvTotal         = view.findViewById(R.id.tvCartTotal);
        tvAddress       = view.findViewById(R.id.tvCartAddress);
        btnEditAddress  = view.findViewById(R.id.btnEditAddress);
        btnCheckout     = view.findViewById(R.id.btnCheckout);

        // 3) RecyclerView
        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        cartAdapter = new CartAdapter(db.cartItemDao());
        rvCart.setAdapter(cartAdapter);

        // 4) Адрес (копипаст из CatalogFragment)
        String userId = AuthUtils.getLoggedInUserId(requireContext());
        if (userId != null) {
            new Thread(() -> {
                Address local = addressDAO.getAddressByUserId(userId);
                if (local != null) {
                    String formatted = local.getCity()
                            + ", " + local.getStreet()
                            + " " + local.getHouse()
                            + (local.getApartment().isEmpty() ? "" : ", кв. " + local.getApartment());
                    requireActivity().runOnUiThread(() -> tvAddress.setText(formatted));
                } else {
                    fetchAddressFromFirestore(userId);
                }
            }).start();
        } else {
            tvAddress.setText("Введите адрес");
        }
        View.OnClickListener addrClick = v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQ_LOC);
            } else {
                requestLastLocation();
            }
        };
        tvAddress.setOnClickListener(addrClick);
        btnEditAddress.setOnClickListener(v ->
                new EditAddressDialogFragment().show(getChildFragmentManager(), "editAddr")
        );

        // 5) Подписываемся на корзину и считаем итог
        LiveData<List<CartItem>> live = db.cartItemDao().getAll();
        live.observe(getViewLifecycleOwner(), items -> {
            cartAdapter.submitList(items);
            int sum = 0;
            for (CartItem ci : items) {
                sum += ci.getQuantity() * ci.getUnitPrice();
            }
            if (sum > 0) {
                tvTotal.setText(String.format(
                        Locale.getDefault(),
                        "Итог: %d ₽ (доставка 100 ₽)", sum + 100
                ));
                btnCheckout.setEnabled(true);
            } else {
                tvTotal.setText("Итог: 0 ₽");
                btnCheckout.setEnabled(false);
            }
        });

        // 6) Оформление заказа
        // 6) Оформление заказа
        btnCheckout.setOnClickListener(v -> {
            new Thread(() -> {
                List<CartItem> items = db.cartItemDao().getAllSync();
                if (items.isEmpty()) return;

                // текущее время
                long now = System.currentTimeMillis();
                // статус «В готовке»
                OrderStatus cook = orderStatusDAO.getByName("В готовке");
                int cookId = (cook != null ? cook.getStatusId() : 1);

                // считаем сумму + доставка
                float total = 0f;
                for (CartItem ci : items) {
                    total += ci.getQuantity() * ci.getUnitPrice();
                }
                total += 100f;

                // создаём Order
                Order order = new Order();
                order.setUserId(userId);
                order.setCreatedAt(now);
                order.setStatusId(cookId);
                order.setTotalPrice(total);
                long newId = orderDAO.insertOrder(order);
                int orderId = (int)newId;

                // каждый CartItem превращаем в OrderedXXX
                for (CartItem ci : items) {
                    switch (ci.getProductType()) {
                        case "drink":
                            OrderedDrink od = new OrderedDrink();
                            od.setOrderId(orderId);
                            od.setDrinkId(ci.getProductId());
                            od.setQuantity(ci.getQuantity());
                            orderedDrinkDAO.insert(od);
                            break;
                        case "dish":
                            OrderedDish od2 = new OrderedDish();
                            od2.setOrderId(orderId);
                            od2.setDishId(ci.getProductId());
                            od2.setQuantity(ci.getQuantity());
                            orderedDishDAO.insert(od2);
                            break;
                        default:
                            OrderedDessert od3 = new OrderedDessert();
                            od3.setOrderId(orderId);
                            od3.setDessertId(ci.getProductId());
                            od3.setQuantity(ci.getQuantity());
                            orderedDessertDAO.insert(od3);
                            break;
                    }
                }

                // очистить корзину
                db.cartItemDao().clearAll();

                // на UI: тост + переключение на «Заказы» + планировщик смены статуса
                new Handler(getContext().getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), "Заказ оформлен", Toast.LENGTH_SHORT).show();
                    ((MainActivity)requireActivity())
                            .getBottomNavigationView()
                            .setSelectedItemId(R.id.nav_orders);

                    // через 20 минут перевести в «Доставлен»
                    new Handler().postDelayed(() -> {
                        Order o = orderDAO.getOrderById(orderId);
                        if (o != null && o.getStatusId() == cookId) {
                            OrderStatus done = orderStatusDAO.getByName("Доставлен");
                            if (done != null) {
                                o.setStatusId(done.getStatusId());
                                orderDAO.updateOrder(o);
                            }
                        }
                    }, 20 * 60 * 1000);
                });

            }).start();
        });

        return view;
    }

    @SuppressLint("MissingPermission")
    private void requestLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) reverseGeocode(loc.getLatitude(), loc.getLongitude());
                    else Toast.makeText(requireContext(),
                            "Не удалось получить локацию", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Ошибка при получении геолокации", Toast.LENGTH_SHORT).show());
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder gc = new Geocoder(requireContext());
                List<android.location.Address> list = gc.getFromLocation(lat, lng, 1);
                if (!list.isEmpty()) {
                    android.location.Address src = list.get(0);
                    saveAndSyncAddress(
                            src.getLocality(), src.getThoroughfare(), src.getFeatureName()
                    );
                }
            } catch (IOException ignored) {}
        }).start();
    }

    private void saveAndSyncAddress(String city, String street, String house) {
        String userId = AuthUtils.getLoggedInUserId(requireContext());
        if (userId == null) return;
        new Thread(() -> {
            Address ex = addressDAO.getAddressByUserId(userId);
            int addrId;
            if (ex != null) {
                ex.setCity(city);
                ex.setStreet(street);
                ex.setHouse(house);
                ex.setApartment("");
                addressDAO.updateAddress(ex);
                addrId = ex.getAddressId();
            } else {
                Address a = new Address();
                a.setUserId(userId);
                a.setCity(city);
                a.setStreet(street);
                a.setHouse(house);
                a.setApartment("");
                addrId = (int)addressDAO.insertAddress(a);
            }
            Map<String,Object> map = new HashMap<>();
            map.put("addressId",addrId);
            map.put("userId",   userId);
            map.put("city",     city);
            map.put("street",   street);
            map.put("house",    house);
            map.put("apartment","");
            firestore.collection("addresses")
                    .document(String.valueOf(addrId)).set(map);
            firestore.collection("users")
                    .document(userId).update("addressId", addrId);
            String fmt = city + ", " + street + " " + house;
            requireActivity().runOnUiThread(() -> tvAddress.setText(fmt));
        }).start();
    }

    private void fetchAddressFromFirestore(String userId) {
        firestore.collection("users")
                .document(userId)
                .get().addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists() || !userDoc.contains("addressId")) return;
                    Long addrId = userDoc.getLong("addressId");
                    if (addrId == null) return;
                    firestore.collection("addresses")
                            .document(String.valueOf(addrId))
                            .get().addOnSuccessListener(addrDoc -> {
                                if (!addrDoc.exists()) return;
                                String city = addrDoc.getString("city");
                                String street = addrDoc.getString("street");
                                String house = addrDoc.getString("house");
                                String apt = addrDoc.getString("apartment");
                                String fmt = city + ", " + street + " " + house +
                                        (apt != null && !apt.isEmpty() ? ", кв. " + apt : "");
                                tvAddress.setText(fmt);
                                new Thread(() -> {
                                    Address a = new Address();
                                    a.setUserId(userId);
                                    a.setCity(city);
                                    a.setStreet(street);
                                    a.setHouse(house);
                                    a.setApartment(apt != null ? apt : "");
                                    addressDAO.insertAddress(a);
                                }).start();
                            });
                });
    }
}
