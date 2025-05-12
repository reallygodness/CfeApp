package com.example.cfeprjct;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final AppDatabase db;
    private final UserDAO     userDAO;
    private final FirebaseFirestore firestore;

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String errorMessage);
    }

    public UserRepository(Context context) {
        db       = AppDatabase.getInstance(context.getApplicationContext());
        userDAO  = db.userDAO();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Регистрация пользователя.
     * Пароль хэшируется через PBKDF2, а userId генерируется
     * автоматически Firestore при создании нового документа.
     */
    public void registerUser(String firstName,
                             String lastName,
                             String email,
                             String phone,
                             String password,
                             AuthCallback callback) {

        new Thread(() -> {
            // 1) Проверяем уникальность email и телефона в локальной БД
            if (userDAO.getUserByEmail(email) != null) {
                callback.onFailure("Пользователь с таким email уже есть");
                return;
            }
            if (userDAO.getUserByPhoneNumber(phone) != null) {
                callback.onFailure("Пользователь с таким номером уже есть");
                return;
            }

            firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(emailSnap -> {
                        if (!emailSnap.isEmpty()) {
                            // <-- ИЗМЕНЕНИЕ: нашли совпадение по email в облаке
                            callback.onFailure("Email уже используется в другом аккаунте!");
                            return;
                        }
                        // 3) Проверяем в Firestore по телефону
                        firestore.collection("users")
                                .whereEqualTo("phoneNumber", phone)
                                .get()
                                .addOnSuccessListener(phoneSnap -> {
                                    if (!phoneSnap.isEmpty()) {
                                        // <-- ИЗМЕНЕНИЕ: нашли совпадение по телефону в облаке
                                        callback.onFailure("Номер телефона уже используется в другом аккаунте!");
                                        return;
                                    }

                                    // 4) Если нигде не найдено — продолжаем регистрацию
                                    try {
                                        // Хэшируем пароль
                                        byte[] salt = PasswordUtils.generateSalt();
                                        String hashedPassword = PasswordUtils.hashPassword(password, salt);

                                        // Получаем авто-ID из Firestore
                                        DocumentReference newUserRef = firestore.collection("users").document();
                                        String generatedId = newUserRef.getId();

                                        // Создаём локального пользователя с этим ID
                                        User newUser = new User(firstName, lastName, email, phone, hashedPassword);
                                        newUser.setUserId(generatedId);
                                        userDAO.insertUser(newUser);

                                        // Готовим данные для Firestore
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("userId",      generatedId);
                                        userMap.put("firstName",   firstName);
                                        userMap.put("lastName",    lastName);
                                        userMap.put("email",       email);
                                        userMap.put("phoneNumber", phone);
                                        userMap.put("password",    hashedPassword);
                                        if (newUser.getProfileImage() != null) {
                                            String b64 = Base64.encodeToString(
                                                    newUser.getProfileImage(), Base64.DEFAULT);
                                            userMap.put("profileImage", b64);
                                        }

                                        // Сохраняем в Firestore
                                        newUserRef.set(userMap)
                                                .addOnSuccessListener(aVoid -> callback.onSuccess(generatedId))
                                                .addOnFailureListener(e ->
                                                        callback.onFailure("Ошибка создания аккаунта в облаке: " + e.getMessage())
                                                );

                                    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                                        callback.onFailure("Ошибка хэширования пароля: " + ex.getMessage());
                                    }
                                })
                                .addOnFailureListener(e ->
                                        callback.onFailure("Ошибка проверки телефона в облаке: " + e.getMessage())
                                );
                    })
                    .addOnFailureListener(e ->
                            callback.onFailure("Ошибка проверки email в облаке: " + e.getMessage())
                    );

            try {
                // 2) Хэшируем пароль с солью
                byte[] salt = PasswordUtils.generateSalt();
                String hashedPassword = PasswordUtils.hashPassword(password, salt);

                // 3) Генерируем авто-ID в Firestore
                CollectionReference usersColl = firestore.collection("users");
                DocumentReference newUserRef = usersColl.document();  // <- авто-ID
                String generatedId = newUserRef.getId();

                // 4) Создаём объект User и сохраняем локально
                User newUser = new User(firstName, lastName, email, phone, hashedPassword);
                newUser.setUserId(generatedId);
                userDAO.insertUser(newUser);

                // 5) Собираем поля для Firestore
                Map<String,Object> userMap = new HashMap<>();
                userMap.put("userId",      generatedId);
                userMap.put("firstName",   firstName);
                userMap.put("lastName",    lastName);
                userMap.put("email",       email);
                userMap.put("phoneNumber", phone);
                userMap.put("password",    hashedPassword);
                if (newUser.getProfileImage() != null) {
                    String b64 = Base64.encodeToString(
                            newUser.getProfileImage(),
                            Base64.DEFAULT
                    );
                    userMap.put("profileImage", b64);
                }

                // 6) Записываем в документ с авто-ID
                newUserRef
                        .set(userMap)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("UserRepo", "Успешная регистрация в Firestore");
                            callback.onSuccess(generatedId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UserRepo", "Ошибка записи в Firestore", e);
                            callback.onFailure("Ошибка регистрации: " + e.getMessage());
                        });

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                callback.onFailure("Ошибка хэширования пароля: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Авторизация пользователя. Сначала локально, иначе — из Firestore.
     */
    public void loginUser(String phoneNumber,
                          String password,
                          AuthCallback callback) {
        new Thread(() -> {
            // Локальная попытка
            User local = userDAO.getUserByPhoneNumber(phoneNumber);
            if (local != null) {
                try {
                    if (PasswordUtils.verifyPassword(password, local.getPassword())) {
                        callback.onSuccess(local.getUserId());
                    } else {
                        callback.onFailure("Неверный номер или пароль");
                    }
                } catch (Exception e) {
                    callback.onFailure("Ошибка проверки пароля: " + e.getMessage());
                }
                return;
            }
            // Из облака
            firestore.collection("users")
                    .whereEqualTo("phoneNumber", phoneNumber)
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (qs.isEmpty()) {
                            callback.onFailure("Пользователь не найден");
                            return;
                        }
                        DocumentReference doc = qs.getDocuments().get(0).getReference();
                        doc.get().addOnSuccessListener(snapshot -> {
                            String storedHash = snapshot.getString("password");
                            try {
                                if (!PasswordUtils.verifyPassword(password, storedHash)) {
                                    callback.onFailure("Неверный номер или пароль");
                                    return;
                                }
                                // Собираем объект
                                User cloudUser = new User();
                                cloudUser.setUserId(snapshot.getString("userId"));
                                cloudUser.setFirstName(snapshot.getString("firstName"));
                                cloudUser.setLastName(snapshot.getString("lastName"));
                                cloudUser.setEmail(snapshot.getString("email"));
                                cloudUser.setPhoneNumber(phoneNumber);
                                cloudUser.setPassword(storedHash);
                                String pi = snapshot.getString("profileImage");
                                if (pi != null) {
                                    cloudUser.setProfileImage(
                                            Base64.decode(pi, Base64.DEFAULT)
                                    );
                                }
                                // Сохраняем локально
                                new Thread(() -> {
                                    userDAO.insertUser(cloudUser);
                                    callback.onSuccess(cloudUser.getUserId());
                                }).start();
                            } catch (Exception ex) {
                                callback.onFailure("Ошибка проверки пароля: " + ex.getMessage());
                            }
                        }).addOnFailureListener(e ->
                                callback.onFailure("Ошибка чтения из облака: " + e.getMessage())
                        );
                    })
                    .addOnFailureListener(e ->
                            callback.onFailure("Ошибка соединения: " + e.getMessage())
                    );
        }).start();
    }
}
