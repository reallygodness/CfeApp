package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "volumes")
public class Volume {
    @PrimaryKey(autoGenerate = true)
    private int volumeId;

    private String volume; // Например "250 мл", "500 мл" и т.д.

    // Геттеры и сеттеры
    public int getVolumeId() { return volumeId; }
    public void setVolumeId(int volumeId) { this.volumeId = volumeId; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
}
