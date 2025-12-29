package com.jetbrains.toolbox.sample

import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.remoteDev.ProviderVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProvider
import com.jetbrains.toolbox.api.remoteDev.RemoteProviderEnvironment
import kotlinx.coroutines.flow.Flow
import java.net.URI

/**
 * Remote Provider implementation that delegates environment management
 * to [EnvironmentRepository].
 */
class SampleRemoteProvider(
    repository: EnvironmentRepository
) : RemoteProvider("Sample Provider") {

    override val environments: Flow<LoadableState<List<RemoteProviderEnvironment>>> =
        repository.environments

    override val canCreateNewEnvironments: Boolean = true
    override val isSingleEnvironment: Boolean = false

    override fun setVisible(visibilityState: ProviderVisibilityState) {}

    // It's recommended you implement this handleURI method to support opening environments from a link
    override suspend fun handleUri(uri: URI) {}

    override fun close() {}
}
