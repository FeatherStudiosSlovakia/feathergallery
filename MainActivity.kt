package com.example.galerryios

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.LruCache
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ----------------------------------------------------
// VLASTNÁ MODRÁ TÉMA (BEZ FIALOVEJ)
// ----------------------------------------------------
val IosBlue = Color(0xFF007AFF)

val CustomLightColorScheme = lightColorScheme(
    primary = IosBlue,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF2F2F7),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.DarkGray
)

val CustomDarkColorScheme = darkColorScheme(
    primary = IosBlue,
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF1C1C1E),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray
)

// Vlastná Crop ikonka (nevyžaduje material-icons-extended)
val CropIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CropIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(17f, 15f)
            horizontalLineToRelative(2f)
            verticalLineTo(7f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(8f)
            close()
            moveTo(7f, 17f)
            verticalLineTo(1f)
            horizontalLineTo(5f)
            verticalLineToRelative(4f)
            horizontalLineTo(1f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(10f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(-2f)
            horizontalLineTo(7f)
            close()
        }
    }.build()
}

// ----------------------------------------------------
// DÁTOVÉ TRIEDY A MODELY
// ----------------------------------------------------
data class MediaPhoto(
    val uri: Uri,
    val albumName: String,
    val dateAdded: Long,
    val displayName: String,
    val mimeType: String,
    val isTrashed: Boolean = false,
    val path: String = "",
    val sizeBytes: Long = 0L
)

sealed class GalleryItem {
    data class Header(val title: String) : GalleryItem()
    data class Photo(val media: MediaPhoto) : GalleryItem()
}

data class EditorTextOverlay(
    val id: Long = System.nanoTime(),
    val text: String,
    val color: Color = Color.White,
    val bgStyle: Int = 0,
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)

data class EditorStickerOverlay(
    val id: Long = System.nanoTime(),
    val uri: Uri,
    val bitmap: Bitmap,
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val alpha: Float = 1f,
    val shapeMode: Int = 0
)

// ----------------------------------------------------
// LOKALIZÁCIA (PREKLADY JAZYKA)
// ----------------------------------------------------
val LocalLang = compositionLocalOf { "sk" }

@Composable
fun tr(sk: String, en: String): String {
    return if (LocalLang.current == "en") en else sk
}

// Prispôsobenie výšky spodného baru podľa toho, či sú na mobile zapnuté gestá alebo tlačidlá
@Composable
fun rememberAdaptiveBottomBarPadding(): Dp {
    val context = LocalContext.current
    val insetsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isGestureNav = remember(context) {
        try {
            Settings.Secure.getInt(context.contentResolver, "navigation_mode", 0) == 2
        } catch (_: Exception) {
            false
        }
    }
    return if (insetsBottom > 0.dp) {
        if (isGestureNav) insetsBottom + 8.dp else insetsBottom + 12.dp
    } else {
        if (isGestureNav) 24.dp else 12.dp
    }
}

// Skrátenie názvu: Začiatok mena... + posledných 5 znakov + prípona
fun formatShortFileNameWithExt(displayName: String, maxBaseLength: Int = 18): String {
    val dotIndex = displayName.lastIndexOf('.')
    if (dotIndex <= 0 || dotIndex == displayName.length - 1) {
        return if (displayName.length > maxBaseLength) {
            displayName.take(8) + "…" + displayName.takeLast(5)
        } else {
            displayName
        }
    }
    val base = displayName.substring(0, dotIndex)
    val ext = displayName.substring(dotIndex)
    return if (base.length > maxBaseLength) {
        "${base.take(8)}…${base.takeLast(5)}$ext"
    } else {
        displayName
    }
}

// Formátovanie veľkosti súboru: pre fotky v KB / MB, pre videá aj v GB
fun formatMediaFileSize(context: Context, photo: MediaPhoto): String {
    var bytes = photo.sizeBytes
    if (bytes <= 0L && photo.path.isNotEmpty()) {
        try {
            val f = File(photo.path)
            if (f.exists()) bytes = f.length()
        } catch (_: Exception) {
        }
    }
    if (bytes <= 0L) {
        try {
            context.contentResolver.openFileDescriptor(photo.uri, "r")?.use { pfd ->
                if (pfd.statSize > 0L) bytes = pfd.statSize
            }
        } catch (_: Exception) {
        }
    }
    if (bytes <= 0L) return "0 KB"

    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val isVideo = photo.mimeType.startsWith("video/")

    return if (isVideo) {
        if (gb >= 1.0) {
            String.format(Locale.getDefault(), "%.2f GB (%.1f MB)", gb, mb)
        } else if (mb >= 1.0) {
            String.format(Locale.getDefault(), "%.2f MB (%.3f GB)", mb, gb)
        } else {
            String.format(Locale.getDefault(), "%.1f KB (%.4f GB)", kb, gb)
        }
    } else {
        if (mb >= 1.0) {
            String.format(Locale.getDefault(), "%.2f MB (%.0f KB)", mb, kb)
        } else {
            String.format(Locale.getDefault(), "%.1f KB", kb)
        }
    }
}

// ----------------------------------------------------
// KOMPONENT INFO RIADKU (PRE INFORMAČNÉ MENU)
// ----------------------------------------------------
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ----------------------------------------------------
// CACHE A KOMPONENT PRE NÁHĽADY VIDEÍ A FOTIEK
// ----------------------------------------------------
val videoThumbnailCache = object : LruCache<String, Bitmap>(60 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

val editedPhotoCache = object : LruCache<String, Bitmap>(40 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}
var editedPhotoCacheVersion by mutableIntStateOf(0)

suspend fun loadVideoThumbnailBitmap(context: Context, uri: Uri, highRes: Boolean): Bitmap? =
    withContext(Dispatchers.IO) {
        val cacheKey = "${uri}_${if (highRes) "hq" else "thumb"}"
        videoThumbnailCache.get(cacheKey)?.let { return@withContext it }

        var bitmap: Bitmap? = null
        if (!highRes && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                bitmap = context.contentResolver.loadThumbnail(uri, Size(360, 360), null)
            } catch (_: Exception) {
            }
        }
        if (bitmap == null) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } catch (_: Exception) {
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        }
        if (bitmap != null) {
            videoThumbnailCache.put(cacheKey, bitmap)
        }
        bitmap
    }

