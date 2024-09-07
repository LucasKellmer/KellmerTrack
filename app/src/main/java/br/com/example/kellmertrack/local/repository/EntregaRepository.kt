package br.com.example.kellmertrack.local.repository

import android.util.Log
import androidx.lifecycle.LiveData
import br.com.example.kellmertrack.CHEGADA_OBRA
import br.com.example.kellmertrack.CHEGADA_USINA
import br.com.example.kellmertrack.SAIDA_OBRA
import br.com.example.kellmertrack.SAIDA_USINA
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.dao.EmpresaDao
import br.com.example.kellmertrack.local.dao.EventoDao
import br.com.example.kellmertrack.local.dao.ObraDao
import br.com.example.kellmertrack.local.dao.TrajetoDao
import br.com.example.kellmertrack.local.model.DTO.EntregaAPI
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.remote.model.RemoteResponse
import br.com.example.kellmertrack.remote.model.Status
import br.com.example.kellmertrack.local.dao.EntregaDao
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.local.model.mappers.ClienteMapper
import br.com.example.kellmertrack.local.model.mappers.ContratoMapper
import br.com.example.kellmertrack.local.model.mappers.EntregaMapper
import br.com.example.kellmertrack.local.model.mappers.ObraMapper
import br.com.example.kellmertrack.remote.service.CommonService
import br.com.example.kellmertrack.remote.service.FirebaseService
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import javax.inject.Inject

