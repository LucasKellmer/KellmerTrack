package br.com.example.kellmertrack.di

import android.content.Context
import androidx.room.Room
import br.com.example.kellmertrack.local.AppDatabase
import br.com.example.kellmertrack.local.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun getInstanceDB(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "sensor.db")
            .allowMainThreadQueries()
            //.addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSensorDao(db: AppDatabase) = db.rotacaoDao()

    @Provides
    @Singleton
    fun provideLocalizacaoDao(db: AppDatabase) = db.trajetoDao()

    @Provides
    @Singleton
    fun provideSetupDao(db: AppDatabase) = db.setupDao()

    @Provides
    @Singleton
    fun provideMinTrackVersionDao(db: AppDatabase) = db.hobitrackVersionDao()

    @Provides
    @Singleton
    fun provideEntregaDao(db : AppDatabase) = db.entregaDao()

    @Provides
    @Singleton
    fun provideEventoDao(db : AppDatabase) = db.eventoDao()

    @Provides
    @Singleton
    fun provideEmpresaDao(db : AppDatabase) = db.empresaDao()

    @Provides
    @Singleton
    fun provideObraDao(db : AppDatabase) = db.obraDao()

    @Provides
    @Singleton
    fun provideClienteDao(db : AppDatabase) = db.clienteDao()

    @Provides
    @Singleton
    fun provideContratoDao(db : AppDatabase) = db.contratoDao()

    @Provides
    @Singleton
    fun provideComprovanteDao(db : AppDatabase) = db.comprovanteDao()
}