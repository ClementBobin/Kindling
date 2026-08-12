package dev.kindling.core.components.ui.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable fun KSmallSpacer()      = Spacer(Modifier.height(8.dp))
@Composable fun KMediumSpacer()     = Spacer(Modifier.height(16.dp))
@Composable fun KLargeSpacer()      = Spacer(Modifier.height(24.dp))
@Composable fun KExtraLargeSpacer() = Spacer(Modifier.height(32.dp))
@Composable fun KCustomSpacer(height: Dp) = Spacer(Modifier.height(height))