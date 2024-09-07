package br.com.example.kellmertrack.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {

    private var _componentes: MutableLiveData<ComponentesFragments> = MutableLiveData<ComponentesFragments>().also {
        it.value = ComponentesFragments()
    }
    val componentes: LiveData<ComponentesFragments> get() = _componentes

    fun setBottomBar(componentes: ComponentesFragments) {
        this._componentes.value = componentes
    }
}

class ComponentesFragments(
    val bottomBar: Boolean = false
)