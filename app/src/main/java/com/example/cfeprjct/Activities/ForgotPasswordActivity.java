package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;
import com.example.cfeprjct.UserDAO;
import com.example.cfeprjct.api.EmailSender;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnsendcode);

        btnResetPassword.setOnClickListener(v -> sendResetCode());
    }

    private void sendResetCode() {
        String email = etEmail.getText().toString().trim();
        if (!AuthUtils.isValidEmail(email)) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Получаем локальную базу и UserDAO
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserDAO userDAO = db.userDAO();
            // Ищем пользователя по email
            User user = userDAO.getUserByEmail(email);

            if (user != null) {
                // Генерируем код восстановления
                String resetCode = AuthUtils.generateResetCode();
                // Обновляем код восстановления в локальной базе (и, возможно, синхронизируем с Firestore, если такой механизм реализован)
                userDAO.updateResetCode(email, resetCode);

                // Отправляем письмо с кодом (EmailSender — твой класс для отправки email)
                boolean emailSent = EmailSender.sendEmail(email, "Восстановление пароля", "Ваш код: " + resetCode);
                runOnUiThread(() -> {
                    if (emailSent) {
                        Toast.makeText(ForgotPasswordActivity.this, "Код отправлен на email", Toast.LENGTH_SHORT).show();
                        // Переходим на активность для подтверждения кода
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Ошибка отправки email", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(ForgotPasswordActivity.this, "Email не найден", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
