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

        firestore = FirebaseFirestore.getInstance();
        localDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries() // Для тестов, лучше делать в отдельном потоке
                .build();

        syncUsersToFirestore(); // Запуск переноса данных
    }

    private void syncUsersToFirestore() {
        new Thread(() -> {
            List<User> users = localDb.userDAO().getAllUsers(); // Получаем всех пользователей из Room
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("email", user.getEmail());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("password", user.getPassword());

                if (user.getProfileImage() != null) {
                    String base64Image = encodeToBase64(user.getProfileImage()); // Конвертируем в Base64
                    userData.put("profileImage", base64Image);
                }

                firestore.collection("users")
                        .document(user.getPhoneNumber()) // Документ = номер телефона (уникальный ID)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "User uploaded: " + user.getPhoneNumber()))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error uploading user", e));
            }
        }).start();
    }

    private String encodeToBase64(byte[] imageBytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
