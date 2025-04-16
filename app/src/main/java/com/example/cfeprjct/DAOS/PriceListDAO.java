package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.PriceList;

import java.util.List;

@Dao
public interface PriceListDAO {

    @Insert
    long insertPriceList(PriceList priceList);

    @Update
    void updatePriceList(PriceList priceList);

    @Delete
    void deletePriceList(PriceList priceList);

    @Query("SELECT * FROM pricelist WHERE priceListId = :id")
    PriceList getPriceListById(int id);

    @Query("SELECT * FROM pricelist")
    List<PriceList> getAllPriceLists();
}
