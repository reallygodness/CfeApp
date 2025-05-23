package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.PhoneNumberTextWatcher;
import com.example.cfeprjct.R;
<<<<<<< HEAD
=======
import com.example.cfeprjct.User;
>>>>>>> d8c31960397f119aba61d2c43f1a411476b6f838
import com.example.cfeprjct.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText phoneEditText, passwordEditText;
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
        togglePassword = findViewById(R.id.togglePassword);

        // Добавляем маску для номера телефона
        phoneEditText.addTextChangedListener(new PhoneNumberTextWatcher(phoneEditText));

        userRepository = new UserRepository(this);

        // Переход к регистрации
        TextView registerLink = findViewById(R.id.btnreg);
        if (registerLink != null) {
            registerLink.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        // Переход к восстановлению пароля
        forgotpass.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        togglePassword.setOnClickListener(view -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
    }

    // Метод для авторизации: пользователь вводит номер телефона и пароль
    public void login(View view) {
        // Получаем номер телефона (оставляем только цифры)
        String phoneNumber = phoneEditText.getText().toString().trim().replaceAll("\\D", "");
        // Получаем пароль
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
<<<<<<< HEAD
            userRepository.loginUser(phoneNumber, password, new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(String userId) {
                    // Сохраняем состояние входа по userId
                    AuthUtils.setLoggedIn(LoginActivity.this, true, userId);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                        // Передаём userId в MainActivity для загрузки данных пользователя по ключу
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                    );
=======
            new UserRepository(this).loginUser(phoneNumber, password, new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    // теперь у вас есть весь User, включая user.getUserId() и user.getRoleId()
                    if (user.getRoleId() == 2) {
                        startActivity(new Intent(LoginActivity.this, CourierMainActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
>>>>>>> d8c31960397f119aba61d2c43f1a411476b6f838
                }
            });
        }).start();
    }
}
