package br.com.example.kellmertrack.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import br.com.example.kellmertrack.local.model.entities.EventoEntity

@Dao
abstract class EventoDao : BaseDao<EventoEntity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: EventoEntity)

    @Delete
    abstract override suspend fun delete(Obj: EventoEntity)

    @Query("SELECT * FROM eventos")
    abstract fun getEventos(): List<EventoEntity>

    @Transaction
    @Query("SELECT * FROM eventos ORDER BY momento DESC")
    abstract fun eventosLiveData(): LiveData<List<EventoEntity>>

    @Query("UPDATE eventos SET sincronizado = 1 WHERE id = :id")
    abstract suspend fun updateEventoSincronizado(id: String?)

    @Query("SELECT * FROM eventos WHERE sincronizado = 0")
    abstract suspend fun getSincronizar(): List<EventoEntity>?

    @Query("SELECT * FROM eventos WHERE entrega_id = :entregaId order by momento desc limit 1")
    abstract suspend fun buscaUltimoEvento(entregaId: String) : EventoEntity?

    @Query("DELETE FROM eventos WHERE (date(momento / 1000, 'unixepoch', 'localtime') < date() AND sincronizado = 1)" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-10 day'))")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM eventos WHERE sincronizado = 1" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-5 day'))")
    abstract suspend fun limpaEvento()
}