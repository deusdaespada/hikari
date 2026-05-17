package eu.kanade.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

@Composable
fun LogoHeader(
    versionName: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.padding.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        val waveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.padding.medium)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val center = Offset(x = size.width, y = size.height / 2f)
                    for (radius in listOf(40.dp, 80.dp, 120.dp, 160.dp, 200.dp, 240.dp)) {
                        drawCircle(
                            color = waveColor,
                            radius = radius.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                    }
                },
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.Transparent
            ),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush)
                    .padding(vertical = 28.dp, horizontal = MaterialTheme.padding.extraLarge),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hikari),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(80.dp),
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    if (versionName == null) {
                        Text(
                            text = stringResource(MR.strings.welcome_back),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(MR.strings.welcome_back_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.secondaryItemAlpha(),
                        )
                    } else {
                        Text(
                            text = stringResource(MR.strings.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val parsedVersion = if (versionName.contains(" (")) {
                            versionName.substringBefore(" (").substringAfter("Stable ")
                        } else {
                            versionName.substringAfter("Stable ")
                        }

                        val parsedBuildTime = if (versionName.contains(" (")) {
                            versionName.substringAfter(" (").substringBefore(")")
                        } else {
                            null
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = stringResource(MR.strings.about_stable_badge),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = parsedVersion,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.secondaryItemAlpha(),
                            )
                        }

                        if (parsedBuildTime != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = parsedBuildTime,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.secondaryItemAlpha(),
                            )
                        }
                    }
                }
            }
        }
    }
}
