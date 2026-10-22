package com.autoclick.app.data

import com.autoclick.app.data.dao.ScriptDao
import com.autoclick.app.data.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow

class ScriptRepository(private val dao: ScriptDao) {

    fun observeAll(): Flow<List<ScriptEntity>> = dao.observeAll()

    suspend fun getById(id: Long): ScriptEntity? = dao.getById(id)

    suspend fun insert(entity: ScriptEntity): Long = dao.insert(entity)

    suspend fun update(entity: ScriptEntity) = dao.update(entity)

    suspend fun delete(entity: ScriptEntity) = dao.delete(entity)

    suspend fun count(): Int = dao.count()
}
