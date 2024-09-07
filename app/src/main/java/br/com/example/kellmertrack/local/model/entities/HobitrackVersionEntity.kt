package br.com.example.kellmertrack.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hobitrack_version")
data class HobitrackVersionEntity(
    @PrimaryKey(autoGenerate = false)
    val id : Int?,
    @ColumnInfo(name = "version_code")
    val versionCode : Int?,
    val uri : String?
) {

}