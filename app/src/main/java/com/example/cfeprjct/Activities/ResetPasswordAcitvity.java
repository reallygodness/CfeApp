package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;
import com.example.cfeprjct.UserDAO;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.PasswordUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class ResetPasswordAcitvity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetPasswordButton;
    private AppDatabase database;

    private TextView requirementLength, requirementUpperLower, requirementDigit, requirementSpecial, requirementNoSpaces;
    private String email;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_acitvity);

        database = AppDatabase.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        requirementLength = findViewById(R.id.requirementLength);
        requirementUpperLower = findViewById(R.id.requirementUpperLower);
        requirementDigit = findViewById(R.id.requirementDigit);
        requirementSpecial = findViewById(R.id.requirementSpecial);
        requirementNoSpaces = findViewById(R.id.requirementNoSpaces);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        email = getIntent().getStringExtra("email");

        resetPasswordButton.setOnClickListener(v -> updatePassword());

        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordValidation(s.toString());
            }
        });
    }

    private void updatePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(newPassword)) {
            Toast.makeText(ResetPasswordAcitvity.this, "Пароль не соответствует требованиям", Toast.LENGTH_LONG).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            UserDAO userDAO = AppDatabase.getInstance(getApplicationContext()).userDAO();
            // Ищем пользователя по email
            User user = userDAO.getUserByEmail(email);
            if (user != null) {
                try {
                    // Генерируем соль и вычисляем хэш нового пароля
                    byte[] salt = PasswordUtils.generateSalt();
                    String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);

                    // Обновляем пароль в локальной базе
                    user.setPassword(hashedPassword);
                    userDAO.updateUser(user);
                    // Удаляем код восстановления
                    userDAO.clearResetCode(email);

                    // Обновляем пароль в Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("password", hashedPassword);

                    firestore.collection("users")
                            .document(user.getUserId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                                Toast.makeText(ResetPasswordAcitvity.this, "Пароль успешно изменен!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPasswordAcitvity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }))
                            .addOnFailureListener(e -> runOnUiThread(() ->
                                    Toast.makeText(ResetPasswordAcitvity.this, "Ошибка обновления в Firestore", Toast.LENGTH_SHORT).show()
                            ));
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ResetPasswordAcitvity.this, "Ошибка хэширования: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(ResetPasswordAcitvity.this, "Ошибка обновления пароля!", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void updatePasswordValidation(String password) {
        // Длина пароля
        if (password.length() >= 8 && password.length() <= 128) {
            requirementLength.setTextColor(Color.GREEN);
        } else {
            requirementLength.setTextColor(Color.RED);
        }
        // Заглавная и строчная буква
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) {
            requirementUpperLower.setTextColor(Color.GREEN);
        } else {
            requirementUpperLower.setTextColor(Color.RED);
        }
        // Хотя бы одна цифра
        if (password.matches(".*\\d.*")) {
            requirementDigit.setTextColor(Color.GREEN);
        } else {
            requirementDigit.setTextColor(Color.RED);
        }
        // Хотя бы один спецсимвол
        if (password.matches(".*[@#$%^&+=!].*")) {
            requirementSpecial.setTextColor(Color.GREEN);
        } else {
            requirementSpecial.setTextColor(Color.RED);
        }
        // Без пробелов
        if (!password.contains(" ")) {
            requirementNoSpaces.setTextColor(Color.GREEN);
        } else {
            requirementNoSpaces.setTextColor(Color.RED);
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 128 &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[@#$%^&+=!].*") &&
                !password.contains(" ");
    }
}
