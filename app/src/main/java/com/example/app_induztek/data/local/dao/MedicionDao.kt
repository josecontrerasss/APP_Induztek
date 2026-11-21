package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_induztek.data.local.entities.MedicionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionDao {

    @Query("SELECT * FROM mediciones WHERE id_protocolo = :protocoloId ORDER BY id_medicion ASC")
    fun getMedicionesByProtocolo(protocoloId: Int): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM mediciones ORDER BY id_medicion ASC")
    fun getAllMediciones(): Flow<List<MedicionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicion(medicion: MedicionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediciones(mediciones: List<MedicionEntity>): List<Long>
}
