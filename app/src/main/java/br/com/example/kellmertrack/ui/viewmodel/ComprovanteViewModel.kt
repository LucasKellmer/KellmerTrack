package br.com.example.kellmertrack.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.example.kellmertrack.TAG_TASK_COMPROVANTE
import br.com.example.kellmertrack.extensions.toBitmap
import br.com.example.kellmertrack.extensions.toByteArray
import br.com.example.kellmertrack.local.model.DTO.ComprovanteDTO
import br.com.example.kellmertrack.local.model.DTO.EntregaDTO
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.local.repository.ComprovanteRepository
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.services.tasks.JobCreator
import br.com.example.kellmertrack.services.tasks.TaskComprovante
import br.com.example.kellmertrack.services.tasks.TaskCreator
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class ComprovanteViewModel @Inject constructor(
    private val entregaRepository: EntregaRepository,
    private val comprovanteRepository: ComprovanteRepository,
    private val taskCreator: TaskCreator,
) : ViewModel(){

    private val _entrega = MutableLiveData<EntregaWithObra?>(null)
    val entrega : LiveData<EntregaWithObra?> = _entrega
    private val _currentBitmap = MutableLiveData<Bitmap?>()
    val currentBitmap : LiveData<Bitmap?> = _currentBitmap
    private val _context = MutableLiveData<Context?>()
    val context : LiveData<Context?> = _context

    val comprovante = MutableLiveData<ComprovanteDTO>()

    fun buscaEntrega(entregaId: String?){
        val entrega = entregaRepository.findEntregaById(entregaId.toString())
        _entrega.value = entrega
    }

    fun buscarComprovante(entregaId : Int?){
        comprovante.value = entregaId?.let { comprovanteRepository.findComprovanteByEntregaId(it) }
    }

    fun gerarComprovante(entregaDTO : EntregaDTO) {
        val novoComprovante = criaNovoComprovante(entregaDTO)
        comprovante.value = novoComprovante
    }

    fun criaNovoComprovante(entregaDTO: EntregaDTO) : ComprovanteDTO{
        return ComprovanteDTO(
            id = null,
            entregaId = entregaDTO.entregaId,
            contrato = entrega.value?.contratoEntity?.contratoEntity?.numero,
            cliente = entrega.value?.contratoEntity?.clienteEntity?.nome,
            obra = entrega.value?.contratoEntity?.obraEntity?.descricao,
            quantidade = entregaDTO.quantidade.toString(),
            recebedor = entregaDTO.recebedor,
            assinatura = entregaDTO.assinatura?.let { stringToBitmap(it).toByteArray() },
            uriComprovante = null,
            imgComprovante = null
        )
    }

    suspend fun confirmarEntrega(quantidade : String?){
        if (entrega.value != null && quantidade != null)
            entregaRepository.confirmarEntrega(entrega.value!!.entregaEntity?.id.toString(), quantidade)
    }

    suspend fun salvarComprovante(){
        if(comprovante.value != null){
            //val imgComprovante = fetchImageFromFileSystem()
            //comprovante.value?.imgComprovante = imgComprovante?.toByteArray()
            comprovanteRepository.salvarComprovante(comprovante.value!!)
            taskComprovante()
        }
    }

    fun setBitmap(bitmap : Bitmap?){
        _currentBitmap.value = bitmap
    }

    fun setContext(context : Context?){
        _context.value = context
    }

    fun geraImgComprovanteBitmap(view: View): Bitmap {
        try {
            val width = view.width
            val height = view.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            val uriImage = saveToFile("comprovante_${System.currentTimeMillis()}.png", bitmap.toByteArray(), context.value)
            Log.d("URI", uriImage)
            comprovante.value?.uriComprovante = uriImage
            return bitmap
        }catch (e : Exception){
            throw Exception("Erro ao gerar imagem do comprovante: ${e.printStackTrace()}")
        }
    }

    fun saveToFile(filename: String, imgBytes: ByteArray, context: Context?): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val contentResolver = context?.contentResolver
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = contentResolver?.insert(contentUri, contentValues)
        val outputStream = contentResolver?.openOutputStream(uri!!)
        val bitmap = imgBytes.toBitmap()
        if (outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
        }
        Log.d("URI", uri.toString())
        return uri.toString()
    }

    fun fetchImageFromFileSystem() : Bitmap?{
        return if (_currentBitmap.value != null){
            _currentBitmap.value
        } else if(comprovante.value?.uriComprovante != null){
            val uri = Uri.parse(comprovante.value?.uriComprovante)
            val inputStream = context.value?.contentResolver?.openInputStream(uri)
            val bitmap= BitmapFactory.decodeStream(inputStream)
            _currentBitmap.value = bitmap
            bitmap
        } else null
    }

    fun createComprovantePart(imageBytes: ByteArray): MultipartBody.Part {
        val requestBody = imageBytes.toRequestBody("application/zip".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("comprovante", "image.zip", requestBody)
    }

    private fun taskComprovante(){
        taskCreator.uniqueRequest(
            TaskComprovante::class,
            TAG_TASK_COMPROVANTE,
            emptyMap(),
        )
    }

    private fun stringToBitmap(string: String): Bitmap {
        val decodedBytes = Base64.decode(string, Base64.DEFAULT)
        return BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
    }
}