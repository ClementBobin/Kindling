package dev.kindling.core.components.ui.consent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.kindlingShapes

@Composable
fun KConsentCategoryRow(
    category: KConsentCategory,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.kindlingShapes.radiusMd
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = cs.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.kindlingShapes.radiusSm)
                    .background(cs.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface
                    )
                    if (category.isRequired) {
                        Surface(
                            shape = MaterialTheme.kindlingShapes.radiusSm,
                            color = cs.outlineVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Always Active",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = category.description,
                    fontSize = 12.sp,
                    color = cs.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = category.isRequired || isEnabled,
                onCheckedChange = if (category.isRequired) null else onToggle,
                enabled = !category.isRequired
            )
        }
    }
}