package cz.covid19cz.app.db

import androidx.lifecycle.LiveData
import cz.covid19cz.app.db.ExpositionEntity

interface ExpositionRepository {

    val data: LiveData<List<ExpositionEntity>>

    fun add(expositions: List<ExpositionEntity>) : List<Long>
    fun add(exposition: ExpositionEntity) : Long
    fun delete(exposition: ExpositionEntity)
}

class ExpositionRepositoryImpl(private val dao: ExpositionDao) :
    ExpositionRepository {

    override val data = dao.findAll()

    override fun add(devices: List<ExpositionEntity>) : List<Long>{
        return dao.insertAll(devices)
    }

    override fun add(device: ExpositionEntity) : Long {
        return dao.insert(device)
    }

    override fun delete(exposition: ExpositionEntity) {
        dao.delete(exposition)
    }
}