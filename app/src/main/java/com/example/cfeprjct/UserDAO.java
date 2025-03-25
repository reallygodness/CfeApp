package com.example.cfeprjct;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDAO {
    @Insert
    long insertUser(User user);  // Метод для добавления пользователя

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password LIMIT 1")
    User authenticateUser(String phoneNumber, String password);  // Метод для аутентификации пользователя

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    User getUserByPhoneNumber(String phoneNumber);  // Метод для проверки наличия пользователя по номеру телефона

    @Update
    void updateUser(User user);  // Этот метод будет использоваться для обновления данных пользователя

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    User getUserByPhone(String phone);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("UPDATE users SET resetCode = :code WHERE email = :email")
    void updateResetCode(String email, String code);

    @Query("SELECT * FROM users WHERE email = :email AND resetCode = :code LIMIT 1")
    User verifyResetCode(String email, String code);

    @Query("UPDATE users SET resetCode = NULL WHERE email = :email")
    void clearResetCode(String email);

    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);

    @Query("UPDATE users SET profileImage = :image WHERE phoneNumber = :phone")
    void updateProfileImage(String phone, byte[] image);

    @Query("SELECT * FROM users")
    List<User> getAllUsers(); // Получить всех пользователей




}
