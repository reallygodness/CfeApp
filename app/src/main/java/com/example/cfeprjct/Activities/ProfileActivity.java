package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneNumberTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneNumberEditText;
    private Button saveButton, editProfileButton, logoutButton;

    private ImageView profileImageView;

    private AppDatabase db;
    private String phoneNumber;  // Хранит изначальный номер телефона пользователя
    private boolean isEditing = false;

    private TextView fullNameTextView; // Добавляем переменную

    private byte[] selectedImageBytes = null; // Переменная для временного хранения фото

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();

        // Инициализация UI компонентов
        fullNameTextView = findViewById(R.id.fullNameTextView); // Инициализация
        emailTextView = findViewById(R.id.emailTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        saveButton = findViewById(R.id.saveButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        profileImageView = findViewById(R.id.profileImageView);
        logoutButton = findViewById(R.id.logoutButton);

        // Получаем номер телефона из Intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        updateUI();

        profileImageView.setEnabled(false);

        // Добавляем обработчик нажатия на изображение профиля
        profileImageView.setOnClickListener(v -> openGallery());
    }

    public void updateUI() {
        if (phoneNumber != null) {
            new Thread(() -> {
                User user = db.userDAO().getUserByPhoneNumber(phoneNumber);
                if (user != null) {
                    runOnUiThread(() -> {
                        fullNameTextView.setText(user.getFirstName() + " " + user.getLastName()); // Объединяем имя и фамилию
                        emailTextView.setText("Email: " + user.getEmail());
                        phoneNumberTextView.setText("Номер телефона: +" + user.getPhoneNumber());

                        // Заполняем EditText данными пользователя
                        firstNameEditText.setText(user.getFirstName());
                        lastNameEditText.setText(user.getLastName());
                        emailEditText.setText(user.getEmail());
                        phoneNumberEditText.setText(user.getPhoneNumber());
                        if (user.getProfileImage() != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getProfileImage(), 0, user.getProfileImage().length);
                            profileImageView.setImageBitmap(bitmap);
                        } else {
                            profileImageView.setImageResource(R.drawable.grayprofile);
                        }
                    });
                }
            }).start();
        }
    }

    public void editProfile(View view) {
        if (!isEditing) {
            isEditing = true;
            fullNameTextView.setVisibility(View.GONE);
            emailTextView.setVisibility(View.GONE);
            phoneNumberTextView.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);

            firstNameEditText.setVisibility(View.VISIBLE);
            lastNameEditText.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.VISIBLE);
            phoneNumberEditText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);

            profileImageView.setEnabled(true);
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

                if (selectedImageBytes != null) {
                    user.setProfileImage(selectedImageBytes);
                    selectedImageBytes = null; // Сбрасываем после сохранения
                }

                Log.d("ProfileActivity", "Фото обновляется: " + (user.getProfileImage() != null));

                db.userDAO().updateUser(user);

                // После успешного обновления меняем phoneNumber на новый
                phoneNumber = newPhoneNumber;

                runOnUiThread(() -> {
                    updateUI();
                    Toast.makeText(ProfileActivity.this, "Профиль обновлен!", Toast.LENGTH_SHORT).show();

                    isEditing = false;
                    fullNameTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    phoneNumberTextView.setVisibility(View.VISIBLE);
                    editProfileButton.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.VISIBLE);

                    firstNameEditText.setVisibility(View.GONE);
                    lastNameEditText.setVisibility(View.GONE);
                    emailEditText.setVisibility(View.GONE);
                    phoneNumberEditText.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);


                    profileImageView.setEnabled(false);
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                saveImageToDatabase(imageUri);
                            }
                        }
                    });

    private void saveImageToDatabase(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            selectedImageBytes = outputStream.toByteArray(); // Сохраняем фото в переменную

            runOnUiThread(() -> {
                Toast.makeText(this, "Фото загружено, нажмите 'Сохранить изменения'", Toast.LENGTH_SHORT).show();
                profileImageView.setImageBitmap(bitmap);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
        }
    }







}
