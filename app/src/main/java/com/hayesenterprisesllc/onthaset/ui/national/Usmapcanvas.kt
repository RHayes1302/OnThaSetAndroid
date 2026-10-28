package com.hayesenterprisesllc.onthaset.ui.national

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.EventCategory
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

/**
 * Native US map for the National Run Calendar (replaces the Leaflet WebView).
 * Draws state shapes from assets/us_states.json in Web Mercator, like the iOS map:
 * yellow states, black borders, tap a state to zoom in, coloured event pins.
 */
class StateShape(val name: String, val rings: List<FloatArray>, val bounds: Rect)

object UsMapData {
    @Volatile private var cache: List<StateShape>? = null

    fun load(context: Context): List<StateShape> = cache ?: synchronized(this) {
        cache ?: parse(context.assets.open("us_states.js").bufferedReader().use { it.readText() }.substringAfter("=").trim().removeSuffix(";")).also { cache = it }
    }

    /** Web Mercator, in degree-like units; y grows downward like the screen. */
    fun projX(lon: Double): Float = lon.toFloat()
    fun projY(lat: Double): Float = -Math.toDegrees(ln(tan(PI / 4 + Math.toRadians(lat) / 2))).toFloat()

    private fun parse(text: String): List<StateShape> {
        val features = JSONObject(text).getJSONArray("features")
        val out = ArrayList<StateShape>(features.length())
        for (i in 0 until features.length()) {
            val f = features.getJSONObject(i)
            val name = f.getJSONObject("properties").getString("name")
            val geom = f.getJSONObject("geometry")
            val coords = geom.getJSONArray("coordinates")
            val polygons: List<JSONArray> =
                if (geom.getString("type") == "Polygon") listOf(coords)
                else (0 until coords.length()).map { coords.getJSONArray(it) }
            val rings = ArrayList<FloatArray>()
            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            for (poly in polygons) {
                for (r in 0 until poly.length()) {
                    val ring = poly.getJSONArray(r)
                    val arr = FloatArray(ring.length() * 2)
                    for (p in 0 until ring.length()) {
                        val pt = ring.getJSONArray(p)
                        val x = projX(pt.getDouble(0))
                        val y = projY(pt.getDouble(1))
                        arr[p * 2] = x; arr[p * 2 + 1] = y
                        minX = min(minX, x); maxX = max(maxX, x)
                        minY = min(minY, y); maxY = max(maxY, y)
                    }
                    rings.add(arr)
                }
            }
            out.add(StateShape(name, rings, Rect(minX, minY, maxX, maxY)))
        }
        return out
    }

