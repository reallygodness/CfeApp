package com.example.cfeprjct.DAOS;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Review;

import java.util.List;

@Dao
public interface ReviewDAO {

    // вставка одного отзыва (или обновление, если уже есть с таким ключом)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Review review);

    // (если вам нужен массовый импорт)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Review> reviews);

    // получить отзывы для напитка
    @Query("SELECT * FROM reviews WHERE drinkId = :id ORDER BY reviewDate DESC")
    LiveData<List<Review>> getReviewsForDrinkId(int id);

    // для блюда
    @Query("SELECT * FROM reviews WHERE dishId = :id ORDER BY reviewDate DESC")
    LiveData<List<Review>> getReviewsForDishId(int id);

    // для десерта
    @Query("SELECT * FROM reviews WHERE dessertId = :id ORDER BY reviewDate DESC")
    LiveData<List<Review>> getReviewsForDessertId(int id);

    // очистить все отзывы (опционально)
    @Query("DELETE FROM reviews")
    void clearAll();

}