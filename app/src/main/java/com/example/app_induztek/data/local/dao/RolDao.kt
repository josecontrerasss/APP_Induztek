package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_induztek.data.local.entities.RolEntity

@Dao
interface RolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RolEntity>): List<Long>

    @Query("SELECT id_rol FROM roles WHERE codigo = :codigo LIMIT 1")
    suspend fun getRolIdPorCodigo(codigo: String): Int?

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun countRoles(): Int
}
