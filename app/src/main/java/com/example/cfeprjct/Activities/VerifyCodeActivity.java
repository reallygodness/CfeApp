package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;
import com.example.cfeprjct.UserDAO;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Button verifyButton;
    private AppDatabase database;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        // Получаем ссылку на локальную базу данных
        database = AppDatabase.getInstance(this);

        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);

        // Получаем email из Intent
        email = getIntent().getStringExtra("email");

        // Обработка клика с использованием лямбды
        verifyButton.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String enteredCode = codeEditText.getText().toString().trim();

        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "Введите код подтверждения", Toast.LENGTH_SHORT).show();
            return;
        }

        // Выполняем проверку в фоновом потоке
        new Thread(() -> {
            UserDAO userDAO = AppDatabase.getInstance(getApplicationContext()).userDAO();
            // Метод verifyResetCode(email, enteredCode) должен вернуть пользователя, если код верный
            User user = userDAO.verifyResetCode(email, enteredCode);

            runOnUiThread(() -> {
                if (user != null) {
                    Toast.makeText(VerifyCodeActivity.this, "Код подтвержден!", Toast.LENGTH_SHORT).show();
                    // Передаем email в ResetPasswordActivity (проверь правильность имени класса)
                    Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordAcitvity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Неверный код", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
