package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.OrderedDessert;

import java.util.List;

@Dao
public interface OrderedDessertDAO {

    @Insert
    long insertOrderedDessert(OrderedDessert orderedDessert);

    @Update
    void updateOrderedDessert(OrderedDessert orderedDessert);

    @Delete
    void deleteOrderedDessert(OrderedDessert orderedDessert);

    @Query("SELECT * FROM ordered_desserts WHERE orderedDessertId = :id")
    OrderedDessert getOrderedDessertById(int id);

    @Query("SELECT * FROM ordered_desserts WHERE orderId = :orderId")
    List<OrderedDessert> getOrderedDessertsByOrderId(int orderId);
}