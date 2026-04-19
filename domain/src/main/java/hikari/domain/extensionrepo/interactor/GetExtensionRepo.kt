package hikari.domain.extensionrepo.interactor

import hikari.domain.extensionrepo.model.ExtensionRepo
import hikari.domain.extensionrepo.repository.ExtensionRepoRepository
import kotlinx.coroutines.flow.Flow

class GetExtensionRepo(
    private val repository: ExtensionRepoRepository,
) {
    fun subscribeAll(): Flow<List<ExtensionRepo>> = repository.subscribeAll()

    suspend fun getAll(): List<ExtensionRepo> = repository.getAll()
}
