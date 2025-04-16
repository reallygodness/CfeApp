package com.example.cfeprjct.DAOS;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Drink;

import java.util.List;

@Dao
public interface DrinkDAO {

    @Insert
    long insertDrink(Drink drink);

    @Update
    void updateDrink(Drink drink);

    @Delete
    void deleteDrink(Drink drink);

    @Query("SELECT * FROM drinks WHERE drinkId = :id")
    Drink getDrinkById(int id);

    @Query("SELECT * FROM drinks")
    List<Drink> getAllDrinks();
}