package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity

@Dao
abstract class RotacaoDao: BaseDao<RotacaoEntity> {

    @Query("SELECT * FROM rotacao")
    abstract fun getRotacao(): RotacaoEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: RotacaoEntity)

    @Query("DELETE FROM rotacao WHERE (date(momento / 1000, 'unixepoch', 'localtime') < date() AND sincronizado = 1)" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-10 day'))")
    abstract suspend fun deleteAll()

    @Delete
    abstract override suspend fun delete(Obj: RotacaoEntity)

    @Query("SELECT * FROM rotacao WHERE id = :id")
    abstract fun getRotacaoById(id : String?) : RotacaoEntity?

    @Query("UPDATE rotacao SET sincronizado = :sincronizado WHERE id = :id")
    abstract suspend fun updateRotacaoSincronizado(id: String, sincronizado: Boolean)

    @Query("SELECT * FROM rotacao WHERE sincronizado = 0")
    abstract suspend fun getSincronizar(): List<RotacaoEntity>

    @Query("SELECT * FROM rotacao ORDER BY momento DESC LIMIT 1")
    abstract suspend fun buscaUltimoRotacao() : RotacaoEntity?

}