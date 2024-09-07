package br.com.example.kellmertrack.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity

@Dao
abstract class KellmertrackVersionDao : BaseDao<HobitrackVersionEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: HobitrackVersionEntity)

    @Delete
    abstract override suspend fun delete(Obj: HobitrackVersionEntity)

    @Query("delete from hobitrack_version")
    abstract suspend fun deleteAll()

    @Query("select * from hobitrack_version order by version_code desc limit 1")
    abstract fun getUltimaVersao(): LiveData<HobitrackVersionEntity>
}