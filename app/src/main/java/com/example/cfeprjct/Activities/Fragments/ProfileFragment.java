package com.example.cfeprjct.Activities.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.cfeprjct.Activities.WelcomeActivity;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneNumberTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneNumberEditText;
    private Button saveButton, editProfileButton, logoutButton;
    private ImageView profileImageView;

    private AppDatabase db;
    private String phoneNumber;
    private boolean isEditing = false;
    private byte[] selectedImageBytes = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();

        // Инициализация UI компонентов
        firstNameTextView = view.findViewById(R.id.fullNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        saveButton = view.findViewById(R.id.saveButton);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        logoutButton = view.findViewById(R.id.logoutButton);

        phoneNumber = AuthUtils.getLoggedInPhone(requireContext());

        updateUI();

        profileImageView.setEnabled(false);
        profileImageView.setOnClickListener(v -> openGallery());

        editProfileButton.setOnClickListener(v -> editProfile());
        saveButton.setOnClickListener(v -> saveProfileChanges());
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void updateUI() {
        if (phoneNumber != null) {
            new Thread(() -> {
                User user = db.userDAO().getUserByPhoneNumber(phoneNumber);
                if (user != null) {
                    requireActivity().runOnUiThread(() -> {
                        firstNameTextView.setText(user.getFirstName() + " " + user.getLastName());
                        emailTextView.setText("Email: " + user.getEmail());
                        phoneNumberTextView.setText("Номер телефона: +" + user.getPhoneNumber());

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

    private void editProfile() {
        isEditing = true;
        firstNameTextView.setVisibility(View.GONE);
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

    private void saveProfileChanges() {
        String newFirstName = firstNameEditText.getText().toString().trim();
        String newLastName = lastNameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhoneNumber = phoneNumberEditText.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(requireContext(), "Введите корректный email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newPhoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDAO().getUserByPhoneNumber(phoneNumber);
            if (user != null) {
                user.setFirstName(newFirstName);
                user.setLastName(newLastName);
                user.setEmail(newEmail);
                user.setPhoneNumber(newPhoneNumber);

                if (selectedImageBytes != null) {
                    user.setProfileImage(selectedImageBytes);
                    selectedImageBytes = null;
                }

                db.userDAO().updateUser(user);
                phoneNumber = newPhoneNumber;

                requireActivity().runOnUiThread(() -> {
                    updateUI();
                    Toast.makeText(requireContext(), "Профиль обновлен!", Toast.LENGTH_SHORT).show();

                    // Переключаем интерфейс обратно в режим просмотра
                    firstNameTextView.setVisibility(View.VISIBLE);
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

                    isEditing = false;
                });
            }
        }).start();
    }


    private void logout() {
        AuthUtils.setLoggedIn(requireContext(), false, null);
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }
}
