package br.com.example.kellmertrack.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import br.com.example.kellmertrack.local.model.entities.ClienteEntity

@Dao
abstract class ClienteDao : BaseDao<ClienteEntity>{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(Obj: ClienteEntity)

    @Delete
    abstract override suspend fun delete(Obj: ClienteEntity)
}