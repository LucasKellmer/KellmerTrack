package br.com.example.kellmertrack.local.model.DTO

data class ContratoDTO(
    val numero : String,
    val empresa : String,
    //val cliente : Int,
    //val obra : Int
    val cliente : ClienteDTO,
    val obra : ObraDTO
) {
}