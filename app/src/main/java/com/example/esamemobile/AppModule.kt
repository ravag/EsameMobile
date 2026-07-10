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
import com.example.esamemobile.data.repositories.CharacterSolver
import com.example.esamemobile.data.repositories.SettingsRepository
import com.example.esamemobile.data.repositories.StaticDataRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import com.example.esamemobile.data.supabase.ImagesRepositoryImpl
import com.example.esamemobile.screens.characterCreation.CharacterCreationViewModel
import com.example.esamemobile.screens.characterDetails.CharacterDetailsViewModel
import com.example.esamemobile.screens.characterLevelUp.LevelUpViewModel
import com.example.esamemobile.screens.home.HomeViewModel
import com.example.esamemobile.screens.login.LoginViewModel
import com.example.esamemobile.screens.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")
val appModule = module {
    single { get<Context>().dataStore }
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Storage)
        }
    }
    single<Storage> { get<SupabaseClient>().storage }

    single { StaticDataRepository(androidContext()) }
    single { CharacterSolver(get()) }
    single { SettingsRepository(get()) }
    single<ImagesRepository> { ImagesRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    single<GroupRepository> { GroupRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(),get()) }

    viewModel { LoginViewModel(get(),get()) }
    viewModel { SettingsViewModel(get(),get(),get()) }
    viewModel { SessionViewModel(get(),get()) }
    viewModel { HomeViewModel(get(),get(), get()) }
    viewModel { CharacterDetailsViewModel(get(),get(),get(),get()) }
    viewModel { CharacterCreationViewModel(get(),get(),get(),get()) }
    viewModel { LevelUpViewModel(get(),get()) }
}