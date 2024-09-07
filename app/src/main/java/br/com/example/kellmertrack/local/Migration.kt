package br.com.example.kellmertrack.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(9, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE setup (" +
                    "id INTEGER NOT NULL,"+
                    "numero_interno TEXT NOT NULL,"+
                    "veiculo_id TEXT NOT NULL," +
                    "device_id INTEGER NOT NULL,"+
                    "mac TEXT NOT NULL,"+
                    "PRIMARY KEY(id));"
        )
    }
}
