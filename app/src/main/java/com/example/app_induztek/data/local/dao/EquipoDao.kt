package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.app_induztek.data.local.entities.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    @Query("SELECT * FROM equipos WHERE id_instalacion = :instalacionId ORDER BY tag ASC")
    fun getEquiposByInstalacion(instalacionId: Int): Flow<List<EquipoEntity>>

    @Query("""
        SELECT * FROM equipos
        WHERE id_instalacion IN (SELECT id_instalacion FROM visitas WHERE id_tecnico = :tecnicoId)
        ORDER BY id_instalacion ASC, tag ASC
    """)
    fun getEquiposByTecnico(tecnicoId: Int): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id_equipo = :id LIMIT 1")
    fun getEquipoById(id: Int): Flow<EquipoEntity?>

    @Upsert
    suspend fun upsertEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipos(equipos: List<EquipoEntity>): List<Long>
}
