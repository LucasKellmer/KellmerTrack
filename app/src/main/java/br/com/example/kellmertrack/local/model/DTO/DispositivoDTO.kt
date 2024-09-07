package br.com.example.kellmertrack.local.model.DTO

import java.util.Date

data class DispositivoDTO (
    val numeroInterno : String,
    val mac : String,
    val modelo : String,
    val veiculo : String,
    val dataVinculo : Date?,
    val motoristaId : Int,
    val motoristaNome : String,
    val empresa : EmpresaDTO,
    ){
}