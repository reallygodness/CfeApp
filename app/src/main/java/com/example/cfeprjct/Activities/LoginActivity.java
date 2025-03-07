package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    private TextView forgotpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneEditText = findViewById(R.id.phoneNumber);
        passwordEditText = findViewById(R.id.password);
        forgotpass = findViewById(R.id.forgotpassword);

        // Добавляем маску для номера телефона
        phoneEditText.addTextChangedListener(new PhoneNumberTextWatcher(phoneEditText));

        togglePassword = findViewById(R.id.togglePassword);

        // Переход к регистрации
        db = AppDatabase.getInstance(this);


        TextView registerLink = findViewById(R.id.btnreg);
        if (registerLink != null) {
            registerLink.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        togglePassword.setOnClickListener(view -> {
            if (isPasswordVisible) {
                // Скрываем пароль
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Показываем пароль
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.getText().length()); // Ставим курсор в конец
        });


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