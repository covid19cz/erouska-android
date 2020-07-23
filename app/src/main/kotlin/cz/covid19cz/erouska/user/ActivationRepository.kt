package cz.covid19cz.erouska.user

import kotlinx.coroutines.delay

interface ActivationRepository {

    suspend fun activate(): ActivationResponse

}

class ActivationRepositoryImpl : ActivationRepository {

    override suspend fun activate(): ActivationResponse {
        // TODO Call registerEhrid
        // Mock delay
        delay(2000)
        return ActivationResponse.SUCCESS
    }

}