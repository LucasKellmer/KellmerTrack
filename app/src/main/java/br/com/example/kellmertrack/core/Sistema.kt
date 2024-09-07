package br.com.example.kellmertrack.core

import br.com.example.kellmertrack.local.model.entities.SetupEntity

object Sistema{
    private var setup: SetupEntity? = null

    fun configuraSistema(setupEntity: SetupEntity?) {
        setup = setupEntity
    }

    fun getSetup() = setup

    fun deslogar(){
        setup = null
    }
}