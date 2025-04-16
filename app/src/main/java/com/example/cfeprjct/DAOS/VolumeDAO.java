package com.example.cfeprjct.DAOS;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cfeprjct.Entities.Volume;

import java.util.List;

@Dao
public interface VolumeDAO {

    @Insert
    long insertVolume(Volume volume);

    @Update
    void updateVolume(Volume volume);

    @Delete
    void deleteVolume(Volume volume);

    @Query("SELECT * FROM volumes WHERE volumeId = :id")
    Volume getVolumeById(int id);

    @Query("SELECT * FROM volumes")
    List<Volume> getAllVolumes();
}