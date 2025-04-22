package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.OrderedDrink;

import java.util.List;

@Dao
public interface OrderedDrinkDAO {

    @Insert
    long insertOrderedDrink(OrderedDrink orderedDrink);

    @Update
    void updateOrderedDrink(OrderedDrink orderedDrink);

    @Delete
    void deleteOrderedDrink(OrderedDrink orderedDrink);

    @Query("SELECT * FROM ordered_drinks WHERE orderedDrinkId = :id")
    OrderedDrink getOrderedDrinkById(int id);

    @Query("SELECT * FROM ordered_drinks WHERE orderId = :orderId")
    List<OrderedDrink> getOrderedDrinksByOrderId(int orderId);
}