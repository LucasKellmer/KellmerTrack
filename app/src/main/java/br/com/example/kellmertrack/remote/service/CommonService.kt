package br.com.example.kellmertrack.remote.service

import br.com.example.kellmertrack.local.model.DTO.ComprovanteAPI
import br.com.example.kellmertrack.local.model.DTO.DispositivoDTO
import br.com.example.kellmertrack.local.model.DTO.EntregaAPI
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface CommonService {
    @GET("/track/setup/{numeroInterno}")
    suspend fun buscarDispositivo(@Path("numeroInterno") numeroInterno: String?): Response<DispositivoDTO?>

    @PUT("/track/setup/{numeroInterno}")
    suspend fun atualizaDataVinculo(@Path("numeroInterno") numeroInterno : String?) : Response<Unit>

    @GET("/track/entrega/{entregaId}")
    suspend fun buscaEntrega(@Path("entregaId") entregaId: String?) : Response<EntregaAPI?>

    @POST("/track/entrega/{id}")
    suspend fun confirmaEntregaRecebida(@Path("id") id: Int?) : Response<Void>

    @POST("/track/entrega/{id}/{status}")
    suspend fun atualizaEntregaStatus(@Path("id") id : Int?, @Path("status") status : Int) : Response<Void>

    @POST("/track/descarregamento/{entregaId}")
    suspend fun descarregamento(@Path("entregaId") entregaId: String): Response<Void>

    @POST("/track/entregas/comprovante")
    suspend fun salvarComprovantes(@Body comprovante: List<ComprovanteAPI>): Response<Void>

    @POST("/track/entregas/comprovanteimg")
    @Multipart
    suspend fun salvarComprovanteImg(@Part comprovante:MultipartBody.Part): Response<Void>

}