package com.example.esamemobile

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.AuthRepositoryImpl
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepositoryImpl
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepositoryImpl
import com.example.esamemobile.data.firebase.firestore.UserRepository
import com.example.esamemobile.data.firebase.firestore.UserRepositoryImpl
import com.example.esamemobile.data.repositories.SettingsRepository
import com.example.esamemobile.screens.login.LoginViewModel
import com.example.esamemobile.screens.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")
val appModule = module {
    single { get<Context>().dataStore }
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }


    single { SettingsRepository(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    single<GroupRepository> { GroupRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(),get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { SettingsViewModel(get(),get()) }
    viewModel { SessionViewModel(get(),get()) }
}