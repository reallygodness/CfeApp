package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Dish;

import java.util.List;

@Dao
public interface DishDAO {

    @Insert
    long insertDish(Dish dish);

    @Update
    void updateDish(Dish dish);

    @Delete
    void deleteDish(Dish dish);

    @Query("SELECT * FROM dishes WHERE dishId = :id")
    Dish getDishById(int id);

    @Query("SELECT * FROM dishes")
    List<Dish> getAllDishes();
}