class EntregaRepository @Inject constructor(
    private val commonService : CommonService,
    private val entregaDao: EntregaDao,
    private val obraDao: ObraDao,
    private val empresaDao: EmpresaDao,
    private val eventoDao: EventoDao,
    private val trajetoDao: TrajetoDao,
    private val firebaseService: FirebaseService,
    private val contratoRepository: ContratoRepository,
    private val obraRepository: ObraRepository,
    private val clienteRepository: ClienteRepository,
) {

    fun findEntregaById(id: String) : EntregaWithObra?{
        return entregaDao.findEntregaById(id)
    }

    fun findEntregaAtiva(): EntregaEntity?{
        return entregaDao.findEntregaAtiva()
    }

    suspend fun salvarEntrega(response : RemoteResponse<EntregaAPI?>) : String?{
        var result: String? = null
        if (response.status == Status.SUCCESS){
            if (response.data != null){
                Log.d(TAG, "entrega encontrada: ${response.data}")
                val entrega = response.data
                entregaDao.insert(EntregaMapper().toEntregaEntity(entrega))
                contratoRepository.salvar(ContratoMapper().toContratoEntity(entrega.contrato))
                obraRepository.salvar(ObraMapper().toObraEntity(entrega.contrato.obra))
                clienteRepository.salvar(ClienteMapper().toClienteEntity(entrega.contrato.cliente))
                atualizaEntregaStatus(entrega.id, 1)

            }else{
                println("Não há entregas no momento!")
                result = "Não há entregas no momento"
            }
        }else {
            println("Não foi possível buscar as entregas")
            result = "Ocorreu um erro ao buscar as entregas"
        }
        return result
    }

    suspend fun buscaEntregaAPI(entregaId : String): RemoteResponse<EntregaAPI?> {
        return try {
            val response = commonService.buscaEntrega(entregaId)
            println("response: $response")
            if (response.body() != null) {
                val entregaResponse = response.body()
                RemoteResponse.success(entregaResponse)
            } else
                RemoteResponse.error(response.code().toString(), null)
        }catch (e: java.lang.Exception) {
            RemoteResponse.error("Entrega não encontrada", null)
        }
    }

    suspend fun atualizaEntregaStatus(id : Int, status : Int){
        entregaDao.updateEntregaStatus(id, status)
        commonService.atualizaEntregaStatus(id, status)
    }

    fun obterLocalizacoesEntregas(): MutableMap<String, LatLng>{
        val localizacoes = HashMap<String, LatLng>()
        val entregas = entregaDao.getEntregas()
        val empresa = empresaDao.getEmpresa()
        localizacoes["0"] = LatLng(empresa.latitude,empresa.longitude )
        entregas?.let {
            if (it.entregaEntity != null && it.contratoEntity.obraEntity != null) {
                localizacoes[it.entregaEntity.id.toString()] = LatLng(it.contratoEntity.obraEntity.latitude, it.contratoEntity.obraEntity.longitude)
            }
        }
        return localizacoes
    }

    fun getEntregas(): LiveData<List<EntregaWithObra?>> = entregaDao.entregasLiveData()

    fun getSoEntregas(): EntregaWithObra? = entregaDao.getEntregas()

    suspend fun limpaEntregas() = entregaDao.deleteAll()

    suspend fun confirmaEntregaRecebida(entregaId: Int) = commonService.confirmaEntregaRecebida(entregaId)

    suspend fun confirmarEntrega(entregaId : String, quantidade : String){
        try {
            entregaDao.confirmarEntrega(entregaId, quantidade)
            val entregaAtualizada = findEntregaById(entregaId)
            if(firebaseService.enviaEntregasFireabase(entregaAtualizada?.entregaEntity!!))
                entregaDao.updateEntregaSincronizado(entregaId)
        }catch (e : Exception){
            Log.d(TAG, "Erro ao confirmar entrega: ${e.message}")
        }
    }

    suspend fun enviaEntregaFirebase(){
        entregaDao.getEntregasNaoSincronizadas()?.forEach { entrega ->
            if(firebaseService.enviaEntregasFireabase(entrega))
                entregaDao.updateEntregaSincronizado(entrega.id.toString())
        }
    }

    suspend fun atualizaMomentoSaidaUsina(momento : Date, entregaId: String){
        entregaDao.atualizaMomentoSaidaUsina(entregaId, momento)
    }

    suspend fun atualizaMomentoSaidaObra(momento : Date, entregaId: String){
        entregaDao.atualizaMomentoSaidaObra(entregaId, momento)
    }

    suspend fun atualizaMomentoChegadaUsina(momento : Date, entregaId: String){
        entregaDao.atualizaMomentoChegadaUsina(entregaId, momento)
    }

    suspend fun atualizaMomentoChegadaObra(momento : Date, entregaId: String){
        entregaDao.atualizaMomentoChegadaObra(entregaId, momento)
    }

    suspend fun atualizaMomentoChegadaSaida(momento: Date, entregaId : String, tipo : String){
        when(tipo){
            CHEGADA_USINA -> atualizaMomentoChegadaUsina(momento, entregaId)
            CHEGADA_OBRA -> atualizaMomentoChegadaObra(momento, entregaId)
            SAIDA_USINA -> atualizaMomentoSaidaUsina(momento, entregaId)
            SAIDA_OBRA -> atualizaMomentoSaidaObra(momento, entregaId)
        }
    }

    suspend fun decarregamento(entregaId : String): RemoteResponse<Unit> {
        return try {
            Log.d(TAG, "decarregamento chamado: entrega recebido como parametro: $entregaId")
            val response = commonService.descarregamento(entregaId)
            println("descarregamento response: $response")
            if (response.isSuccessful) {
                RemoteResponse.success(null)
            } else
                RemoteResponse.error(response.message().toString(), null)
        }catch (e: java.lang.Exception) {
            RemoteResponse.error("Erro ao realizar descarregamento: ${e.message}", null)
        }
    }

    suspend fun limpaEntregaCompleto(): Boolean {
        return try {
            entregaDao.limpaEntrega()
            trajetoDao.limpaTrajetos()
            eventoDao.limpaEvento()
            obraDao.deleteAll()
            return verificaRegistros()
        }catch (e: Exception){
            Log.d(TAG, "Não foi possível realizar o descarregamento completo: ${e.message}")
            false
        }
    }

    fun verificaRegistros() : Boolean{
        val entregas = entregaDao.getEntregas()
        val trajetos = trajetoDao.getLocalizacao()
        val eventos = eventoDao.getEventos()
        return !(entregas?.entregaEntity == null || trajetos.isNullOrEmpty() || eventos.isNullOrEmpty())
    }
}
