//package com.example.paintapp
//
//import android.content.ContentValues
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.ActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material3.Button
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.sourceInformationMarkerEnd
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Canvas
//import androidx.compose.ui.graphics.RectangleShape
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.painter.BitmapPainter
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.graphics.applyCanvas
//import com.example.paintapp.ui.theme.PaintAppTheme
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.launch
//import java.io.OutputStream
//import kotlin.contracts.contract
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent{
//            PaintAppTheme{
//              PaintApp()
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun PaintApp()
//{
//    val context= LocalContext.current.applicationContext
//    val coroutine= rememberCoroutineScope()
//    var currentColor by remember { mutableStateOf(Color.BLACK) }
//    val lines= remember { mutableStateListOf<Line>() }
//    var brushSize by remember { mutableFloatStateOf(10f) }
//    var isEraser by remember { mutableStateOf(false) }
//    val launcher= rememberLauncherForActivityResult(
//        contract =ActivityResultContracts.RequestPermission())
//    {
//     granted->
//        if(!granted)
//        {
//            Toast.makeText(context,"Require Permission",Toast.LENGTH_SHORT).show()
//        }
//    }
//    LaunchedEffect(Unit) {
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
//        {
//            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//    }
//    Column(Modifier.fillMaxSize()){
//        Row(Modifier.fillMaxSize()
//            .padding(8.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            ColorPicker {selectedColor->
//                currentColor=selectedColor
//                isEraser=false
//            }
//            BrushSizeSelector(brushSize.onSizeSelected={selectedSize->brushSize=selectedSize
//            isEraser=isEraser,keepMode={keepEraserMode->isEraser=keepEraserMode}})
//            Button(onClick = {isEraser=true}) {
//              Text("Eraser")
//            }
//            Button(onClick = {lines.clear()}) {
//                Text("Reset")
//            }
//            Button(onClick = {
//                coroutine.launch {
//                    saveDrawingToGallery(context,lines)
//                }
//            }) {
//                Text("save")
//            }
//            Canvas(modifier=Modifier.fillMaxSize()
//                .background(Color.WHITE)
//                .pointerInput(true)
//                {
//                    detectDragGestures{change,dragAmount->
//                        change.consume()
//
//                        val line=Line(
//                            start=change.position-dragAmount,
//                            end=change.position,
//                            color = if(isEraser) Color.WHITE else currentColor,
//                            strokeWith = brushSize
//                        )
//                        lines.add(line)
//                    }
//                }
//            )
//            {
//                lines.forEach{line->
//                    drawLine(
//                        color=line.color,
//                        start=line.start,
//                        end=line.end,
//                        strokeWidth=line.strokeWith,
//                        cap= StrokeCap.Round
//
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ColorPicker(onColorSelected:(Color)->Unit) {
//    var context = LocalContext.current.applicationContext;
//    var colorMap = mapOf(
//        Color.RED to "Red",
//        Color.GREEN to "Green",
//        Color.BLUE to "Blue", Color.BLACK to "Black"
//    )
//    Row {
//        colorMap.forEach { (color, name) ->
//         Box(Modifier.size(40.dp)
//             .background(color, CircleShape)
//             .padding(4.dp)
//             .clickable {
//                 onColorSelected(color)
//                 Toast.makeText(context,name,Toast.LENGTH_SHORT).show()
//             }
//         )
//    }
//}
//
//
//
//    @Composable
//fun  BrushSizeSelector(currentSize:Float,onSizeSelected:(Float)->Unit,
//                       isEraser:Boolean,keepMode: (Boolean)->Unit)
//{
//    var sizeText by remember{ mutableStateOf(currentSize.toString()) }
//    Row {
//      BasicTextField(
//          value = sizeText,
//          onValueChange = {
//              sizeText=it
//              val newSize=it.toFloatOrNull()?:currentSize
//              onSizeSelected(newSize)
//              keepMode(isEraser)
//          },
//          textStyle = TextStyle(fontSize = 16.sp),
//          modifier = Modifier.width(60.dp)
//              .background(Color.GRAY, CircleShape)
//              .padding(8.dp)
//      )
//        Text("px",Modifier.align(Alignment.CenterVertically))
//    }
//}}
//data class Line(val start:Offset,
//    val end:Offset,
//    val color:Color,
//    val strokeWith: Float=10f
//)
//suspend fun  saveDrawingToGallery(context:Context,lines: List<Line>)
//{
//    val bitmap= Bitmap.createBitmap(1080,1920,Bitmap.Config.ARGB_8888)
//    bitmap.applyCanvas {
//        drawColor(android.graphics.Color.WHITE)
//        lines.forEach { line->
//            val paint=android.graphics.Paint.apply{
//                color=line.color.toArgb()
//                storkeWidth=line.strokeWith
//                style=android.graphics.Paint.Style.STROKE
//                strokeCap=android.graphics.Paint.Cap.ROUND
//                strokeJoin=android.graphics.Paint.Join.ROUND
//            }
//            drawLine(line.start.x,line.start.y,line.end.x,line.end.y,paint)
//        }
//    }
//
//    val contentValues=ContentValues.apply{
//        put(MediaStore.MediaColumns.DISPLAY_NAME,"drawing_${System.currentTimeMillis()}.png")
//        put(MediaStore.MediaColumns.MIME_TYPE,"image/png")
//        put(MediaStore.MediaColumns.RELATIVE_PATH,"Pictures/PaintApp")
//    }
//    val resolver=context.contentResolver
//    val uri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
//    if(uri!=null)
//    {
//        val outputStream:OutputStream?=resolver.openOutputStream(uri)
//        outputStream.use {
//            if(it!=null)
//            {
//                bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
//            }
//        }
//        Toast.makeText(context,"Saved to Gallery",Toast.LENGTH_SHORT).show()
//
//    }
//    else
//    {
//        Toast.makeText(context,"failed to save",Toast.LENGTH_SHORT).show()
//    }
//}
//
//


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
import java.io.OutputStream

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
            Toast.makeText(context, "Require Permission", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorPicker { selectedColor ->
                currentColor = selectedColor
                isEraser = false
            }
            BrushSizeSelector(currentSize = brushSize, onSizeSelected = { selectedSize ->
                brushSize = selectedSize
            })
            Button(onClick = { isEraser = true }) { Text("Eraser") }
            Button(onClick = { lines.clear() }) { Text("Reset") }
            Button(onClick = {
                coroutineScope.launch {
                    saveDrawingToGallery(context, lines)
                }
            }) { Text("Save") }
        }

        Box(modifier = Modifier.fillMaxSize().background(ComposeColor.White)) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
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
    val colorMap = mapOf(
        ComposeColor.Red to "Red",
        ComposeColor.Green to "Green",
        ComposeColor.Blue to "Blue",
        ComposeColor.Black to "Black"
    )

    Row {
        colorMap.forEach { (color, name) ->
            Box(
                Modifier
                    .size(40.dp)
                    .background(color, CircleShape)
                    .clickable {
                        onColorSelected(color)
                        Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
                    }
            )
        }
    }
}

@Composable
fun BrushSizeSelector(currentSize: Float, onSizeSelected: (Float) -> Unit) {
    var sizeText by remember { mutableStateOf(currentSize.toString()) }
    Row {
        BasicTextField(
            value = sizeText,
            onValueChange = {
                sizeText = it
                val newSize = it.toFloatOrNull() ?: currentSize
                onSizeSelected(newSize)
            },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.width(60.dp).background(ComposeColor.Gray, CircleShape).padding(8.dp)
        )
        Text("px", Modifier.align(Alignment.CenterVertically))
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
        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
}
