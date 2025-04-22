package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int orderId;

    private String userId;   // id_пользователя
    private long orderDate;  // Дата заказа (в миллисекундах)
    private float totalAmount; // Сумма
    private int orderStatusId; // id_статуса заказа

    // Геттеры и сеттеры
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getOrderDate() { return orderDate; }
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }
    public float getTotalAmount() { return totalAmount; }
    public void setTotalAmount(float totalAmount) { this.totalAmount = totalAmount; }
    public int getOrderStatusId() { return orderStatusId; }
    public void setOrderStatusId(int orderStatusId) { this.orderStatusId = orderStatusId; }
}
