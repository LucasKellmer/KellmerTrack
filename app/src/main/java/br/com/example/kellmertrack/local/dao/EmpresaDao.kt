
package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity

@Dao
abstract class EmpresaDao : BaseDao<EmpresaEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: EmpresaEntity)

    @Delete
    abstract override suspend fun delete(Obj: EmpresaEntity)

    @Query("select * from empresa")
    abstract fun getEmpresa(): EmpresaEntity

    @Query("DELETE FROM empresa")
    abstract suspend fun deleteAll()
}