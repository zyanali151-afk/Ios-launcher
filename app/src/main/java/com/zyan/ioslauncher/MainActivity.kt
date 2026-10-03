package com.zyan.ioslauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

data class AppItem(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    LauncherScreen()
                }
            }
        }
    }
}

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    val apps = remember { loadApps(context) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppIcon(app = app) {
                launchApp(context, app)
            }
        }
    }
}

@Composable
fun AppIcon(app: AppItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Image(
            bitmap = app.icon.toBitmap(width = 144, height = 144).asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = app.label,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

fun loadApps(context: Context): List<AppItem> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveInfos = packageManager.queryIntentActivities(intent, 0)

    return resolveInfos
        .filter { it.activityInfo.packageName != context.packageName }
        .map { resolveInfo ->
            AppItem(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                activityName = resolveInfo.activityInfo.name,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

fun launchApp(context: Context, app: AppItem) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // If app cannot launch, ignore for now.
    }
}