package cz.covid19cz.app.db

import androidx.lifecycle.LiveData
import io.reactivex.Single

interface DatabaseRepository {

    val data: Single<List<ExpositionEntity>>

    fun add(exposition: ExpositionEntity) : Long
    fun delete(exposition: ExpositionEntity)
}

class ExpositionRepositoryImpl(private val dao: ExpositionDao) :
    DatabaseRepository {

    override val data: Single<List<ExpositionEntity>> = dao.getAll()

    override fun add(device: ExpositionEntity) : Long {
        return dao.insert(device)
    }

    override fun delete(exposition: ExpositionEntity) {
        dao.delete(exposition)
    }
}