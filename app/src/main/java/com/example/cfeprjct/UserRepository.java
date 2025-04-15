package com.example.cfeprjct;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.room.Room;

import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserRepository {

    private final AppDatabase db;
    private final UserDAO userDAO;
    private final FirebaseFirestore firestore;

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String errorMessage);
    }

    public UserRepository(Context context) {
        // Инициализируем локальную базу данных и DAO
        db = AppDatabase.getInstance(context.getApplicationContext());
        userDAO = db.userDAO();
        // Инициализируем Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Регистрация пользователя.
     * Перед сохранением пароль хэшируется с использованием PBKDF2, а userId генерируется автоматически.
     */
    public void registerUser(String firstName, String lastName, String email, String phone, String password, AuthCallback callback) {
        new Thread(() -> {
            if (userDAO.getUserByEmail(email) != null) {
                callback.onFailure("Пользователь с таким email уже зарегистрирован!");
                return;
            }
            if (userDAO.getUserByPhoneNumber(phone) != null) {
                callback.onFailure("Пользователь с таким номером уже зарегистрирован!");
                return;
            }
            try {
                // Генерируем уникальный userId
                String userId = UUID.randomUUID().toString();
                // Генерируем соль и хэшируем пароль
                byte[] salt = PasswordUtils.generateSalt();
                String hashedPassword = PasswordUtils.hashPassword(password, salt);
                // Создаем нового пользователя с хэшированным паролем
                User newUser = new User(userId, firstName, lastName, email, phone);
                newUser.setPassword(hashedPassword);
                userDAO.insertUser(newUser);  // Сохраняем в Room

                // Подготавливаем данные для Firestore
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", newUser.getUserId());
                userMap.put("firstName", newUser.getFirstName());
                userMap.put("lastName", newUser.getLastName());
                userMap.put("email", newUser.getEmail());
                userMap.put("phoneNumber", newUser.getPhoneNumber());
                userMap.put("password", newUser.getPassword()); // Сохраняем хэш пароля
                if (newUser.getProfileImage() != null) {
                    userMap.put("profileImage", Base64.encodeToString(newUser.getProfileImage(), Base64.DEFAULT));
                }

                firestore.collection("users").document(newUser.getUserId())
                        .set(userMap)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("UserRepository", "Пользователь успешно зарегистрирован в Firestore");
                            callback.onSuccess(newUser.getUserId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UserRepository", "Ошибка регистрации в Firestore", e);
                            callback.onFailure("Ошибка регистрации в Firestore: " + e.getMessage());
                        });
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                callback.onFailure("Ошибка хэширования пароля: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Авторизация пользователя.
     * По номеру телефона ищется пользователь, а затем проверяется введенный пароль через PBKDF2.
     */
    public void loginUser(String phoneNumber, String password, AuthCallback callback) {
        new Thread(() -> {
            // Пытаемся найти пользователя в локальной базе по номеру телефона.
            User user = userDAO.getUserByPhoneNumber(phoneNumber);
            if (user != null) {
                try {
                    if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                        callback.onSuccess(user.getUserId());
                    } else {
                        callback.onFailure("Неверный номер телефона или пароль");
                    }
                } catch (Exception e) {
                    callback.onFailure("Ошибка проверки пароля: " + e.getMessage());
                }
            } else {
                // Если пользователя нет в локальной базе – можно добавить логику загрузки из Firestore, если требуется.
                callback.onFailure("Пользователь не найден");
            }
        }).start();
    }
}
