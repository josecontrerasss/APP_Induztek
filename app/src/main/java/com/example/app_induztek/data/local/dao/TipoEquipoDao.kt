package com.example.app_induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.app_induztek.data.local.entities.TipoEquipoEntity

@Dao
interface TipoEquipoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiposEquipo(tipos: List<TipoEquipoEntity>): List<Long>
}
