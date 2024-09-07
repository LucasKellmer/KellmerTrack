package br.com.example.kellmertrack.local.model.DTO

data class ClienteDTO(
    val id: Int,
    val nome: String,
    val cpf: String,
    val cnpj: String?,
    val email: String?
) {
}