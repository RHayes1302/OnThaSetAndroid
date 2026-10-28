package com.hayesenterprisesllc.onthaset.ui.events

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.SheetHeader
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class ReportReason(val raw: String, val icon: ImageVector) {
    INAPPROPRIATE("Inappropriate Content", Icons.Filled.Warning),
    SPAM("Spam / Fake Event", Icons.Filled.FlashOff),
    HATE("Hate Speech", Icons.Filled.DoNotTouch),
    WRONG_CATEGORY("Wrong Category", Icons.Filled.LabelOff),
    DUPLICATE("Duplicate Event", Icons.Filled.ContentCopy),
    OTHER("Other", Icons.Filled.MoreHoriz)
}

/** Port of ReportEventView.swift — writes to the `event_reports` table. */
@Composable
fun ReportEventScreen(eventID: String, eventTitle: String, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var reason by remember { mutableStateOf<ReportReason?>(null) }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val r = reason ?: return
        submitting = true
        scope.launch {
            val body = JSONObject().apply {
                put("event_id", eventID); put("event_title", eventTitle)
                put("reported_by_user_id", AuthService.currentUserId ?: "anonymous")
                put("reason", r.raw); put("additional_notes", notes.trim()); put("status", "pending")
            }
            val res = runCatching { Http.restPost("event_reports", body) }
            submitting = false
            res.onSuccess { if (it.isSuccess) success = true else error = it.body.ifEmpty { "Unknown error" } }
                .onFailure { error = it.message ?: "Unknown error" }
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            SheetHeader(title = "Report Event", closeLabel = "Cancel", onClose = onClose)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(Modifier.padding(top = 20.dp).size(80.dp).clip(CircleShape).background(AppColors.Red.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Flag, null, tint = AppColors.Red, modifier = Modifier.size(36.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("REPORT EVENT", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("“$eventTitle”", color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, maxLines = 2)
                }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("REASON", color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    ReportReason.entries.forEach { r ->
                        val sel = reason == r
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(if (sel) AppColors.Red else Color.White.copy(alpha = 0.07f))
                                .border(1.dp, if (sel) AppColors.Red else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { reason = r }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(r.icon, null, tint = if (sel) Color.Black else AppColors.Red, modifier = Modifier.width(28.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(r.raw, color = if (sel) Color.Black else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (sel) Icon(Icons.Filled.CheckCircle, null, tint = Color.Black)
                        }
                    }
                }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ADDITIONAL DETAILS (optional)", color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 90.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(14.dp)
                    ) {
                        if (notes.isEmpty()) Text("Describe the issue...", color = AppColors.Gray.copy(alpha = 0.6f), fontSize = 15.sp)
                        BasicTextField(notes, { notes = it }, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(AppColors.Yellow), modifier = Modifier.fillMaxWidth())
                    }
                }
                Row(
                    Modifier.padding(horizontal = 16.dp).padding(bottom = 30.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (reason == null) AppColors.Red.copy(alpha = 0.3f) else AppColors.Red)
                        .clickable(enabled = reason != null && !submitting) { submit() }.padding(16.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    if (submitting) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        val c = if (reason == null) Color.White.copy(alpha = 0.4f) else Color.White
                        Icon(Icons.Filled.Flag, null, tint = c)
                        Spacer(Modifier.width(8.dp))
                        Text("SUBMIT REPORT", color = c, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (success) InfoAlert("Report Submitted", "Thank you. Our team will review this event and take action if necessary.", onDismiss = { success = false; onClose() })
    error?.let { InfoAlert("Submission Failed", it, onDismiss = { error = null }) }
}
