package com.example.cfeprjct.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "roles")
public class Role {
    @PrimaryKey
    @ColumnInfo(name = "role_id")
    public int roleId;

    @ColumnInfo(name = "role_name")
    public String roleName;

    // Пустой конструктор для Room
    public Role() { }

    public Role(int roleId, String roleName) {
        this.roleId   = roleId;
        this.roleName = roleName;
    }


    // Геттеры и сеттеры
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
