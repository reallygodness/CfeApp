package com.example.cfeprjct.DAOS;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Order;

import java.util.List;

@Dao
public interface OrderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrder(Order order);

    @Update
    void updateOrder(Order order);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllLiveOrders();

    @Delete
    void deleteOrder(Order order);

    @Query("SELECT * FROM orders WHERE orderId = :id LIMIT 1")
    Order getOrderById(int id);

    @Query("SELECT * FROM orders WHERE userId = :uid ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllByUser(String uid);

    @Query("SELECT * FROM orders WHERE userId = :userId")
    List<Order> getOrdersByUserId(String userId);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByUserIdLive(String userId);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    List<Order> getAllSync();

    @Query("DELETE FROM orders")
    void clearAll();


}