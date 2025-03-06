package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneNumberTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneNumberEditText;
    private Button saveButton, editProfileButton;

    private AppDatabase db;
    private String phoneNumber;  // Хранит изначальный номер телефона пользователя
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();

        // Инициализация UI компонентов
        firstNameTextView = findViewById(R.id.firstNameTextView);
        lastNameTextView = findViewById(R.id.lastNameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        saveButton = findViewById(R.id.saveButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        // Получаем номер телефона из Intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        updateUI();
    }

    public void updateUI() {
        if (phoneNumber != null) {
            new Thread(() -> {
                User user = db.userDAO().getUserByPhoneNumber(phoneNumber);
                if (user != null) {
                    runOnUiThread(() -> {
                        firstNameTextView.setText("Имя: " + user.getFirstName());
                        lastNameTextView.setText("Фамилия: " + user.getLastName());
                        emailTextView.setText("Email: " + user.getEmail());
                        phoneNumberTextView.setText("Номер телефона: +" + user.getPhoneNumber());

                        // Заполняем EditText данными пользователя
                        firstNameEditText.setText(user.getFirstName());
                        lastNameEditText.setText(user.getLastName());
                        emailEditText.setText(user.getEmail());
                        phoneNumberEditText.setText(user.getPhoneNumber());
                    });
                }
            }).start();
        }
    }

    public void editProfile(View view) {
        if (!isEditing) {
            isEditing = true;
            firstNameTextView.setVisibility(View.GONE);
            lastNameTextView.setVisibility(View.GONE);
            emailTextView.setVisibility(View.GONE);
            phoneNumberTextView.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.GONE);


            firstNameEditText.setVisibility(View.VISIBLE);
            lastNameEditText.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.VISIBLE);
            phoneNumberEditText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    public void saveProfileChanges(View view) {
        String newFirstName = firstNameEditText.getText().toString().trim();
        String newLastName = lastNameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhoneNumber = phoneNumberEditText.getText().toString().trim();

        // Валидация email
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Введите корректный email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newPhoneNumber.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDAO().getUserByPhoneNumber(phoneNumber); // Ищем по старому номеру
            if (user != null) {
                user.setFirstName(newFirstName);
                user.setLastName(newLastName);
                user.setEmail(newEmail);
                user.setPhoneNumber(newPhoneNumber);

                db.userDAO().updateUser(user);

                // После успешного обновления меняем phoneNumber на новый
                phoneNumber = newPhoneNumber;

                runOnUiThread(() -> {
                    updateUI();
                    Toast.makeText(ProfileActivity.this, "Профиль обновлен!", Toast.LENGTH_SHORT).show();

                    isEditing = false;
                    firstNameTextView.setVisibility(View.VISIBLE);
                    lastNameTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    phoneNumberTextView.setVisibility(View.VISIBLE);
                    editProfileButton.setVisibility(View.VISIBLE);

                    firstNameEditText.setVisibility(View.GONE);
                    lastNameEditText.setVisibility(View.GONE);
                    emailEditText.setVisibility(View.GONE);
                    phoneNumberEditText.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    public void logout(View view) {
        AuthUtils.setLoggedIn(this, false, null);
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
