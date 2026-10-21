package com.example.app_induztek.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.RolEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.local.entities.VisitaEntity

@Dao
interface InduztekDao {


    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password_hash = :password AND activo = 1 LIMIT 1")
    suspend fun login(correo: String, password: String): UsuarioEntity?


    @Query("SELECT r.codigo FROM roles r INNER JOIN usuarios u ON r.id_rol = u.id_rol WHERE u.id_usuario = :usuarioId LIMIT 1")
    suspend fun getRolCodigoByUsuario(usuarioId: Int): String?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun countRoles(): Int


    @Query("SELECT * FROM instalaciones ORDER BY id_instalacion ASC")
    fun getAllInstalaciones(): Flow<List<InstalacionEntity>>


    @Query("SELECT * FROM equipos WHERE id_instalacion = :instalacionId ORDER BY tag ASC")
    fun getEquiposByInstalacion(instalacionId: Int): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos ORDER BY id_equipo ASC")
    fun getAllEquipos(): Flow<List<EquipoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocolo(protocolo: ProtocoloEntity): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediciones(mediciones: List<MedicionEntity>)


    @Query("SELECT COUNT(*) FROM protocolos WHERE sincronizado = 0")
    fun countPendientesSincronizar(): Flow<Int>

    @Query("UPDATE protocolos SET sincronizado = 1 WHERE sincronizado = 0")
    suspend fun sincronizarTodos()


    @Query("SELECT * FROM instalaciones WHERE id_usuario_cliente = :clienteId ORDER BY id_instalacion ASC")
    fun getInstalacionesByCliente(clienteId: Int): Flow<List<InstalacionEntity>>

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


    @Query("SELECT * FROM mediciones WHERE id_protocolo = :protocoloId ORDER BY id_medicion ASC")
    fun getMedicionesByProtocolo(protocoloId: Int): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM mediciones ORDER BY id_medicion ASC")
    fun getAllMediciones(): Flow<List<MedicionEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstalaciones(instalaciones: List<InstalacionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiposEquipo(tipos: List<TipoEquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitas(visitas: List<VisitaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocolos(protocolos: List<ProtocoloEntity>)
}