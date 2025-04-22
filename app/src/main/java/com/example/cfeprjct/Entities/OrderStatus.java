package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_status")
public class OrderStatus {
    @PrimaryKey(autoGenerate = true)
    private int orderStatusId;

    private String statusName; // Название статуса

    // Геттеры и сеттеры
    public int getOrderStatusId() { return orderStatusId; }
    public void setOrderStatusId(int orderStatusId) { this.orderStatusId = orderStatusId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
