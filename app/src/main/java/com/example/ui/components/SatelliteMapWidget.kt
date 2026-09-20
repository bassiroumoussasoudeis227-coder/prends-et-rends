package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalizationManager
import java.util.Locale

enum class MapVisualMode {
    SATELLITE,
    HYBRID
}

@Composable
fun SatelliteMapWidget(
    latitude: Double,
    longitude: Double,
    address: String?,
    personName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapMode by remember { mutableStateOf(MapVisualMode.SATELLITE) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
        color = Color(0xFF0F172A)
    ) {
        Column {
            // Satellite Canvas rendering area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Procedural Satellite Terrain / Hybrid Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Satellite Deep Ocean & Terrain Palette
                    val terrainGradient = if (mapMode == MapVisualMode.SATELLITE) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF0A0F1D)
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.7f
                        )
                    } else {
                        // Hybrid: Satellite with topographic contrast
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF132A13),
                                Color(0xFF1E293B),
                                Color(0xFF0B192C)
                            )
                        )
                    }
                    drawRect(brush = terrainGradient)

                    // Contour & terrain lines
                    for (i in 1..6) {
                        val y = h * (i / 7f)
                        drawLine(
                            color = if (mapMode == MapVisualMode.HYBRID) Color(0x334ADE80) else Color(0x1FFFFFFF),
                            start = Offset(0f, y),
                            end = Offset(w, y + (i % 2 * 15f)),
                            strokeWidth = 1f
                        )
                    }

                    for (j in 1..8) {
                        val x = w * (j / 9f)
                        drawLine(
                            color = if (mapMode == MapVisualMode.HYBRID) Color(0x2260A5FA) else Color(0x1AFFFFFF),
                            start = Offset(x, 0f),
                            end = Offset(x + (j % 2 * 10f), h),
                            strokeWidth = 1f
                        )
                    }

                    // Satellite Radar pulse waves around marker center
                    val centerX = w * 0.5f
                    val centerY = h * 0.5f

                    drawCircle(
                        color = Color(0x4038BDF8),
                        radius = 45f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = Color(0x2038BDF8),
                        radius = 75f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Satellite Mode Badge (Top-left)
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                        .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (mapMode == MapVisualMode.SATELLITE)
                            LocalizationManager.string("satellite_view")
                        else
                            LocalizationManager.string("hybrid_view"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mode Toggle Button (Top-right)
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            mapMode = if (mapMode == MapVisualMode.SATELLITE)
                                MapVisualMode.HYBRID
                            else
                                MapVisualMode.SATELLITE
                        },
                    color = Color(0xCC1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle",
                            tint = Color(0xFFE2E8F0),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (mapMode == MapVisualMode.SATELLITE) "Hybride" else "Satellite",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Custom Pin / Marker centered in the Satellite view
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Callout Bubble
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xEEFFFFFF),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = personName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    // Red Pin Icon with pulse shadow
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFE11D48), CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // GPS coordinates chip in bottom-left
                Text(
                    text = "${String.format(Locale.US, "%.5f", latitude)}°N, ${String.format(Locale.US, "%.5f", longitude)}°E",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color(0xCC0A0F1D), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Bottom bar with Address and Navigation Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address ?: "Position satellite capturée",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Coordonnées GPS certifiées",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // External Navigation Button
                Button(
                    onClick = {
                        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(personName)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Open Maps",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LocalizationManager.string("open_maps"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
