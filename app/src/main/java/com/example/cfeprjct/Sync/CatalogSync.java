package com.example.cfeprjct.Sync;

import android.content.Context;
import android.util.Log;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.Entities.Dessert;
import com.example.cfeprjct.Entities.Dish;
import com.example.cfeprjct.Entities.Drink;
import com.example.cfeprjct.Entities.PriceList;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CatalogSync {
    private final AppDatabase db;
    private final FirebaseFirestore firestore;

    public interface Callback {
        void onComplete();
    }

    public CatalogSync(Context ctx) {
        db = AppDatabase.getInstance(ctx.getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
    }

    /** 1) Синхронизируем напитки, а затем цены */
    public void syncDrinks(Callback cb) {
        firestore.collection("drinks")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Drink> drinks = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        Drink d = new Drink();
                        try {
                            d.setDrinkId(Integer.parseInt(doc.getId()));
                        } catch (NumberFormatException e) {
                            Log.w("CatalogSync", "Неверный формат id напитка: " + doc.getId());
                            continue;
                        }
                        d.setName(doc.getString("name"));
                        d.setDescription(doc.getString("description"));
                        d.setImageUrl(doc.getString("imageUrl"));
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> vols = (List<Map<String, Object>>) doc.get("volumes");
                        if (vols != null && !vols.isEmpty()) {
                            Object vid = vols.get(0).get("volumeId");
                            if (vid instanceof Number) {
                                d.setVolumeId(((Number) vid).intValue());
                            }
                        }
                        drinks.add(d);
                    }
                    new Thread(() -> {
                        db.drinkDAO().insertAll(drinks);
                        // только после вставки напитков — синхронизируем цены
                        syncPrices(cb);
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("CatalogSync", "Ошибка загрузки drinks", e);
                    // даже если напитки не прилетели — пробуем обновить цены и вернуть колбэк
                    syncPrices(cb);
                });
    }

    /** 2) Синхронизируем блюда */
    public void syncDishes(Callback cb) {
        firestore.collection("dishes")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Dish> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        Dish d = new Dish();
                        try {
                            d.setDishId(Integer.parseInt(doc.getId()));
                        } catch (NumberFormatException e) {
                            Log.w("CatalogSync", "Неверный формат id блюда: " + doc.getId());
                            continue;
                        }
                        d.setName(doc.getString("name"));
                        d.setDescription(doc.getString("description"));
                        d.setImageUrl(doc.getString("imageUrl"));
                        list.add(d);
                    }
                    new Thread(() -> {
                        db.dishDAO().insertAll(list);
                        Log.d("CatalogSync", "Room: записано блюд = " + list.size());
                        cb.onComplete();
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("CatalogSync", "Ошибка загрузки dishes", e);
                    cb.onComplete();
                });
    }

    /** 3) Синхронизируем десерты */
    public void syncDesserts(Callback cb) {
        firestore.collection("desserts")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Dessert> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        Dessert d = new Dessert();
                        try {
                            d.setDessertId(Integer.parseInt(doc.getId()));
                        } catch (NumberFormatException e) {
                            Log.w("CatalogSync", "Неверный формат id десерта: " + doc.getId());
                            continue;
                        }
                        d.setName(doc.getString("name"));
                        d.setDescription(doc.getString("description"));
                        d.setImageUrl(doc.getString("imageUrl"));
                        list.add(d);
                    }
                    new Thread(() -> {
                        db.dessertDAO().insertAll(list);
                        Log.d("CatalogSync", "Room: записано десертов = " + list.size());
                        cb.onComplete();
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("CatalogSync", "Ошибка загрузки desserts", e);
                    cb.onComplete();
                });
    }

    /**
     * 4) Синхронизируем прайс-лист.
     * Только вызывается из syncDrinks, чтобы гарантировать, что
     * и напитки, и цены прилетели до того, как UI перерисует список.
     */
    private void syncPrices(Callback cb) {
        firestore.collection("price_list")
                .get()
                .addOnSuccessListener(qs -> {
                    List<PriceList> prices = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        PriceList p = new PriceList();

                        Long itemIdLong = doc.getLong("itemId");
                        String itemType = doc.getString("itemType");
                        Double priceD   = doc.getDouble("price");
                        if (itemIdLong == null || itemType == null || priceD == null) {
                            continue;
                        }
                        switch (itemType) {
                            case "drink":   p.setDrinkId(itemIdLong.intValue());   break;
                            case "dish":    p.setDishId(itemIdLong.intValue());    break;
                            case "dessert": p.setDessertId(itemIdLong.intValue()); break;
                            default: continue;
                        }
                        p.setPrice(priceD.floatValue());

                        // читаем дату: сначала пробуем .getDate(), потом .get("date")
                        Date dt = doc.getDate("date");
                        if (dt != null) {
                            p.setDate(dt.getTime());
                        } else {
                            Object raw = doc.get("date");
                            if (raw instanceof Number) {
                                p.setDate(((Number) raw).longValue());
                            } else {
                                p.setDate(System.currentTimeMillis());
                            }
                        }
                        prices.add(p);
                    }
                    new Thread(() -> {
                        db.priceListDAO().insertAll(prices);
                        Log.d("CatalogSync", "Room: записано цен = " + prices.size());
                        cb.onComplete();
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Log.e("CatalogSync", "Ошибка загрузки price_list", e);
                    cb.onComplete();
                });
    }
}
