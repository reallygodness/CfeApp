package com.example.cfeprjct;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

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

    @Query("DELETE FROM users WHERE phoneNumber = :phoneNumber")
    void deleteUserByPhoneNumber(String phoneNumber);

    @Query("SELECT * FROM users")
    List<User> getAllUsers(); // Получить всех пользователей

    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUserById(String userId);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User getUserById(String userId);



}
