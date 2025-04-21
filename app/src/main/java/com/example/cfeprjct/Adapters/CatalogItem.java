// File: com/example/cfeprjct/Adapters/CatalogItem.java
package com.example.cfeprjct.Adapters;

public class CatalogItem {
    private final int id;
    private final String title;
    private final String description;
    private final int price;
    private final String category;
    private final String imageUrl;


    public CatalogItem(int id,
                       String title,
                       String description,
                       int price,
                       String category,
                       String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
