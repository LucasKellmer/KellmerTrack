package br.com.example.kellmertrack.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.remote.model.Status
import java.util.Date

@Dao
abstract class
EntregaDao : BaseDao<EntregaEntity> {

    @Query("SELECT * FROM entrega")
    abstract fun getEntregas(): EntregaWithObra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: EntregaEntity)

    @Query("UPDATE entrega SET sincronizado = 1 WHERE cast(id as text) = :id")
    abstract suspend fun updateEntregaSincronizado(id: String)

    @Query("UPDATE entrega SET status = :status WHERE cast(id as text) = :entregaId")
    abstract suspend fun updateEntregaStatus(entregaId : Int, status: Int)

    @Query("DELETE FROM entrega")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM entrega WHERE sincronizado = 1" +
            " OR (date(momento / 1000, 'unixepoch', 'localtime') < date('now', '-5 day'))")
    abstract suspend fun limpaEntrega()

    @Delete
    abstract override suspend fun delete(Obj: EntregaEntity)

    @Transaction
    @Query("SELECT * FROM entrega ORDER BY momento DESC")
    abstract fun entregasLiveData(): LiveData<List<EntregaWithObra?>>

    @Query("SELECT * FROM entrega WHERE cast(id as text) = :id")
    abstract fun findEntregaById(id: String): EntregaWithObra?

    @Query("SELECT * FROM entrega")
    abstract fun findEntregaAtiva(): EntregaEntity?

    @Query("UPDATE entrega SET status = 2, quantidade_entregue = :quantidade WHERE cast(id as text) = :id")
    abstract suspend fun confirmarEntrega(id: String, quantidade : String)

    @Query("SELECT * FROM entrega WHERE sincronizado = 0")
    abstract suspend fun getEntregasNaoSincronizadas() : List<EntregaEntity>?

    @Query("UPDATE entrega SET data_saida_obra = :momento, sincronizado = 0 where cast(id as text) = :id")
    abstract fun atualizaMomentoSaidaObra(id : String, momento : Date)

    @Query("UPDATE entrega SET data_saida_usina = :momento, sincronizado = 0 where cast(id as text) = :id")
    abstract fun atualizaMomentoSaidaUsina(id : String, momento : Date)

    @Query("UPDATE entrega SET data_entrada_obra = :momento, sincronizado = 0 where cast(id as text) = :id")
    abstract fun atualizaMomentoChegadaObra(id : String, momento : Date)

    @Query("UPDATE entrega SET data_entrada_usina = :momento, sincronizado = 0 where cast(id as text) = :id")
    abstract fun atualizaMomentoChegadaUsina(id : String, momento : Date)
}