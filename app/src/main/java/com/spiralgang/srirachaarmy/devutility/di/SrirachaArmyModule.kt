package com.spiralgang.srirachaarmy.devutility.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * SrirachaArmyModule - Hilt dependency injection module
 * 
 * Provides all dependencies for the SrirachaArmy IDE system including:
 * - Bot orchestration services
 * - AI engines and prompts
 * - Storage and execution systems
 * - Terminal and SSH clients
 * - Accessibility services
 */
@Module
@InstallIn(SingletonComponent::class)
object SrirachaArmyModule {
    // All dependencies are handled via @Inject constructors
    // This module can be expanded to provide specific implementations
    // or external service configurations as needed
}