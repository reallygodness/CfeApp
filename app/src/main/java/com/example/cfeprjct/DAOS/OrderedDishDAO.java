package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.OrderedDish;

import java.util.List;

@Dao
public interface OrderedDishDAO {

    @Insert
    long insertOrderedDish(OrderedDish orderedDish);

    @Update
    void updateOrderedDish(OrderedDish orderedDish);

    @Delete
    void deleteOrderedDish(OrderedDish orderedDish);

    @Query("SELECT * FROM ordered_dishes WHERE orderedDishId = :id")
    OrderedDish getOrderedDishById(int id);

    @Query("SELECT * FROM ordered_dishes WHERE orderId = :orderId")
    List<OrderedDish> getOrderedDishesByOrderId(int orderId);
}