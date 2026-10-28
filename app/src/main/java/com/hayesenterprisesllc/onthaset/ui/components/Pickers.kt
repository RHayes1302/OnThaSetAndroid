package com.hayesenterprisesllc.onthaset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
private val pickerColors @Composable get() = DatePickerDefaults.colors(
    containerColor = Color(0xFF1C1C1E),
    selectedDayContainerColor = AppColors.Yellow, selectedDayContentColor = Color.Black,
    todayDateBorderColor = AppColors.Yellow, todayContentColor = AppColors.Yellow,
    dayContentColor = Color.White, weekdayContentColor = AppColors.Gray,
    navigationContentColor = AppColors.Yellow, subheadContentColor = Color.White,
    titleContentColor = AppColors.Gray, headlineContentColor = Color.White,
    yearContentColor = Color.White, selectedYearContainerColor = AppColors.Yellow, selectedYearContentColor = Color.Black
)

/**
 * Compact DatePicker replacement: a dark field showing the value; tapping opens a date (and optionally
 * time) picker dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(value: Instant, onChange: (Instant) -> Unit, includeTime: Boolean, modifier: Modifier = Modifier) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    val zone = ZoneId.systemDefault()
    val local = value.atZone(zone)

    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Field).clickable { showDate = true }.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (includeTime) DateUtils.abbreviatedWithTime(value) else DateUtils.abbreviated(value), color = Color.White, fontSize = 17.sp)
        Icon(Icons.Filled.ArrowDropDown, null, tint = AppColors.Yellow)
    }

    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = local.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val ms = state.selectedDateMillis
                    showDate = false
                    if (ms != null) {
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        if (includeTime) { pendingDate = d; showTime = true }
                        else onChange(DateUtils.combine(d, local.hour, local.minute))
                    }
                }) { Text("OK", color = AppColors.Yellow) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel", color = AppColors.Gray) } },
            colors = pickerColors
        ) { DatePicker(state = state, colors = pickerColors) }
    }

    if (showTime) {
        val tState = rememberTimePickerState(initialHour = local.hour, initialMinute = local.minute, is24Hour = false)
        Dialog(onDismissRequest = { showTime = false }) {
            androidx.compose.foundation.layout.Column(
                Modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF1C1C1E)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select time", color = AppColors.Gray, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(
                    state = tState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF2C2C2E), selectorColor = AppColors.Yellow,
                        clockDialSelectedContentColor = Color.Black, clockDialUnselectedContentColor = Color.White,
                        timeSelectorSelectedContainerColor = AppColors.Yellow, timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContainerColor = Color(0xFF2C2C2E), timeSelectorUnselectedContentColor = Color.White,
                        periodSelectorSelectedContainerColor = AppColors.Yellow, periodSelectorSelectedContentColor = Color.Black,
                        periodSelectorUnselectedContentColor = Color.White
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showTime = false }) { Text("Cancel", color = AppColors.Gray) }
                    TextButton(onClick = {
                        showTime = false
                        pendingDate?.let { onChange(DateUtils.combine(it, tState.hour, tState.minute)) }
                    }) { Text("OK", color = AppColors.Yellow) }
                }
            }
        }
    }
}

/** Menu-style Picker (`.pickerStyle(.menu)`). */
@Composable
fun MenuPicker(value: String, placeholder: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Field).clickable { open = true }.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value.ifEmpty { placeholder }, color = AppColors.Yellow, fontSize = 17.sp)
            Icon(Icons.Filled.ArrowDropDown, null, tint = AppColors.Yellow)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(placeholder) }, onClick = { open = false; onSelect("") })
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { open = false; onSelect(o) }) }
        }
    }
}
