package com.example.focuslock.features.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslock.core.permissions.Permissions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    init {
        checkPermissions()
    }
    
    private fun checkPermissions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                usageGranted = Permissions.hasUsageAccess(context),
                overlayGranted = Permissions.canDrawOverlays(context)
            )
        }
    }
    
    fun next() {
        val currentStep = _state.value.step
        val nextStep = when (currentStep) {
            OnboardingStep.Welcome -> OnboardingStep.UsageAccess
            OnboardingStep.UsageAccess -> OnboardingStep.Overlay
            OnboardingStep.Overlay -> OnboardingStep.Accessibility
            OnboardingStep.Accessibility -> OnboardingStep.Battery
            OnboardingStep.Battery -> OnboardingStep.VPN
            OnboardingStep.VPN -> OnboardingStep.Done
            OnboardingStep.Done -> OnboardingStep.Done
        }
        _state.value = _state.value.copy(step = nextStep)
    }
    
    fun requestUsage() {
        Permissions.openUsageAccess(context)
    }
    
    fun requestOverlay() {
        Permissions.openOverlaySettings(context)
    }
    
    fun requestBattery() {
        Permissions.requestIgnoreBattery(context)
    }
    
    fun requestVpnConsent() {
        // TODO: Implement VPN consent flow
        next()
    }
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val usageGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val batteryOptimized: Boolean = true,
    val vpnConsented: Boolean = false
)

enum class OnboardingStep {
    Welcome,
    UsageAccess,
    Overlay,
    Accessibility,
    Battery,
    VPN,
    Done
}
