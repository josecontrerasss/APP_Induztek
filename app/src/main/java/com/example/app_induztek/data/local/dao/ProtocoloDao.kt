package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProtocoloDao {

    @Query("SELECT * FROM protocolos WHERE id_usuario_registra = :tecnicoId ORDER BY id_protocolo DESC")
    abstract fun getProtocolosByTecnico(tecnicoId: Int): Flow<List<ProtocoloEntity>>

    @Query("""
        SELECT p.* FROM protocolos p
        INNER JOIN equipos e ON p.id_equipo = e.id_equipo
        INNER JOIN instalaciones i ON e.id_instalacion = i.id_instalacion
        WHERE i.id_usuario_cliente = :clienteId 
        AND p.estado NOT IN ('BORRADOR', 'ANULADO')
        ORDER BY p.id_protocolo DESC
    """)
    abstract fun getProtocolosByCliente(clienteId: Int): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos WHERE sincronizado = 0 ORDER BY id_protocolo DESC")
    abstract fun getProtocolosPendientesSincronizar(): Flow<List<ProtocoloEntity>>

    @Query("SELECT COUNT(*) FROM protocolos WHERE sincronizado = 0 AND id_usuario_registra = :tecnicoId")
    abstract fun countPendientesByTecnico(tecnicoId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProtocolo(protocolo: ProtocoloEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProtocolos(protocolos: List<ProtocoloEntity>): List<Long>

    @Update
    abstract suspend fun updateProtocolo(protocolo: ProtocoloEntity): Int

    @Query("UPDATE protocolos SET sincronizado = 1 WHERE sincronizado = 0 AND id_usuario_registra = :tecnicoId")
    abstract suspend fun sincronizarByTecnico(tecnicoId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMediciones(mediciones: List<MedicionEntity>): List<Long>

    @Transaction
    open suspend fun guardarProtocoloConMediciones(protocolo: ProtocoloEntity, mediciones: List<MedicionEntity>): Long {
        val protocoloId = insertProtocolo(protocolo)
        val medicionesConId = mediciones.map { it.copy(id_protocolo = protocoloId.toInt()) }
        insertMediciones(medicionesConId)
        return protocoloId
    }
}
