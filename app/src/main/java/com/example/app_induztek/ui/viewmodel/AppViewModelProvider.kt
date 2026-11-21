package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.app_induztek.InduztekApplication

object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            LoginViewModel(app.container.repository, app.container.session)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            RegistroViewModel(app.container.repository)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            RecuperarClaveViewModel(app.container.repository)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            CambiarClaveViewModel(app.container.repository, app.container.session, app.container.messages)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            PerfilViewModel(app.container.repository, app.container.session, app.container.messages)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            ShellViewModel(app.container.repository, app.container.session, app.container.messages)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            MedicionViewModel(app.container.repository, app.container.session, app.container.messages)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            TecnicoViewModel(app.container.repository, app.container.session, app.container.messages)
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InduztekApplication
            ClienteViewModel(app.container.repository, app.container.session, app.container.messages)
        }
    }
}
