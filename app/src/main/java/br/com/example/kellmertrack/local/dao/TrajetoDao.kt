package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.TrajetoEntity

@Dao
abstract class TrajetoDao : BaseDao<TrajetoEntity> {

    @Query("SELECT * FROM trajeto")
    abstract fun getLocalizacao(): List<TrajetoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: TrajetoEntity)

    @Delete
    abstract override suspend fun delete(Obj: TrajetoEntity)

    @Query("DELETE FROM trajeto WHERE (date(momento / 1000, 'unixepoch', 'localtime') < date() AND sincronizado = 1)" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-10 day'))")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM trajeto WHERE sincronizado = 1" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-5 day'))")
    abstract suspend fun limpaTrajetos()

    @Query("SELECT * FROM trajeto WHERE id = :id")
    abstract fun getTrajetoById(id : String?) : TrajetoEntity?

    @Query("UPDATE trajeto SET sincronizado = 1 WHERE id = :id")
    abstract fun updateTrajetoSincronizado(id: String)

    @Query("SELECT * FROM trajeto WHERE sincronizado = 0")
    abstract suspend fun getSincronizar(): List<TrajetoEntity>?
}