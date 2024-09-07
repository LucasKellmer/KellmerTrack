package br.com.example.kellmertrack.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.local.repository.EntregaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EntregaViewModel @Inject constructor(
    private val entregaRepository: EntregaRepository,
): ViewModel() {

    private val _entregaSelecionada  = MutableLiveData<EntregaWithObra?>()
    val entregaSelecionada : LiveData<EntregaWithObra?> = _entregaSelecionada
    val entregas = entregaRepository.getEntregas()

    fun setEntregaSelecionada(entrega : EntregaWithObra?){
        _entregaSelecionada.value = entrega
    }
}