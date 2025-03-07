package com.example.cfeprjct.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cfeprjct.R;
import com.example.cfeprjct.api.ApiClient;
import com.example.cfeprjct.api.ApiService;
import com.example.cfeprjct.api.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnsendcode);

        btnResetPassword.setOnClickListener(v -> sendResetRequest());
    }

    private void sendResetRequest() {
        String email = etEmail.getText().toString().trim();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        apiService.resetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Код отправлен на email", Toast.LENGTH_SHORT).show();
                    // Переход на экран ввода кода (реализуем далее)
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Ошибка: email не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

}