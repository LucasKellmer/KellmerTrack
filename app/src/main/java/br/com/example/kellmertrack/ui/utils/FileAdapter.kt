package br.com.example.kellmertrack.ui.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class FileAdapter @Inject constructor(
    @ApplicationContext private val context: Context
){
    data class FileWithBytes(val fileAndName: FileAndName, val bytes: ByteArray)
    data class FileAndName(val path: String, val name: String)

    fun getFileByUri(fileAndName: FileAndName): FileWithBytes? {
        return getFile(fileAndName)
    }

    fun zipFiles(filesBytes: List<FileWithBytes>): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).let { zipOutStream ->
                filesBytes.forEach { fileWithBytes ->
                    val entry = ZipEntry(fileWithBytes.fileAndName.name)
                    zipOutStream.putNextEntry(entry)
                    zipOutStream.write(fileWithBytes.bytes)
                    zipOutStream.closeEntry()
                }
            }
            outputStream.toByteArray()
        }
    }

    private fun getFile(fileAndName: FileAndName): FileWithBytes? {
        val androidUri = Uri.parse(fileAndName.path)
        return try {
            context.contentResolver.openInputStream(androidUri)?.let { inputStream ->
                FileWithBytes(fileAndName, inputStream.readBytes())
            }
        } catch (e: FileNotFoundException) {
            Log.e("getFile", "Não foi possível encontrar a imagem do comprovante", )
            null
        }
    }
}