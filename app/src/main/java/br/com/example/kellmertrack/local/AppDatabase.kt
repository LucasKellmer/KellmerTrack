package br.com.example.kellmertrack.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.example.kellmertrack.local.dao.ClienteDao
import br.com.example.kellmertrack.local.dao.ComprovanteDao
import br.com.example.kellmertrack.local.dao.ContratoDao
import br.com.example.kellmertrack.local.dao.EmpresaDao
import br.com.example.kellmertrack.local.dao.EventoDao
import br.com.example.kellmertrack.local.dao.KellmertrackVersionDao
import br.com.example.kellmertrack.local.dao.ObraDao
import br.com.example.kellmertrack.local.dao.RotacaoDao
import br.com.example.kellmertrack.local.dao.SetupDao
import br.com.example.kellmertrack.local.dao.TrajetoDao
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.EventoEntity
import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity
import br.com.example.kellmertrack.local.model.entities.ObraEntity
import br.com.example.kellmertrack.local.dao.EntregaDao
import br.com.example.kellmertrack.local.model.entities.ClienteEntity
import br.com.example.kellmertrack.local.model.entities.ComprovanteEntity
import br.com.example.kellmertrack.local.model.entities.ContratoEntity
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.local.model.entities.TrajetoEntity

@Database(entities = [RotacaoEntity::class, TrajetoEntity::class, SetupEntity::class, HobitrackVersionEntity::class, EntregaEntity::class,
                      EventoEntity::class, EmpresaEntity::class, ObraEntity::class, ClienteEntity::class, ContratoEntity::class, ComprovanteEntity::class
                     ], version = 1 , exportSchema = false)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rotacaoDao(): RotacaoDao

    abstract fun trajetoDao(): TrajetoDao

    abstract fun setupDao(): SetupDao

    abstract fun hobitrackVersionDao(): KellmertrackVersionDao

    abstract fun entregaDao(): EntregaDao

    abstract fun eventoDao() : EventoDao

    abstract fun empresaDao() : EmpresaDao

    abstract fun obraDao() : ObraDao

    abstract fun clienteDao() : ClienteDao

    abstract fun contratoDao() : ContratoDao

    abstract fun comprovanteDao() : ComprovanteDao
}