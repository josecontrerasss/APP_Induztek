package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_induztek.data.local.entities.VisitaEntity

@Dao
interface VisitaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitas(visitas: List<VisitaEntity>): List<Long>

    @Query("SELECT id_visita FROM visitas WHERE id_instalacion = :instalacionId ORDER BY id_visita DESC LIMIT 1")
    suspend fun getUltimaVisitaId(instalacionId: Int): Int?

    @Query("SELECT id_visita FROM visitas WHERE id_instalacion = :instalacionId AND id_tecnico = :tecnicoId ORDER BY id_visita DESC LIMIT 1")
    suspend fun getUltimaVisitaIdDeTecnico(instalacionId: Int, tecnicoId: Int): Int?
}
