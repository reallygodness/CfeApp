package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drinks")
public class Drink {
    @PrimaryKey(autoGenerate = true)
    private int drinkId;

    private String name;        // Название
    private String description; // Описание
    private int volumeId;       // Объем_id

    // Геттеры и сеттеры
    public int getDrinkId() { return drinkId; }
    public void setDrinkId(int drinkId) { this.drinkId = drinkId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getVolumeId() { return volumeId; }
    public void setVolumeId(int volumeId) { this.volumeId = volumeId; }
}
