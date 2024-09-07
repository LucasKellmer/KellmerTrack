package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.KellmertrackVersionDao
import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class HobitrackVersionRepository @Inject constructor(
    private val kellmertrackVersionDao: KellmertrackVersionDao
) {

    fun salvaTrackVersion(hobtrackVersionEntity: HobitrackVersionEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            kellmertrackVersionDao.insert(hobtrackVersionEntity)
        }
    }

    fun buscaUltimaVersao() = kellmertrackVersionDao.getUltimaVersao()

}