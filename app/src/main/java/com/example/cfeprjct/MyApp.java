package com.example.cfeprjct;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.room.Room;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyApp extends Application {
    private FirebaseFirestore firestore;
    private AppDatabase localDb;

    @Override
    public void onCreate() {
        super.onCreate();

        // Инициализируем Firestore
        firestore = FirebaseFirestore.getInstance();

        // Инициализируем локальную базу данных (Room)
        localDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();

        syncUsersToFirestore(); // Запускаем синхронизацию данных при старте приложения
    }

    private void syncUsersToFirestore() {
        new Thread(() -> {
            // Получаем локальную базу и всех пользователей
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<User> users = db.userDAO().getAllUsers();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            for (User user : users) {
                // Проверяем, что userId корректно задан
                if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
                    Log.w("Firestore", "❗ Пропущен пользователь с пустым userId.");
                    continue;
                }

                // Готовим карту данных для Firestore
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("firstName", user.getFirstName());
                userMap.put("lastName", user.getLastName());
                userMap.put("email", user.getEmail());
                userMap.put("phoneNumber", user.getPhoneNumber());

                // Добавляем пароль, если он установлен (хэшированная строка)
                if (user.getPassword() != null) {
                    userMap.put("password", user.getPassword());
                }
                // Если есть изображение профиля, кодируем в Base64
                if (user.getProfileImage() != null) {
                    userMap.put("profileImage", Base64.encodeToString(user.getProfileImage(), Base64.DEFAULT));
                }

                firestore.collection("users").document(user.getUserId())
                        .set(userMap)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Пользователь синхронизирован: " + user.getUserId()))
                        .addOnFailureListener(e -> Log.e("Firestore", "❌ Ошибка синхронизации", e));
            }
        }).start();
    }

    // Вспомогательный метод для кодирования изображения в Base64 (если потребуется)
    private String encodeToBase64(byte[] imageBytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
