package br.com.example.kellmertrack.ui.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.local.repository.ConfigRepository
import br.com.example.kellmertrack.local.repository.EmpresaRepository
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.ui.utils.BitmapConverter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val entregaRepository: EntregaRepository,
    private val empresaRepository: EmpresaRepository,
) : ViewModel() {

    fun criaMarcacoes(entregas: EntregaWithObra?, map: GoogleMap, empresa : EmpresaEntity?, context : Context) {
        entregas?.let { entrega ->
            if(entrega.contratoEntity.obraEntity != null){
                val localizacaoEntrega = LatLng(entrega.contratoEntity.obraEntity.latitude, entrega.contratoEntity.obraEntity.longitude)
                criaMarcador(map, localizacaoEntrega, "Localização da obra", R.drawable.entrega_icon, context)
            }
            if (empresa != null){
                val localizacaoEmpresa = LatLng(empresa.latitude, empresa.longitude)
                criaMarcador(map, localizacaoEmpresa, "Localização da usina", R.drawable.usina_icon, context)
            }
        }
    }

    private fun criaMarcador(map: GoogleMap, localizacao : LatLng, title : String, icon : Int, context : Context){
        val bitmapIcon = BitmapConverter().vectorToBitmap(context, icon)
        map.addMarker(
            MarkerOptions().position(localizacao).title(title).icon(bitmapIcon)
        )
        map.addCircle(
            CircleOptions()
                .center(localizacao)
                .strokeColor(ContextCompat.getColor(context, R.color.marker))
                .fillColor(ContextCompat.getColor(context, R.color.marker) )
                .radius(200.0)
        )
    }

    suspend fun buscaEntrega(): List<EntregaWithObra?>? {
        return entregaRepository.getEntregas().value
    }

    fun getSoEntregas(): EntregaWithObra? {
        return entregaRepository.getSoEntregas()
    }

    fun getEmpresa() : EmpresaEntity {
        return empresaRepository.getEmpresa()
    }
}