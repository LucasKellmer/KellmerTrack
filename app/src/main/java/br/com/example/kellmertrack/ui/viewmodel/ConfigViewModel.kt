package br.com.example.kellmertrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.local.repository.ConfigRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private var configRepository : ConfigRepository,
    private var setupRepository: SetupRepository
): ViewModel(){

    fun buscaDispositivoByNumeroInterno(numeroInterno: String?) = liveData(Dispatchers.IO) {
        emit(configRepository.buscaAtualizacaoCadastro(numeroInterno))
    }

    suspend fun atualizaDispositivo(mac : String){
        configRepository.atualizaMacDispositivo(mac)
    }

    suspend fun buscaSetup() : SetupEntity? {
        return setupRepository.buscaSetup()
    }
}