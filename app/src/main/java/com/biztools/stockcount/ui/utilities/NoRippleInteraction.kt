package com.biztools.stockcount.ui.utilities

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoRippleInteraction(override val interactions: Flow<Interaction> = emptyFlow()) :
    MutableInteractionSource {
    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction): Boolean = true

}