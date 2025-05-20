package com.example.cfeprjct.Sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.content.Context;
import android.util.Base64;
import android.util.Log;


import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.DAOS.RoleDAO;
import com.example.cfeprjct.Entities.Role;
import com.example.cfeprjct.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;

public class SyncUserWorker extends Worker {
    private static final String TAG = "SyncUsersWorker";

    public SyncUserWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db     = AppDatabase.getInstance(getApplicationContext());
        RoleDAO    roleDao = db.roleDAO();
        List<Role> roles   = roleDao.getAll();
        List<User> users   = db.userDAO().getAllUsers();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // 1) Синхронизация ролей
        for (Role r : roles) {
            Map<String,Object> roleMap = new HashMap<>();
            roleMap.put("roleId",   r.getRoleId());
            roleMap.put("roleName", r.getRoleName());

            firestore.collection("roles")
                    .document(String.valueOf(r.getRoleId()))
                    .set(roleMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✅ Role synced: " + r.getRoleId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "❌ Error syncing role " + r.getRoleId(), e));
        }

        // 2) Синхронизация пользователей
        for (User u : users) {
            if (u.getUserId() == null || u.getUserId().isEmpty()) {
                Log.w(TAG, "Skipped user with empty userId");
                continue;
            }
            Map<String,Object> userMap = new HashMap<>();
            userMap.put("userId",      u.getUserId());
            userMap.put("firstName",   u.getFirstName());
            userMap.put("lastName",    u.getLastName());
            userMap.put("email",       u.getEmail());
            userMap.put("phoneNumber", u.getPhoneNumber());
            userMap.put("roleId",      u.getRoleId());  // теперь доступно

            if (u.getPassword() != null) {
                userMap.put("password", u.getPassword());
            }
            if (u.getProfileImage() != null) {
                String b64 = Base64.encodeToString(
                        u.getProfileImage(),
                        Base64.DEFAULT
                );
                userMap.put("profileImage", b64);
            }

            firestore.collection("users")
                    .document(u.getUserId())
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✅ User synced: " + u.getUserId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "❌ Error syncing user " + u.getUserId(), e));
        }

        return Result.success();
    }
}
