package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ordered_desserts")
public class OrderedDessert {
    @PrimaryKey(autoGenerate = true)
    private int orderedDessertId;

    private int orderId;   // id_заказа
    private int dessertId; // id_десерта

    // Геттеры и сеттеры
    public int getOrderedDessertId() { return orderedDessertId; }
    public void setOrderedDessertId(int orderedDessertId) { this.orderedDessertId = orderedDessertId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getDessertId() { return dessertId; }
    public void setDessertId(int dessertId) { this.dessertId = dessertId; }
}
