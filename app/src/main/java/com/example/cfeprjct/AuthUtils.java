package com.example.cfeprjct;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

public class AuthUtils {
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_PHONE = "user_phone";

    public static void setLoggedIn(Context context, boolean isLoggedIn, String phoneNumber) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

        if (isLoggedIn) {
            editor.putString(KEY_USER_PHONE, phoneNumber);  // Сохраняем номер телефона
        } else {
            editor.remove(KEY_USER_PHONE);  // Убираем номер телефона при выходе
        }

        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);  // По умолчанию - false
    }

    public static String getLoggedInPhone(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PHONE, null);  // Возвращает null, если нет сохраненного номера
    }

    // Проверка email
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
