package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScoreEntity
import com.example.models.GestureState
import com.example.ui.components.ArcadeIconButton
import com.example.ui.components.SharedHandCursor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScoreScreen(
    scores: List<ScoreEntity>,
    bestScore: ScoreEntity?,
    todayBest: ScoreEntity?,
    isSoundOn: Boolean,
    gestureState: GestureState,
    onHome: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF140727),
                        Color(0xFF220A40),
                        Color(0xFF0F041F)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArcadeIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onHome,
                        contentDescription = "Back",
                        testTag = "scores_back_button"
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "LEADERBOARD",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }

                ArcadeIconButton(
                    icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    onClick = onToggleSound,
                    contentDescription = "Toggle Sound",
                    testTag = "scores_sound_button"
                )
            }

            // Top Highlights (Best Ever & Today's Best)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HighlightCard(
                    title = "BEST SCORE",
                    score = bestScore?.score ?: 0,
                    player = bestScore?.playerName ?: "-",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                )

                HighlightCard(
                    title = "TODAY'S BEST",
                    score = todayBest?.score ?: 0,
                    player = todayBest?.playerName ?: "-",
                    icon = Icons.Default.Today,
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                )
            }

            // High Score List
            Text(
                text = "TOP PLAYERS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(scores) { index, entry ->
                    ScoreRowItem(
                        rank = index + 1,
                        entry = entry
                    )
                }
            }
        }

        // Shared Hand Gesture Cursor Overlay
        SharedHandCursor(gestureState = gestureState)
    }
}

@Composable
private fun HighlightCard(
    title: String,
    score: Int,
    player: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E0E38))
            .border(2.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = player,
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
private fun ScoreRowItem(
    rank: Int,
    entry: ScoreEntity,
    modifier: Modifier = Modifier
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD600)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF19092E))
            .border(1.dp, Color(0xFF331557), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.2f))
                    .border(1.5.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = rankColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.playerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.playerName.contains("1")) Color(0xFF00E5FF) else Color(0xFFFF5252)
                    )
                    if (entry.isWinner) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Winner",
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Score
        Text(
            text = "${entry.score} pts",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
