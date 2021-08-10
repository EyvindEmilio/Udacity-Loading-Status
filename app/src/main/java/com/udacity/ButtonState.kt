package com.udacity


sealed class ButtonState {
    object Initial : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}