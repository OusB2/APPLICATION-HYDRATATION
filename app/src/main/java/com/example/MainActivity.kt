package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WaterRecord
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TurquoisePrimary
import com.example.ui.theme.TurquoiseSecondary
import com.example.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          HydrationScreen(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          )
        }
      }
    }
  }
}

@Composable
fun HydrationScreen(
  modifier: Modifier = Modifier,
  viewModel: WaterViewModel = viewModel()
) {
  val totalWaterMl by viewModel.totalWaterMl.collectAsStateWithLifecycle()
  val progressPercent by viewModel.progressPercent.collectAsStateWithLifecycle()
  val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
  val goalMl = viewModel.goalMl

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.WaterDrop,
          contentDescription = stringResource(R.string.water_drop_desc),
          tint = TurquoisePrimary,
          modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      // Reset Button top right
      IconButton(
        onClick = { viewModel.resetToday() },
        modifier = Modifier
          .testTag("reset_button")
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        colors = IconButtonDefaults.iconButtonColors(
          contentColor = TurquoisePrimary
        )
      ) {
        Icon(
          imageVector = Icons.Filled.Refresh,
          contentDescription = stringResource(R.string.reset_content_description)
        )
      }
    }

    // Circular Progress Indicator Container
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressWidget(
        totalWaterMl = totalWaterMl,
        goalMl = goalMl,
        progressPercent = progressPercent
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Motivational / Progress Text Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val diff = goalMl - totalWaterMl
        val message = if (diff <= 0) {
          stringResource(R.string.goal_reached)
        } else {
          stringResource(R.string.remaining_amount, diff)
        }
        
        Text(
          text = message,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
          ),
          color = if (diff <= 0) TurquoisePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (diff < 0) {
          Text(
            text = stringResource(R.string.remaining_amount_exceeded, -diff),
            style = MaterialTheme.typography.bodyMedium,
            color = TurquoiseSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // "+ 250 ml" Action Button
    Button(
      onClick = { viewModel.addWater(250) },
      modifier = Modifier
        .testTag("add_250_button")
        .fillMaxWidth()
        .height(56.dp),
      shape = RoundedCornerShape(28.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = TurquoisePrimary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ),
      elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 4.dp,
        pressedElevation = 8.dp
      )
    ) {
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.add_water),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // History log list at the bottom
    Text(
      text = stringResource(R.string.history_title),
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier
        .align(Alignment.Start)
        .padding(bottom = 12.dp)
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (todayRecords.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.WaterDrop,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(R.string.empty_history),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(todayRecords, key = { it.id }) { record ->
            HistoryRow(
              record = record,
              onDelete = { viewModel.deleteRecord(record) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun CircularProgressWidget(
  totalWaterMl: Int,
  goalMl: Int,
  progressPercent: Float,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progressPercent,
    animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
    label = "water_progress"
  )

  Box(
    modifier = modifier.size(220.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokeWidth = 14.dp.toPx()
      val size = size
      val radius = (size.minDimension - strokeWidth) / 2
      val center = center

      // Track (Background circle)
      drawCircle(
        color = TurquoisePrimary.copy(alpha = 0.12f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
      )

      // Progress Arc
      drawArc(
        color = TurquoisePrimary,
        startAngle = -90f,
        sweepAngle = animatedProgress * 360f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )
    }

    // Content in the center of the progress circle
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = TurquoisePrimary,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = "$totalWaterMl",
          style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
          color = TurquoisePrimary
        )
        Text(
          text = " / $goalMl ml",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${(animatedProgress * 100).toInt()}%",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TurquoiseSecondary
      )
    }
  }
}

@Composable
fun HistoryRow(
  record: WaterRecord,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(TurquoisePrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = TurquoisePrimary,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
          Text(
            text = "+ ${record.amountMl} ml",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = formatTime(record.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.testTag("delete_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Delete,
          contentDescription = stringResource(R.string.delete_content_description),
          tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )
      }
    }
  }
}

fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
