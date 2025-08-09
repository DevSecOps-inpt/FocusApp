package com.example.focuslock.di

import com.example.focuslock.services.focus.EnforcementGate
import com.example.focuslock.services.focus.EnforcementGateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    @Binds
    @Singleton
    abstract fun bindEnforcementGate(
        enforcementGateImpl: EnforcementGateImpl
    ): EnforcementGate
} 