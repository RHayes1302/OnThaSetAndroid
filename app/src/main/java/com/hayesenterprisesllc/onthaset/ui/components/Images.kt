package com.hayesenterprisesllc.onthaset.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// AsyncImage replacements
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Loads a remote URL, local file path, File or Uri (AsyncImage / Image(uiImage:)).
 * Shows [loading] while fetching and [placeholder] on failure / null model.
 */
@Composable
fun AppImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    loading: @Composable () -> Unit = { ImagePlaceholder(Modifier.fillMaxSize(), showSpinner = true) },
    placeholder: @Composable () -> Unit = { ImagePlaceholder(Modifier.fillMaxSize()) }
) {
    val resolved: Any? = when (model) {
        is String -> when {
            model.isEmpty() -> null
            model.startsWith("http") || model.startsWith("content:") || model.startsWith("file:") -> model
            else -> File(model)
        }
        else -> model
    }
    if (resolved == null) {
        Box(modifier) { placeholder() }
        return
    }
    SubcomposeAsyncImage(
        model = resolved,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = { loading() },
        error = { placeholder() }
    )
}

@Composable
fun ImagePlaceholder(modifier: Modifier = Modifier, showSpinner: Boolean = false, label: String? = null, corner: Dp = 8.dp) {
    Box(modifier.clip(RoundedCornerShape(corner)).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
        if (showSpinner) CircularProgressIndicator(color = AppColors.Yellow, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        else Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Image, null, tint = AppColors.Gray)
            if (label != null) Text(label, color = AppColors.Gray, fontSize = 11.sp)
        }
    }
}

/** Circular avatar — local file first, then remote URL, else person icon. */
@Composable
fun ProfileAvatar(localPath: String?, remoteUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    val model: Any? = when {
        !localPath.isNullOrEmpty() && File(localPath).exists() -> File(localPath)
        !remoteUrl.isNullOrEmpty() -> remoteUrl
        else -> null
    }
    val fallback: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, null, tint = AppColors.Yellow, modifier = Modifier.size(size * 0.45f))
        }
    }
    AppImage(
        model, modifier = modifier.size(size).clip(androidx.compose.foundation.shape.CircleShape),
        loading = fallback, placeholder = fallback
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Photo picker & camera (PhotosPicker / CameraPicker)
// ─────────────────────────────────────────────────────────────────────────────

class ImagePickerHandle(val pickFromLibrary: () -> Unit, val takePhoto: () -> Unit)

/** Returns handles to launch the system photo picker or the camera. */
@Composable
fun rememberImagePicker(onPicked: (Uri) -> Unit): ImagePickerHandle {
    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri)
    }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) onPicked(uri)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = newCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    return remember {
        ImagePickerHandle(
            pickFromLibrary = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            takePhoto = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val uri = newCameraUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    permLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}

private fun newCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

// ─────────────────────────────────────────────────────────────────────────────
// Full screen viewers
// ─────────────────────────────────────────────────────────────────────────────

/** Port of FullScreenImageView.swift — pinch to zoom, drag, optional delete. */
@Composable
fun FullScreenImageViewer(model: Any?, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AppImage(
                model,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset += pan
                        }
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
            )
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp)) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete", tint = AppColors.Red, modifier = Modifier.size(30.dp)) }
                }
                Box(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Cancel, "Close", tint = Color.White, modifier = Modifier.size(30.dp)) }
            }
            if (scale != 1f || offset != Offset.Zero) {
                TextButton(
                    onClick = { scale = 1f; offset = Offset.Zero },
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 40.dp)
                        .clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = Color.White)
                    Text(" Reset", color = Color.White)
                }
            }
        }
    }
}

/** Paged gallery (TabView .page style) with "n / total" counter. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryViewer(models: List<Any>, startIndex: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val pager = rememberPagerState(initialPage = startIndex.coerceIn(0, maxOf(0, models.size - 1))) { models.size }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                AppImage(models[page], contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            }
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Cancel, "Close", tint = Color.White, modifier = Modifier.size(32.dp)) }
            }
            Text(
                "${pager.currentPage + 1} / ${models.size}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 40.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small static map (MapKit Map + Marker) — Leaflet/OpenStreetMap in a WebView
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SmallMap(lat: Double, lng: Double, label: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    val safeLabel = label.replace("\\", "").replace("'", "\\'").replace("<", "").replace(">", "")
    val html = """
        <!DOCTYPE html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>html,body,#map{margin:0;height:100%;background:#111}</style></head>
        <body><div id="map"></div><script>
        var map = L.map('map',{zoomControl:false,dragging:false,scrollWheelZoom:false,doubleClickZoom:false,touchZoom:false,attributionControl:false}).setView([$lat,$lng],13);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',{maxZoom:19}).addTo(map);
        L.circleMarker([$lat,$lng],{radius:10,color:'#000',weight:2,fillColor:'#FFD60A',fillOpacity:1}).addTo(map).bindTooltip('$safeLabel',{permanent:true,direction:'top',offset:[0,-10]});
        </script></body></html>
    """.trimIndent()
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                setBackgroundColor(android.graphics.Color.BLACK)
                loadDataWithBaseURL("https://onthaset.app/", html, "text/html", "utf-8", null)
            }
        }
    )
}
