package com.yourname.smartfinancialtracker.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartfinancialtracker.data.repository.AuthRepositoryImpl
import com.example.smartfinancialtracker.domain.repository.AuthRepository
import com.yourname.smartfinancialtracker.data.remote.firebase.FirebaseAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseAuthService = FirebaseAuthService(auth, firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: FirebaseAuthService
    ): AuthRepository = AuthRepositoryImpl(authService)
}