    fun contains(s: StateShape, x: Float, y: Float): Boolean {
        if (x < s.bounds.left || x > s.bounds.right || y < s.bounds.top || y > s.bounds.bottom) return false
        var inside = false
        for (r in s.rings) {
            val n = r.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val xi = r[i * 2]; val yi = r[i * 2 + 1]
                val xj = r[j * 2]; val yj = r[j * 2 + 1]
                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
                j = i
            }
        }
        return inside
    }

    /** Label positions (same as the iOS/Leaflet map). */
    val labels: List<Triple<String, Double, Double>> = listOf(
        Triple("AL", 32.8, -86.8), Triple("AZ", 34.3, -111.1), Triple("AR", 34.8, -92.2), Triple("CA", 36.8, -119.4),
        Triple("CO", 39.0, -105.5), Triple("CT", 41.6, -72.7), Triple("DE", 39.0, -75.5), Triple("FL", 27.8, -81.7),
        Triple("GA", 32.2, -83.4), Triple("ID", 44.4, -114.6), Triple("IL", 40.0, -89.2), Triple("IN", 40.3, -86.1),
        Triple("IA", 42.0, -93.2), Triple("KS", 38.5, -98.4), Triple("KY", 37.7, -85.3), Triple("LA", 31.2, -91.8),
        Triple("ME", 45.4, -69.0), Triple("MD", 39.0, -76.8), Triple("MA", 42.3, -71.8), Triple("MI", 44.3, -85.4),
        Triple("MN", 46.4, -93.1), Triple("MS", 32.7, -89.7), Triple("MO", 38.5, -92.5), Triple("MT", 47.0, -110.0),
        Triple("NE", 41.5, -99.9), Triple("NV", 38.5, -117.1), Triple("NH", 44.0, -71.6), Triple("NJ", 40.1, -74.5),
        Triple("NM", 34.5, -106.1), Triple("NY", 42.9, -75.5), Triple("NC", 35.6, -79.4), Triple("ND", 47.5, -100.4),
        Triple("OH", 40.4, -82.8), Triple("OK", 35.6, -97.5), Triple("OR", 44.1, -120.5), Triple("PA", 40.9, -77.8),
        Triple("RI", 41.7, -71.5), Triple("SC", 33.9, -80.9), Triple("SD", 44.4, -100.2), Triple("TN", 35.9, -86.4),
        Triple("TX", 31.5, -99.3), Triple("UT", 39.3, -111.1), Triple("VT", 44.1, -72.7), Triple("VA", 37.8, -78.2),
        Triple("WA", 47.4, -120.5), Triple("WV", 38.6, -80.6), Triple("WI", 44.3, -89.6), Triple("WY", 43.0, -107.6)
    )
}

private val StateYellow = Color(0xFFFFD60A).copy(alpha = 0.65f)
private val StateSelected = Color(0xFFFF9500).copy(alpha = 0.88f)
private val MapBackground = Color(0xFF1A1A1A)

private data class MapTransform(val scale: Float, val ox: Float, val oy: Float) {
    fun toScreen(x: Float, y: Float) = Offset(x * scale + ox, y * scale + oy)
    fun toMap(p: Offset) = Offset((p.x - ox) / scale, (p.y - oy) / scale)
}

private fun fit(size: Size, view: Rect, padFraction: Float, maxScale: Float): MapTransform {
    val vw = view.width * (1 + 2 * padFraction)
    val vh = view.height * (1 + 2 * padFraction)
    val scale = min(min(size.width / vw, size.height / vh), maxScale)
    return MapTransform(scale, size.width / 2 - view.center.x * scale, size.height / 2 - view.center.y * scale)
}

