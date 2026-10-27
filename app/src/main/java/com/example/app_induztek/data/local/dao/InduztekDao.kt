package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.RolEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.local.entities.VisitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InduztekDao {

    // --- INSTALACIONES ---
    @Query("SELECT * FROM instalaciones ORDER BY id_instalacion ASC")
    fun getAllInstalaciones(): Flow<List<InstalacionEntity>>

    @Query("SELECT * FROM instalaciones WHERE id_usuario_cliente = :clienteId ORDER BY id_instalacion ASC")
    fun getInstalacionesByCliente(clienteId: Int): Flow<List<InstalacionEntity>>

    @Query("SELECT * FROM instalaciones WHERE id_instalacion = :id LIMIT 1")
    fun getInstalacionById(id: Int): Flow<InstalacionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInstalaciones(instalaciones: List<InstalacionEntity>): List<Long>

    // --- EQUIPOS ---
    @Query("SELECT * FROM equipos WHERE id_instalacion = :instalacionId ORDER BY tag ASC")
    fun getEquiposByInstalacion(instalacionId: Int): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos ORDER BY id_equipo ASC")
    fun getAllEquipos(): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id_equipo = :id LIMIT 1")
    fun getEquipoById(id: Int): Flow<EquipoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEquipos(equipos: List<EquipoEntity>): List<Long>

    // --- PROTOCOLOS ---
    @Query("SELECT * FROM protocolos ORDER BY id_protocolo DESC")
    fun getAllProtocolos(): Flow<List<ProtocoloEntity>>

    @Query("""
        SELECT p.* FROM protocolos p
        INNER JOIN equipos e ON p.id_equipo = e.id_equipo
        INNER JOIN instalaciones i ON e.id_instalacion = i.id_instalacion
        WHERE i.id_usuario_cliente = :clienteId 
        AND p.estado NOT IN ('BORRADOR', 'ANULADO')
        ORDER BY p.id_protocolo DESC
    """)
    fun getProtocolosByCliente(clienteId: Int): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos WHERE sincronizado = 0 ORDER BY id_protocolo DESC")
    fun getProtocolosPendientesSincronizar(): Flow<List<ProtocoloEntity>>

    @Query("SELECT COUNT(*) FROM protocolos WHERE sincronizado = 0")
    fun countPendientesSincronizar(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProtocolo(protocolo: ProtocoloEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProtocolos(protocolos: List<ProtocoloEntity>): List<Long>

    @Update
    fun updateProtocolo(protocolo: ProtocoloEntity): Int

    @Query("UPDATE protocolos SET sincronizado = 1 WHERE sincronizado = 0")
    fun sincronizarTodos(): Int

    // --- MEDICIONES ---
    @Query("SELECT * FROM mediciones WHERE id_protocolo = :protocoloId ORDER BY id_medicion ASC")
    fun getMedicionesByProtocolo(protocoloId: Int): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM mediciones ORDER BY id_medicion ASC")
    fun getAllMediciones(): Flow<List<MedicionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMedicion(medicion: MedicionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediciones(mediciones: List<MedicionEntity>): List<Long>

    // --- USUARIOS Y ROLES ---
    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND password_hash = :password LIMIT 1")
    fun login(correo: String, password: String): UsuarioEntity?

    @Query("SELECT r.codigo FROM roles r INNER JOIN usuarios u ON r.id_rol = u.id_rol WHERE u.id_usuario = :usuarioId LIMIT 1")
    fun getRolCodigoByUsuario(usuarioId: Int): String?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun getUsuarioById(id: Int): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE id_rol = (SELECT id_rol FROM roles WHERE codigo = 'CLIENTE' LIMIT 1)")
    fun getClientes(): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoles(roles: List<RolEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsuarios(usuarios: List<UsuarioEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTiposEquipo(tipos: List<TipoEquipoEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVisitas(visitas: List<VisitaEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM roles")
    fun countRoles(): Int
}