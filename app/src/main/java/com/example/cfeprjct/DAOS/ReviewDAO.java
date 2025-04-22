package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Review;

import java.util.List;

@Dao
public interface ReviewDAO {

    @Insert
    long insertReview(Review review);

    @Update
    void updateReview(Review review);

    @Delete
    void deleteReview(Review review);

    @Query("SELECT * FROM reviews WHERE reviewId = :id")
    Review getReviewById(int id);

    @Query("SELECT * FROM reviews WHERE userId = :userId")
    List<Review> getReviewsByUserId(String userId);
}