package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.ComprovanteEntity
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra

@Dao
abstract class ComprovanteDao : BaseDao<ComprovanteEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: ComprovanteEntity)

    @Delete
    abstract override suspend fun delete(Obj: ComprovanteEntity)

    @Query("UPDATE entrega_comprovante SET sincronizado = :sincronizado WHERE entrega_id = :entregaId")
    abstract suspend fun updateComprovanteSincronizado(entregaId: Int, sincronizado: Boolean)

    @Query("SELECT * FROM entrega_comprovante WHERE entrega_id = :entregaId")
    abstract fun findComprovanteByEntregaId(entregaId: Int): ComprovanteEntity?

    @Query("SELECT * FROM entrega_comprovante WHERE sincronizado = 0")
    abstract fun getComprovanteSync(): List<ComprovanteEntity>

}