package com.autoclick.app.engine

sealed class StepResult {
    data object Success : StepResult()
    data class Failed(val message: String) : StepResult()
}
