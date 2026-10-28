package com.hayesenterprisesllc.onthaset.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors

// ─────────────────────────────────────────────────────────────────────────────
// Brand shield ("shield.fill" + ON / THA / SET)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The yellow highway-sign shield logo used on almost every screen.
 * [size] is the SF Symbol point size used on iOS; text sizes scale with it.
 */
@Composable
fun ShieldLogo(size: Dp = 85.dp, modifier: Modifier = Modifier) {
    val s = size.value
    Box(modifier = modifier.size(size * 1.15f), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Filled.Shield, contentDescription = "On Tha Set",
            tint = AppColors.Yellow, modifier = Modifier.size(size * 1.15f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-s * 0.05f).dp)
        ) {
            ShieldText("ON", (s * 0.176f).sp)
            ShieldText("THA", (s * 0.141f).sp)
            ShieldText("SET", (s * 0.235f).sp)
        }
    }
}

@Composable
private fun ShieldText(t: String, size: TextUnit) =
    Text(
        t, color = Color.Black, fontSize = size, fontWeight = FontWeight.Black, lineHeight = size,
        style = TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false))
    )

// ─────────────────────────────────────────────────────────────────────────────
// Screen scaffolding
// ─────────────────────────────────────────────────────────────────────────────

/** Black full-screen container (Color.black.ignoresSafeArea()). */
@Composable
fun BlackScreen(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Color.Black), content = content)
}

/** Simple top bar with a yellow back chevron, optional centered title/shield, and trailing actions. */
@Composable
fun TopBar(
    onBack: (() -> Unit)?,
    title: String? = null,
    showShield: Boolean = false,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    actions: @Composable () -> Unit = {}
) {
    Box(
        Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(backIcon, "Back", tint = AppColors.Yellow)
            }
        }
        when {
            showShield -> ShieldLogo(32.dp, Modifier.align(Alignment.Center))
            title != null -> Text(
                title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) { actions() }
    }
}

/** Header with "Close" text button used by sheets. */
@Composable
fun SheetHeader(title: String? = null, closeLabel: String = "Close", onClose: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(52.dp).padding(horizontal = 8.dp)) {
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
            Text(closeLabel, color = AppColors.Yellow, fontSize = 17.sp)
        }
        if (title != null) Text(
            title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
    }
}

@Composable
fun CloseXButton(onClick: () -> Unit, tint: Color = AppColors.Yellow) {
    IconButton(onClick = onClick) { Icon(Icons.Filled.Close, "Close", tint = tint) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Buttons
// ─────────────────────────────────────────────────────────────────────────────

/** Solid yellow button with black bold text (makeMenuButton / styleEnter). */
@Composable
fun YellowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    color: Color = AppColors.Yellow,
    corner: Dp = 8.dp,
    verticalPadding: Dp = 16.dp,
    leading: ImageVector? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner))
            .background(if (enabled) color else AppColors.Gray)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = verticalPadding, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        } else if (leading != null) {
            Icon(leading, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

/** Yellow outlined button ("GO BACK"). */
@Composable
fun OutlineYellowButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, corner: Dp = 8.dp, leading: ImageVector? = null) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner))
            .border(2.dp, AppColors.Yellow, RoundedCornerShape(corner))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Icon(leading, null, tint = AppColors.Yellow, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = AppColors.Yellow, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cards & text
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Card(
    modifier: Modifier = Modifier,
    corner: Dp = 15.dp,
    background: Color = AppColors.Card,
    border: BorderStroke? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    var m = modifier.clip(RoundedCornerShape(corner)).background(background)
    if (border != null) m = m.border(border, RoundedCornerShape(corner))
    Column(m.padding(padding), content = content)
}

@Composable
fun SectionHeader(icon: ImageVector?, title: String, color: Color = Color.White) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun YellowDivider() = HorizontalDivider(color = AppColors.Yellow.copy(alpha = 0.3f))

@Composable
fun LoadingOverlay(text: String? = null) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(enabled = true) {}, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.Yellow)
            if (text != null) {
                Spacer(Modifier.height(12.dp))
                Text(text, color = AppColors.Yellow)
            }
        }
    }
}

@Composable
fun CenteredSpinner(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Yellow)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text fields
// ─────────────────────────────────────────────────────────────────────────────

/** Dark rounded text field (FormTextFieldStyle / ProfileTextFieldStyle). */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 5,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    background: Color = AppColors.Field,
    borderColor: Color? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    var m = modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(background)
    if (borderColor != null) m = m.border(1.dp, borderColor, RoundedCornerShape(8.dp))
    Row(m.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, color = AppColors.Gray.copy(alpha = 0.7f), fontSize = 17.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                cursorBrush = SolidColor(AppColors.Yellow),
                keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun FieldLabel(label: String, color: Color = AppColors.Yellow, small: Boolean = true) {
    Text(
        label, color = color, fontWeight = FontWeight.Bold,
        fontSize = if (small) 11.sp else 12.sp, modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
    )
}

@Composable
fun LabeledField(label: String, labelColor: Color = AppColors.Yellow, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        FieldLabel(label, labelColor)
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs (SwiftUI .alert)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InfoAlert(title: String, message: String, onDismiss: () -> Unit, confirmText: String = "OK", onConfirm: () -> Unit = onDismiss) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = AppColors.Yellow) } },
        containerColor = Color(0xFF1C1C1E), titleContentColor = Color.White, textContentColor = Color(0xFFD1D1D6)
    )
}

@Composable
fun ConfirmAlert(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
    cancelText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmText, color = if (destructive) AppColors.Red else AppColors.Yellow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cancelText, color = AppColors.Yellow) } },
        containerColor = Color(0xFF1C1C1E), titleContentColor = Color.White, textContentColor = Color(0xFFD1D1D6)
    )
}

/** confirmationDialog replacement: a list of choices. */
@Composable
fun ChoiceDialog(title: String, message: String?, choices: List<Pair<String, () -> Unit>>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (message != null) Text(message, modifier = Modifier.padding(bottom = 8.dp))
                choices.forEach { (label, action) ->
                    TextButton(onClick = { action(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text(label, color = AppColors.Yellow, fontSize = 17.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AppColors.Gray) } },
        containerColor = Color(0xFF1C1C1E), titleContentColor = Color.White, textContentColor = Color(0xFFD1D1D6)
    )
}

/** Small pill badge (e.g. "BEFORE", "THIS WEEK"). */
@Composable
fun Badge(text: String, background: Color, color: Color = Color.Black, fontSize: TextUnit = 10.sp, modifier: Modifier = Modifier) {
    Text(
        text, color = color, fontSize = fontSize, fontWeight = FontWeight.Black,
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(background).padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun IconLabelRow(icon: ImageVector, text: String, iconTint: Color = AppColors.Yellow, textColor: Color = AppColors.Gray, fontSize: TextUnit = 12.sp, maxLines: Int = Int.MAX_VALUE) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size((fontSize.value + 2).dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = textColor, fontSize = fontSize, maxLines = maxLines)
    }
}
