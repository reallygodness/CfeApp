package com.example.cfeprjct.Activities.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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

import com.example.cfeprjct.Activities.LoginActivity;
import com.example.cfeprjct.Activities.MainActivity;
import com.example.cfeprjct.Activities.WelcomeActivity;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.AuthUtils;
import com.example.cfeprjct.R;
import com.example.cfeprjct.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView firstNameTextView, emailTextView, phoneNumberTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneNumberEditText;
    private Button saveButton, editProfileButton, logoutButton;
    private ImageView profileImageView;
    private AppDatabase db;
    private FirebaseFirestore firestore;
    private ListenerRegistration userListener;

    // Основной ключ – userId, полученный из SharedPreferences
    private String userId;
    // Номер телефона – обычное поле пользователя
    private String phoneNumber;
    private boolean isEditing = false;
    private byte[] selectedImageBytes = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Инициализация базы данных и Firestore
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .build();
        firestore = FirebaseFirestore.getInstance();

        // Инициализация UI элементов
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

        // Получаем userId из SharedPreferences через AuthUtils
        userId = AuthUtils.getLoggedInUserId(requireContext());
        if (userId != null) {
            User user = db.userDAO().getUserById(userId);
            if (user != null) {
                phoneNumber = user.getPhoneNumber(); // сохраняем номер телефона для локального отображения
            }
        }

        updateUI();

        profileImageView.setEnabled(false);
        profileImageView.setOnClickListener(v -> openGallery());

        editProfileButton.setOnClickListener(v -> editProfile());
        saveButton.setOnClickListener(v -> saveProfileChanges());
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void updateUI() {
        if (userId != null) {
            new Thread(() -> {
                User user = db.userDAO().getUserById(userId);
                if (user != null) {
                    requireActivity().runOnUiThread(() -> updateUIWithUser(user));
                }
            }).start();
            subscribeToFirestoreUser(userId);
        }
    }

    private void updateUIWithUser(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        firstNameTextView.setText(fullName);
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

        // Проверка корректности email
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(requireContext(), "Введите корректный email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newPhoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Считываем пользователя из локальной базы по userId
            User user = db.userDAO().getUserById(userId);
            if (user != null) {
                // Обновляем данные пользователя
                user.setFirstName(newFirstName);
                user.setLastName(newLastName);
                user.setEmail(newEmail);
                user.setPhoneNumber(newPhoneNumber);
                if (selectedImageBytes != null) {
                    user.setProfileImage(selectedImageBytes);
                    // Обнуляем переменную после использования
                    selectedImageBytes = null;
                }

                // Сохраняем обновлённого пользователя в локальную базу
                db.userDAO().updateUser(user);

                // Считываем обновлённого пользователя, чтобы быть уверенными в актуальности данных
                User updatedUser = db.userDAO().getUserById(userId);
                phoneNumber = updatedUser.getPhoneNumber();

                // Обновляем UI в главном потоке, используя свежие данные
                requireActivity().runOnUiThread(() -> {
                    updateUIWithUser(updatedUser);
                    Toast.makeText(requireContext(), "Профиль обновлён!", Toast.LENGTH_SHORT).show();
                    exitEditMode();
                    // Если используется BottomNavigationView – переключаем пункт меню на профиль
                    ((MainActivity) requireActivity()).getBottomNavigationView().setSelectedItemId(R.id.nav_profile);
                });

                // Готовим обновлённые данные для Firestore, включая зашифрованный пароль и изображение
                Map<String, Object> updatedUserMap = new HashMap<>();
                updatedUserMap.put("userId", updatedUser.getUserId());
                updatedUserMap.put("phoneNumber", updatedUser.getPhoneNumber());
                updatedUserMap.put("firstName", updatedUser.getFirstName());
                updatedUserMap.put("lastName", updatedUser.getLastName());
                updatedUserMap.put("email", updatedUser.getEmail());
                updatedUserMap.put("password", updatedUser.getPassword());
                if (updatedUser.getProfileImage() != null) {
                    updatedUserMap.put("profileImage", Base64.encodeToString(updatedUser.getProfileImage(), Base64.DEFAULT));
                }

                // Отправляем данные в Firestore
                firestore.collection("users").document(updatedUser.getUserId())
                        .set(updatedUserMap)
                        .addOnSuccessListener(aVoid ->
                                Log.d("Firestore", "Данные в Firestore успешно обновлены"))
                        .addOnFailureListener(e ->
                                Log.e("Firestore", "❌ Ошибка обновления данных в Firestore", e));
            }
        }).start();
    }


    private void exitEditMode() {
        isEditing = false;
        editProfileButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
        firstNameEditText.setVisibility(View.GONE);
        lastNameEditText.setVisibility(View.GONE);
        emailEditText.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        profileImageView.setEnabled(false);
    }

    private void logout() {
        AuthUtils.setLoggedIn(requireContext(), false, null);
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void subscribeToFirestoreUser(String id) {
        if (id == null) return;
        if (userListener != null) {
            userListener.remove();
        }
        userListener = firestore.collection("users").document(id)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "❌ Ошибка при получении данных", error);
                        return;
                    }
                    if (documentSnapshot == null || documentSnapshot.getData() == null) {
                        Log.w("Firestore", "Данные не получены (null) для userId=" + id);
                        return; // Если данных нет, не обновляем UI
                    }
                    // Выведем полное содержимое документа в лог:
                    Log.d("FirestoreListener", "Document data: " + documentSnapshot.getData().toString());
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String profileImageBase64 = documentSnapshot.getString("profileImage");

                        // Если ключевые поля отсутствуют, обновлять UI не будем
                        if (firstName == null || lastName == null || email == null || phone == null) {
                            Log.w("Firestore", "Пока не все нужные поля заполнены для userId=" + id);
                            return;
                        }

                        User firestoreUser = new User(id, firstName, lastName, email, phone);
                        if (profileImageBase64 != null) {
                            firestoreUser.setProfileImage(Base64.decode(profileImageBase64, Base64.DEFAULT));
                        }
                        // Обновляем UI, если данные из Firestore валидны
                        updateUIWithUser(firestoreUser);
                    } else {
                        Log.w("Firestore", "Документ с userId " + id + " не найден.");
                        Toast.makeText(requireContext(), "Данные в Firestore недоступны", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == requireActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            saveImageToDatabase(imageUri);
        }
    }

    private void saveImageToDatabase(Uri imageUri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Декодируем изображение из InputStream
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) {
                Log.e("saveImageToDatabase", "Не удалось декодировать изображение");
                return;
            }

            // Масштабирование: если ширина изображения больше 300 пикселей, изменяем размер
            int maxWidth = 300;
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            if (originalWidth > maxWidth) {
                newWidth = maxWidth;
                newHeight = (int) (((double) originalHeight / originalWidth) * newWidth);
            }

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            // Сжимаем изображение до JPEG с качеством 80
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            selectedImageBytes = outputStream.toByteArray();

            Log.d("saveImageToDatabase", "Scaled image byte array length: " + selectedImageBytes.length);

            // Обновляем ImageView масштабированным изображением
            profileImageView.setImageBitmap(scaledBitmap);

        } catch (IOException e) {
            Log.e("saveImageToDatabase", "Ошибка при сохранении изображения", e);
            e.printStackTrace();
        }
    }

}
