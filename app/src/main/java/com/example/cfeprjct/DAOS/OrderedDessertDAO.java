// OrderedDessertDAO.java
package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.cfeprjct.Entities.OrderedDessert;

import java.util.List;

@Dao
public interface OrderedDessertDAO {
    @Insert
    long insert(OrderedDessert od);

    @Query("SELECT * FROM ordered_desserts WHERE orderId = :orderId")
    List<OrderedDessert> getOrderedDessertsByOrderId(int orderId);
}
