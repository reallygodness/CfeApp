package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pricelist")
public class PriceList {
    @PrimaryKey(autoGenerate = true)
    private int priceListId;

    private Integer drinkId;   // id_напитка (может быть null, если используется для блюд или десертов)
    private Integer dishId;    // id_блюда
    private Integer dessertId; // id_десерта
    private float price;
    private long date;         // Дата (например, время обновления прайс-листа)

    // Геттеры и сеттеры
    public int getPriceListId() { return priceListId; }
    public void setPriceListId(int priceListId) { this.priceListId = priceListId; }
    public Integer getDrinkId() { return drinkId; }
    public void setDrinkId(Integer drinkId) { this.drinkId = drinkId; }
    public Integer getDishId() { return dishId; }
    public void setDishId(Integer dishId) { this.dishId = dishId; }
    public Integer getDessertId() { return dessertId; }
    public void setDessertId(Integer dessertId) { this.dessertId = dessertId; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
}
