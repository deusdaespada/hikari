package eu.kanade.presentation.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.presentation.components.AdaptiveSheet
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

@Composable
fun UpdatesDeleteConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AdaptiveSheet(
        onDismissRequest = onDismissRequest,
        header = {
            Text(
                text = stringResource(MR.strings.confirm_delete_chapters),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.padding.medium,
                        top = MaterialTheme.padding.small,
                        end = MaterialTheme.padding.medium,
                        bottom = MaterialTheme.padding.small,
                    ),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.padding.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            Text(
                text = "Are you sure you want to delete these downloaded chapters?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.secondaryItemAlpha(),
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfirm()
                        onDismissRequest()
                    },
                ) {
                    Text(text = stringResource(MR.strings.action_ok))
                }
            }
        }
    }
}
