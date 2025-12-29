package com.jetbrains.toolbox.sample.environment

import com.jetbrains.toolbox.api.remoteDev.EnvironmentVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProviderEnvironment
import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView
import com.jetbrains.toolbox.api.remoteDev.states.EnvironmentDescription
import com.jetbrains.toolbox.api.remoteDev.states.RemoteEnvironmentState
import com.jetbrains.toolbox.api.remoteDev.states.StandardRemoteEnvironmentState
import kotlinx.coroutines.flow.MutableStateFlow
import com.jetbrains.toolbox.api.localization.LocalizableStringFactory
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive wrapper around [EnvironmentConfig] that satisfies the Toolbox API.
 */
class RemoteEnvironment(
  private val initialConfig: EnvironmentConfig,
  private val contentsViewFactory: EnvironmentContentsViewFactory = ManualContentsViewFactory(),
  private val localizableStringFactory: LocalizableStringFactory
) : RemoteProviderEnvironment(initialConfig.id) {

  // Mutable internal state
  private var _currentConfig: EnvironmentConfig = initialConfig
  private val _state = MutableStateFlow<RemoteEnvironmentState>(StandardRemoteEnvironmentState.Active)
  private val _description = MutableStateFlow<EnvironmentDescription>(
    EnvironmentDescription.General(initialConfig.description?.let { localizableStringFactory.ptrl(it) })
  )
  private var _visible: Boolean = false

  //  Public reactive properties (observed by Toolbox UI)
  override var name: String
    get() = _currentConfig.name
    set(value) {
      _currentConfig = _currentConfig.copy(name = value)
    }

  override val state: StateFlow<RemoteEnvironmentState> = _state.asStateFlow()

  override val description: StateFlow<EnvironmentDescription> = _description.asStateFlow()

  override suspend fun getContentsView(): EnvironmentContentsView {
    return contentsViewFactory.create(_currentConfig)
  }

  override fun setVisible(visibilityState: EnvironmentVisibilityState) {
    _visible = visibilityState.statusVisible
  }

  override fun onDelete() {
    // It's recommended that you add any cleanup logic here.
  }

  // Update methods (called by Repository)
  fun updateState(newState: RemoteEnvironmentState, errorMessage: String? = null) {
    _state.update { newState }
    if (errorMessage != null) {
      _description.update { EnvironmentDescription.General(localizableStringFactory.ptrl(errorMessage)) }
    }
  }

  fun updateConfig(newConfig: EnvironmentConfig) {
    require(newConfig.id == initialConfig.id) { "Cannot change environment ID" }
    _currentConfig = newConfig
    _description.update {
      EnvironmentDescription.General(newConfig.description?.let { text ->
        localizableStringFactory.ptrl(text)
      })
    }
  }

  fun getConfig(): EnvironmentConfig = _currentConfig
}

/**
 * Extension function to create RemoteEnvironment from config.
 */
fun EnvironmentConfig.toRemoteEnvironment(
  factory: EnvironmentContentsViewFactory = ManualContentsViewFactory(),
  localizableStringFactory: LocalizableStringFactory,
): RemoteEnvironment {
  return RemoteEnvironment(this, factory, localizableStringFactory)
}