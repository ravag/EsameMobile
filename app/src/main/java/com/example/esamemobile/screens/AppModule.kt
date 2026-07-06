package com.example.esamemobile.screens

import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.AuthRepositoryImpl
import com.example.esamemobile.screens.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { LoginViewModel(get()) }
}