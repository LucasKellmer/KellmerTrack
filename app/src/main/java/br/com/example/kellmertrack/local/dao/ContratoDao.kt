package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import br.com.example.kellmertrack.local.model.entities.ContratoEntity
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity

@Dao
abstract class ContratoDao : BaseDao<ContratoEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: ContratoEntity)

    @Delete
    abstract override suspend fun delete(Obj: ContratoEntity)
}