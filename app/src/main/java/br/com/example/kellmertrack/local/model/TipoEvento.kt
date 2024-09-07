package br.com.example.kellmertrack.local.model

enum class TipoEvento(val tipo : String) {
    ENTRADA("ENTRADA"),
    SAIDA("SAIDA"),
    PERMANECEU("PERMANECEU"),
    NOVA_ENTREGA("NOVA_ENTREGA"),
    ENTREGA_CONFIRMADA("ENTREGA_CONFIRMADA")
}