package com.example.cfeprjct;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRoleMigration {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Вызывайте этот метод один раз сразу после миграции Room,
     * например, в Application.onCreate() после Room.databaseBuilder().build().
     */
    public void migrateRoles() {
        // 1) Убедимся, что коллекция roles содержит дефолтную роль
        Map<String,Object> defaultRole = new HashMap<>();
        defaultRole.put("roleId",   1);
        defaultRole.put("roleName", "user");
        db.collection("roles")
                .document("1")
                .set(defaultRole, SetOptions.merge());

        // 2) Обновим всех пользователей в Firestore
        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot userDoc : snapshot.getDocuments()) {
                        // если поле roleId ещё не задано — добавим его
                        if (!userDoc.contains("roleId")) {
                            DocumentReference uref = userDoc.getReference();
                            batch.update(uref, "roleId", 1);
                        }
                    }
                    // commit батча
                    batch.commit()
                            .addOnSuccessListener(__ ->
                                    Log.i("Migration", "Все пользователи получили roleId=1")
                            )
                            .addOnFailureListener(e ->
                                    Log.e("Migration", "Не удалось обновить пользователей", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e("Migration", "Не удалось прочитать коллекцию users", e)
                );
    }
}
