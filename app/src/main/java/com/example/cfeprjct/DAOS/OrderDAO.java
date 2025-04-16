package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Order;

import java.util.List;

@Dao
public interface OrderDAO {

    @Insert
    long insertOrder(Order order);

    @Update
    void updateOrder(Order order);

    @Delete
    void deleteOrder(Order order);

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    Order getOrderById(int orderId);

    @Query("SELECT * FROM orders WHERE userId = :userId")
    List<Order> getOrdersByUserId(String userId);
}