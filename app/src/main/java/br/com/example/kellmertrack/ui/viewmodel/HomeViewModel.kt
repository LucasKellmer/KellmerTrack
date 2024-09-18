package br.com.example.kellmertrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.example.kellmertrack.BLAZONLABS
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.local.model.DTO.RotacaoDTO
import br.com.example.kellmertrack.local.model.DTO.UltimosDadosDTO
import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.local.model.mappers.RotacaoMapper
import br.com.example.kellmertrack.local.repository.ConfigRepository
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.EventoRepository
import br.com.example.kellmertrack.local.repository.HobitrackVersionRepository
import br.com.example.kellmertrack.local.repository.RotacaoRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val setupRepository: SetupRepository,
    private val rotacaoRepository: RotacaoRepository,
    private val hobitrackVersionRepository: HobitrackVersionRepository,
    private val eventoRepository : EventoRepository,
    private val entregaRepository: EntregaRepository,
    private val firebaseService: FirebaseService,
): ViewModel() {

    val iniciaBluetooth = MutableLiveData<Boolean>().apply { value = false }
    val locationServiceStatus = MutableLiveData<Boolean>().apply { value = false }
    val webSocketServiceStatus = MutableLiveData<Boolean>().apply { value = false }
    var logado = MutableLiveData<Boolean>().apply { value = false }

    val eventos = eventoRepository.getEventos()
    val setup = setupRepository.buscaSetupLocalLive()
    private val _conexaoBluetoothStatus = MutableLiveData<Boolean>().apply { value = false }
    val conexaoBluetoothStatus: MutableLiveData<Boolean>
        get() = _conexaoBluetoothStatus
    private val _broadcastReceiver = MutableLiveData<Boolean>().apply { value = false }
    val broadcastReceiver: MutableLiveData<Boolean>
        get() = _broadcastReceiver
    private val _versao = hobitrackVersionRepository.buscaUltimaVersao()
    val versao: LiveData<HobitrackVersionEntity>
        get() = _versao

    fun salvaLoginDispositivo(setup: SetupEntity) {
        Sistema.configuraSistema(
            setupEntity = setup
        )
    }

    fun buscaSetup(): SetupEntity? {
        return setupRepository.buscaSetup()
    }

    suspend fun buscaUltimosDados(): UltimosDadosDTO {
        val dados = rotacaoRepository.buscaUltimoRotacao()
        return UltimosDadosDTO(
            direcao = dados?.direcao,
            momento = dados?.momento,
            bateria = dados?.bateria,
            temperatura = dados?.temperatura,
        )
    }

    suspend fun salvaRotacao(rotacaoEntity: RotacaoEntity){
        Log.d(TAG, "=================== salvaRotacao: chamado ")
        val rotacaoDTO = RotacaoMapper().fromRotacaoEntityToDTO(rotacaoEntity)
        rotacaoRepository.salvaDadosRotacao(rotacaoEntity)
        firebaseService.enviaInformacaoDispositivoBluetoothFirebase(rotacaoDTO)
    }

    fun criaRotacaoEntity(bateria : Int, temperatura : Int, direcao : Int): RotacaoEntity? {
        val setup = setupRepository.buscaSetup()
        return if (setup != null){
            val entrega = entregaRepository.findEntregaAtiva()
            val rotacao = RotacaoEntity(
                id = UUID.randomUUID().toString(),
                veiculoId = setup.veiculosId,
                dispositivo = setup.numeroInterno,
                rpm = if(setup.modelo == BLAZONLABS) direcao else 0,
                momento = Date(),
                entregaId = entrega?.id,
                bateria = bateria,
                temperatura = temperatura,
                direcao = direcao,
            )
            rotacao
        } else null
    }
}