package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app_induztek.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun getUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT r.codigo FROM roles r INNER JOIN usuarios u ON r.id_rol = u.id_rol WHERE u.id_usuario = :usuarioId LIMIT 1")
    suspend fun getRolCodigoByUsuario(usuarioId: Int): String?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun getUsuarioById(id: Int): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE id_rol = (SELECT id_rol FROM roles WHERE codigo = 'CLIENTE' LIMIT 1)")
    fun getClientes(): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>): List<Long>

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity): Int

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    suspend fun getUsuarioPorId(id: Int): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND id_usuario != :idExcluido")
    suspend fun contarCorreoEnOtros(correo: String, idExcluido: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long
}
