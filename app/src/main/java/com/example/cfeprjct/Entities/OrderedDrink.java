package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ordered_drinks")
public class OrderedDrink {
    @PrimaryKey(autoGenerate = true)
    private int orderedDrinkId;

    private int orderId;   // id_заказа
    private int drinkId;   // id_напитка
    private int quantity;  // Количество

    // Геттеры и сеттеры
    public int getOrderedDrinkId() { return orderedDrinkId; }
    public void setOrderedDrinkId(int orderedDrinkId) { this.orderedDrinkId = orderedDrinkId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getDrinkId() { return drinkId; }
    public void setDrinkId(int drinkId) { this.drinkId = drinkId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
