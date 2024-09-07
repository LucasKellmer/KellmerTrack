package br.com.example.kellmertrack.local.location

import android.location.Location
import android.location.LocationManager
import android.util.Log
import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.model.TipoEvento
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import java.util.concurrent.TimeUnit

class KellmerTrackGeofence(
    //O valor da String é o id da entrega
    private val areasGeofence : MutableMap<String, LatLng>,
    private val raio : Int,
) {

    private var usinaExited: Boolean = false
    private var lastLocation : Location? = null
    private var momentoEntrada : Date? = null

    //O valor da String é a latitude e longitude da localização
    private var locationsEntered: MutableMap<String, LatLng> = mutableMapOf()
    //O valor da String é o id da entrega
    private var entregaLocationEntered : MutableMap<String, String> = mutableMapOf()

    fun novaLocalizacao(location : Location) : List<GeofenceTransition>{
        Log.d(TAG, "usinaExited: $usinaExited")
        Log.d(TAG, "====================== novaLocalizacao: $location")
        val results = mutableListOf<GeofenceTransition>()

        //Verifica se a localização passada como parâmetro está dentro do raio de alguma obra
        //Se estiver cria uma transição geofence
        this.entrouNaArea(location).forEach {
            Log.d(TAG, "it")
            var tempo : Long = 0
            if(momentoEntrada != null && lastLocation != null){
                tempo = TimeUnit.MILLISECONDS.toSeconds(lastLocation!!.time - momentoEntrada!!.time)
            }

            Log.d(TAG, "GEOFENCE TEMPO $tempo")

            Log.d(TAG, "============== obterProximas : ${it.value}")
            locationsEntered[it.key] = it.value

            //Se a área entrada for diferente
            Log.d(TAG, "============= locationsEntered[it.key]: ${locationsEntered[it.key]}")
            Log.d(TAG, "============= entrouNaArea => it.key: ${it.key}")
            if(entregaLocationEntered[it.key] != it.key && usinaExited){
                val entrada = criaTransicao(TipoEvento.ENTRADA, it.key, LatLng(location.latitude, location.longitude))
                results.add(entrada)
                entregaLocationEntered[it.key] = it.key
                momentoEntrada = Date()
            }else{
                //Quando permanecer dentro de uma área que não for da usina
                if (tempo > 30 && (it.key != "0")){
                    val transicao = criaTransicao(TipoEvento.PERMANECEU, it.key, LatLng(location.latitude, location.longitude))
                    results.add(transicao)
                    momentoEntrada = null
                }
            }
        }

        //Verifica se não está mais no raio da obra
        val remove = this.locationsEntered.filter {
            val enteredLocation = criaLocalizacao(it.value)
            val distancia = distancia(location, enteredLocation)
            (distancia > raio) && (it.key != "0")
        }

        remove.forEach {
            this.locationsEntered.remove(it.key)
            val saida = criaTransicao(TipoEvento.SAIDA, it.key, LatLng(location.latitude, location.longitude))
            results.add(saida)
            momentoEntrada = null
            if(it.key == entregaLocationEntered[it.key])
                this.entregaLocationEntered.remove(it.key)
        }

        // Verifica se a localização atual está em uma distância maior do que o raio da usina
        // Se estiver, cria o evento SAÍDA da usina
        if (!usinaExited) {
            val distanciaUsina = distancia(location, criaLocalizacao(areasGeofence["0"]!!))
            if (distanciaUsina > raio && ((location.accuracy < 20 && location.speed >= 10) || (BuildConfig.DEBUG))) {
                usinaExited = true
                results.add(
                    GeofenceTransition(
                        TipoEvento.SAIDA,
                        "0",
                        LatLng(location.latitude, location.longitude),
                        Date()
                    )
                )
            }
        }
        lastLocation = location
        return results
    }

    private fun criaTransicao(tipoEvento: TipoEvento, entregaId: String, latLng: LatLng) : GeofenceTransition {
        return GeofenceTransition(
            tipoEvento,
            entregaId,
            latLng,
            Date()
        )
    }

    private fun criaLocalizacao(latLng: LatLng): Location {
        return Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
    }

    private fun entrouNaArea(atual: Location) = this.areasGeofence.filter {
        val location = criaLocalizacao(it.value)
        if (it.key == "0")
            if(usinaExited)
                distancia(location, atual) <= raio - 50
            else
                distancia(location, atual) <= raio + 50
        else
            distancia(location, atual) <= raio //&& (this.locationsEntered[it.key] == null) Precisei comentar pra fazer funcionar o tempo
    }

    private fun distancia(origem: Location, destino: Location) = origem.distanceTo(destino)
}
