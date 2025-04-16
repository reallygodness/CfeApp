package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ordered_dishes")
public class OrderedDish {
    @PrimaryKey(autoGenerate = true)
    private int orderedDishId;

    private int orderId; // id_заказа
    private int dishId;  // id_блюда

    // Геттеры и сеттеры
    public int getOrderedDishId() { return orderedDishId; }
    public void setOrderedDishId(int orderedDishId) { this.orderedDishId = orderedDishId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
}