@Composable
fun UsMapCanvas(
    pins: List<SupabaseEvent>,
    selectedState: String?,
    onStateTap: (String) -> Unit,
    onEventTap: (SupabaseEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val states = remember { UsMapData.load(context) }
    val lower48 = remember(states) {
        states.filter { it.name !in setOf("Alaska", "Hawaii", "Puerto Rico") }
            .map { it.bounds }
            .reduce { a, b -> Rect(min(a.left, b.left), min(a.top, b.top), max(a.right, b.right), max(a.bottom, b.bottom)) }
    }
    val target = selectedState?.let { n -> states.firstOrNull { it.name.equals(n, ignoreCase = true) }?.bounds } ?: lower48

    // Animate the visible area so tapping a state zooms in smoothly.
    val l by animateFloatAsState(target.left, tween(500), label = "left")
    val t by animateFloatAsState(target.top, tween(500), label = "top")
    val r by animateFloatAsState(target.right, tween(500), label = "right")
    val b by animateFloatAsState(target.bottom, tween(500), label = "bottom")
    val view = Rect(l, t, r, b)
    val pad = if (selectedState != null) 0.15f else 0.03f

    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.Black.copy(alpha = 0.45f), fontSize = 8.sp, fontWeight = FontWeight.Black)

    val latestPins by rememberUpdatedState(pins)
    val latestView by rememberUpdatedState(view)
    val latestPad by rememberUpdatedState(pad)
    val latestOnState by rememberUpdatedState(onStateTap)
    val latestOnEvent by rememberUpdatedState(onEventTap)

    Column(modifier.background(MapBackground)) {
        Canvas(
            Modifier.fillMaxWidth().weight(1f).clipToBounds().pointerInput(states) {
                detectTapGestures { tap ->
                    val base = fit(Size(size.width.toFloat(), size.height.toFloat()), lower48, 0.03f, Float.MAX_VALUE)
                    val tf = fit(Size(size.width.toFloat(), size.height.toFloat()), latestView, latestPad, base.scale * 10f)
                    val hitRadius = 16.dp.toPx()
                    val pin = latestPins.minByOrNull { e ->
                        val p = tf.toScreen(UsMapData.projX(e.longitude), UsMapData.projY(e.latitude))
                        (p - tap).getDistance()
                    }
                    if (pin != null) {
                        val p = tf.toScreen(UsMapData.projX(pin.longitude), UsMapData.projY(pin.latitude))
                        if ((p - tap).getDistance() <= hitRadius) { latestOnEvent(pin); return@detectTapGestures }
                    }
                    val m = tf.toMap(tap)
                    states.firstOrNull { UsMapData.contains(it, m.x, m.y) }?.let { latestOnState(it.name) }
                }
            }
        ) {
            val base = fit(size, lower48, 0.03f, Float.MAX_VALUE)
            val tf = fit(size, view, pad, base.scale * 10f)

            states.forEach { st ->
                val path = Path().apply {
                    fillType = PathFillType.EvenOdd
                    st.rings.forEach { ring ->
                        val n = ring.size / 2
                        if (n > 0) {
                            val p0 = tf.toScreen(ring[0], ring[1])
                            moveTo(p0.x, p0.y)
                            for (i in 1 until n) {
                                val p = tf.toScreen(ring[i * 2], ring[i * 2 + 1])
                                lineTo(p.x, p.y)
                            }
                            close()
                        }
                    }
                }
                val selected = selectedState != null && st.name.equals(selectedState, ignoreCase = true)
                drawPath(path, if (selected) StateSelected else StateYellow)
                drawPath(path, Color.Black.copy(alpha = 0.9f), style = Stroke(width = 1.2.dp.toPx()))
            }

            UsMapData.labels.forEach { (abbr, lat, lng) ->
                val p = tf.toScreen(UsMapData.projX(lng), UsMapData.projY(lat))
                if (p.x in 0f..size.width && p.y in 0f..size.height) {
                    val layout = measurer.measure(abbr, labelStyle)
                    drawText(layout, topLeft = Offset(p.x - layout.size.width / 2f, p.y - layout.size.height / 2f))
                }
            }

            pins.forEach { e ->
                val p = tf.toScreen(UsMapData.projX(e.longitude), UsMapData.projY(e.latitude))
                val color = NationalStyle.pinColor(EventCategory.fromRaw(e.category))
                drawCircle(color.copy(alpha = 0.35f), radius = 10.dp.toPx(), center = p)
                drawCircle(color, radius = 7.dp.toPx(), center = p)
                drawCircle(Color.Black.copy(alpha = 0.4f), radius = 7.dp.toPx(), center = p, style = Stroke(2.dp.toPx()))
            }
        }

        // Territory bar (Hawaii, Alaska, Puerto Rico), as on iOS.
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(Color(0xF70A0A0A)),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TerritoryButton("🌺", "HAWAII") { onStateTap("Hawaii") }
            TerritoryButton("🏔️", "ALASKA") { onStateTap("Alaska") }
            TerritoryButton("🇵🇷", "PUERTO RICO") { onStateTap("Puerto Rico") }
        }
    }
}

@Composable
private fun TerritoryButton(icon: String, name: String, onClick: () -> Unit) {
    val yellow = Color(0xFFFFD60A)
    Row(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(yellow.copy(alpha = 0.06f))
            .border(1.dp, yellow.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Text("  $name", color = yellow, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}