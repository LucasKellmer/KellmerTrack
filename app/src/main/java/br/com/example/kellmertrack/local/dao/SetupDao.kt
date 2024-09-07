package br.com.example.kellmertrack.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import br.com.example.kellmertrack.local.model.entities.SetupEntity

@Dao
abstract class SetupDao : BaseDao<SetupEntity> {

    @Insert
    abstract override suspend fun insert(Obj: SetupEntity)

    @Delete
    abstract override suspend fun delete(Obj: SetupEntity)

    @Query("SELECT * FROM setup")
    abstract fun getSetup(): SetupEntity?
    //abstract suspend fun getSetup(): SetupEntity?

    @Query("SELECT * FROM setup")
    abstract fun getSetupLive(): LiveData<SetupEntity?>

    @Query("SELECT * FROM setup WHERE numero_interno = :numeroInterno")
    abstract fun buscaSetupByNumeroInterno(numeroInterno : String?): SetupEntity?

    @Query("UPDATE setup SET mac = :mac")
    abstract suspend fun atualizaMacDispositivo(mac :String)
}