@Composable
fun SmartMediaImage(
    uri: Uri,
    mimeType: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    highResVideoThumb: Boolean = false
) {
    val version = editedPhotoCacheVersion
    val editedBitmap = remember(uri, version) { editedPhotoCache.get(uri.toString()) }

    if (editedBitmap != null) {
        Image(
            bitmap = editedBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (!mimeType.startsWith("video/")) {
        AsyncImage(
            model = uri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        val context = LocalContext.current
        val cacheKey = "${uri}_${if (highResVideoThumb) "hq" else "thumb"}"
        val fallbackKey = "${uri}_thumb"
        var bitmap by remember(uri, highResVideoThumb) {
            mutableStateOf(videoThumbnailCache.get(cacheKey) ?: videoThumbnailCache.get(fallbackKey))
        }

        LaunchedEffect(uri, highResVideoThumb) {
            if (videoThumbnailCache.get(cacheKey) == null) {
                val loaded = loadVideoThumbnailBitmap(context, uri, highResVideoThumb)
                if (loaded != null) {
                    bitmap = loaded
                }
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = modifier.background(Color.DarkGray.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
fun PauseBarsIcon(
    barWidth: Dp = 6.dp,
    barHeight: Dp = 20.dp,
    spacing: Dp = 6.dp,
    tint: Color = Color.White
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .background(tint, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(spacing))
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .background(tint, RoundedCornerShape(2.dp))
        )
    }
}

fun formatVideoTime(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

fun adjustVideoAspectRatio(view: TextureView, videoWidth: Int, videoHeight: Int) {
    if (videoWidth == 0 || videoHeight == 0 || view.width == 0 || view.height == 0) return
    val viewWidth = view.width.toFloat()
    val viewHeight = view.height.toFloat()
    val aspectRatio = videoHeight.toFloat() / videoWidth.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (viewHeight > (viewWidth * aspectRatio).toInt()) {
        newWidth = viewWidth.toInt()
        newHeight = (viewWidth * aspectRatio).toInt()
    } else {
        newWidth = (viewHeight / aspectRatio).toInt()
        newHeight = viewHeight.toInt()
    }
    val xoff = (viewWidth - newWidth) / 2f
    val yoff = (viewHeight - newHeight) / 2f
    val txform = Matrix()
    view.getTransform(txform)
    txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
    txform.postTranslate(xoff, yoff)
    view.setTransform(txform)
}

// ----------------------------------------------------
// POMOCNÉ FUNKCIE PRE FOTO EDITOR (MATICE, FILTRE, RENDERING)
// ----------------------------------------------------
enum class PhotoFilterPreset(val skName: String, val enName: String) {
    NONE("Originál", "Original"),
    VIVID("Živý", "Vivid"),
    BW("Čiernobiely", "B&W"),
    SEPIA("Sépia", "Sepia"),
    WARM("Teplý", "Warm"),
    COOL("Studený", "Cool"),
    VINTAGE("Vintage", "Vintage"),
    DRAMA("Dráma", "Dramatic"),
    INVERT("Negatív", "Invert")
}

fun buildCombinedColorMatrixArray(
    exposure: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float,
    preset: PhotoFilterPreset
): FloatArray {
    val master = ColorMatrix()

    when (preset) {
        PhotoFilterPreset.NONE -> {}
        PhotoFilterPreset.VIVID -> {
            val sat = ColorMatrix().apply { setSaturation(1.45f) }
            val con = ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, -18f,
                    0f, 1.15f, 0f, 0f, -18f,
                    0f, 0f, 1.15f, 0f, -18f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            master.postConcat(sat)
            master.postConcat(con)
        }
        PhotoFilterPreset.BW -> {
            val bw = ColorMatrix().apply { setSaturation(0f) }
            val con = ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, -20f,
                    0f, 1.2f, 0f, 0f, -20f,
                    0f, 0f, 1.2f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            master.postConcat(bw)
            master.postConcat(con)
        }
        PhotoFilterPreset.SEPIA -> {
            val sep = ColorMatrix().apply { setSaturation(0f) }
            val tint = ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, 15f,
                    0f, 1.02f, 0f, 0f, 5f,
                    0f, 0f, 0.78f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            master.postConcat(sep)
            master.postConcat(tint)
        }
        PhotoFilterPreset.WARM -> {
            master.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 18f,
                        0f, 1.03f, 0f, 0f, 6f,
                        0f, 0f, 0.88f, 0f, -18f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
        PhotoFilterPreset.COOL -> {
            master.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0f, 0f, 0f, -14f,
                        0f, 1.02f, 0f, 0f, 4f,
                        0f, 0f, 1.14f, 0f, 22f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
        PhotoFilterPreset.VINTAGE -> {
            val sat = ColorMatrix().apply { setSaturation(0.65f) }
            val fade = ColorMatrix(
                floatArrayOf(
                    0.95f, 0.05f, 0f, 0f, 22f,
                    0f, 0.92f, 0.05f, 0f, 16f,
                    0.05f, 0f, 0.82f, 0f, 8f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            master.postConcat(sat)
            master.postConcat(fade)
        }
        PhotoFilterPreset.DRAMA -> {
            val sat = ColorMatrix().apply { setSaturation(0.55f) }
            val con = ColorMatrix(
                floatArrayOf(
                    1.38f, 0f, 0f, 0f, -42f,
                    0f, 1.38f, 0f, 0f, -42f,
                    0f, 0f, 1.42f, 0f, -35f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            master.postConcat(sat)
            master.postConcat(con)
        }
        PhotoFilterPreset.INVERT -> {
            master.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
    }

    val satFactor = ((saturation + 100f) / 100f).coerceIn(0f, 2f)
    if (abs(satFactor - 1f) > 0.01f) {
        val satMatrix = ColorMatrix().apply { setSaturation(satFactor) }
        master.postConcat(satMatrix)
    }

    val contrastFactor = if (contrast >= 0f) {
        1f + (contrast / 100f) * 0.9f
    } else {
        1f + (contrast / 100f) * 0.55f
    }
    val brightnessOffset = exposure * 1.4f
    val contrastTranslate = (1f - contrastFactor) * 128f + brightnessOffset

    if (abs(contrastFactor - 1f) > 0.01f || abs(brightnessOffset) > 0.1f) {
        val expConMatrix = ColorMatrix(
            floatArrayOf(
                contrastFactor, 0f, 0f, 0f, contrastTranslate,
                0f, contrastFactor, 0f, 0f, contrastTranslate,
                0f, 0f, contrastFactor, 0f, contrastTranslate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        master.postConcat(expConMatrix)
    }

    if (abs(warmth) > 0.5f) {
        val rShift = warmth * 0.65f
        val gShift = warmth * 0.18f
        val bShift = -warmth * 0.65f
        val warmthMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, rShift,
                0f, 1f, 0f, 0f, gShift,
                0f, 0f, 1f, 0f, bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        master.postConcat(warmthMatrix)
    }

    return master.array
}

suspend fun loadEditableBitmap(context: Context, uri: Uri, maxDimension: Int = 2560): Bitmap? =
    withContext(Dispatchers.IO) {
        editedPhotoCache.get(uri.toString())?.let { cached ->
            return@withContext cached.copy(Bitmap.Config.ARGB_8888, true)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                    val w = info.size.width
                    val h = info.size.height
                    if (w > maxDimension || h > maxDimension) {
                        val scale = maxDimension.toFloat() / max(w, h).toFloat()
                        decoder.setTargetSize(
                            (w * scale).roundToInt().coerceAtLeast(1),
                            (h * scale).roundToInt().coerceAtLeast(1)
                        )
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.copy(Bitmap.Config.ARGB_8888, true)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

fun transformBitmapOnly(
    source: Bitmap,
    rotationDegrees: Int,
    flipHorizontal: Boolean
): Bitmap {
    val matrix = Matrix()
    if (rotationDegrees != 0) {
        matrix.postRotate(rotationDegrees.toFloat())
    }
    if (flipHorizontal) {
        matrix.postScale(-1f, 1f)
    }
    return if (rotationDegrees != 0 || flipHorizontal) {
        Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    } else {
        source
    }
}

fun cropNormalizedBitmap(
    transformed: Bitmap,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float
): Bitmap {
    val srcW = transformed.width
    val srcH = transformed.height
    val l = cropLeft.coerceIn(0f, 0.95f)
    val t = cropTop.coerceIn(0f, 0.95f)
    val r = cropRight.coerceIn(l + 0.05f, 1f)
    val b = cropBottom.coerceIn(t + 0.05f, 1f)

    if (l <= 0.001f && t <= 0.001f && r >= 0.999f && b >= 0.999f) {
        return transformed
    }

    val xOff = (l * srcW).roundToInt().coerceIn(0, srcW - 1)
    val yOff = (t * srcH).roundToInt().coerceIn(0, srcH - 1)
    val cropW = ((r - l) * srcW).roundToInt().coerceIn(1, srcW - xOff)
    val cropH = ((b - t) * srcH).roundToInt().coerceIn(1, srcH - yOff)
    return Bitmap.createBitmap(transformed, xOff, yOff, cropW, cropH)
}

fun shapeStickerBitmap(src: Bitmap, shapeMode: Int): Bitmap {
    if (shapeMode == 0) return src
    val w = src.width
    val h = src.height
    val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

    if (shapeMode == 1) {
        val radius = min(w, h) * 0.18f
        canvas.drawRoundRect(rect, radius, radius, paint)
    } else {
        val r = min(w, h) / 2f
        canvas.drawCircle(w / 2f, h / 2f, r, paint)
    }
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return output
}

suspend fun renderFinalEditedBitmap(
    baseCroppedBitmap: Bitmap,
    targetWidth: Int,
    targetHeight: Int,
    colorMatrixArray: FloatArray,
    vignetteStrength: Float,
    stickers: List<EditorStickerOverlay>,
    texts: List<EditorTextOverlay>
): Bitmap = withContext(Dispatchers.Default) {
    val safeW = targetWidth.coerceIn(64, 6000)
    val safeH = targetHeight.coerceIn(64, 6000)

    val result = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix(colorMatrixArray))
    }
    val dstRect = RectF(0f, 0f, safeW.toFloat(), safeH.toFloat())
    canvas.drawBitmap(baseCroppedBitmap, null, dstRect, photoPaint)

    if (vignetteStrength > 1f) {
        val radius = max(safeW, safeH) * 0.75f
        val alpha = ((vignetteStrength / 100f) * 200).roundToInt().coerceIn(0, 220)
        val radialGradient = RadialGradient(
            safeW / 2f,
            safeH / 2f,
            radius,
            intArrayOf(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.argb(alpha, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = radialGradient
        }
        canvas.drawRect(dstRect, vigPaint)
    }

    for (sticker in stickers) {
        val shaped = shapeStickerBitmap(sticker.bitmap, sticker.shapeMode)
        val baseStickerWidth = safeW * 0.28f * sticker.scale
        val aspect = shaped.height.toFloat() / shaped.width.toFloat().coerceAtLeast(1f)
        val stickerW = baseStickerWidth.coerceAtLeast(16f)
        val stickerH = (stickerW * aspect).coerceAtLeast(16f)

        val centerX = sticker.normX * safeW
        val centerY = sticker.normY * safeH

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(sticker.rotation)

        val sPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (sticker.alpha * 255).roundToInt().coerceIn(0, 255)
        }
        val sRect = RectF(-stickerW / 2f, -stickerH / 2f, stickerW / 2f, stickerH / 2f)
        canvas.drawBitmap(shaped, null, sRect, sPaint)
        canvas.restore()
    }

    for (textItem in texts) {
        if (textItem.text.isBlank()) continue
        val textSizePx = (safeW * 0.065f * textItem.scale).coerceIn(14f, 600f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textItem.color.toArgb()
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            if (textItem.bgStyle == 0) {
                setShadowLayer(
                    textSizePx * 0.15f,
                    0f,
                    textSizePx * 0.06f,
                    android.graphics.Color.argb(190, 0, 0, 0)
                )
            }
        }

        val lines = textItem.text.split("\n")
        val fontMetrics = textPaint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.15f
        val totalHeight = lineHeight * lines.size
        var maxLineWidth = 0f
        for (line in lines) {
            maxLineWidth = max(maxLineWidth, textPaint.measureText(line))
        }

        val centerX = textItem.normX * safeW
        val centerY = textItem.normY * safeH

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(textItem.rotation)

        if (textItem.bgStyle != 0) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (textItem.bgStyle == 1) {
                    android.graphics.Color.argb(185, 20, 20, 20)
                } else {
                    android.graphics.Color.argb(215, 245, 245, 245)
                }
            }
            val padH = textSizePx * 0.45f
            val padV = textSizePx * 0.28f
            val bgRect = RectF(
                -maxLineWidth / 2f - padH,
                -totalHeight / 2f - padV,
                maxLineWidth / 2f + padH,
                totalHeight / 2f + padV
            )
            val cornerRadius = textSizePx * 0.35f
            canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)
        }

        var startY = -totalHeight / 2f - fontMetrics.ascent
        for (line in lines) {
            canvas.drawText(line, 0f, startY, textPaint)
            startY += lineHeight
        }
        canvas.restore()
    }

    result
}

// Spoľahlivé uloženie novej kópie pre staršie (< Android 10) aj novšie (Android 10–15+) verzie
suspend fun saveBitmapAsNewCopy(
    context: Context,
    bitmap: Bitmap,
    originalPhoto: MediaPhoto
): Boolean = withContext(Dispatchers.IO) {
    val baseName = originalPhoto.displayName.substringBeforeLast(".").ifBlank { "Photo" }
    val fileName = "${baseName}_edit_${System.currentTimeMillis()}.jpg"
    val nowSeconds = System.currentTimeMillis() / 1000L

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, nowSeconds)
                put(MediaStore.Images.Media.DATE_MODIFIED, nowSeconds)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val newUri = context.contentResolver.insert(collection, values)
            if (newUri != null) {
                val stream = context.contentResolver.openOutputStream(newUri, "w")
                    ?: context.contentResolver.openOutputStream(newUri)
                if (stream != null) {
                    stream.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                        out.flush()
                    }
                    val updateValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    context.contentResolver.update(newUri, updateValues, null, null)
                    return@withContext true
                } else {
                    context.contentResolver.delete(newUri, null, null)
                }
            }
        } catch (_: Exception) {
        }
    }

    // Fallback pre staršie verzie Androidu (API < 29) alebo špecifické nadstavby
    try {
        @Suppress("DEPRECATION")
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }
        val outFile = File(picturesDir, fileName)
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.flush()
        }
        try {
            @Suppress("DEPRECATION")
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, nowSeconds)
                put(MediaStore.Images.Media.DATE_MODIFIED, nowSeconds)
                put(MediaStore.Images.Media.DATA, outFile.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (_: Exception) {
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(outFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
        true
    } catch (_: Exception) {
        false
    }
}

// Spoľahlivý prepis existujúcej fotky (skúša "wt", "w", default stream aj priamy súbor)
fun writeBitmapToExistingPhoto(context: Context, photo: MediaPhoto, bitmap: Bitmap): Boolean {
    var lastSecurityEx: SecurityException? = null
    val modes = listOf("wt", "w", "rwt")
    for (mode in modes) {
        try {
            val stream = context.contentResolver.openOutputStream(photo.uri, mode)
            if (stream != null) {
                stream.use { out ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                        out.flush()
                        if (photo.path.isNotEmpty()) {
                            MediaScannerConnection.scanFile(context, arrayOf(photo.path), arrayOf("image/jpeg"), null)
                        }
                        return true
                    }
                }
            }
        } catch (sec: SecurityException) {
            lastSecurityEx = sec
        } catch (fnf: java.io.FileNotFoundException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                lastSecurityEx = SecurityException(fnf.message)
            }
        } catch (_: Exception) {
        }
    }

    try {
        val stream = context.contentResolver.openOutputStream(photo.uri)
        if (stream != null) {
            stream.use { out ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                    out.flush()
                    if (photo.path.isNotEmpty()) {
                        MediaScannerConnection.scanFile(context, arrayOf(photo.path), arrayOf("image/jpeg"), null)
                    }
                    return true
                }
            }
        }
    } catch (sec: SecurityException) {
        lastSecurityEx = sec
    } catch (fnf: java.io.FileNotFoundException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            lastSecurityEx = SecurityException(fnf.message)
        }
    } catch (_: Exception) {
    }

    if (photo.path.isNotEmpty()) {
        try {
            val file = File(photo.path)
            if (file.exists() && file.canWrite()) {
                FileOutputStream(file, false).use { out ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                        out.flush()
                        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
                        return true
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    if (lastSecurityEx != null) {
        throw lastSecurityEx
    }
    return false
}

// ----------------------------------------------------
// MAIN ACTIVITY A TÉMA
// ----------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }

            var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0)) }
            var lang by remember { mutableStateOf(prefs.getString("app_lang", "sk") ?: "sk") }
            var showHeaders by remember { mutableStateOf(prefs.getBoolean("show_headers", true)) }
            var confirmDelete by remember { mutableStateOf(prefs.getBoolean("confirm_delete", true)) }
            var enableZoom by remember { mutableStateOf(prefs.getBoolean("enable_zoom_v2", true)) }
            var syncFilmstrip by remember { mutableStateOf(prefs.getBoolean("sync_filmstrip", true)) }

            val isDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(LocalLang provides lang) {
                MaterialTheme(
                    colorScheme = if (isDarkTheme) CustomDarkColorScheme else CustomLightColorScheme
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainGalleryScreen(
                            currentThemeMode = themeMode,
                            onThemeChanged = { newMode ->
                                prefs.edit().putInt("theme_mode", newMode).apply()
                                themeMode = newMode
                            },
                            currentLang = lang,
                            onLangChanged = { newLang ->
                                prefs.edit().putString("app_lang", newLang).apply()
                                lang = newLang
                            },
                            showHeaders = showHeaders,
                            onShowHeadersChanged = { newVal ->
                                prefs.edit().putBoolean("show_headers", newVal).apply()
                                showHeaders = newVal
                            },
                            confirmDelete = confirmDelete,
                            onConfirmDeleteChanged = { newVal ->
                                prefs.edit().putBoolean("confirm_delete", newVal).apply()
                                confirmDelete = newVal
                            },
                            enableZoom = enableZoom,
                            onEnableZoomChanged = { newVal ->
                                prefs.edit().putBoolean("enable_zoom_v2", newVal).apply()
                                enableZoom = newVal
                            },
                            syncFilmstrip = syncFilmstrip,
                            onSyncFilmstripChanged = { newVal ->
                                prefs.edit().putBoolean("sync_filmstrip", newVal).apply()
                                syncFilmstrip = newVal
                            }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// HLAVNÁ OBRAZOVKA S NAVIGÁCIOU A GROUPING PILULKOU
// ----------------------------------------------------
@Composable
fun MainGalleryScreen(
    currentThemeMode: Int,
    onThemeChanged: (Int) -> Unit,
    currentLang: String,
    onLangChanged: (String) -> Unit,
    showHeaders: Boolean,
    onShowHeadersChanged: (Boolean) -> Unit,
    confirmDelete: Boolean,
    onConfirmDeleteChanged: (Boolean) -> Unit,
    enableZoom: Boolean,
    onEnableZoomChanged: (Boolean) -> Unit,
    syncFilmstrip: Boolean,
    onSyncFilmstripChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }
    val adaptiveBottomPadding = rememberAdaptiveBottomBarPadding()

    var hasPermission by remember { mutableStateOf(false) }
    var allPhotos by remember { mutableStateOf<List<MediaPhoto>>(emptyList()) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var currentSwipeList by remember { mutableStateOf<List<MediaPhoto>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    var showShotTypesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var photosColumnsCount by rememberSaveable { mutableIntStateOf(5) }
    var photosGroupingMode by rememberSaveable { mutableStateOf("DAYS") }
    var showGroupingPill by rememberSaveable { mutableStateOf(false) }

    var hiddenUriStrings by remember {
        mutableStateOf(prefs.getStringSet("hidden_uris", emptySet())?.toSet() ?: emptySet())
    }
    var favoriteUriStrings by remember {
        mutableStateOf(prefs.getStringSet("favorite_uris", emptySet())?.toSet() ?: emptySet())
    }
    // Lokálny kôš pre staršie verzie Androidu (< Android 11)
    var localTrashedUriStrings by remember {
        mutableStateOf(prefs.getStringSet("local_trashed_uris", emptySet())?.toSet() ?: emptySet())
    }
    var isAlbumsFullScreenSubMenuOpen by rememberSaveable { mutableStateOf(false) }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            hasPermission = permissionsMap.values.any { it }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(permissionsToRequest)
    }

    LaunchedEffect(hasPermission, refreshTrigger) {
        if (hasPermission) {
            allPhotos = loadPhotosFromStorage(context)
        }
    }

    val mergedAllPhotos = remember(allPhotos, localTrashedUriStrings) {
        allPhotos.map { photo ->
            if (localTrashedUriStrings.contains(photo.uri.toString())) {
                photo.copy(isTrashed = true)
            } else {
                photo
            }
        }
    }

    val activePhotos = remember(mergedAllPhotos, hiddenUriStrings) {
        mergedAllPhotos.filter { !it.isTrashed && !hiddenUriStrings.contains(it.uri.toString()) }
    }
    val hiddenPhotos = remember(mergedAllPhotos, hiddenUriStrings) {
        mergedAllPhotos.filter { !it.isTrashed && hiddenUriStrings.contains(it.uri.toString()) }
    }
    val favoritePhotos = remember(activePhotos, favoriteUriStrings) {
        activePhotos.filter { favoriteUriStrings.contains(it.uri.toString()) }
    }

    val moveUrisToLocalTrash: (Set<String>) -> Unit = { uris ->
        val updated = localTrashedUriStrings + uris
        localTrashedUriStrings = updated
        prefs.edit().putStringSet("local_trashed_uris", updated).apply()
        refreshTrigger++
    }

    val restoreUrisFromLocalTrash: (Set<String>) -> Unit = { uris ->
        val updated = localTrashedUriStrings - uris
        localTrashedUriStrings = updated
        prefs.edit().putStringSet("local_trashed_uris", updated).apply()
        refreshTrigger++
    }

    val swipeList = if (currentSwipeList.isNotEmpty()) currentSwipeList else activePhotos
    val initialIndex = swipeList.indexOfFirst { it.uri.toString() == selectedPhotoUriString }.coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!hasPermission) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        tr(
                            "Na zobrazenie galérie povoľ prístup k fotkám a videám.",
                            "Allow media access to view gallery."
                        )
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> PhotosScreenWithFilters(
                        allPhotos = activePhotos,
                        showHeaders = showHeaders,
                        confirmDelete = confirmDelete,
                        onRefresh = { refreshTrigger++ },
                        onMoveToLocalTrash = moveUrisToLocalTrash,
                        columnsCount = photosColumnsCount,
                        onColumnsCountChanged = { newCols ->
                            photosColumnsCount = newCols
                            if (photosGroupingMode != "ALL") {
                                photosGroupingMode = when (newCols) {
                                    28 -> "YEARS"
                                    14, 7 -> "MONTHS"
                                    else -> "DAYS"
                                }
                            }
                        },
                        groupingMode = photosGroupingMode,
                        onPhotoClick = { photo, list ->
                            currentSwipeList = list
                            selectedPhotoUriString = photo.uri.toString()
                        }
                    )
                    1 -> AlbumsScreen(
                        allPhotos = mergedAllPhotos,
                        activePhotos = activePhotos,
                        hiddenPhotos = hiddenPhotos,
                        favoritePhotos = favoritePhotos,
                        showHeaders = showHeaders,
                        confirmDelete = confirmDelete,
                        onRefresh = { refreshTrigger++ },
                        onMoveToLocalTrash = moveUrisToLocalTrash,
                        onRestoreFromLocalTrash = restoreUrisFromLocalTrash,
                        onHidePhotos = { urisToHide ->
                            val updated = hiddenUriStrings + urisToHide
                            hiddenUriStrings = updated
                            prefs.edit().putStringSet("hidden_uris", updated).apply()
                        },
                        onUnhidePhotos = { urisToUnhide ->
                            val updated = hiddenUriStrings - urisToUnhide
                            hiddenUriStrings = updated
                            prefs.edit().putStringSet("hidden_uris", updated).apply()
                        },
                        onFullScreenMoreChanged = { isOpen ->
                            isAlbumsFullScreenSubMenuOpen = isOpen
                        }
                    ) { photo, list ->
                        currentSwipeList = list
                        selectedPhotoUriString = photo.uri.toString()
                    }
                }
            }

            if (!(selectedTab == 1 && isAlbumsFullScreenSubMenuOpen)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = adaptiveBottomPadding)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = selectedTab == 0 && showGroupingPill,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .width(324.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val groupOptions = listOf(
                                    "ALL" to tr("Všetky", "All"),
                                    "DAYS" to tr("Dni", "Days"),
                                    "MONTHS" to tr("Mesiace", "Months"),
                                    "YEARS" to tr("Roky", "Years")
                                )
                                groupOptions.forEach { (modeKey, label) ->
                                    val isSelected = photosGroupingMode == modeKey
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                photosGroupingMode = modeKey
                                                when (modeKey) {
                                                    "ALL" -> {}
                                                    "DAYS" -> {
                                                        if (!showHeaders) onShowHeadersChanged(true)
                                                        if (photosColumnsCount != 3 && photosColumnsCount != 5) {
                                                            photosColumnsCount = 5
                                                        }
                                                    }
                                                    "MONTHS" -> {
                                                        if (!showHeaders) onShowHeadersChanged(true)
                                                        photosColumnsCount = 7
                                                    }
                                                    "YEARS" -> {
                                                        if (!showHeaders) onShowHeadersChanged(true)
                                                        photosColumnsCount = 28
                                                    }
                                                }
                                            }
                                            .padding(vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .width(288.dp)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PillNavItem(
                                label = tr("Fotky", "Photos"),
                                icon = Icons.Default.Image,
                                selected = selectedTab == 0,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (selectedTab == 0) {
                                    showGroupingPill = !showGroupingPill
                                } else {
                                    selectedTab = 0
                                }
                            }
                            PillNavItem(
                                label = tr("Albumy", "Albums"),
                                icon = Icons.Default.Folder,
                                selected = selectedTab == 1,
                                modifier = Modifier.weight(1f)
                            ) {
                                selectedTab = 1
                                showGroupingPill = false
                            }
                            PillNavItem(
                                label = tr("Nastavenia", "Settings"),
                                icon = Icons.Default.Settings,
                                selected = false,
                                modifier = Modifier.weight(1f)
                            ) {
                                showSettingsDialog = true
                            }
                        }
                    }
                }
            }
        }

        if (showShotTypesDialog) {
            AlertDialog(
                onDismissRequest = { showShotTypesDialog = false },
                title = { Text(tr("O Typoch záberov", "About Shot Types")) },
                text = {
                    Text(
                        tr(
                            "Aplikácia automaticky rozpoznáva typy médií ako Video, GIF, Selfie, Portréty, Motion/Slow-mo a Pro fotky podľa ich názvu a formátu.",
                            "The app automatically detects media types such as Video, GIF, Selfie, Portraits, Motion/Slow-mo, and Pro photos."
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showShotTypesDialog = false }) { Text("OK") }
                }
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(tr("Nastavenia galérie", "Gallery Settings")) },
                text = {
                    Column {
                        Text(tr("Vzhľad aplikácie", "App Theme"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { onThemeChanged(0) }) { Text("Sys") }
                            Button(onClick = { onThemeChanged(1) }) { Text(tr("Svetlý", "Light")) }
                            Button(onClick = { onThemeChanged(2) }) { Text(tr("Tmavý", "Dark")) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tr("Jazyk aplikácie", "App Language"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { onLangChanged("sk") }) { Text("SK") }
                            Button(onClick = { onLangChanged("en") }) { Text("EN") }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShowHeadersChanged(!showHeaders) }
                        ) {
                            Checkbox(checked = showHeaders, onCheckedChange = { onShowHeadersChanged(it) })
                            Text(tr("Zobraziť hlavičky dátumov", "Show date headers"))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirmDeleteChanged(!confirmDelete) }
                        ) {
                            Checkbox(checked = confirmDelete, onCheckedChange = { onConfirmDeleteChanged(it) })
                            Text(tr("Potvrdiť pred vymazaním", "Confirm before delete"))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEnableZoomChanged(!enableZoom) }
                        ) {
                            Checkbox(checked = enableZoom, onCheckedChange = { onEnableZoomChanged(it) })
                            Text(tr("Povoliť zoom (Experimentálne)", "Enable zoom (Experimental)"))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSyncFilmstripChanged(!syncFilmstrip) }
                        ) {
                            Checkbox(checked = syncFilmstrip, onCheckedChange = { onSyncFilmstripChanged(it) })
                            Text(tr("Synch. filmstrip so swipovaním", "Sync filmstrip with swiping"))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Verzia: 1.8 (Feather Studios)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text(tr("Zavrieť", "Close")) }
                }
            )
        }

        if (selectedPhotoUriString != null && swipeList.isNotEmpty()) {
            FullScreenPhotoView(
                photos = swipeList,
                allGalleryPhotos = activePhotos,
                initialIndex = initialIndex,
                confirmDelete = confirmDelete,
                enableZoom = enableZoom,
                syncFilmstrip = syncFilmstrip,
                favoriteUriStrings = favoriteUriStrings,
                onToggleFavorite = { uriStr ->
                    val updated = if (favoriteUriStrings.contains(uriStr)) {
                        favoriteUriStrings - uriStr
                    } else {
                        favoriteUriStrings + uriStr
                    }
                    favoriteUriStrings = updated
                    prefs.edit().putStringSet("favorite_uris", updated).apply()
                },
                onMoveToLocalTrash = moveUrisToLocalTrash,
                onBack = {
                    selectedPhotoUriString = null
                    val activity = context as? Activity
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                },
                onDeleteSuccess = {
                    selectedPhotoUriString = null
                    val activity = context as? Activity
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    refreshTrigger++
                },
                onEditSaved = {
                    refreshTrigger++
                },
                onPhotoIndexChanged = { newIndex ->
                    selectedPhotoUriString = swipeList.getOrNull(newIndex)?.uri?.toString()
                }
            )
        }
    }
}

@Composable
fun PillNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------
// CELOOBRAZOVKOVÝ REŽIM
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPhotoView(
    photos: List<MediaPhoto>,
    allGalleryPhotos: List<MediaPhoto>,
    initialIndex: Int,
    confirmDelete: Boolean,
    enableZoom: Boolean,
    syncFilmstrip: Boolean,
    favoriteUriStrings: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onMoveToLocalTrash: (Set<String>) -> Unit = {},
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onEditSaved: () -> Unit,
    onPhotoIndexChanged: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isUiVisible by rememberSaveable { mutableStateOf(true) }

    var showInfoSheet by remember { mutableStateOf(false) }
    var showPhotoEditor by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val adaptiveBottomPadding = rememberAdaptiveBottomBarPadding()

    LaunchedEffect(Unit) {
        if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { photos.size })
    val filmstripState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, initialIndex))
    val currentPhoto = photos.getOrNull(pagerState.currentPage) ?: return

    val msgDeletedLauncher = tr("Položka bola presunutá do koša", "Item moved to trash")
    val msgNotImplemented = tr("Zatiaľ neimplementované", "Not implemented yet")
    val msgFavAdded = tr("Pridané do obľúbených", "Added to favorites")
    val msgFavRemoved = tr("Odstránené z obľúbených", "Removed from favorites")
    val msgVideoNoEdit = tr("Editor je dostupný len pre fotky", "Editor is available for photos only")
    val msgShare = tr("Zdieľať položku", "Share item")
    val msgDeleted = tr("Položka presunutá do koša", "Item moved to trash")
    val msgDeleteError = tr("Chyba pri mazaní", "Error deleting item")
    val msgPauseFirst = tr("Pre posun na inú fotku najprv zastav video", "Pause video first to swipe")

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, msgDeletedLauncher, Toast.LENGTH_SHORT).show()
            onDeleteSuccess()
        }
    }

    fun performSingleDeleteOrTrash(targetPhoto: MediaPhoto) {
        if (targetPhoto.isTrashed) {
            // Ak už je v koši, zmažeme ju natrvalo
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intentSender = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        listOf(targetPhoto.uri)
                    ).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    val rows = context.contentResolver.delete(targetPhoto.uri, null, null)
                    if (rows == 0 && targetPhoto.path.isNotEmpty()) {
                        File(targetPhoto.path).delete()
                    }
                    Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                    onDeleteSuccess()
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverable = e as? RecoverableSecurityException
                    recoverable?.let {
                        deleteLauncher.launch(
                            IntentSenderRequest.Builder(it.userAction.actionIntent.intentSender).build()
                        )
                    } ?: Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Presun do koša (Android 11+ systémový kôš, staršie Androidy lokálny kôš)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intentSender = MediaStore.createTrashRequest(
                        context.contentResolver,
                        listOf(targetPhoto.uri),
                        true
                    ).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    onMoveToLocalTrash(setOf(targetPhoto.uri.toString()))
                    Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                    onDeleteSuccess()
                }
            } catch (_: Exception) {
                onMoveToLocalTrash(setOf(targetPhoto.uri.toString()))
                Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                onDeleteSuccess()
            }
        }
    }

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var isVideoPlaying by remember { mutableStateOf(false) }
    var hasVideoStarted by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var videoPosition by remember { mutableIntStateOf(0) }
    var videoDuration by remember { mutableIntStateOf(0) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var pendingSeekPosition by remember { mutableIntStateOf(-1) }
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val filmstripItemWidth = 42.dp
    val filmstripHorizontalPadding = (screenWidth - filmstripItemWidth) / 2

    val isFilmstripDragged by filmstripState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(filmstripState, syncFilmstrip, isFilmstripDragged) {
        if (syncFilmstrip) {
            snapshotFlow {
                if (isFilmstripDragged) {
                    val layoutInfo = filmstripState.layoutInfo
                    val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    layoutInfo.visibleItemsInfo.minByOrNull {
                        abs(it.offset + (it.size / 2) - center)
                    }?.index
                } else null
            }.collect { centerIndex ->
                if (centerIndex != null && centerIndex != pagerState.currentPage) {
                    pagerState.scrollToPage(centerIndex)
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (!isFilmstripDragged) {
            filmstripState.animateScrollToItem(pagerState.currentPage)
        }
        zoomScale = 1f
        offsetX = 0f
        offsetY = 0f

        isVideoPlaying = false
        hasVideoStarted = false
        isVideoReady = false
        videoPosition = 0
        videoDuration = 0
        pendingSeekPosition = -1
        try {
            activeMediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            activeMediaPlayer?.release()
        } catch (_: Exception) {
        }
        activeMediaPlayer = null

        onPhotoIndexChanged(pagerState.currentPage)

        val pageMedia = photos.getOrNull(pagerState.currentPage)
        if (pageMedia != null && pageMedia.mimeType.startsWith("video/")) {
            withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, pageMedia.uri)
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val dur = durStr?.toIntOrNull() ?: 0
                    withContext(Dispatchers.Main) {
                        if (videoDuration == 0) videoDuration = dur
                    }
                } catch (_: Exception) {
                } finally {
                    try {
                        retriever.release()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    LaunchedEffect(isVideoPlaying, isVideoReady) {
        val mp = activeMediaPlayer ?: return@LaunchedEffect
        if (isVideoReady) {
            try {
                if (isVideoPlaying && !mp.isPlaying) {
                    mp.start()
                } else if (!isVideoPlaying && mp.isPlaying) {
                    mp.pause()
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(isVideoPlaying, isVideoReady) {
        while (isVideoPlaying && isVideoReady) {
            val mp = activeMediaPlayer
            if (mp != null && !isDraggingSlider) {
                try {
                    if (mp.isPlaying) {
                        videoPosition = mp.currentPosition
                    }
                } catch (_: Exception) {
                }
            }
            delay(100)
        }
    }

    BackHandler(enabled = !showPhotoEditor) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isVideoPlaying && !showPhotoEditor && (if (enableZoom) zoomScale <= 1.01f else true),
            reverseLayout = true
        ) { page ->
            val pagePhoto = photos[page]
            val isCurrentPage = page == pagerState.currentPage

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (enableZoom) {
                            Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (zoomScale > 1.01f) {
                                                zoomScale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                            } else {
                                                zoomScale = 2.5f
                                            }
                                        },
                                        onTap = {
                                            isUiVisible = !isUiVisible
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        var isPanning = false
                                        var totalPan = Offset.Zero
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val pressedCount = event.changes.count { it.pressed }
                                            if (pressedCount == 0) {
                                                isPanning = false
                                                totalPan = Offset.Zero
                                            } else if (pressedCount >= 2) {
                                                isPanning = true
                                                val zoomChange = event.calculateZoom()
                                                val panChange = event.calculatePan()
                                                val newScale = (zoomScale * zoomChange).coerceIn(1f, 5f)
                                                val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                                                val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                                                zoomScale = newScale
                                                if (newScale > 1.01f) {
                                                    offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                                                    offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                                } else {
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                }
                                                event.changes.forEach { it.consume() }
                                            } else if (pressedCount == 1 && zoomScale > 1.01f) {
                                                val panChange = event.calculatePan()
                                                totalPan += panChange
                                                if (!isPanning && totalPan.getDistance() > viewConfiguration.touchSlop) {
                                                    isPanning = true
                                                }
                                                if (isPanning) {
                                                    val maxOffsetX = (size.width * (zoomScale - 1f)) / 2f
                                                    val maxOffsetY = (size.height * (zoomScale - 1f)) / 2f
                                                    offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                                                    offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                                    event.changes.forEach { it.consume() }
                                                }
                                            }
                                        }
                                    }
                                }
                        } else {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isUiVisible = !isUiVisible
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val appliedScale = if (isCurrentPage) zoomScale else 1f
                val appliedOffsetX = if (isCurrentPage) offsetX else 0f
                val appliedOffsetY = if (isCurrentPage) offsetY else 0f

                if (pagePhoto.mimeType.startsWith("video/")) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isCurrentPage && hasVideoStarted) {
                            DisposableEffect(pagePhoto.uri) {
                                onDispose {
                                    try {
                                        activeMediaPlayer?.stop()
                                    } catch (_: Exception) {
                                    }
                                    try {
                                        activeMediaPlayer?.release()
                                    } catch (_: Exception) {
                                    }
                                    activeMediaPlayer = null
                                }
                            }

                            AndroidView(
                                factory = { ctx ->
                                    TextureView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        val textureView = this

                                        fun startPlayerWithSurface(surfaceTexture: SurfaceTexture) {
                                            try {
                                                activeMediaPlayer?.release()
                                            } catch (_: Exception) {
                                            }
                                            val mp = MediaPlayer()
                                            activeMediaPlayer = mp
                                            try {
                                                mp.setDataSource(ctx, pagePhoto.uri)
                                                mp.setSurface(Surface(surfaceTexture))
                                                mp.setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                                    adjustVideoAspectRatio(textureView, vWidth, vHeight)
                                                }
                                                mp.setOnPreparedListener { preparedMp ->
                                                    isVideoReady = true
                                                    videoDuration = preparedMp.duration.coerceAtLeast(1)
                                                    adjustVideoAspectRatio(
                                                        textureView,
                                                        preparedMp.videoWidth,
                                                        preparedMp.videoHeight
                                                    )
                                                    if (pendingSeekPosition >= 0) {
                                                        preparedMp.seekTo(pendingSeekPosition)
                                                        pendingSeekPosition = -1
                                                    } else if (videoPosition > 0) {
                                                        preparedMp.seekTo(videoPosition)
                                                    }
                                                    if (isVideoPlaying) {
                                                        preparedMp.start()
                                                    }
                                                }
                                                mp.setOnCompletionListener {
                                                    isVideoPlaying = false
                                                    videoPosition = 0
                                                    hasVideoStarted = false
                                                    isVideoReady = false
                                                }
                                                mp.prepareAsync()
                                            } catch (_: Exception) {
                                                isVideoPlaying = false
                                            }
                                        }

                                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                            override fun onSurfaceTextureAvailable(
                                                surface: SurfaceTexture,
                                                width: Int,
                                                height: Int
                                            ) {
                                                startPlayerWithSurface(surface)
                                            }

                                            override fun onSurfaceTextureSizeChanged(
                                                surface: SurfaceTexture,
                                                width: Int,
                                                height: Int
                                            ) {
                                                activeMediaPlayer?.let { mp ->
                                                    try {
                                                        adjustVideoAspectRatio(textureView, mp.videoWidth, mp.videoHeight)
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                            }

                                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                                try {
                                                    activeMediaPlayer?.release()
                                                } catch (_: Exception) {
                                                }
                                                activeMediaPlayer = null
                                                return true
                                            }

                                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                                        }

                                        if (isAvailable && surfaceTexture != null) {
                                            startPlayerWithSurface(surfaceTexture!!)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = appliedScale,
                                        scaleY = appliedScale,
                                        translationX = appliedOffsetX,
                                        translationY = appliedOffsetY
                                    )
                            )
                        }

                        if (!isCurrentPage || !hasVideoStarted || !isVideoReady) {
                            SmartMediaImage(
                                uri = pagePhoto.uri,
                                mimeType = pagePhoto.mimeType,
                                contentDescription = tr("Náhľad videa", "Video thumbnail"),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = appliedScale,
                                        scaleY = appliedScale,
                                        translationX = appliedOffsetX,
                                        translationY = appliedOffsetY
                                    ),
                                contentScale = ContentScale.Fit,
                                highResVideoThumb = true
                            )
                        }

                        val showCenterButton = !isCurrentPage || !isVideoPlaying || isUiVisible
                        if (showCenterButton) {
                            IconButton(
                                onClick = {
                                    if (isCurrentPage) {
                                        if (!hasVideoStarted) {
                                            hasVideoStarted = true
                                            isVideoPlaying = true
                                        } else {
                                            isVideoPlaying = !isVideoPlaying
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) {
                                if (isCurrentPage && isVideoPlaying) {
                                    PauseBarsIcon(
                                        barWidth = 8.dp,
                                        barHeight = 28.dp,
                                        spacing = 8.dp,
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    SmartMediaImage(
                        uri = pagePhoto.uri,
                        mimeType = pagePhoto.mimeType,
                        contentDescription = tr("Náhľad fotky", "Photo preview"),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = appliedScale,
                                scaleY = appliedScale,
                                translationX = appliedOffsetX,
                                translationY = appliedOffsetY
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isUiVisible && !showPhotoEditor,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            val formattedDate = remember(currentPhoto.dateAdded) {
                SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date(currentPhoto.dateAdded * 1000L))
            }
            val shortNameWithExt = remember(currentPhoto.displayName) {
                formatShortFileNameWithExt(currentPhoto.displayName, maxBaseLength = 18)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.DarkGray.copy(alpha = 0.6f),
                    contentColor = Color.White
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = tr("Späť", "Back"))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.DarkGray.copy(alpha = 0.6f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = shortNameWithExt,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.DarkGray.copy(alpha = 0.6f),
                    contentColor = Color.White
                ) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = tr("Viac", "More"))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(tr("Otočiť obrazovku", "Rotate screen")) },
                                onClick = {
                                    menuExpanded = false
                                    val currentOrientation = activity?.resources?.configuration?.orientation
                                    if (currentOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    } else {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr("Nastaviť ako pozadie", "Set as Wallpaper")) },
                                onClick = {
                                    menuExpanded = false
                                    Toast.makeText(context, msgNotImplemented, Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr("Prezentácia (Slideshow)", "Slideshow")) },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(tr("Tlačiť", "Print")) },
                                onClick = { menuExpanded = false }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isUiVisible && !showPhotoEditor,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Video bar: maličký, dlhší, bez veľkej pilulky a s Pause/Play tlačidlom v strede nad ním
                if (currentPhoto.mimeType.startsWith("video/")) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (!hasVideoStarted) {
                                    hasVideoStarted = true
                                    isVideoPlaying = true
                                } else {
                                    isVideoPlaying = !isVideoPlaying
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        ) {
                            if (isVideoPlaying) {
                                PauseBarsIcon(
                                    barWidth = 4.dp,
                                    barHeight = 14.dp,
                                    spacing = 4.dp,
                                    tint = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatVideoTime(videoPosition),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Slider(
                                value = videoPosition.toFloat().coerceIn(0f, videoDuration.coerceAtLeast(1).toFloat()),
                                onValueChange = { newVal ->
                                    isDraggingSlider = true
                                    videoPosition = newVal.toInt()
                                    if (!hasVideoStarted) {
                                        pendingSeekPosition = videoPosition
                                        hasVideoStarted = true
                                    } else if (isVideoReady) {
                                        try {
                                            activeMediaPlayer?.seekTo(videoPosition)
                                        } catch (_: Exception) {
                                        }
                                    }
                                },
                                onValueChangeFinished = {
                                    isDraggingSlider = false
                                },
                                valueRange = 0f..videoDuration.coerceAtLeast(1).toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(20.dp)
                                    .padding(horizontal = 6.dp)
                            )

                            Text(
                                text = formatVideoTime(videoDuration),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                LazyRow(
                    state = filmstripState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = filmstripHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    reverseLayout = true
                ) {
                    itemsIndexed(
                        items = photos,
                        key = { _, photo -> photo.uri.toString() }
                    ) { index, filmPhoto ->
                        val isSelected = index == pagerState.currentPage
                        SmartMediaImage(
                            uri = filmPhoto.uri,
                            mimeType = filmPhoto.mimeType,
                            contentDescription = null,
                            modifier = Modifier
                                .width(if (isSelected) 42.dp else 30.dp)
                                .height(if (isSelected) 56.dp else 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isVideoPlaying) {
                                        Toast.makeText(context, msgPauseFirst, Toast.LENGTH_SHORT).show()
                                    } else {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                val isCurrentFav = favoriteUriStrings.contains(currentPhoto.uri.toString())

                Surface(
                    modifier = Modifier.padding(bottom = adaptiveBottomPadding),
                    shape = RoundedCornerShape(50),
                    color = Color.DarkGray.copy(alpha = 0.8f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val uriStr = currentPhoto.uri.toString()
                                val willBeFav = !favoriteUriStrings.contains(uriStr)
                                onToggleFavorite(uriStr)
                                Toast.makeText(
                                    context,
                                    if (willBeFav) msgFavAdded else msgFavRemoved,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = tr("Obľúbené", "Favorites"),
                                tint = if (isCurrentFav) Color(0xFFFF3B30) else Color.White
                            )
                        }
                        IconButton(onClick = {
                            if (currentPhoto.mimeType.startsWith("video/")) {
                                Toast.makeText(context, msgVideoNoEdit, Toast.LENGTH_SHORT).show()
                            } else {
                                showPhotoEditor = true
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = tr("Upraviť", "Edit"))
                        }
                        IconButton(onClick = { showInfoSheet = true }) {
                            Icon(Icons.Default.Info, contentDescription = tr("Info", "Info"))
                        }

                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = currentPhoto.mimeType
                                putExtra(Intent.EXTRA_STREAM, currentPhoto.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, msgShare))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = tr("Zdieľať", "Share"))
                        }

                        IconButton(onClick = {
                            if (confirmDelete) {
                                showDeleteDialog = true
                            } else {
                                performSingleDeleteOrTrash(currentPhoto)
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = tr("Do koša", "Delete"))
                        }
                    }
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(
                        tr("Informácie", "Information"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val df = SimpleDateFormat("dd. MM. yyyy HH:mm", Locale.getDefault())
                    val dateStr = df.format(Date(currentPhoto.dateAdded * 1000L))
                    val sizeStr = remember(currentPhoto, editedPhotoCacheVersion) {
                        formatMediaFileSize(context, currentPhoto)
                    }

                    InfoRow(Icons.Default.Image, tr("Názov", "Name"), currentPhoto.displayName)
                    InfoRow(Icons.Default.DateRange, tr("Dátum", "Date"), dateStr)
                    InfoRow(Icons.Default.Info, tr("Veľkosť súboru", "File Size"), sizeStr)
                    InfoRow(Icons.Default.Folder, tr("Cesta k súboru", "File Path"), currentPhoto.path)
                }
            }
        }

        if (showPhotoEditor) {
            FullPhotoEditorModal(
                photo = currentPhoto,
                allGalleryPhotos = allGalleryPhotos.filter { !it.mimeType.startsWith("video/") },
                onClose = { showPhotoEditor = false },
                onSaved = {
                    showPhotoEditor = false
                    onEditSaved()
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    if (currentPhoto.isTrashed) tr("Vymazať natrvalo?", "Delete permanently?")
                    else tr("Presunúť do koša?", "Move to trash?")
                )
            },
            text = {
                Text(
                    if (currentPhoto.isTrashed) tr("Táto položka bude natrvalo vymazaná zo zariadenia.", "This item will be permanently deleted.")
                    else tr("Položky budú uchované v koši 30 dní.", "Items will be kept in trash for 30 days.")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        performSingleDeleteOrTrash(currentPhoto)
                    }
                ) {
                    Text(tr("Zmazať", "Delete"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(tr("Zrušiť", "Cancel")) }
            }
        )
    }
}

// ----------------------------------------------------
// KOMPONENT: PLNOHODNOTNÝ FOTO EDITOR (FEATHER STUDIOS)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPhotoEditorModal(
    photo: MediaPhoto,
    allGalleryPhotos: List<MediaPhoto>,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val msgSavedCopy = tr("Uložené ako nová kópia", "Saved as new copy")
    val msgSavedOverwrite = tr("Pôvodná fotka bola prepísaná", "Original photo overwritten")
    val msgSaveError = tr("Chyba pri ukladaní fotky", "Error saving photo")

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var activeToolTab by rememberSaveable { mutableIntStateOf(0) }

    var rotationDegrees by rememberSaveable { mutableIntStateOf(0) }
    var flipHorizontal by rememberSaveable { mutableStateOf(false) }
    var selectedAspectIndex by rememberSaveable { mutableIntStateOf(0) }

    // Možnosti orezu vrátane voľného ("Voľný" / "Free")
    val aspectOptions = listOf(
        tr("Voľný", "Free") to -1f,
        tr("Originál", "Original") to null,
        "1:1" to 1f,
        "4:3" to (4f / 3f),
        "3:4" to (3f / 4f),
        "16:9" to (16f / 9f),
        "9:16" to (9f / 16f)
    )

    // Normalizované súradnice 4 rohov orezávacieho rámika (0f..1f)
    var cropLeft by rememberSaveable { mutableFloatStateOf(0f) }
    var cropTop by rememberSaveable { mutableFloatStateOf(0f) }
    var cropRight by rememberSaveable { mutableFloatStateOf(1f) }
    var cropBottom by rememberSaveable { mutableFloatStateOf(1f) }

    var resizeScalePercent by rememberSaveable { mutableIntStateOf(100) }
    var customTargetWidth by rememberSaveable { mutableIntStateOf(0) }
    var customTargetHeight by rememberSaveable { mutableIntStateOf(0) }
    var showCustomSizeDialog by remember { mutableStateOf(false) }

    var selectedFilter by rememberSaveable { mutableStateOf(PhotoFilterPreset.NONE) }
    var exposure by rememberSaveable { mutableFloatStateOf(0f) }
    var contrast by rememberSaveable { mutableFloatStateOf(0f) }
    var saturation by rememberSaveable { mutableFloatStateOf(0f) }
    var warmth by rememberSaveable { mutableFloatStateOf(0f) }
    var vignette by rememberSaveable { mutableFloatStateOf(0f) }
    var selectedAdjustParam by rememberSaveable { mutableIntStateOf(0) }

    var textOverlays by remember { mutableStateOf<List<EditorTextOverlay>>(emptyList()) }
    var selectedTextId by remember { mutableStateOf<Long?>(null) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var editingTextId by remember { mutableStateOf<Long?>(null) }
    var dialogTextInput by remember { mutableStateOf("") }
    var dialogTextColor by remember { mutableStateOf(Color.White) }
    var dialogTextBgStyle by remember { mutableIntStateOf(1) }

    var stickerOverlays by remember { mutableStateOf<List<EditorStickerOverlay>>(emptyList()) }
    var selectedStickerId by remember { mutableStateOf<Long?>(null) }
    var showStickerPickerSheet by remember { mutableStateOf(false) }

    var showSaveChoiceDialog by remember { mutableStateOf(false) }
    var pendingOverwriteBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val overwritePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingOverwriteBitmap != null) {
            coroutineScope.launch {
                isSaving = true
                val bmp = pendingOverwriteBitmap!!
                val success = withContext(Dispatchers.IO) {
                    try {
                        writeBitmapToExistingPhoto(context, photo, bmp)
                    } catch (_: Exception) {
                        false
                    }
                }
                isSaving = false
                if (success) {
                    editedPhotoCache.put(photo.uri.toString(), bmp)
                    editedPhotoCacheVersion++
                    Toast.makeText(context, msgSavedOverwrite, Toast.LENGTH_SHORT).show()
                    onSaved()
                } else {
                    Toast.makeText(context, msgSaveError, Toast.LENGTH_SHORT).show()
                }
                pendingOverwriteBitmap = null
            }
        } else {
            isSaving = false
            pendingOverwriteBitmap = null
        }
    }

    val systemStickerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { pickedUri: Uri? ->
        if (pickedUri != null) {
            coroutineScope.launch {
                val loaded = loadEditableBitmap(context, pickedUri, maxDimension = 900)
                if (loaded != null) {
                    val newSticker = EditorStickerOverlay(uri = pickedUri, bitmap = loaded)
                    stickerOverlays = stickerOverlays + newSticker
                    selectedStickerId = newSticker.id
                    selectedTextId = null
                    showStickerPickerSheet = false
                }
            }
        }
    }

    LaunchedEffect(photo.uri) {
        sourceBitmap = loadEditableBitmap(context, photo.uri, maxDimension = 2560)
    }

    val transformedBitmap = remember(sourceBitmap, rotationDegrees, flipHorizontal) {
        sourceBitmap?.let { src ->
            transformBitmapOnly(
                source = src,
                rotationDegrees = rotationDegrees,
                flipHorizontal = flipHorizontal
            )
        }
    }

    fun applyAspectRatioToCropBox(aspect: Float?, bmpW: Int, bmpH: Int) {
        if (aspect == null) {
            cropLeft = 0f
            cropTop = 0f
            cropRight = 1f
            cropBottom = 1f
            return
        }
        if (aspect < 0f) {
            return
        }
        if (bmpW <= 0 || bmpH <= 0) return
        val imgRatio = bmpW.toFloat() / bmpH.toFloat()
        val centerX = (cropLeft + cropRight) / 2f
        val centerY = (cropTop + cropBottom) / 2f
        val normW: Float
        val normH: Float
        if (imgRatio > aspect) {
            normH = 1f
            normW = (aspect / imgRatio).coerceIn(0.1f, 1f)
        } else {
            normW = 1f
            normH = (imgRatio / aspect).coerceIn(0.1f, 1f)
        }
        val newLeft = (centerX - normW / 2f).coerceIn(0f, 1f - normW)
        val newTop = (centerY - normH / 2f).coerceIn(0f, 1f - normH)
        cropLeft = newLeft
        cropTop = newTop
        cropRight = newLeft + normW
        cropBottom = newTop + normH
    }

    val croppedPreviewBitmap = remember(
        transformedBitmap,
        cropLeft,
        cropTop,
        cropRight,
        cropBottom
    ) {
        transformedBitmap?.let { trans ->
            cropNormalizedBitmap(
                transformed = trans,
                cropLeft = cropLeft,
                cropTop = cropTop,
                cropRight = cropRight,
                cropBottom = cropBottom
            )
        }
    }

    val baseWidth = croppedPreviewBitmap?.width ?: 0
    val baseHeight = croppedPreviewBitmap?.height ?: 0
    val finalOutputWidth =
        if (customTargetWidth > 0) customTargetWidth else (baseWidth * resizeScalePercent / 100).coerceAtLeast(1)
    val finalOutputHeight =
        if (customTargetHeight > 0) customTargetHeight else (baseHeight * resizeScalePercent / 100).coerceAtLeast(1)

    val combinedMatrixArray = remember(exposure, contrast, saturation, warmth, selectedFilter) {
        buildCombinedColorMatrixArray(exposure, contrast, saturation, warmth, selectedFilter)
    }

    BackHandler { onClose() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101012),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = tr("Zrušiť", "Cancel"), tint = Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = tr("Foto Editor", "Photo Editor"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    TextButton(
                        onClick = {
                            rotationDegrees = 0
                            flipHorizontal = false
                            selectedAspectIndex = 0
                            cropLeft = 0f
                            cropTop = 0f
                            cropRight = 1f
                            cropBottom = 1f
                            resizeScalePercent = 100
                            customTargetWidth = 0
                            customTargetHeight = 0
                            selectedFilter = PhotoFilterPreset.NONE
                            exposure = 0f
                            contrast = 0f
                            saturation = 0f
                            warmth = 0f
                            vignette = 0f
                            textOverlays = emptyList()
                            stickerOverlays = emptyList()
                            selectedTextId = null
                            selectedStickerId = null
                        }
                    ) {
                        Text(tr("Reset", "Reset"), color = Color(0xFFFF6B6B), fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = { showSaveChoiceDialog = true },
                    enabled = croppedPreviewBitmap != null && !isSaving,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Uložiť", "Save"), fontWeight = FontWeight.Bold)
                }
            }

            var containerSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .onSizeChanged { containerSize = it },
                contentAlignment = Alignment.Center
            ) {
                val displayedBitmap = if (activeToolTab == 0) transformedBitmap else croppedPreviewBitmap

                if (displayedBitmap == null) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    val bmp = displayedBitmap
                    val imgAspect = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                    val availW = containerSize.width.toFloat().coerceAtLeast(1f)
                    val availH = containerSize.height.toFloat().coerceAtLeast(1f)

                    val fittedW: Float
                    val fittedH: Float
                    if (availW / availH > imgAspect) {
                        fittedH = availH
                        fittedW = availH * imgAspect
                    } else {
                        fittedW = availW
                        fittedH = availW / imgAspect
                    }

                    val fittedWDp = with(density) { fittedW.toDp() }
                    val fittedHDp = with(density) { fittedH.toDp() }

                    Box(
                        modifier = Modifier
                            .size(fittedWDp, fittedHDp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        selectedTextId = null
                                        selectedStickerId = null
                                    }
                                )
                            }
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            colorFilter = ColorFilter.colorMatrix(
                                androidx.compose.ui.graphics.ColorMatrix(combinedMatrixArray)
                            )
                        )

                        if (vignette > 1f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                Color.Black.copy(alpha = (vignette / 100f * 0.82f).coerceIn(0f, 0.85f))
                                            )
                                        )
                                    )
                            )
                        }

                        if (activeToolTab == 0) {
                            // INTERAKTÍVNY CROP OVERLAY SO 4 ROHOVÝMI BODMI A POSÚVANÍM PO FOTKE
                            val cornerTouchRadiusPx = with(density) { 36.dp.toPx() }
                            var dragMode by remember { mutableIntStateOf(0) } // 1=TL, 2=TR, 3=BL, 4=BR, 5=PAN

                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(fittedW, fittedH, selectedAspectIndex, bmp.width, bmp.height) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val lPx = cropLeft * fittedW
                                                val tPx = cropTop * fittedH
                                                val rPx = cropRight * fittedW
                                                val bPx = cropBottom * fittedH

                                                val dTL = hypot(offset.x - lPx, offset.y - tPx)
                                                val dTR = hypot(offset.x - rPx, offset.y - tPx)
                                                val dBL = hypot(offset.x - lPx, offset.y - bPx)
                                                val dBR = hypot(offset.x - rPx, offset.y - bPx)

                                                val minDist = min(min(dTL, dTR), min(dBL, dBR))
                                                dragMode = if (minDist <= cornerTouchRadiusPx) {
                                                    when (minDist) {
                                                        dTL -> 1
                                                        dTR -> 2
                                                        dBL -> 3
                                                        else -> 4
                                                    }
                                                } else {
                                                    5 // Ťahanie vo vnútri / presúvanie výrezu po fotke
                                                }
                                            },
                                            onDragEnd = { dragMode = 0 },
                                            onDragCancel = { dragMode = 0 },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                if (fittedW <= 1f || fittedH <= 1f) return@detectDragGestures
                                                val dx = dragAmount.x / fittedW
                                                val dy = dragAmount.y / fittedH
                                                val minSpan = 0.08f
                                                val chosenAspect = aspectOptions[selectedAspectIndex].second

                                                if (dragMode == 5) {
                                                    // Posúvanie (Pan) celého orezávacieho rámika po fotke
                                                    val w = cropRight - cropLeft
                                                    val h = cropBottom - cropTop
                                                    val newL = (cropLeft + dx).coerceIn(0f, 1f - w)
                                                    val newT = (cropTop + dy).coerceIn(0f, 1f - h)
                                                    cropLeft = newL
                                                    cropRight = newL + w
                                                    cropTop = newT
                                                    cropBottom = newT + h
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                } else if (chosenAspect != null && chosenAspect < 0f) {
                                                    // Voľné ťahanie 4 rohov (Free crop)
                                                    when (dragMode) {
                                                        1 -> {
                                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                                        }
                                                        2 -> {
                                                            cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                            cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                                        }
                                                        3 -> {
                                                            cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                                        }
                                                        4 -> {
                                                            cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                            cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                                        }
                                                    }
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                } else {
                                                    // Ťahanie rohov pri fixnom pomere strán (Aspect Ratio)
                                                    val effAspect = chosenAspect
                                                        ?: (bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f))
                                                    val normRatio = (effAspect * (bmp.height.toFloat() / bmp.width.toFloat().coerceAtLeast(1f)))
                                                        .coerceAtLeast(0.05f)

                                                    when (dragMode) {
                                                        1 -> {
                                                            val anchorX = cropRight
                                                            val anchorY = cropBottom
                                                            val maxAllowedW = min(anchorX, anchorY * normRatio)
                                                            val minAllowedW = max(minSpan, minSpan * normRatio)
                                                            if (maxAllowedW >= minAllowedW) {
                                                                val newW = (anchorX - (cropLeft + dx)).coerceIn(minAllowedW, maxAllowedW)
                                                                val newH = newW / normRatio
                                                                cropLeft = (anchorX - newW).coerceIn(0f, anchorX - minSpan)
                                                                cropTop = (anchorY - newH).coerceIn(0f, anchorY - minSpan)
                                                            }
                                                        }
                                                        2 -> {
                                                            val anchorX = cropLeft
                                                            val anchorY = cropBottom
                                                            val maxAllowedW = min(1f - anchorX, anchorY * normRatio)
                                                            val minAllowedW = max(minSpan, minSpan * normRatio)
                                                            if (maxAllowedW >= minAllowedW) {
                                                                val newW = ((cropRight + dx) - anchorX).coerceIn(minAllowedW, maxAllowedW)
                                                                val newH = newW / normRatio
                                                                cropRight = (anchorX + newW).coerceIn(anchorX + minSpan, 1f)
                                                                cropTop = (anchorY - newH).coerceIn(0f, anchorY - minSpan)
                                                            }
                                                        }
                                                        3 -> {
                                                            val anchorX = cropRight
                                                            val anchorY = cropTop
                                                            val maxAllowedW = min(anchorX, (1f - anchorY) * normRatio)
                                                            val minAllowedW = max(minSpan, minSpan * normRatio)
                                                            if (maxAllowedW >= minAllowedW) {
                                                                val newW = (anchorX - (cropLeft + dx)).coerceIn(minAllowedW, maxAllowedW)
                                                                val newH = newW / normRatio
                                                                cropLeft = (anchorX - newW).coerceIn(0f, anchorX - minSpan)
                                                                cropBottom = (anchorY + newH).coerceIn(anchorY + minSpan, 1f)
                                                            }
                                                        }
                                                        4 -> {
                                                            val anchorX = cropLeft
                                                            val anchorY = cropTop
                                                            val maxAllowedW = min(1f - anchorX, (1f - anchorY) * normRatio)
                                                            val minAllowedW = max(minSpan, minSpan * normRatio)
                                                            if (maxAllowedW >= minAllowedW) {
                                                                val newW = ((cropRight + dx) - anchorX).coerceIn(minAllowedW, maxAllowedW)
                                                                val newH = newW / normRatio
                                                                cropRight = (anchorX + newW).coerceIn(anchorX + minSpan, 1f)
                                                                cropBottom = (anchorY + newH).coerceIn(anchorY + minSpan, 1f)
                                                            }
                                                        }
                                                    }
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                }
                                            }
                                        )
                                    }
                            ) {
                                val lPx = cropLeft * size.width
                                val tPx = cropTop * size.height
                                val rPx = cropRight * size.width
                                val bPx = cropBottom * size.height
                                val cW = (rPx - lPx).coerceAtLeast(1f)
                                val cH = (bPx - tPx).coerceAtLeast(1f)

                                val maskColor = Color.Black.copy(alpha = 0.58f)
                                // Stmavenie mimo orezávacieho rámika
                                drawRect(color = maskColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, tPx))
                                drawRect(color = maskColor, topLeft = Offset(0f, bPx), size = androidx.compose.ui.geometry.Size(size.width, size.height - bPx))
                                drawRect(color = maskColor, topLeft = Offset(0f, tPx), size = androidx.compose.ui.geometry.Size(lPx, cH))
                                drawRect(color = maskColor, topLeft = Offset(rPx, tPx), size = androidx.compose.ui.geometry.Size(size.width - rPx, cH))

                                // Biely rámik orezu
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(lPx, tPx),
                                    size = androidx.compose.ui.geometry.Size(cW, cH),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Mriežka tretín (Rule of thirds)
                                val gridColor = Color.White.copy(alpha = 0.45f)
                                val gridStroke = 1.dp.toPx()
                                drawLine(gridColor, Offset(lPx + cW / 3f, tPx), Offset(lPx + cW / 3f, bPx), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(lPx + cW * 2f / 3f, tPx), Offset(lPx + cW * 2f / 3f, bPx), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(lPx, tPx + cH / 3f), Offset(rPx, tPx + cH / 3f), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(lPx, tPx + cH * 2f / 3f), Offset(rPx, tPx + cH * 2f / 3f), strokeWidth = gridStroke)

                                // 4 rohové body (handles), ktoré môžeš chytiť a ťahať
                                val outerRadius = 10.dp.toPx()
                                val innerRadius = 6.dp.toPx()
                                val corners = listOf(
                                    Offset(lPx, tPx),
                                    Offset(rPx, tPx),
                                    Offset(lPx, bPx),
                                    Offset(rPx, bPx)
                                )
                                corners.forEach { pt ->
                                    drawCircle(color = Color.White, radius = outerRadius, center = pt)
                                    drawCircle(color = IosBlue, radius = innerRadius, center = pt)
                                }
                            }
                        } else {
                            stickerOverlays.forEach { sticker ->
                                val isSelected = selectedStickerId == sticker.id
                                val baseStickerWidthPx = fittedW * 0.28f * sticker.scale
                                val aspect = sticker.bitmap.height.toFloat() / sticker.bitmap.width.toFloat().coerceAtLeast(1f)
                                val stickerWPx = baseStickerWidthPx.coerceAtLeast(32f)
                                val stickerHPx = (stickerWPx * aspect).coerceAtLeast(32f)

                                val stickerWDp = with(density) { stickerWPx.toDp() }
                                val stickerHDp = with(density) { stickerHPx.toDp() }

                                val offsetXPx = sticker.normX * fittedW - stickerWPx / 2f
                                val offsetYPx = sticker.normY * fittedH - stickerHPx / 2f

                                val shapeClip = when (sticker.shapeMode) {
                                    1 -> RoundedCornerShape(18)
                                    2 -> CircleShape
                                    else -> RoundedCornerShape(0.dp)
                                }

                                Box(
                                    modifier = Modifier
                                        .offset {
                                            androidx.compose.ui.unit.IntOffset(
                                                offsetXPx.roundToInt(),
                                                offsetYPx.roundToInt()
                                            )
                                        }
                                        .size(stickerWDp, stickerHDp)
                                        .graphicsLayer(
                                            rotationZ = sticker.rotation,
                                            alpha = sticker.alpha
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF4DA3FF) else Color.Transparent,
                                            shape = shapeClip
                                        )
                                        .clip(shapeClip)
                                        .pointerInput(sticker.id, fittedW, fittedH) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val pressed = event.changes.count { it.pressed }
                                                    if (pressed >= 2) {
                                                        selectedStickerId = sticker.id
                                                        selectedTextId = null
                                                        val zoom = event.calculateZoom()
                                                        val rot = event.calculateRotation()
                                                        val pan = event.calculatePan()
                                                        stickerOverlays = stickerOverlays.map { item ->
                                                            if (item.id == sticker.id) {
                                                                item.copy(
                                                                    scale = (item.scale * zoom).coerceIn(0.2f, 4f),
                                                                    rotation = item.rotation + rot,
                                                                    normX = (item.normX + pan.x / fittedW).coerceIn(0.02f, 0.98f),
                                                                    normY = (item.normY + pan.y / fittedH).coerceIn(0.02f, 0.98f)
                                                                )
                                                            } else item
                                                        }
                                                        event.changes.forEach { it.consume() }
                                                    } else if (pressed == 1) {
                                                        val pan = event.calculatePan()
                                                        if (pan != Offset.Zero) {
                                                            selectedStickerId = sticker.id
                                                            selectedTextId = null
                                                            activeToolTab = 4
                                                            stickerOverlays = stickerOverlays.map { item ->
                                                                if (item.id == sticker.id) {
                                                                    item.copy(
                                                                        normX = (item.normX + pan.x / fittedW).coerceIn(0.02f, 0.98f),
                                                                        normY = (item.normY + pan.y / fittedH).coerceIn(0.02f, 0.98f)
                                                                    )
                                                                } else item
                                                            }
                                                            event.changes.forEach { it.consume() }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .clickable {
                                            selectedStickerId = sticker.id
                                            selectedTextId = null
                                            activeToolTab = 4
                                        }
                                ) {
                                    Image(
                                        bitmap = sticker.bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            textOverlays.forEach { textItem ->
                                val isSelected = selectedTextId == textItem.id
                                var textBoxSize by remember { mutableStateOf(IntSize.Zero) }
                                val fontSizeSp = with(density) { (fittedW * 0.065f * textItem.scale).toSp() }

                                val offsetXPx = textItem.normX * fittedW - textBoxSize.width / 2f
                                val offsetYPx = textItem.normY * fittedH - textBoxSize.height / 2f

                                val bgColor = when (textItem.bgStyle) {
                                    1 -> Color.Black.copy(alpha = 0.72f)
                                    2 -> Color.White.copy(alpha = 0.85f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .onSizeChanged { textBoxSize = it }
                                        .offset {
                                            androidx.compose.ui.unit.IntOffset(
                                                offsetXPx.roundToInt(),
                                                offsetYPx.roundToInt()
                                            )
                                        }
                                        .graphicsLayer(rotationZ = textItem.rotation)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF4DA3FF) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(bgColor, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .pointerInput(textItem.id, fittedW, fittedH) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    selectedTextId = textItem.id
                                                    selectedStickerId = null
                                                    activeToolTab = 3
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    textOverlays = textOverlays.map { item ->
                                                        if (item.id == textItem.id) {
                                                            item.copy(
                                                                normX = (item.normX + dragAmount.x / fittedW).coerceIn(0.05f, 0.95f),
                                                                normY = (item.normY + dragAmount.y / fittedH).coerceIn(0.05f, 0.95f)
                                                            )
                                                        } else item
                                                    }
                                                }
                                            )
                                        }
                                        .clickable {
                                            selectedTextId = textItem.id
                                            selectedStickerId = null
                                            activeToolTab = 3
                                        }
                                ) {
                                    Text(
                                        text = textItem.text,
                                        color = textItem.color,
                                        fontSize = fontSizeSp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "${finalOutputWidth} × ${finalOutputHeight} px",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                tr("Ukladám fotku...", "Saving photo..."),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Surface(
                color = Color(0xFF1A1A1E),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 135.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (activeToolTab) {
                            0 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            tr("Ťahaj 4 rohy alebo posúvaj výrez po fotke", "Drag 4 corners or pan crop box on photo"),
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    rotationDegrees = (rotationDegrees + 90) % 360
                                                    cropLeft = 0f
                                                    cropTop = 0f
                                                    cropRight = 1f
                                                    cropBottom = 1f
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("90°", fontSize = 11.sp, color = Color.White)
                                            }
                                            OutlinedButton(
                                                onClick = { flipHorizontal = !flipHorizontal },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(tr("Zrkadlo", "Flip"), fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        aspectOptions.forEachIndexed { idx, (label, ratioVal) ->
                                            val selected = selectedAspectIndex == idx
                                            FilterChip(
                                                selected = selected,
                                                onClick = {
                                                    selectedAspectIndex = idx
                                                    transformedBitmap?.let { trans ->
                                                        applyAspectRatioToCropBox(ratioVal, trans.width, trans.height)
                                                    }
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                },
                                                label = { Text(label, fontSize = 12.sp) }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${finalOutputWidth}×${finalOutputHeight}px:",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                        listOf(100, 75, 50, 25).forEach { pct ->
                                            val isSelected = customTargetWidth == 0 && resizeScalePercent == pct
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    resizeScalePercent = pct
                                                    customTargetWidth = 0
                                                    customTargetHeight = 0
                                                },
                                                label = { Text("$pct%", fontSize = 12.sp) }
                                            )
                                        }
                                        FilterChip(
                                            selected = customTargetWidth > 0,
                                            onClick = { showCustomSizeDialog = true },
                                            label = { Text(tr("Vlastné px...", "Custom px..."), fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }

                            1 -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(PhotoFilterPreset.values().size) { idx ->
                                        val preset = PhotoFilterPreset.values()[idx]
                                        val isSelected = selectedFilter == preset
                                        val previewMatrix = remember(preset) {
                                            buildCombinedColorMatrixArray(0f, 0f, 0f, 0f, preset)
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedFilter = preset }
                                                .padding(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                            ) {
                                                croppedPreviewBitmap?.let { bmp ->
                                                    Image(
                                                        bitmap = bmp.asImageBitmap(),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        colorFilter = ColorFilter.colorMatrix(
                                                            androidx.compose.ui.graphics.ColorMatrix(previewMatrix)
                                                        )
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = tr(preset.skName, preset.enName),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            2 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val adjustNames = listOf(
                                        tr("Expozícia", "Exposure") to exposure.roundToInt(),
                                        tr("Kontrast", "Contrast") to contrast.roundToInt(),
                                        tr("Saturácia", "Saturation") to saturation.roundToInt(),
                                        tr("Teplota", "Warmth") to warmth.roundToInt(),
                                        tr("Vinetácia", "Vignette") to vignette.roundToInt()
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        adjustNames.forEachIndexed { idx, (name, value) ->
                                            FilterChip(
                                                selected = selectedAdjustParam == idx,
                                                onClick = { selectedAdjustParam = idx },
                                                label = { Text("$name ($value)", fontSize = 12.sp) }
                                            )
                                        }
                                    }

                                    val currentVal = when (selectedAdjustParam) {
                                        0 -> exposure
                                        1 -> contrast
                                        2 -> saturation
                                        3 -> warmth
                                        else -> vignette
                                    }
                                    val valRange = if (selectedAdjustParam == 4) 0f..100f else -100f..100f

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Slider(
                                            value = currentVal,
                                            onValueChange = { v ->
                                                when (selectedAdjustParam) {
                                                    0 -> exposure = v
                                                    1 -> contrast = v
                                                    2 -> saturation = v
                                                    3 -> warmth = v
                                                    else -> vignette = v
                                                }
                                            },
                                            valueRange = valRange,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = {
                                                when (selectedAdjustParam) {
                                                    0 -> exposure = 0f
                                                    1 -> contrast = 0f
                                                    2 -> saturation = 0f
                                                    3 -> warmth = 0f
                                                    else -> vignette = 0f
                                                }
                                            }
                                        ) {
                                            Text("0", color = Color.LightGray)
                                        }
                                    }
                                }
                            }

                            3 -> {
                                val activeText = textOverlays.find { it.id == selectedTextId }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                editingTextId = null
                                                dialogTextInput = ""
                                                dialogTextColor = Color.White
                                                dialogTextBgStyle = 1
                                                showAddTextDialog = true
                                            },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr("Pridať text", "Add Text"), fontSize = 13.sp)
                                        }

                                        if (activeText != null) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        editingTextId = activeText.id
                                                        dialogTextInput = activeText.text
                                                        dialogTextColor = activeText.color
                                                        dialogTextBgStyle = activeText.bgStyle
                                                        showAddTextDialog = true
                                                    },
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        textOverlays = textOverlays.filterNot { it.id == activeText.id }
                                                        selectedTextId = null
                                                    },
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = Color(0xFFFF6B6B)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (activeText != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                tr("Veľkosť", "Size"),
                                                fontSize = 12.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.width(56.dp)
                                            )
                                            Slider(
                                                value = activeText.scale,
                                                onValueChange = { s ->
                                                    textOverlays = textOverlays.map {
                                                        if (it.id == activeText.id) it.copy(scale = s) else it
                                                    }
                                                },
                                                valueRange = 0.4f..3.5f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                tr("Uhol", "Angle"),
                                                fontSize = 12.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Slider(
                                                value = activeText.rotation,
                                                onValueChange = { r ->
                                                    textOverlays = textOverlays.map {
                                                        if (it.id == activeText.id) it.copy(rotation = r) else it
                                                    }
                                                },
                                                valueRange = -180f..180f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = tr(
                                                "Tip: Pridaj text a posúvaj ho prstom priamo po fotke.",
                                                "Tip: Add text and drag it directly on the photo."
                                            ),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            4 -> {
                                val activeSticker = stickerOverlays.find { it.id == selectedStickerId }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { showStickerPickerSheet = true },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr("Pridať fotku (Sticker)", "Add Photo Sticker"), fontSize = 13.sp)
                                        }

                                        if (activeSticker != null) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        stickerOverlays = stickerOverlays.map {
                                                            if (it.id == activeSticker.id) it.copy(shapeMode = (it.shapeMode + 1) % 3) else it
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    val shapeLabel = when (activeSticker.shapeMode) {
                                                        1 -> tr("Zaoblený", "Rounded")
                                                        2 -> tr("Kruh", "Circle")
                                                        else -> tr("Ostrý", "Square")
                                                    }
                                                    Text(shapeLabel, fontSize = 11.sp, color = Color.White)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        stickerOverlays = stickerOverlays.filterNot { it.id == activeSticker.id }
                                                        selectedStickerId = null
                                                    },
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = Color(0xFFFF6B6B)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (activeSticker != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                tr("Veľkosť", "Size"),
                                                fontSize = 11.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.width(48.dp)
                                            )
                                            Slider(
                                                value = activeSticker.scale,
                                                onValueChange = { s ->
                                                    stickerOverlays = stickerOverlays.map {
                                                        if (it.id == activeSticker.id) it.copy(scale = s) else it
                                                    }
                                                },
                                                valueRange = 0.25f..3.5f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                tr("Uhol", "Angle"),
                                                fontSize = 11.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.width(36.dp)
                                            )
                                            Slider(
                                                value = activeSticker.rotation,
                                                onValueChange = { r ->
                                                    stickerOverlays = stickerOverlays.map {
                                                        if (it.id == activeSticker.id) it.copy(rotation = r) else it
                                                    }
                                                },
                                                valueRange = -180f..180f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = tr(
                                                "Tip: Vyber inú fotku z galérie ako sticker a umiestni ju kamkoľvek.",
                                                "Tip: Pick another photo from gallery as a sticker."
                                            ),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // Karta premenovaná na "Crop" s vlastnou CropIcon
                    val tabs = listOf(
                        "Crop" to CropIcon,
                        tr("Filtre", "Filters") to Icons.Default.Star,
                        tr("Úpravy", "Adjust") to Icons.Default.Settings,
                        tr("Text", "Text") to Icons.Default.Create,
                        tr("Stickers", "Stickers") to Icons.Default.Image
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tabs.forEachIndexed { idx, (label, icon) ->
                            val selected = activeToolTab == idx
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { activeToolTab = idx }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTextDialog) {
        val palette = listOf(
            Color.White, Color.Black, Color(0xFFFF3B30), Color(0xFFFFCC00),
            Color(0xFF34C759), Color(0xFF007AFF), Color(0xFFAF52DE),
            Color(0xFFFF2D55), Color(0xFFFF9500), Color(0xFF5AC8FA)
        )
        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text(if (editingTextId == null) tr("Pridať text", "Add Text") else tr("Upraviť text", "Edit Text")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = dialogTextInput,
                        onValueChange = { dialogTextInput = it },
                        placeholder = { Text(tr("Napíš svoj text...", "Type your text...")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(tr("Farba textu", "Text Color"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        palette.forEach { col ->
                            val isSelected = dialogTextColor == col
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { dialogTextColor = col }
                            )
                        }
                    }

                    Text(tr("Štýl pozadia", "Background Style"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            0 to tr("Žiadne", "None"),
                            1 to tr("Tmavé", "Dark"),
                            2 to tr("Svetlé", "Light")
                        ).forEach { (styleId, label) ->
                            FilterChip(
                                selected = dialogTextBgStyle == styleId,
                                onClick = { dialogTextBgStyle = styleId },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dialogTextInput.isNotBlank()) {
                            if (editingTextId == null) {
                                val newItem = EditorTextOverlay(
                                    text = dialogTextInput,
                                    color = dialogTextColor,
                                    bgStyle = dialogTextBgStyle
                                )
                                textOverlays = textOverlays + newItem
                                selectedTextId = newItem.id
                                selectedStickerId = null
                            } else {
                                textOverlays = textOverlays.map {
                                    if (it.id == editingTextId) {
                                        it.copy(
                                            text = dialogTextInput,
                                            color = dialogTextColor,
                                            bgStyle = dialogTextBgStyle
                                        )
                                    } else it
                                }
                            }
                        }
                        showAddTextDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) {
                    Text(tr("Zrušiť", "Cancel"))
                }
            }
        )
    }

    if (showCustomSizeDialog) {
        var widthInput by remember { mutableStateOf(finalOutputWidth.toString()) }
        var heightInput by remember { mutableStateOf(finalOutputHeight.toString()) }
        var lockRatio by remember { mutableStateOf(false) }
        val ratio = if (baseHeight > 0) baseWidth.toFloat() / baseHeight.toFloat() else 1f

        AlertDialog(
            onDismissRequest = { showCustomSizeDialog = false },
            title = { Text(tr("Vlastná veľkosť (px)", "Custom Size (px)")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthInput,
                        onValueChange = { wStr ->
                            widthInput = wStr.filter { it.isDigit() }
                            val wVal = widthInput.toIntOrNull()
                            if (lockRatio && wVal != null && ratio > 0f) {
                                heightInput = (wVal / ratio).roundToInt().coerceAtLeast(1).toString()
                            }
                        },
                        label = { Text(tr("Šírka (px)", "Width (px)")) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { hStr ->
                            heightInput = hStr.filter { it.isDigit() }
                            val hVal = heightInput.toIntOrNull()
                            if (lockRatio && hVal != null && ratio > 0f) {
                                widthInput = (hVal * ratio).roundToInt().coerceAtLeast(1).toString()
                            }
                        },
                        label = { Text(tr("Výška (px)", "Height (px)")) },
                        singleLine = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { lockRatio = !lockRatio }
                    ) {
                        Checkbox(checked = lockRatio, onCheckedChange = { lockRatio = it })
                        Text(tr("Zachovať pomer strán", "Keep aspect ratio"), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val w = widthInput.toIntOrNull()?.coerceIn(64, 6000)
                        val h = heightInput.toIntOrNull()?.coerceIn(64, 6000)
                        if (w != null && h != null) {
                            customTargetWidth = w
                            customTargetHeight = h
                        }
                        showCustomSizeDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSizeDialog = false }) {
                    Text(tr("Zrušiť", "Cancel"))
                }
            }
        )
    }

    if (showStickerPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStickerPickerSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Vyber fotku ako Sticker", "Pick a Photo as Sticker"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    OutlinedButton(onClick = { systemStickerPickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Súbory", "Files"), fontSize = 12.sp)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allGalleryPhotos, key = { it.uri.toString() }) { item ->
                        AsyncImage(
                            model = item.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        val loaded = loadEditableBitmap(context, item.uri, maxDimension = 900)
                                        if (loaded != null) {
                                            val newSticker = EditorStickerOverlay(uri = item.uri, bitmap = loaded)
                                            stickerOverlays = stickerOverlays + newSticker
                                            selectedStickerId = newSticker.id
                                            selectedTextId = null
                                            showStickerPickerSheet = false
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    if (showSaveChoiceDialog && croppedPreviewBitmap != null) {
        AlertDialog(
            onDismissRequest = { showSaveChoiceDialog = false },
            title = { Text(tr("Uložiť upravenú fotku", "Save Edited Photo")) },
            text = {
                Text(
                    tr(
                        "Chceš uložiť upravenú fotku ako novú kópiu, alebo prepísať pôvodnú fotku (${finalOutputWidth} × ${finalOutputHeight} px)?",
                        "Do you want to save as a new copy, or overwrite the original photo (${finalOutputWidth} × ${finalOutputHeight} px)?"
                    )
                )
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            showSaveChoiceDialog = false
                            coroutineScope.launch {
                                isSaving = true
                                val finalBmp = renderFinalEditedBitmap(
                                    baseCroppedBitmap = croppedPreviewBitmap!!,
                                    targetWidth = finalOutputWidth,
                                    targetHeight = finalOutputHeight,
                                    colorMatrixArray = combinedMatrixArray,
                                    vignetteStrength = vignette,
                                    stickers = stickerOverlays,
                                    texts = textOverlays
                                )
                                val ok = saveBitmapAsNewCopy(context, finalBmp, photo)
                                isSaving = false
                                if (ok) {
                                    Toast.makeText(context, msgSavedCopy, Toast.LENGTH_SHORT).show()
                                    onSaved()
                                } else {
                                    Toast.makeText(context, msgSaveError, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tr("Uložiť ako kópiu", "Save as Copy"))
                    }

                    OutlinedButton(
                        onClick = {
                            showSaveChoiceDialog = false
                            coroutineScope.launch {
                                isSaving = true
                                val finalBmp = renderFinalEditedBitmap(
                                    baseCroppedBitmap = croppedPreviewBitmap!!,
                                    targetWidth = finalOutputWidth,
                                    targetHeight = finalOutputHeight,
                                    colorMatrixArray = combinedMatrixArray,
                                    vignetteStrength = vignette,
                                    stickers = stickerOverlays,
                                    texts = textOverlays
                                )
                                try {
                                    val written = withContext(Dispatchers.IO) {
                                        writeBitmapToExistingPhoto(context, photo, finalBmp)
                                    }
                                    isSaving = false
                                    if (written) {
                                        editedPhotoCache.put(photo.uri.toString(), finalBmp)
                                        editedPhotoCacheVersion++
                                        Toast.makeText(context, msgSavedOverwrite, Toast.LENGTH_SHORT).show()
                                        onSaved()
                                    } else {
                                        Toast.makeText(context, msgSaveError, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (secEx: SecurityException) {
                                    pendingOverwriteBitmap = finalBmp
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        val writeIntent = MediaStore.createWriteRequest(
                                            context.contentResolver,
                                            listOf(photo.uri)
                                        ).intentSender
                                        overwritePermissionLauncher.launch(
                                            IntentSenderRequest.Builder(writeIntent).build()
                                        )
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && secEx is RecoverableSecurityException) {
                                        overwritePermissionLauncher.launch(
                                            IntentSenderRequest.Builder(secEx.userAction.actionIntent.intentSender).build()
                                        )
                                    } else {
                                        val savedCopyFallback = saveBitmapAsNewCopy(context, finalBmp, photo)
                                        isSaving = false
                                        pendingOverwriteBitmap = null
                                        if (savedCopyFallback) {
                                            Toast.makeText(context, msgSavedCopy, Toast.LENGTH_SHORT).show()
                                            onSaved()
                                        } else {
                                            Toast.makeText(context, msgSaveError, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (_: Exception) {
                                    isSaving = false
                                    Toast.makeText(context, msgSaveError, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tr("Prepísať originál", "Overwrite Original"), color = MaterialTheme.colorScheme.error)
                    }

                    TextButton(
                        onClick = { showSaveChoiceDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(tr("Zrušiť", "Cancel"))
                    }
                }
            }
        )
    }
}

// ----------------------------------------------------
// OBRAZOVKA FOTIEK S VÝBEROM A PODPOROU HLAVIČIEK
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotosScreenWithFilters(
    allPhotos: List<MediaPhoto>,
    showHeaders: Boolean,
    confirmDelete: Boolean,
    onRefresh: () -> Unit,
    onMoveToLocalTrash: (Set<String>) -> Unit = {},
    isInsideTrashAlbum: Boolean = false,
    columnsCount: Int = 5,
    onColumnsCountChanged: ((Int) -> Unit)? = null,
    groupingMode: String = "DAYS",
    onPhotoClick: (MediaPhoto, List<MediaPhoto>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(true) }

    var internalColumnsCount by rememberSaveable { mutableIntStateOf(5) }
    val activeColumns = if (onColumnsCountChanged != null) columnsCount else internalColumnsCount
    val setColumns: (Int) -> Unit = { newCols ->
        if (onColumnsCountChanged != null) {
            onColumnsCountChanged(newCols)
        } else {
            internalColumnsCount = newCols
        }
    }

    var filterMenuExpanded by remember { mutableStateOf(false) }
    var mediaTypeFilter by remember { mutableStateOf("Všetky") }

    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedUris by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentLang = LocalLang.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val adaptiveBottomPadding = rememberAdaptiveBottomBarPadding()

    val showScrollToBottomButton by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 200
        }
    }

    // Bočný Fast Scrollbar (neviditeľný a neaktívny, kým sa neskroluje)
    var isScrollbarVisible by remember { mutableStateOf(false) }
    var isDraggingScrollbar by remember { mutableStateOf(false) }
    var scrollbarTrackHeightPx by remember { mutableFloatStateOf(1f) }
    var dragThumbNormY by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(gridState.isScrollInProgress, isDraggingScrollbar) {
        if (gridState.isScrollInProgress || isDraggingScrollbar) {
            isScrollbarVisible = true
        } else if (isScrollbarVisible) {
            delay(1500)
            if (!gridState.isScrollInProgress && !isDraggingScrollbar) {
                isScrollbarVisible = false
            }
        }
    }

    val msgItemsDeletedLauncher = tr("Položky boli presunuté do koša", "Items moved to trash")
    val msgShareSelected = tr("Zdieľať vybraté položky", "Share selected items")
    val msgItemsDeleted = tr("Položky presunuté do koša", "Items moved to trash")
    val msgDeleteError = tr("Chyba pri mazaní", "Error deleting items")

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, msgItemsDeletedLauncher, Toast.LENGTH_SHORT).show()
            isSelectionMode = false
            selectedUris = emptySet()
            onRefresh()
        }
    }

    fun performBatchDeleteOrTrash() {
        val urisToDelete = selectedUris.map { Uri.parse(it) }
        if (isInsideTrashAlbum) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intentSender = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        urisToDelete
                    ).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    val pathMap = allPhotos.associateBy({ it.uri.toString() }, { it.path })
                    urisToDelete.forEach { uri ->
                        val rows = context.contentResolver.delete(uri, null, null)
                        if (rows == 0) {
                            pathMap[uri.toString()]?.takeIf { it.isNotEmpty() }?.let { File(it).delete() }
                        }
                    }
                    Toast.makeText(context, msgItemsDeleted, Toast.LENGTH_SHORT).show()
                    isSelectionMode = false
                    selectedUris = emptySet()
                    onRefresh()
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverable = e as? RecoverableSecurityException
                    recoverable?.let {
                        deleteLauncher.launch(
                            IntentSenderRequest.Builder(it.userAction.actionIntent.intentSender).build()
                        )
                    } ?: Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intentSender = MediaStore.createTrashRequest(
                        context.contentResolver,
                        urisToDelete,
                        true
                    ).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    onMoveToLocalTrash(selectedUris)
                    Toast.makeText(context, msgItemsDeleted, Toast.LENGTH_SHORT).show()
                    isSelectionMode = false
                    selectedUris = emptySet()
                }
            } catch (_: Exception) {
                onMoveToLocalTrash(selectedUris)
                Toast.makeText(context, msgItemsDeleted, Toast.LENGTH_SHORT).show()
                isSelectionMode = false
                selectedUris = emptySet()
            }
        }
    }

    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedUris = emptySet()
    }

    var currentScale by remember { mutableFloatStateOf(1f) }

    val filteredPhotos = remember(allPhotos, searchQuery, mediaTypeFilter) {
        allPhotos.filter { photo ->
            val matchesSearch = photo.displayName.contains(searchQuery, ignoreCase = true)
            val matchesType = when (mediaTypeFilter) {
                "Video" -> photo.mimeType.startsWith("video/")
                "GIF" -> photo.mimeType == "image/gif"
                "Selfie / Portrait" -> photo.displayName.contains("selfie", true) || photo.displayName.contains("portrait", true)
                "Motion / Slow-mo" -> photo.displayName.contains("slow", true) || photo.displayName.contains("motion", true)
                "Pro Photo" -> photo.displayName.contains("pro", true) || photo.displayName.contains("raw", true)
                else -> true
            }
            matchesSearch && matchesType
        }
    }

    val effectiveGrouping = if (onColumnsCountChanged != null) {
        groupingMode
    } else {
        when (activeColumns) {
            28 -> "YEARS"
            14, 7 -> "MONTHS"
            else -> "DAYS"
        }
    }

    val galleryItems = remember(filteredPhotos, activeColumns, currentLang, showHeaders, effectiveGrouping) {
        if (!showHeaders || effectiveGrouping == "ALL") {
            filteredPhotos.map { GalleryItem.Photo(it) }
        } else {
            val locale = if (currentLang == "en") Locale.ENGLISH else Locale("sk", "SK")
            val pattern = when (effectiveGrouping) {
                "YEARS" -> "yyyy"
                "MONTHS" -> "MMMM yyyy"
                else -> "dd. MMMM yyyy"
            }
            val formatter = SimpleDateFormat(pattern, locale)
            val grouped = filteredPhotos.groupBy { formatter.format(Date(it.dateAdded * 1000)) }

            val itemsList = mutableListOf<GalleryItem>()
            for ((dateString, photos) in grouped) {
                photos.forEach { itemsList.add(GalleryItem.Photo(it)) }
                itemsList.add(GalleryItem.Header(dateString))
            }
            itemsList
        }
    }

    val scrollProgressFromBottom by remember(galleryItems) {
        derivedStateOf {
            if (galleryItems.isEmpty()) 0f
            else {
                val visibleCount = gridState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                val maxStartIndex = (galleryItems.size - visibleCount).coerceAtLeast(1)
                (gridState.firstVisibleItemIndex.toFloat() / maxStartIndex.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    val currentThumbNormY = if (isDraggingScrollbar) {
        dragThumbNormY.coerceIn(0f, 1f)
    } else {
        (1f - scrollProgressFromBottom).coerceIn(0f, 1f)
    }

    val currentScrollbarDateLabel by remember(galleryItems, effectiveGrouping, currentLang) {
        derivedStateOf {
            if (galleryItems.isEmpty()) ""
            else {
                val visibleCount = gridState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                val maxStartIndex = (galleryItems.size - visibleCount).coerceAtLeast(1)
                val targetIndex = if (isDraggingScrollbar) {
                    ((1f - dragThumbNormY) * maxStartIndex).roundToInt().coerceIn(0, galleryItems.lastIndex)
                } else {
                    gridState.firstVisibleItemIndex.coerceIn(0, galleryItems.lastIndex)
                }

                var mediaPhoto: MediaPhoto? = null
                val directItem = galleryItems.getOrNull(targetIndex)
                if (directItem is GalleryItem.Photo) {
                    mediaPhoto = directItem.media
                } else {
                    for (step in 1..6) {
                        val prev = galleryItems.getOrNull(targetIndex - step)
                        if (prev is GalleryItem.Photo) {
                            mediaPhoto = prev.media
                            break
                        }
                        val next = galleryItems.getOrNull(targetIndex + step)
                        if (next is GalleryItem.Photo) {
                            mediaPhoto = next.media
                            break
                        }
                    }
                }

                if (mediaPhoto != null) {
                    val locale = if (currentLang == "en") Locale.ENGLISH else Locale("sk", "SK")
                    val pattern = when (effectiveGrouping) {
                        "YEARS" -> "yyyy"
                        "MONTHS" -> "MMMM yyyy"
                        else -> "dd. MMMM yyyy"
                    }
                    SimpleDateFormat(pattern, locale).format(Date(mediaPhoto.dateAdded * 1000L))
                } else if (directItem is GalleryItem.Header) {
                    directItem.title
                } else {
                    ""
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSelectionMode) {
            TopAppBar(
                title = { Text("${selectedUris.size} ${tr("vybratých", "selected")}") },
                navigationIcon = {
                    IconButton(onClick = { isSelectionMode = false; selectedUris = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Zrušiť", "Cancel"))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "*/*"
                            val urisToShare = ArrayList(selectedUris.map { Uri.parse(it) })
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, urisToShare)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, msgShareSelected))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = tr("Zdieľať", "Share"))
                    }
                    IconButton(onClick = {
                        if (confirmDelete) showMultiDeleteDialog = true
                        else performBatchDeleteOrTrash()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Zmazať", "Delete"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.statusBarsPadding()
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .animateContentSize()
                        .weight(1f, fill = false)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            Icon(Icons.Default.Search, contentDescription = tr("Hľadať", "Search"))
                        }
                        if (isSearchExpanded) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(tr("Hľadať podľa mena...", "Search by name..."), fontSize = 14.sp)
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = tr("Filtre", "Filters"))
                        }
                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            Text(
                                tr("Typ média", "Media Type"),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                            val types = listOf(
                                "Všetky" to "All",
                                "Video" to "Video",
                                "GIF" to "GIF",
                                "Selfie / Portrait" to "Selfie / Portrait",
                                "Motion / Slow-mo" to "Motion / Slow-mo",
                                "Pro Photo" to "Pro Photo"
                            )
                            types.forEach { (skType, enType) ->
                                val displayType = tr(skType, enType)
                                val queryType = if (currentLang == "en" && skType == "Všetky") "Všetky" else skType

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayType,
                                            color = if (mediaTypeFilter == queryType || (queryType == "Všetky" && mediaTypeFilter == "All"))
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = { mediaTypeFilter = queryType; filterMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        val currentColsState by rememberUpdatedState(activeColumns)
        val setColsState by rememberUpdatedState(setColumns)

        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(activeColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (!isSelectionMode) {
                                        val pressedCount = event.changes.count { it.pressed }
                                        if (pressedCount >= 2) {
                                            val zoomChange = event.calculateZoom()
                                            currentScale *= zoomChange
                                            if (currentScale < 0.8f) {
                                                when (currentColsState) {
                                                    3 -> { setColsState(5); currentScale = 1f }
                                                    5 -> { setColsState(7); currentScale = 1f }
                                                    7 -> { setColsState(14); currentScale = 1f }
                                                    14 -> { setColsState(28); currentScale = 1f }
                                                }
                                            } else if (currentScale > 1.2f) {
                                                when (currentColsState) {
                                                    28 -> { setColsState(14); currentScale = 1f }
                                                    14 -> { setColsState(7); currentScale = 1f }
                                                    7 -> { setColsState(5); currentScale = 1f }
                                                    5 -> { setColsState(3); currentScale = 1f }
                                                }
                                            }
                                            event.changes.forEach { it.consume() }
                                        } else if (pressedCount == 0) {
                                            currentScale = 1f
                                        }
                                    }
                                }
                            }
                        },
                    contentPadding = PaddingValues(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    reverseLayout = true
                ) {
                    items(
                        items = galleryItems,
                        key = { item ->
                            when (item) {
                                is GalleryItem.Header -> "header_${item.title}"
                                is GalleryItem.Photo -> "photo_${item.media.uri}"
                            }
                        },
                        span = { item ->
                            when (item) {
                                is GalleryItem.Header -> GridItemSpan(maxLineSpan)
                                is GalleryItem.Photo -> GridItemSpan(1)
                            }
                        }
                    ) { item ->
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            when (item) {
                                is GalleryItem.Header -> {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (activeColumns <= 5) 18.sp else 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 12.dp)
                                    )
                                }
                                is GalleryItem.Photo -> {
                                    val uriStr = item.media.uri.toString()
                                    val isSelected = isSelectionMode && selectedUris.contains(uriStr)

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(if (isSelected) 12.dp else 0.dp))
                                            .padding(if (isSelected) 8.dp else 0.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedUris.contains(uriStr)) {
                                                            selectedUris = selectedUris - uriStr
                                                        } else {
                                                            selectedUris = selectedUris + uriStr
                                                        }
                                                        if (selectedUris.isEmpty()) isSelectionMode = false
                                                    } else {
                                                        onPhotoClick(item.media, filteredPhotos)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        isSelectionMode = true
                                                        selectedUris = setOf(uriStr)
                                                    }
                                                }
                                            )
                                    ) {
                                        SmartMediaImage(
                                            uri = item.media.uri,
                                            mimeType = item.media.mimeType,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (item.media.mimeType.startsWith("video/")) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Video",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(32.dp)
                                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                                            )
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.3f))
                                            )
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Vybraté",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .size(24.dp)
                                                    .background(Color.White, RoundedCornerShape(50))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bočný Fast Scrollbar s dátumovou bublinou (zobrazí sa a reaguje až po posunutí fotiek)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 12.dp, bottom = adaptiveBottomPadding + 124.dp, end = 4.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isScrollbarVisible && galleryItems.size > activeColumns,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val thumbHeightDp = 42.dp
                    val thumbHeightPx = with(density) { thumbHeightDp.toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(240.dp)
                            .onSizeChanged { size ->
                                scrollbarTrackHeightPx = (size.height.toFloat() - thumbHeightPx).coerceAtLeast(1f)
                            }
                    ) {
                        val thumbOffsetYPx = (currentThumbNormY * scrollbarTrackHeightPx).roundToInt()

                        // Jemná vodiaca čiara na kraji
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp)
                                .width(3.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        )

                        // Dátumová bublina a posuvník (thumb)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset { androidx.compose.ui.unit.IntOffset(0, thumbOffsetYPx) }
                        ) {
                            if (currentScrollbarDateLabel.isNotEmpty() && (isDraggingScrollbar || gridState.isScrollInProgress)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = currentScrollbarDateLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDraggingScrollbar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(thumbHeightDp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = if (isDraggingScrollbar) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (isDraggingScrollbar) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Dotyková zóna pre plynulé ťahanie posuvníka (aktívna iba keď je scrollbar viditeľný)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .fillMaxHeight()
                                .width(44.dp)
                                .pointerInput(galleryItems.size, activeColumns) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            isDraggingScrollbar = true
                                            val trackH = scrollbarTrackHeightPx.coerceAtLeast(1f)
                                            val newNorm = ((startOffset.y - thumbHeightPx / 2f) / trackH).coerceIn(0f, 1f)
                                            dragThumbNormY = newNorm
                                            val visibleCount = gridState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                                            val maxStartIndex = (galleryItems.size - visibleCount).coerceAtLeast(1)
                                            val targetIdx = ((1f - newNorm) * maxStartIndex).roundToInt()
                                                .coerceIn(0, galleryItems.lastIndex.coerceAtLeast(0))
                                            coroutineScope.launch {
                                                gridState.scrollToItem(targetIdx)
                                            }
                                        },
                                        onDragEnd = {
                                            isDraggingScrollbar = false
                                        },
                                        onDragCancel = {
                                            isDraggingScrollbar = false
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val trackH = scrollbarTrackHeightPx.coerceAtLeast(1f)
                                            val newNorm = (dragThumbNormY + dragAmount.y / trackH).coerceIn(0f, 1f)
                                            dragThumbNormY = newNorm
                                            val visibleCount = gridState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                                            val maxStartIndex = (galleryItems.size - visibleCount).coerceAtLeast(1)
                                            val targetIdx = ((1f - newNorm) * maxStartIndex).roundToInt()
                                                .coerceIn(0, galleryItems.lastIndex.coerceAtLeast(0))
                                            coroutineScope.launch {
                                                gridState.scrollToItem(targetIdx)
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            // Tlačidlo na rýchle vrátenie sa na spodok
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = adaptiveBottomPadding + 68.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showScrollToBottomButton,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(0)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = tr("Na spodok", "Scroll to bottom"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMultiDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteDialog = false },
            title = {
                Text(
                    if (isInsideTrashAlbum) tr("Vymazať natrvalo?", "Delete permanently?")
                    else tr("Presunúť do koša?", "Move to trash?")
                )
            },
            text = {
                Text(
                    if (isInsideTrashAlbum) tr("Vybraté položky budú natrvalo vymazané.", "Selected items will be permanently deleted.")
                    else tr("Položky budú uchované v koši 30 dní.", "Items will be kept in trash for 30 days.")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMultiDeleteDialog = false
                        performBatchDeleteOrTrash()
                    }
                ) {
                    Text(tr("Zmazať", "Delete"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMultiDeleteDialog = false }) { Text(tr("Zrušiť", "Cancel")) }
            }
        )
    }
}

// ----------------------------------------------------
// OBRAZOVKA ALBUMOV (11 ALBUMOV + MORE, TYPY MÉDIÍ BEZ IKON, KÔŠ A SKRYTÝ ALBUM S BIOMETRIOU/PINOM)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    allPhotos: List<MediaPhoto>,
    activePhotos: List<MediaPhoto> = allPhotos.filter { !it.isTrashed },
    hiddenPhotos: List<MediaPhoto> = emptyList(),
    favoritePhotos: List<MediaPhoto> = emptyList(),
    showHeaders: Boolean,
    confirmDelete: Boolean,
    onRefresh: () -> Unit,
    onMoveToLocalTrash: (Set<String>) -> Unit = {},
    onRestoreFromLocalTrash: (Set<String>) -> Unit = {},
    onHidePhotos: (Set<String>) -> Unit = {},
    onUnhidePhotos: (Set<String>) -> Unit = {},
    onFullScreenMoreChanged: (Boolean) -> Unit = {},
    onPhotoClick: (MediaPhoto, List<MediaPhoto>) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }

    val msgAllUnhidden = tr("Všetky fotky boli odkryté", "All photos unhidden")
    val msgMovedToHidden = tr("Fotka presunutá do skrytého albumu", "Photo moved to Hidden Album")
    val msgMax11Albums = tr("Môžeš vybrať maximálne 11 albumov", "You can select up to 11 albums")
    val msgInvalidPin = tr("Nesprávny PIN (min. 4 číslice)", "Invalid PIN (min. 4 digits)")
    val msgMapInfo = tr("Zobrazenie podľa miest", "Location view")
    val msgTrashRestored = tr("Položky z koša boli obnovené", "Items restored from trash")
    val msgTrashEmptied = tr("Kôš bol vyprázdnený", "Trash emptied")
    val msgDeleteError = tr("Chyba pri mazaní", "Error deleting items")
    val authTitle = tr("Skrytý album", "Hidden Album")
    val authDesc = tr(
        "Potvrď totožnosť pomocou PINu alebo biometrie telefónu",
        "Confirm identity using your device PIN or biometrics"
    )

    var selectedAlbum by rememberSaveable { mutableStateOf<String?>(null) }
    var showAllAlbumsFullScreen by rememberSaveable { mutableStateOf(false) }

    val trashActionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    LaunchedEffect(showAllAlbumsFullScreen) {
        onFullScreenMoreChanged(showAllAlbumsFullScreen)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenMoreChanged(false) }
    }

    val trashedPhotos = remember(allPhotos) { allPhotos.filter { it.isTrashed } }
    val albums = remember(activePhotos) { activePhotos.groupBy { it.albumName } }
    val allAlbumKeys = remember(albums) { albums.keys.toList() }

    var pinnedAlbums by remember {
        val savedStr = prefs.getString("pinned_11_albums", "") ?: ""
        val list = if (savedStr.isBlank()) emptyList() else savedStr.split("|||").filter { it.isNotBlank() }
        mutableStateOf(list)
    }

    val displayed11Albums = remember(allAlbumKeys, pinnedAlbums) {
        val validPinned = pinnedAlbums.filter { allAlbumKeys.contains(it) }
        if (validPinned.isNotEmpty()) {
            validPinned.take(11)
        } else {
            allAlbumKeys.take(11)
        }
    }

    var topMenuExpanded by remember { mutableStateOf(false) }
    var showSelect11AlbumsDialog by remember { mutableStateOf(false) }

    var mediaTypesExpanded by rememberSaveable { mutableStateOf(true) }
    var utilitiesExpanded by rememberSaveable { mutableStateOf(true) }

    var showFallbackPinDialog by remember { mutableStateOf(false) }
    var showAddHiddenPhotosSheet by remember { mutableStateOf(false) }

    val deviceCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedAlbum = "HIDDEN_ALBUM"
        }
    }

    fun requestHiddenAlbumAccess() {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager != null && keyguardManager.isDeviceSecure) {
            @Suppress("DEPRECATION")
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(authTitle, authDesc)
            if (intent != null) {
                deviceCredentialLauncher.launch(intent)
            } else {
                showFallbackPinDialog = true
            }
        } else {
            showFallbackPinDialog = true
        }
    }

    // 1. OTVORENÝ KONKRÉTNY ALBUM ALEBO ŠPECIÁLNA KATEGÓRIA
    if (selectedAlbum != null) {
        BackHandler { selectedAlbum = null }

        val albumTitle = when (selectedAlbum) {
            "TRASH_ALBUM" -> tr("Kôš", "Recently Deleted")
            "HIDDEN_ALBUM" -> tr("Skrytý album", "Hidden")
            "MEDIA_VIDEOS" -> tr("Videá", "Videos")
            "MEDIA_PANORAMAS" -> tr("Panorámy", "Panoramas")
            "MEDIA_DOCUMENTS" -> tr("Dokumenty", "Documents")
            "MEDIA_ANIMATED" -> tr("Animované", "Animated")
            "MEDIA_SCREENSHOTS" -> tr("Snímky obrazovky", "Screenshots")
            "MEDIA_STICKERS" -> tr("Moje nálepky", "My Stickers")
            "UTIL_LARGE_FILES" -> tr("Veľké súbory", "Large Files")
            "UTIL_DUPLICATES" -> tr("Duplikáty", "Duplicates")
            "UTIL_FAVORITES" -> tr("Obľúbené", "Favorites")
            "UTIL_MAP" -> tr("Mapa", "Map")
            else -> if (selectedAlbum == "Neznáme") tr("Neznáme", "Unknown") else selectedAlbum!!
        }

        val displayPhotos = remember(selectedAlbum, activePhotos, trashedPhotos, hiddenPhotos, favoritePhotos, albums) {
            when (selectedAlbum) {
                "TRASH_ALBUM" -> trashedPhotos
                "HIDDEN_ALBUM" -> hiddenPhotos
                "UTIL_FAVORITES" -> favoritePhotos
                "UTIL_MAP" -> activePhotos.filter { it.albumName.contains("Camera", true) || it.displayName.startsWith("IMG", true) }
                "MEDIA_VIDEOS" -> activePhotos.filter { it.mimeType.startsWith("video/") }
                "MEDIA_PANORAMAS" -> activePhotos.filter {
                    it.displayName.contains("pano", true) || it.albumName.contains("pano", true)
                }
                "MEDIA_DOCUMENTS" -> activePhotos.filter {
                    it.displayName.contains("doc", true) ||
                            it.displayName.contains("scan", true) ||
                            it.displayName.contains("pdf", true) ||
                            it.albumName.contains("doc", true) ||
                            it.albumName.contains("notes", true)
                }
                "MEDIA_ANIMATED" -> activePhotos.filter {
                    it.mimeType.contains("gif", true) || it.displayName.endsWith(".gif", true)
                }
                "MEDIA_SCREENSHOTS" -> activePhotos.filter {
                    it.displayName.contains("screenshot", true) ||
                            it.albumName.contains("screenshot", true) ||
                            it.albumName.contains("snímky", true)
                }
                "MEDIA_STICKERS" -> activePhotos.filter {
                    it.displayName.contains("sticker", true) ||
                            it.albumName.contains("sticker", true) ||
                            it.mimeType.contains("webp", true) ||
                            it.mimeType.contains("png", true)
                }
                "UTIL_LARGE_FILES" -> activePhotos.filter {
                    it.mimeType.startsWith("video/") || (it.path.isNotEmpty() && File(it.path).length() > 8 * 1024 * 1024)
                }
                "UTIL_DUPLICATES" -> {
                    val groupedByName = activePhotos.groupBy { it.displayName.lowercase(Locale.getDefault()) }
                    groupedByName.values.filter { it.size > 1 }.flatten()
                }
                else -> albums[selectedAlbum] ?: emptyList()
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(albumTitle) },
                navigationIcon = {
                    IconButton(onClick = { selectedAlbum = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = tr("Späť", "Back"))
                    }
                },
                actions = {
                    if (selectedAlbum == "TRASH_ALBUM" && trashedPhotos.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                val allTrashedUriSet = trashedPhotos.map { it.uri.toString() }.toSet()
                                onRestoreFromLocalTrash(allTrashedUriSet)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    try {
                                        val sysTrashed = trashedPhotos.map { it.uri }
                                        val intentSender = MediaStore.createTrashRequest(
                                            context.contentResolver,
                                            sysTrashed,
                                            false
                                        ).intentSender
                                        trashActionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                    } catch (_: Exception) {
                                    }
                                } else {
                                    Toast.makeText(context, msgTrashRestored, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(tr("Obnoviť", "Restore"))
                        }
                        TextButton(
                            onClick = {
                                val allTrashedUriSet = trashedPhotos.map { it.uri.toString() }.toSet()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    try {
                                        onRestoreFromLocalTrash(allTrashedUriSet)
                                        val intentSender = MediaStore.createDeleteRequest(
                                            context.contentResolver,
                                            trashedPhotos.map { it.uri }
                                        ).intentSender
                                        trashActionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                    } catch (_: Exception) {
                                        Toast.makeText(context, msgDeleteError, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    trashedPhotos.forEach { item ->
                                        val rows = try {
                                            context.contentResolver.delete(item.uri, null, null)
                                        } catch (_: Exception) {
                                            0
                                        }
                                        if (rows == 0 && item.path.isNotEmpty()) {
                                            try {
                                                File(item.path).delete()
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                    onRestoreFromLocalTrash(allTrashedUriSet)
                                    Toast.makeText(context, msgTrashEmptied, Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                }
                            }
                        ) {
                            Text(tr("Vyprázdniť", "Empty"), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    if (selectedAlbum == "HIDDEN_ALBUM") {
                        TextButton(onClick = { showAddHiddenPhotosSheet = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Pridať", "Add"))
                        }
                        if (hiddenPhotos.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    onUnhidePhotos(hiddenPhotos.map { it.uri.toString() }.toSet())
                                    Toast.makeText(context, msgAllUnhidden, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(tr("Odkryť všetko", "Unhide All"))
                            }
                        }
                    }
                }
            )
            PhotosScreenWithFilters(
                allPhotos = displayPhotos,
                showHeaders = showHeaders,
                confirmDelete = confirmDelete,
                onRefresh = onRefresh,
                onMoveToLocalTrash = onMoveToLocalTrash,
                isInsideTrashAlbum = selectedAlbum == "TRASH_ALBUM",
                onPhotoClick = onPhotoClick
            )
        }

        if (showAddHiddenPhotosSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddHiddenPhotosSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = tr("Vyber fotky na skrytie do Skrytého albumu", "Tap photos to hide into Hidden Album"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(activePhotos, key = { it.uri.toString() }) { item ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onHidePhotos(setOf(item.uri.toString()))
                                        Toast.makeText(context, msgMovedToHidden, Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                SmartMediaImage(
                                    uri = item.uri,
                                    mimeType = item.mimeType,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // 2. CELOOBRAZOVKOVÉ MENU "VIAC" (VŠETKY ALBUMY - HORE IBA SPÄŤ A SEARCH BAR)
    if (showAllAlbumsFullScreen) {
        BackHandler { showAllAlbumsFullScreen = false }

        var allAlbumsSearchQuery by remember { mutableStateOf("") }
        val filteredAllAlbums = remember(allAlbumKeys, allAlbumsSearchQuery) {
            allAlbumKeys.filter { it.contains(allAlbumsSearchQuery, ignoreCase = true) }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        IconButton(onClick = { showAllAlbumsFullScreen = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = tr("Späť", "Back"))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = tr("Hľadať", "Search"),
                                modifier = Modifier.padding(start = 14.dp)
                            )
                            TextField(
                                value = allAlbumsSearchQuery,
                                onValueChange = { allAlbumsSearchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(tr("Hľadať vo všetkých albumoch...", "Search all albums..."), fontSize = 14.sp)
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAllAlbums, key = { "all_$it" }) { albumName ->
                        val coverMedia = albums[albumName]?.firstOrNull()
                        val photoCount = albums[albumName]?.size ?: 0

                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { selectedAlbum = albumName }
                        ) {
                            if (coverMedia != null) {
                                SmartMediaImage(
                                    uri = coverMedia.uri,
                                    mimeType = coverMedia.mimeType,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (albumName == "Neznáme") tr("Neznáme", "Unknown") else albumName,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$photoCount ${tr("fotiek", "photos")}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // 3. HLAVNÁ OBRAZOVKA ALBUMOV
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(true) }

    val filteredPinnedAlbums = remember(displayed11Albums, allAlbumKeys, searchQuery) {
        if (searchQuery.isBlank()) {
            displayed11Albums
        } else {
            allAlbumKeys.filter { it.contains(searchQuery, ignoreCase = true) }.take(11)
        }
    }

    val mediaTypeRows = listOf(
        ("MEDIA_VIDEOS" to tr("Videá", "Videos")) to ("MEDIA_PANORAMAS" to tr("Panorámy", "Panoramas")),
        ("MEDIA_DOCUMENTS" to tr("Dokumenty", "Documents")) to ("MEDIA_ANIMATED" to tr("Animované", "Animated")),
        ("MEDIA_SCREENSHOTS" to tr("Snímky obrazovky", "Screenshots")) to ("MEDIA_STICKERS" to tr("Moje nálepky", "My Stickers"))
    )

    val labelHidden = tr("Skrytý album", "Hidden")
    val labelMap = tr("Mapa", "Map")
    val labelTrash = "${tr("Kôš", "Recently Deleted")} (${trashedPhotos.size})"
    val labelLarge = tr("Veľké súbory", "Large Files")
    val labelDuplicates = tr("Duplikáty", "Duplicates")
    val labelFavorites = "${tr("Obľúbené", "Favorites")} (${favoritePhotos.size})"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .animateContentSize()
                    .weight(1f, fill = false)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = tr("Hľadať", "Search"))
                    }
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            placeholder = { Text(tr("Hľadať album...", "Search album..."), fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box {
                    IconButton(onClick = { topMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = tr("Možnosti albumov", "Album options"))
                    }
                    DropdownMenu(
                        expanded = topMenuExpanded,
                        onDismissRequest = { topMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(tr("Vybrať 11 hlavných albumov", "Select 11 main albums")) },
                            onClick = {
                                topMenuExpanded = false
                                showSelect11AlbumsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(tr("Zobraziť všetky albumy", "Show all albums")) },
                            onClick = {
                                topMenuExpanded = false
                                showAllAlbumsFullScreen = true
                            }
                        )
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPinnedAlbums, key = { "pinned_$it" }) { albumName ->
                val coverMedia = albums[albumName]?.firstOrNull()
                val photoCount = albums[albumName]?.size ?: 0

                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { selectedAlbum = albumName }
                ) {
                    if (coverMedia != null) {
                        SmartMediaImage(
                            uri = coverMedia.uri,
                            mimeType = coverMedia.mimeType,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (albumName == "Neznáme") tr("Neznáme", "Unknown") else albumName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$photoCount ${tr("fotiek", "photos")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 12. dlaždica: "Viac" (More)
            item(key = "more_albums_tile") {
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { showAllAlbumsFullScreen = true }
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("Viac", "More"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("Viac", "More"),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${allAlbumKeys.size} ${tr("albumov", "albums")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // SEKCIA: TYPY MÉDIÍ (BEZ IKONIEK)
            item(span = { GridItemSpan(maxLineSpan) }, key = "media_types_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 4.dp, end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mediaTypesExpanded = !mediaTypesExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr("Typy médií", "Media Types"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (mediaTypesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = mediaTypesExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            mediaTypeRows.forEach { (leftItem, rightItem) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TextOnlyCategoryTile(
                                        title = leftItem.second,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedAlbum = leftItem.first }
                                    )
                                    TextOnlyCategoryTile(
                                        title = rightItem.second,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedAlbum = rightItem.first }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SEKCIA: NÁSTROJE (SKRYTÝ ALBUM NA PIN/BIOMETRIU, KÔŠ, VEĽKÉ SÚBORY, DUPLIKÁTY, OBĽÚBENÉ)
            item(span = { GridItemSpan(maxLineSpan) }, key = "utilities_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, start = 4.dp, end = 4.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { utilitiesExpanded = !utilitiesExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr("Nástroje", "Utilities"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (utilitiesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = utilitiesExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextOnlyCategoryTile(
                                    title = labelHidden,
                                    modifier = Modifier.weight(1f),
                                    onClick = { requestHiddenAlbumAccess() }
                                )
                                TextOnlyCategoryTile(
                                    title = labelMap,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        Toast.makeText(context, msgMapInfo, Toast.LENGTH_SHORT).show()
                                        selectedAlbum = "UTIL_MAP"
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextOnlyCategoryTile(
                                    title = labelTrash,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedAlbum = "TRASH_ALBUM" }
                                )
                                TextOnlyCategoryTile(
                                    title = labelLarge,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedAlbum = "UTIL_LARGE_FILES" }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextOnlyCategoryTile(
                                    title = labelDuplicates,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedAlbum = "UTIL_DUPLICATES" }
                                )
                                TextOnlyCategoryTile(
                                    title = labelFavorites,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedAlbum = "UTIL_FAVORITES" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALÓG NA VÝBER 11 HLAVNÝCH ALBUMOV
    if (showSelect11AlbumsDialog) {
        var tempSelection by remember(displayed11Albums) { mutableStateOf(displayed11Albums) }

        AlertDialog(
            onDismissRequest = { showSelect11AlbumsDialog = false },
            title = {
                Text("${tr("Vyber hlavné albumy", "Select Main Albums")} (${tempSelection.size}/11)")
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(allAlbumKeys) { albumName ->
                        val isChecked = tempSelection.contains(albumName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        tempSelection = tempSelection - albumName
                                    } else if (tempSelection.size < 11) {
                                        tempSelection = tempSelection + albumName
                                    } else {
                                        Toast.makeText(context, msgMax11Albums, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (tempSelection.size < 11) {
                                            tempSelection = tempSelection + albumName
                                        } else {
                                            Toast.makeText(context, msgMax11Albums, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        tempSelection = tempSelection - albumName
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (albumName == "Neznáme") tr("Neznáme", "Unknown") else albumName,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pinnedAlbums = tempSelection
                        prefs.edit().putString("pinned_11_albums", tempSelection.joinToString("|||")).apply()
                        showSelect11AlbumsDialog = false
                    }
                ) {
                    Text(tr("Uložiť", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSelect11AlbumsDialog = false }) {
                    Text(tr("Zrušiť", "Cancel"))
                }
            }
        )
    }

    // ZÁLOŽNÝ PIN DIALÓG (Ak na mobile nie je aktívny systémový zámok obrazovky)
    if (showFallbackPinDialog) {
        val savedPin = remember { prefs.getString("fallback_hidden_pin", "") ?: "" }
        var pinInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFallbackPinDialog = false },
            title = {
                Text(
                    if (savedPin.isEmpty()) tr("Nastav PIN pre Skrytý album", "Set PIN for Hidden Album")
                    else tr("Zadaj PIN pre Skrytý album", "Enter PIN for Hidden Album")
                )
            },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("PIN") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (savedPin.isEmpty() && pinInput.length >= 4) {
                            prefs.edit().putString("fallback_hidden_pin", pinInput).apply()
                            showFallbackPinDialog = false
                            selectedAlbum = "HIDDEN_ALBUM"
                        } else if (savedPin.isNotEmpty() && pinInput == savedPin) {
                            showFallbackPinDialog = false
                            selectedAlbum = "HIDDEN_ALBUM"
                        } else {
                            Toast.makeText(context, msgInvalidPin, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFallbackPinDialog = false }) {
                    Text(tr("Zrušiť", "Cancel"))
                }
            }
        )
    }
}

// Karta pre kategórie bez ikoniek
@Composable
fun TextOnlyCategoryTile(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ----------------------------------------------------
// NAČÍTANIE Z PAMÄTE (S PODPOROU PRE SYSTÉMOVÝ KOŠ, DÁTUMY, VEĽKOSŤ A DATA CESTU)
// ----------------------------------------------------
suspend fun loadPhotosFromStorage(context: Context): List<MediaPhoto> = withContext(Dispatchers.IO) {
    val photos = mutableListOf<MediaPhoto>()
    val queryUri = MediaStore.Files.getContentUri("external")

    val projectionList = mutableListOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.SIZE
    )

    val hasTrashColumn = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    if (hasTrashColumn) {
        projectionList.add(MediaStore.Files.FileColumns.IS_TRASHED)
    }

    val projection = projectionList.toTypedArray()
    val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
    val selectionArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

    val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bundle = Bundle().apply {
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
            putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putString(android.content.ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
        }
        context.contentResolver.query(queryUri, projection, bundle, null)
    } else {
        context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)
    }

    cursor?.use { c ->
        val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val albumCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
        val dateAddedCol = c.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
        val dateModCol = c.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
        val nameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val mimeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        val dataCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val sizeCol = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
        val trashCol = if (hasTrashColumn) c.getColumnIndex(MediaStore.Files.FileColumns.IS_TRASHED) else -1

        while (c.moveToNext()) {
            val id = c.getLong(idCol)
            val albumName = c.getString(albumCol) ?: "Neznáme"
            val displayName = c.getString(nameCol) ?: ""
            val mimeType = c.getString(mimeCol) ?: ""
            val path = c.getString(dataCol) ?: ""

            var sizeBytes = if (sizeCol != -1) c.getLong(sizeCol) else 0L
            if (sizeBytes <= 0L && path.isNotEmpty()) {
                try {
                    sizeBytes = File(path).length()
                } catch (_: Exception) {
                }
            }

            var dateAdded = if (dateAddedCol != -1) c.getLong(dateAddedCol) else 0L
            if (dateAdded <= 0L && dateModCol != -1) {
                dateAdded = c.getLong(dateModCol)
            }
            if (dateAdded <= 0L && path.isNotEmpty()) {
                dateAdded = File(path).lastModified() / 1000L
            }
            if (dateAdded <= 0L) {
                dateAdded = System.currentTimeMillis() / 1000L
            }

            val isTrashed = if (trashCol != -1) c.getInt(trashCol) == 1 else false

            val baseUri = if (mimeType.startsWith("video/")) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val itemUri = ContentUris.withAppendedId(baseUri, id)

            photos.add(MediaPhoto(itemUri, albumName, dateAdded, displayName, mimeType, isTrashed, path, sizeBytes))
        }
    }
    photos
}
