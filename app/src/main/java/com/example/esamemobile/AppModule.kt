package com.example.esamemobile

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.AuthRepositoryImpl
import com.example.esamemobile.data.repositories.SettingsRepository
import com.example.esamemobile.screens.login.LoginViewModel
import com.example.esamemobile.screens.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")
val appModule = module {
    single { get<Context>().dataStore }

    single { SettingsRepository(get()) }

    single { FirebaseAuth.getInstance() }

    single<AuthRepository> { AuthRepositoryImpl(get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { SettingsViewModel(get()) }
}