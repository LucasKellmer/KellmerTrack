package br.com.example.kellmertrack.remote.service

import android.util.Log
import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.model.DTO.EventoDTO
import br.com.example.kellmertrack.local.model.DTO.RotacaoDTO
import br.com.example.kellmertrack.local.model.DTO.TrajetoDTO
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity
import br.com.example.kellmertrack.local.repository.TrajetoRepository
import br.com.example.kellmertrack.remote.model.DispositivoStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class FirebaseService @Inject constructor(
    private val trajetoRepository: TrajetoRepository
) {

    private var listnerAtuApp: ListenerRegistration? = null

    fun enviaLocalizacaoFirebase(trajeto: TrajetoDTO): Boolean {
        return try {
            FirebaseFirestore.getInstance().collection("trajetos")
                .document(trajeto.id)
                .set(trajeto).addOnSuccessListener {
                    Log.d("Location", "Localização enviada com sucesso")
                    trajetoRepository.updateTrajetoSincronizado(trajeto.id)
                }
            Log.d("Location", "Resposta retornada")
            true
        } catch (e: Exception) {
            throw Exception("Erro ao enviar localização para o Firebase: ${e.message}")
        }
    }

    fun enviaRotacoesFirebase(sensor : RotacaoDTO) : Boolean{
        return try {
            FirebaseFirestore.getInstance().collection("rotacao")
                .document(sensor.id)
                .set(sensor)
            true
        }catch (e: Exception){
            throw Exception("Erro ao enviar informações do dispositivo para o Firebase: ${e.message}")
        }
    }

    //Eventos
    fun enviaEventoFirebase(evento : EventoDTO) : Boolean{
        return try {
            FirebaseFirestore.getInstance().collection("eventos")
                .document(evento.id.toString())
                .set(evento)
            true
        }catch (e : Exception){
            throw Exception("Erro ao enviar evento para o Firebase: ${e.message}")
        }
    }

    //Entregas
    fun enviaEntregasFireabase(entrega : EntregaEntity) : Boolean{
        return try {
            FirebaseFirestore.getInstance().collection("entregas")
                .document(entrega.id.toString())
                .set(entrega)
            true
        }catch (e : Exception){
            throw Exception("Erro ao enviar entrega para o Firebase: ${e.message}")
        }
    }

    fun criaDispostivoStatus(status: DispositivoStatus) {
        try {
            FirebaseFirestore.getInstance().collection("dispositivos")
                .document(status.dispositivo)
                .set(status)
        } catch (e: Exception) {
            Log.d(TAG, "Erro ao criar status dispositivo: ${e.message}")
        }
    }

    fun buscaAtuApp(
        atuVersion: (version: HobitrackVersionEntity) -> (Unit)
    ) {
        Log.d(TAG, "========== buscaAtuApp chamado ")
        if (listnerAtuApp != null) {
            listnerAtuApp!!.remove()
            listnerAtuApp = null
        }

        val docRef = FirebaseFirestore
            .getInstance()
            .collection("versoes")
            .document("versao")
        listnerAtuApp = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Erro na busca de versoes", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val hobitrackVersionEntity =
                    HobitrackVersionEntity(
                        snapshot.getLong("id")?.toInt(),
                        snapshot.getLong("versionCode")?.toInt(),
                        snapshot.getString("uri")
                    )
                Log.d(TAG, "================== hobitrackVersionEntity: $hobitrackVersionEntity ")
                if (hobitrackVersionEntity.versionCode != null && hobitrackVersionEntity.uri != null) {
                    if (hobitrackVersionEntity.versionCode > BuildConfig.VERSION_CODE) {
                        atuVersion(hobitrackVersionEntity)
                    }
                }
            } else {
                Log.e(TAG, "Erro na busca de versoes null")
            }
        }
    }
}