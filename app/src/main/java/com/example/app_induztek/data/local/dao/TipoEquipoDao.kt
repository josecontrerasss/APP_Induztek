package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoEquipoDao {

    @Query("SELECT * FROM tipos_equipo ORDER BY nombre ASC")
    fun getTiposEquipo(): Flow<List<TipoEquipoEntity>>

    @Upsert
    suspend fun upsertTiposEquipo(tipos: List<TipoEquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiposEquipo(tipos: List<TipoEquipoEntity>): List<Long>
}
