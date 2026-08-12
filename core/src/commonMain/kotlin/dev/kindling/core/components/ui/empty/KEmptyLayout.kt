package dev.kindling.core.components.ui.empty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KEmptyHeader(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun KEmptyMedia(
    modifier: Modifier = Modifier,
    variant: KEmptyMediaVariant = KEmptyMediaVariant.Icon,
    size: Dp = 56.dp,
    iconBoxColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit
) {
    val boxMod = when (variant) {
        KEmptyMediaVariant.Icon   -> modifier.size(size).clip(RoundedCornerShape(12.dp)).background(iconBoxColor).padding(12.dp)
        KEmptyMediaVariant.Avatar -> modifier.size(size).clip(CircleShape)
        KEmptyMediaVariant.Image  -> modifier.size(size)
    }
    Box(modifier = boxMod, contentAlignment = Alignment.Center, content = content)
}

@Composable
fun KEmptyTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
}

@Composable
fun KEmptyDescription(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.padding(horizontal = 16.dp), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp)
}

@Composable
fun KEmptyContent(modifier: Modifier = Modifier, verticalSpacing: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(8.dp))
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(verticalSpacing), content = content)
}