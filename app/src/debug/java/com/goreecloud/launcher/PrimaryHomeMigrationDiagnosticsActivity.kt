package com.goreecloud.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import com.goreecloud.launcher.core.workspace.db.LauncherDatabaseProvider
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeGridMigrationObservation
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeGridMigrationObservationState
import com.goreecloud.launcher.core.workspace.db.WorkspacePrimaryHomeGridMigrationPresentation
import com.goreecloud.launcher.ui.theme.GlazeMetrics
import com.goreecloud.launcher.ui.theme.GlazeTheme
import com.goreecloud.launcher.ui.theme.GlazeThemeMode

class PrimaryHomeMigrationDiagnosticsActivity : ComponentActivity() {
    private lateinit var observation: WorkspacePrimaryHomeGridMigrationObservation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workspaceRepository = WorkspaceRepository(this)
        observation = WorkspacePrimaryHomeGridMigrationObservation(
            authorityRepository = workspaceRepository,
            workspaceDaoProvider = {
                LauncherDatabaseProvider.get(this).workspaceDao()
            },
        )

        setContent {
            val state by observation.observe().collectAsStateWithLifecycle(
                initialValue = WorkspacePrimaryHomeGridMigrationObservationState.WaitingForRoom,
            )
            GlazeTheme(GlazeThemeMode.SYSTEM) {
                PrimaryHomeMigrationDiagnosticsSurface(
                    state = state,
                    onClose = ::finish,
                )
            }
        }
    }
}

@Composable
private fun PrimaryHomeMigrationDiagnosticsSurface(
    state: WorkspacePrimaryHomeGridMigrationObservationState,
    onClose: () -> Unit,
) {
    val presentation = diagnosticsPresentation(state)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(GlazeMetrics.space5),
            verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space5),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Primary Home migration",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Development diagnostics · review only",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = presentation.badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(GlazeMetrics.space5),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space3),
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = presentation.detail,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(GlazeMetrics.radiusExtraLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(GlazeMetrics.space5),
                    verticalArrangement = Arrangement.spacedBy(GlazeMetrics.space2),
                ) {
                    Text(
                        text = "No migration authority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "This debug surface can only read bounded migration evidence. It cannot execute the planner, write Room, change coordinates or page rank, promote workspace authority, or start a migration.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GlazeMetrics.comfortableTarget),
            ) {
                Text("Close")
            }
        }
    }
}

private data class DiagnosticsPresentation(
    val badge: String,
    val title: String,
    val detail: String,
)

private fun diagnosticsPresentation(
    state: WorkspacePrimaryHomeGridMigrationObservationState,
): DiagnosticsPresentation = when (state) {
    WorkspacePrimaryHomeGridMigrationObservationState.WaitingForRoom -> DiagnosticsPresentation(
        badge = "Waiting",
        title = "Waiting for authoritative Room state",
        detail = "Migration evidence is withheld until Room is the terminal workspace authority.",
    )

    is WorkspacePrimaryHomeGridMigrationObservationState.Evidence -> DiagnosticsPresentation(
        badge = when (state.presentation.status) {
            WorkspacePrimaryHomeGridMigrationPresentation.Status.READY -> "Ready"
            WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED -> "Current"
            WorkspacePrimaryHomeGridMigrationPresentation.Status.BLOCKED -> "Blocked"
        },
        title = state.presentation.title,
        detail = state.presentation.detail,
    )

    is WorkspacePrimaryHomeGridMigrationObservationState.Unavailable -> DiagnosticsPresentation(
        badge = "Unknown",
        title = "Migration evidence unavailable",
        detail = when (state.reason) {
            WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.ROOM_UNAVAILABLE ->
                "The authoritative Room workspace could not be opened for read-only diagnostics."
            WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.MISSING_PRIMARY_PAGE ->
                "The canonical protected primary Home page is missing from the authoritative workspace."
            WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.READ_FAILED ->
                "The authoritative workspace could not be read safely for migration diagnostics."
        },
    )
}
