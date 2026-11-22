package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.app_induztek.data.local.entities.InstalacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalacionDao {

    @Query("SELECT * FROM instalaciones WHERE id_usuario_cliente = :clienteId ORDER BY id_instalacion ASC")
    fun getInstalacionesByCliente(clienteId: Int): Flow<List<InstalacionEntity>>

    @Query("""
        SELECT DISTINCT i.* FROM instalaciones i
        INNER JOIN visitas v ON v.id_instalacion = i.id_instalacion
        WHERE v.id_tecnico = :tecnicoId
        ORDER BY i.id_instalacion ASC
    """)
    fun getInstalacionesByTecnico(tecnicoId: Int): Flow<List<InstalacionEntity>>

    @Query("SELECT * FROM instalaciones WHERE id_instalacion = :id LIMIT 1")
    fun getInstalacionById(id: Int): Flow<InstalacionEntity?>

    @Upsert
    suspend fun upsertInstalaciones(instalaciones: List<InstalacionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstalaciones(instalaciones: List<InstalacionEntity>): List<Long>
}
