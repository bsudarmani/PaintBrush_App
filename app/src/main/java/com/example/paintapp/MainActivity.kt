package com.example.paintapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaintApp()
        }
    }
}

@Composable
fun PaintApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentColor by remember { mutableStateOf(ComposeColor.Black) }
    val lines = remember { mutableStateListOf<Line>() }
    var brushSize by remember { mutableStateOf(10f) }
    var isEraser by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "Storage permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isEraser = true },
                modifier = Modifier.weight(1f).padding(5.dp)
            ) {
                Text("Eraser")
            }

            Button(
                onClick = { lines.clear() },
                modifier = Modifier.weight(1f).padding(5.dp)
            ) {
                Text("Reset")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        saveDrawingToGallery(context, lines)
                    }
                },
                modifier = Modifier.weight(1f).padding(5.dp)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ColorPicker { selectedColor ->
                currentColor = selectedColor
                isEraser = false
            }

            BrushSizeSelector(currentSize = brushSize, onSizeSelected = { selectedSize ->
                brushSize = selectedSize
            })
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor.White)
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val line = Line(
                                start = change.position - dragAmount,
                                end = change.position,
                                color = if (isEraser) ComposeColor.White else currentColor,
                                strokeWidth = brushSize
                            )
                            lines.add(line)
                        }
                    }
            ) {
                lines.forEach { line ->
                    drawLine(
                        color = line.color,
                        start = line.start,
                        end = line.end,
                        strokeWidth = line.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPicker(onColorSelected: (ComposeColor) -> Unit) {
    val context = LocalContext.current
    val colors = listOf(
        ComposeColor.Red to "Red",
        ComposeColor.Green to "Green",
        ComposeColor.Blue to "Blue",
        ComposeColor.Black to "Black"
    )

    Row {
        colors.forEach { (color, name) ->
            Box(
                Modifier
                    .size(50.dp)
                    .background(color, CircleShape)
                    .clickable {
                        onColorSelected(color)
                        Toast.makeText(context, "$name selected", Toast.LENGTH_SHORT).show()
                    }
                    .padding(5.dp)
            )
        }
    }
}

@Composable
fun BrushSizeSelector(currentSize: Float, onSizeSelected: (Float) -> Unit) {
    var sizeText by remember { mutableStateOf(currentSize.toString()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = sizeText,
            onValueChange = {
                sizeText = it
                val newSize = it.toFloatOrNull() ?: currentSize
                onSizeSelected(newSize)
            },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .width(80.dp)
                .background(ComposeColor.LightGray, CircleShape)
                .padding(8.dp)
        )
        Text("px", Modifier.padding(start = 5.dp))
    }
}

data class Line(val start: Offset, val end: Offset, val color: ComposeColor, val strokeWidth: Float = 10f)

suspend fun saveDrawingToGallery(context: Context, lines: List<Line>) {
    val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    bitmap.applyCanvas {
        drawColor(Color.WHITE)
        val paint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        lines.forEach { line ->
            paint.color = line.color.toArgb()
            paint.strokeWidth = line.strokeWidth
            drawLine(line.start.x, line.start.y, line.end.x, line.end.y, paint)
        }
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PaintApp")
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
        }
        Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Failed to save!", Toast.LENGTH_SHORT).show()
}
