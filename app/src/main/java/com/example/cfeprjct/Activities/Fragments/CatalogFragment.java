package com.example.cfeprjct.Activities.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfeprjct.Adapters.CatalogAdapter;
import com.example.cfeprjct.Adapters.CatalogItem;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.DAOS.AddressDAO;
import com.example.cfeprjct.DAOS.DessertDAO;
import com.example.cfeprjct.DAOS.DishDAO;
import com.example.cfeprjct.DAOS.DrinkDAO;
import com.example.cfeprjct.DAOS.PriceListDAO;
import com.example.cfeprjct.Entities.Address;
import com.example.cfeprjct.Entities.Dessert;
import com.example.cfeprjct.Entities.Dish;
import com.example.cfeprjct.Entities.Drink;
import com.example.cfeprjct.R;
import com.example.cfeprjct.Sync.CatalogSync;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogFragment extends Fragment {
    private static final int REQ_LOC = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;

    private AppDatabase db;
    private DrinkDAO     drinkDAO;
    private DishDAO      dishDAO;
    private DessertDAO   dessertDAO;
    private PriceListDAO priceListDAO;
    private AddressDAO   addressDAO;

    private EditText     searchEditText;
    private RecyclerView catalogRecyclerView;
    private CatalogAdapter catalogAdapter;
    private Button       drinkTabButton, dishTabButton, dessertTabButton;
    private TextView     addressTextView;
    private ImageView    editAddressButton;

    private enum Category { DRINKS, DISHES, DESSERTS }
    private Category currentCategory = Category.DRINKS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = requireActivity().getWindow();
        // Делаем фон статус‑бара чёрным
        window.setStatusBarColor(Color.BLACK);

        // Управляем цветом иконок
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        // false = светлые иконки (для тёмного фона), true = тёмные иконки (для светлого фона)
        insetsController.setAppearanceLightStatusBars(false);

        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // 1) Инициализируем базу и DAO
        db             = AppDatabase.getInstance(requireContext());
        drinkDAO       = db.drinkDAO();
        dishDAO        = db.dishDAO();
        dessertDAO     = db.dessertDAO();
        priceListDAO   = db.priceListDAO();
        addressDAO     = db.addressDAO();
        firestore      = FirebaseFirestore.getInstance();

        // 2) Находим view
        searchEditText      = view.findViewById(R.id.searchEditText);
        drinkTabButton      = view.findViewById(R.id.drinkTabButton);
        dishTabButton       = view.findViewById(R.id.dishTabButton);
        dessertTabButton    = view.findViewById(R.id.dessertTabButton);
        catalogRecyclerView = view.findViewById(R.id.catalogRecyclerView);
        addressTextView     = view.findViewById(R.id.addressTextView);
        editAddressButton   = view.findViewById(R.id.editAddressButton);

        catalogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        catalogAdapter = new CatalogAdapter();
        catalogRecyclerView.setAdapter(catalogAdapter);

        // 3) Сначала пробуем загрузить адрес из Room
        String userId = AuthUtils.getLoggedInUserId(requireContext());
        if (userId != null) {
            new Thread(() -> {
                Address local = addressDAO.getAddressByUserId(userId);
                if (local != null) {
                    String formatted = local.getCity()
                            + ", " + local.getStreet()
                            + " " + local.getHouse()
                            + (local.getApartment().isEmpty() ? "" : ", кв. " + local.getApartment());
                    requireActivity().runOnUiThread(() -> addressTextView.setText(formatted));
                } else {
                    // если в локальной БД нет — дергаем Firestore
                    fetchAddressFromFirestore(userId);
                }
            }).start();
        }

        // 4) Настраиваем геолокацию
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
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
        addressTextView.setOnClickListener(addrClick);
        editAddressButton.setOnClickListener(addrClick);

        // 5) Синхронизируем каталог из Firestore → Room
        CatalogSync sync = new CatalogSync(requireContext());
        sync.syncDrinks  (() -> requireActivity().runOnUiThread(() -> loadDrinks  (searchEditText.getText().toString())));
        sync.syncDishes  (() -> requireActivity().runOnUiThread(() -> loadDishes  (searchEditText.getText().toString())));
        sync.syncDesserts(() -> requireActivity().runOnUiThread(() -> loadDesserts(searchEditText.getText().toString())));

        // 6) Вкладки
        drinkTabButton .setOnClickListener(v -> { currentCategory = Category.DRINKS;  loadDrinks  (searchEditText.getText().toString()); });
        dishTabButton  .setOnClickListener(v -> { currentCategory = Category.DISHES;  loadDishes  (searchEditText.getText().toString()); });
        dessertTabButton.setOnClickListener(v -> { currentCategory = Category.DESSERTS; loadDesserts(searchEditText.getText().toString()); });

        // 7) Поиск
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged    (CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged (Editable s) {
                String q = s.toString().trim();
                switch (currentCategory) {
                    case DRINKS:   loadDrinks  (q); break;
                    case DISHES:   loadDishes  (q); break;
                    case DESSERTS: loadDesserts(q); break;
                }
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_LOC
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLastLocation();
        } else {
            Toast.makeText(requireContext(),
                    "Разрешение на геолокацию не получено",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) reverseGeocode(loc.getLatitude(), loc.getLongitude());
                    else Toast.makeText(requireContext(),
                            "Не удалось получить локацию", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CatalogFragment", "Ошибка локации", e);
                    Toast.makeText(requireContext(),
                            "Ошибка при получении геолокации", Toast.LENGTH_SHORT).show();
                });
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder gc = new Geocoder(requireContext());
                List<android.location.Address> list = gc.getFromLocation(lat, lng, 1);
                if (list != null && !list.isEmpty()) {
                    android.location.Address src = list.get(0);
                    String city   = src.getLocality();
                    String street = src.getThoroughfare();
                    String house  = src.getFeatureName();
                    String formatted = city + ", " + street + " " + house;
                    requireActivity().runOnUiThread(() -> addressTextView.setText(formatted));
                    saveAndSyncAddress(city, street, house);
                }
            } catch (IOException e) {
                Log.e("CatalogFragment", "reverseGeocode error", e);
            }
        }).start();
    }

    private void saveAndSyncAddress(String city, String street, String house) {
        String userId = AuthUtils.getLoggedInUserId(requireContext());
        if (userId == null) return;

        // 1) Вставляем в локальную Room
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setCity(city);
        addr.setStreet(street);
        addr.setHouse(house);
        addr.setApartment("");
        long rowId = addressDAO.insertAddress(addr);
        int addressId = (int) rowId;

        // 2) Сохраняем в Firestore
        Map<String, Object> map = new HashMap<>();
        map.put("addressId", addressId);
        map.put("userId",    userId);
        map.put("city",      city);
        map.put("street",    street);
        map.put("house",     house);
        map.put("apartment","");

        firestore.collection("addresses")
                .document(String.valueOf(addressId))
                .set(map);

        // 3) Обновляем поле в users/{userId}
        firestore.collection("users")
                .document(userId)
                .update("addressId", addressId);
    }

    private void fetchAddressFromFirestore(String userId) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && userDoc.contains("addressId")) {
                        Long addrId = userDoc.getLong("addressId");
                        if (addrId != null) {
                            firestore.collection("addresses")
                                    .document(String.valueOf(addrId))
                                    .get()
                                    .addOnSuccessListener(addrDoc -> {
                                        if (addrDoc.exists()) {
                                            String city   = addrDoc.getString("city");
                                            String street = addrDoc.getString("street");
                                            String house  = addrDoc.getString("house");
                                            String apt    = addrDoc.getString("apartment");
                                            String formatted = (city != null ? city : "")
                                                    + (street != null ? ", " + street : "")
                                                    + (house != null ? " " + house : "")
                                                    + (apt != null && !apt.isEmpty() ? ", кв. " + apt : "");
                                            addressTextView.setText(formatted);

                                            // Сохраняем в Room
                                            new Thread(() -> {
                                                Address a = new Address();
                                                a.setAddressId(addrId.intValue());
                                                a.setUserId(userId);
                                                a.setCity(city != null ? city : "");
                                                a.setStreet(street != null ? street : "");
                                                a.setHouse(house != null ? house : "");
                                                a.setApartment(apt != null ? apt : "");
                                                addressDAO.insertAddress(a);
                                            }).start();
                                        }
                                    });
                        }
                    }
                });
    }

    private void loadDrinks(String query) {
        new Thread(() -> {
            List<Drink> list = query.isEmpty()
                    ? drinkDAO.getAllDrinks()
                    : drinkDAO.searchDrinksByName("%" + query + "%");
            List<CatalogItem> items = new ArrayList<>();
            for (Drink d : list) {
                float price = priceListDAO.getLatestPriceForDrink(d.getDrinkId());
                items.add(new CatalogItem(
                        d.getDrinkId(),
                        d.getName(),
                        d.getDescription(),
                        (int) price,
                        "drink",
                        d.getImageUrl()
                ));
            }
            requireActivity().runOnUiThread(() -> catalogAdapter.setItems(items));
        }).start();
    }

    private void loadDishes(String query) {
        new Thread(() -> {
            List<Dish> list = query.isEmpty()
                    ? dishDAO.getAllDishes()
                    : dishDAO.searchDishesByName("%" + query + "%");
            List<CatalogItem> items = new ArrayList<>();
            for (Dish d : list) {
                float price = priceListDAO.getLatestPriceForDish(d.getDishId());
                items.add(new CatalogItem(
                        d.getDishId(),
                        d.getName(),
                        d.getDescription(),
                        (int) price,
                        "dish",
                        d.getImageUrl()
                ));
            }
            requireActivity().runOnUiThread(() -> catalogAdapter.setItems(items));
        }).start();
    }

    private void loadDesserts(String query) {
        new Thread(() -> {
            List<Dessert> list = query.isEmpty()
                    ? dessertDAO.getAllDesserts()
                    : dessertDAO.searchDessertsByName("%" + query + "%");
            List<CatalogItem> items = new ArrayList<>();
            for (Dessert d : list) {
                float price = priceListDAO.getLatestPriceForDessert(d.getDessertId());
                items.add(new CatalogItem(
                        d.getDessertId(),
                        d.getName(),
                        d.getDescription(),
                        (int) price,
                        "dessert",
                        d.getImageUrl()
                ));
            }
            requireActivity().runOnUiThread(() -> catalogAdapter.setItems(items));
        }).start();
    }
}
