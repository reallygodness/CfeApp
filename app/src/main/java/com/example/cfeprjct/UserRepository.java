package com.example.cfeprjct;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final UserDAO userDAO;
    private final FirebaseFirestore firestore;

    public UserRepository(AppDatabase db) {
        this.userDAO = db.userDAO();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /** 🔹 Сохранение пользователя в Firestore */
    public void saveUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("email", user.getEmail());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("password", user.getPassword()); // Пароль только локально!
        userData.put("profileImage", user.getProfileImage()); // Base64 фото

        firestore.collection("users")
                .document(user.getPhoneNumber()) // Уникальный ID = номер телефона
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));
    }

    /** 🔹 Регистрация нового пользователя */
    public void registerUser(User user) {
        userDAO.insertUser(user); // Сохраняем в Room
        saveUserToFirestore(user); // Дублируем в Firestore
    }

    /** 🔹 Обновление данных пользователя */
    public void updateUser(User user) {
        userDAO.updateUser(user); // Обновляем в Room
        saveUserToFirestore(user); // Обновляем в Firestore
    }

    /** 🔹 Загрузка пользователя из Firestore */
    public void loadUserFromFirestore(String phoneNumber, OnUserLoadedCallback callback) {
        firestore.collection("users").document(phoneNumber).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = convertDocumentToUser(document);
                        userDAO.insertUser(user); // Сохраняем в Room
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Пользователь не найден");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** 🔹 Авторизация пользователя */
    public void login(String phoneNumber, String password, OnLoginCallback callback) {
        User user = userDAO.getUserByPhone(phoneNumber);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                callback.onSuccess(user);
            } else {
                callback.onFailure("Неверный пароль");
            }
        } else {
            loadUserFromFirestore(phoneNumber, new OnUserLoadedCallback() {
                @Override
                public void onSuccess(User fetchedUser) {
                    callback.onSuccess(fetchedUser);
                }

                @Override
                public void onFailure(String errorMessage) {
                    callback.onFailure(errorMessage);
                }
            });
        }
    }

    /** 🔹 Конвертация Firestore-данных в User */
    private User convertDocumentToUser(DocumentSnapshot document) {
        return new User(
                document.getString("phoneNumber"),
                document.getString("firstName"),
                document.getString("lastName"),
                document.getString("email"),
                document.getString("profileImage")
        );
    }

    /** 🔹 Интерфейсы для обратного вызова */
    public interface OnUserLoadedCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface OnLoginCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }
}
