package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Address;

import java.util.List;

@Dao
public interface AddressDAO {
    @Insert
    long insertAddress(Address address);

    @Update
    void updateAddress(Address address);

    @Delete
    void deleteAddress(Address address);

    @Query("SELECT * FROM addresses WHERE addressId = :addressId")
    Address getAddressById(int addressId);

    @Query("SELECT * FROM addresses WHERE userId = :userId")
    List<Address> getAddressesByUserId(String userId);

}
