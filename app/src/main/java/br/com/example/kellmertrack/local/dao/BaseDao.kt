package br.com.example.kellmertrack.local.dao

interface BaseDao<Any> {

    suspend fun insert(Obj : Any)

    suspend fun delete(Obj : Any)
}