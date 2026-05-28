package com.iptvapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.iptvapp.data.model.EpgProgram
import com.iptvapp.ui.theme.*

/** Minimal EPG row showing current + next program for a channel. */
@Composable
fun EpgRow(
    current: EpgProgram?,
    next: EpgProgram?,
    modifier: Modifier = Modifier
) {
    if (current == null) return
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .background(LiveRed, RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("AHORA", style = MaterialTheme.typography.labelSmall, color = OnDark)
            }
            Spacer(Modifier.width(8.dp))
            Text(current.title, style = MaterialTheme.typography.bodyMedium, color = OnDark)
            Spacer(Modifier.weight(1f))
            Text(current.timeLabel, style = MaterialTheme.typography.labelSmall, color = OnDarkMuted)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { current.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = NetflixRed,
            trackColor = SurfaceElevated
        )
        next?.let {
            Spacer(Modifier.height(4.dp))
            Row {
                Text("A continuación: ", style = MaterialTheme.typography.bodySmall, color = OnDarkMuted)
                Text(it.title, style = MaterialTheme.typography.bodySmall, color = OnDarkSubtle)
                Spacer(Modifier.weight(1f))
                Text(it.timeLabel, style = MaterialTheme.typography.labelSmall, color = OnDarkSubtle)
            }
        }
    }
}
