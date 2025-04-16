package com.example.cfeprjct.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "desserts")
public class Dessert {
    @PrimaryKey(autoGenerate = true)
    private int dessertId;

    private String name;        // Название
    private String description; // Описание

    // Геттеры и сеттеры
    public int getDessertId() { return dessertId; }
    public void setDessertId(int dessertId) { this.dessertId = dessertId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
