package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Button verifyButton;
    private AppDatabase database;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        database = AppDatabase.getInstance(this);

        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);

        email = getIntent().getStringExtra("email"); // Получаем email из Intent

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
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
            User user = userDAO.verifyResetCode(email, enteredCode);

            runOnUiThread(() -> {
                if (user != null) {
                    Toast.makeText(VerifyCodeActivity.this, "Код подтвержден!", Toast.LENGTH_SHORT).show();
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
