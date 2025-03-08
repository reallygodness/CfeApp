package com.example.cfeprjct.Activities;

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
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserDAO userDAO = db.userDAO();
            User user = userDAO.getUserByEmail(email);

            if (user != null) {
                String resetCode = AuthUtils.generateResetCode();
                userDAO.updateResetCode(email, resetCode);

                boolean emailSent = EmailSender.sendEmail(email, "Восстановление пароля", "Ваш код: " + resetCode);
                runOnUiThread(() -> {
                    if (emailSent) {
                        Toast.makeText(this, "Код отправлен на email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка отправки email", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Email не найден", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


}