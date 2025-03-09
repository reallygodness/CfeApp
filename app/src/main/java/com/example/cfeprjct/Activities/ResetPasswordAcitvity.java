package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;
import com.example.cfeprjct.UserDAO;

public class ResetPasswordAcitvity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetPasswordButton;
    private AppDatabase database;

    private TextView requirementLength, requirementUpperLower, requirementDigit, requirementSpecial, requirementNoSpaces;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_acitvity);

        database = AppDatabase.getInstance(this);

        requirementLength = findViewById(R.id.requirementLength);
        requirementUpperLower = findViewById(R.id.requirementUpperLower);
        requirementDigit = findViewById(R.id.requirementDigit);
        requirementSpecial = findViewById(R.id.requirementSpecial);
        requirementNoSpaces = findViewById(R.id.requirementNoSpaces);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        email = getIntent().getStringExtra("email");

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordValidation(s.toString());
            }
        });
    }

//    private void resetPassword() {
//        String newPassword = newPasswordEditText.getText().toString();
//        String confirmPassword = confirmPasswordEditText.getText().toString();
//
//        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
//            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!newPassword.equals(confirmPassword)) {
//            Toast.makeText(this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        User user = database.userDAO().getUserByEmail(email);
//
//        if (user != null) {
//            user.setPassword(newPassword);
//            database.userDAO().updateUser(user);
//            database.userDAO().clearResetCode(email); // Удаляем код
//
//            Toast.makeText(this, "Пароль успешно изменён!", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(ResetPasswordAcitvity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        } else {
//            Toast.makeText(this, "Ошибка обновления пароля!", Toast.LENGTH_SHORT).show();
//        }
//    }

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
            userDAO.updatePassword(email, newPassword);

            runOnUiThread(() -> {
                Toast.makeText(ResetPasswordAcitvity.this, "Пароль успешно изменен!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordAcitvity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
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