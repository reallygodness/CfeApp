package com.example.cfeprjct;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cfeprjct.DAOS.AddressDAO;
import com.example.cfeprjct.DAOS.CourierDAO;
import com.example.cfeprjct.DAOS.DeliveryDAO;
import com.example.cfeprjct.DAOS.DessertDAO;
import com.example.cfeprjct.DAOS.DishDAO;
import com.example.cfeprjct.DAOS.DrinkDAO;
import com.example.cfeprjct.DAOS.DrinkIngredientDAO;
import com.example.cfeprjct.DAOS.FavoriteDrinkDAO;
import com.example.cfeprjct.DAOS.IngredientDAO;
import com.example.cfeprjct.DAOS.OrderDAO;
import com.example.cfeprjct.DAOS.OrderStatusDAO;
import com.example.cfeprjct.DAOS.OrderedDessertDAO;
import com.example.cfeprjct.DAOS.OrderedDishDAO;
import com.example.cfeprjct.DAOS.OrderedDrinkDAO;
import com.example.cfeprjct.DAOS.PriceListDAO;
import com.example.cfeprjct.DAOS.ReviewDAO;
import com.example.cfeprjct.DAOS.VolumeDAO;
import com.example.cfeprjct.Entities.Address;
import com.example.cfeprjct.Entities.Courier;
import com.example.cfeprjct.Entities.Delivery;
import com.example.cfeprjct.Entities.FavoriteDrink;
import com.example.cfeprjct.Entities.Order;
import com.example.cfeprjct.Entities.OrderedDrink;
import com.example.cfeprjct.Entities.Dessert;
import com.example.cfeprjct.Entities.Dish;
import com.example.cfeprjct.Entities.Drink;
import com.example.cfeprjct.Entities.DrinkIngredient;
import com.example.cfeprjct.Entities.Ingredient;
import com.example.cfeprjct.Entities.OrderStatus;
import com.example.cfeprjct.Entities.OrderedDessert;
import com.example.cfeprjct.Entities.OrderedDish;
import com.example.cfeprjct.Entities.PriceList;
import com.example.cfeprjct.Entities.Review;
import com.example.cfeprjct.Entities.Volume;
import com.example.cfeprjct.User;

@Database(entities = {
        User.class,
        Address.class,
        Delivery.class,
        OrderedDrink.class,
        Order.class,
        FavoriteDrink.class,
        Ingredient.class,
        Courier.class,
        Drink.class,
        DrinkIngredient.class,
        Volume.class,
        Review.class,
        PriceList.class,
        OrderStatus.class,
        Dish.class,
        OrderedDish.class,
        Dessert.class,
        OrderedDessert.class
}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // Определяем DAO для всех сущностей
    public abstract UserDAO userDAO();
    public abstract AddressDAO addressDAO();
    public abstract DeliveryDAO deliveryDAO();
    public abstract OrderedDrinkDAO orderedDrinkDAO();
    public abstract OrderDAO orderDAO();
    public abstract FavoriteDrinkDAO favoriteDrinkDAO();
    public abstract IngredientDAO ingredientDAO();
    public abstract CourierDAO courierDAO();
    public abstract DrinkDAO drinkDAO();
    public abstract DrinkIngredientDAO drinkIngredientDAO();
    public abstract VolumeDAO volumeDAO();
    public abstract ReviewDAO reviewDAO();
    public abstract PriceListDAO priceListDAO();
    public abstract OrderStatusDAO orderStatusDAO();
    public abstract DishDAO dishDAO();
    public abstract OrderedDishDAO orderedDishDAO();
    public abstract DessertDAO dessertDAO();
    public abstract OrderedDessertDAO orderedDessertDAO();

    /**
     * Миграция с версии 4 на 5 для таблицы пользователей.
     * Предполагается, что в предыдущей версии уже используется столбец "userId" (TEXT, non-null, primary key),
     * поэтому здесь мы просто копируем данные в новую таблицу.
     */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Создаем новую таблицу с корректной схемой
            database.execSQL("CREATE TABLE IF NOT EXISTS `users_new` (" +
                    "`userId` TEXT NOT NULL, " +
                    "`firstName` TEXT, " +
                    "`lastName` TEXT, " +
                    "`email` TEXT, " +
                    "`phoneNumber` TEXT, " +
                    "`resetCode` TEXT, " +
                    "`password` TEXT, " +
                    "`profileImage` BLOB, " +
                    "PRIMARY KEY(`userId`))");

            // Копируем данные из старой таблицы в новую (так как в старой версии уже используется userId)
            database.execSQL("INSERT INTO `users_new` (userId, firstName, lastName, email, phoneNumber, resetCode, password, profileImage) " +
                    "SELECT userId, firstName, lastName, email, phoneNumber, resetCode, password, profileImage FROM users");

            // Удаляем старую таблицу
            database.execSQL("DROP TABLE users");

            // Переименовываем новую таблицу в users
            database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .addMigrations(MIGRATION_4_5) // Если миграция вам нужна – удалите fallbackToDestructiveMigration() и раскомментируйте эту строку
                    .fallbackToDestructiveMigration() // Альтернатива: уничтожить данные и создать новую базу
                    .build();
        }
        return instance;
    }
}
