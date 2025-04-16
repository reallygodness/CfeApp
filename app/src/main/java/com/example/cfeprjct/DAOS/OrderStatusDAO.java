package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.OrderStatus;

import java.util.List;

@Dao
public interface OrderStatusDAO {

    @Insert
    long insertOrderStatus(OrderStatus orderStatus);

    @Update
    void updateOrderStatus(OrderStatus orderStatus);

    @Delete
    void deleteOrderStatus(OrderStatus orderStatus);

    @Query("SELECT * FROM order_status WHERE orderStatusId = :id")
    OrderStatus getOrderStatusById(int id);

    @Query("SELECT * FROM order_status")
    List<OrderStatus> getAllOrderStatuses();
}
