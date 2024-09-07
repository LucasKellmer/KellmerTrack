package br.com.example.kellmertrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.local.repository.SetupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val setupRepository: SetupRepository
) : ViewModel() {

    fun buscaDispositivo(numeroInterno: String) = liveData(Dispatchers.IO) {
        val teste = setupRepository.buscaSetup(numeroInterno)
        Log.d("viewModel", "buscaDispositivo viewModel: $teste")
        emit(setupRepository.buscaSetup(numeroInterno))
    }

    suspend fun salvaDispositivo(dispositivo: SetupEntity) {
        return setupRepository.salvaDispositivo(dispositivo)
    }
}