package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.ObraEntity

@Dao
abstract class ObraDao : BaseDao<ObraEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: ObraEntity)

    @Delete
    abstract override suspend fun delete(Obj: ObraEntity)

    @Query("DELETE FROM obra")
    abstract suspend fun deleteAll()
}