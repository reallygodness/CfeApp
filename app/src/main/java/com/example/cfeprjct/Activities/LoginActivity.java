package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfeprjct.Activities.RegisterActivity;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.PhoneNumberTextWatcher;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneEditText, passwordEditText;
    private AppDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneEditText = findViewById(R.id.phoneNumber);
        passwordEditText = findViewById(R.id.password);

        // Добавляем маску для номера телефона
        phoneEditText.addTextChangedListener(new PhoneNumberTextWatcher(phoneEditText));


        // Переход к регистрации
        db = AppDatabase.getInstance(this);


        TextView registerLink = findViewById(R.id.registerLink);
        if (registerLink != null) {
            registerLink.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

    }

    // Метод для авторизации
    public void login(View view) {
        String phoneNumber = phoneEditText.getText().toString().trim().replaceAll("\\D", ""); // Убираем символы
        String password = passwordEditText.getText().toString().trim();

        if (phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите номер телефона и пароль!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phoneNumber.startsWith("7") || phoneNumber.length() != 11) {
            Toast.makeText(this, "Введите корректный номер телефона!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDAO().getUserByPhoneNumber(phoneNumber);

            if (user != null && user.getPassword().equals(password)) {
                // Если данные правильные, сохраняем информацию о пользователе
                AuthUtils.setLoggedIn(LoginActivity.this, true, phoneNumber);
                // Переходим в профиль
                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);  // Передаем номер телефона для получения данных профиля
                startActivity(intent);
                finish();
            } else {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Неверный номер телефона или пароль", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}