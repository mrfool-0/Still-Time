package com.mrfool.stilltime.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.mrfool.stilltime.R
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.data.MotivationLibrary
import com.mrfool.stilltime.model.AccentChoice
import com.mrfool.stilltime.model.BrightnessMode
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.MotivationEntry
import com.mrfool.stilltime.model.TimeFormatPreference
import com.mrfool.stilltime.power.BurnInOffset
import com.mrfool.stilltime.power.rememberBatteryState
import com.mrfool.stilltime.power.rememberClockMoment
import com.mrfool.stilltime.ui.components.ClockFace
import com.mrfool.stilltime.ui.components.ClockReadout
import com.mrfool.stilltime.util.TimeTextFormatter
import kotlinx.coroutines.delay

@Composable
fun StandbyScreen(
    preferences: ClockPreferences,
    settingsStore: SettingsStore,
    tickerActive: Boolean,
    showControls: Boolean,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val use24HourTime = TimeTextFormatter.uses24HourTime(
        preference = preferences.timeFormat,
        systemUses24HourTime = DateFormat.is24HourFormat(context),
    )
    val moment by rememberClockMoment(
        showSeconds = preferences.showSeconds,
        isActive = tickerActive,
    )
    val battery by rememberBatteryState()
    val time = remember(moment, use24HourTime, preferences.showSeconds, locale) {
        TimeTextFormatter.time(moment, use24HourTime, preferences.showSeconds, locale)
    }
    val longDate = remember(moment.toLocalDate(), locale) {
        TimeTextFormatter.longDate(moment, locale)
    }
    val compactDate = remember(moment.toLocalDate(), locale) {
        TimeTextFormatter.compactDate(moment, locale)
    }
    val batteryLabel = if (battery.isCharging) {
        stringResource(R.string.charging_percent, battery.percent)
    } else {
        stringResource(R.string.battery_percent, battery.percent)
    }
    val motivation = remember(
        moment.toEpochSecond() / 900L,
        preferences.motivationCategory,
    ) {
        MotivationLibrary.entryFor(
            context = context,
            category = preferences.motivationCategory,
            epochMinute = moment.toEpochSecond() / 60L,
        )
    }
    val accessibilityLabel = buildList {
        add(time.spoken)
        if (preferences.style == ClockStyle.MUSE) {
            add(motivation.text)
            add(motivation.attribution)
        }
        if (preferences.showDate) add(longDate)
        if (preferences.showBattery) add(batteryLabel)
    }.joinToString(separator = ". ")
    val readout = ClockReadout(
        moment = moment,
        time = time,
        longDate = longDate,
        compactDate = compactDate,
        battery = battery,
        batteryLabel = batteryLabel,
        accessibilityLabel = accessibilityLabel,
        use24HourTime = use24HourTime,
        motivation = motivation,
    )

    var controlsVisible by remember { mutableStateOf(false) }
    var hintVisible by remember { mutableStateOf(showControls) }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4_500L)
            hintVisible = false
        }
    }

    if (showControls) {
        BackHandler(enabled = controlsVisible) { controlsVisible = false }
    }

    val burnInBucket = moment.toEpochSecond() / 120L
    val burnInDistancePx = with(LocalDensity.current) { 3.dp.toPx() }
    val offset = remember(burnInBucket, preferences.burnInProtection) {
        if (preferences.burnInProtection) {
            BurnInOffset.forEpochMinute(burnInBucket)
        } else {
            com.mrfool.stilltime.power.PixelOffset(0f, 0f)
        }
    }
    val faceInteraction = remember { MutableInteractionSource() }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        ClockFace(
            preferences = preferences,
            readout = readout,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Slight overscan keeps every background edge covered while the face drifts.
                    scaleX = if (preferences.burnInProtection) 1.02f else 1f
                    scaleY = if (preferences.burnInProtection) 1.02f else 1f
                    translationX = offset.xFraction * burnInDistancePx
                    translationY = offset.yFraction * burnInDistancePx
                }
                .then(
                    if (showControls) {
                        Modifier.clickable(
                            interactionSource = faceInteraction,
                            indication = null,
                        ) {
                            hintVisible = false
                            controlsVisible = true
                        }
                    } else {
                        Modifier
                    },
                ),
        )

        AnimatedVisibility(
            visible = hintVisible && !controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                modifier = Modifier.navigationBarsPadding().padding(bottom = 24.dp),
                color = Color.Black.copy(alpha = 0.58f),
                contentColor = Color.White,
                shape = RoundedCornerShape(100),
            ) {
                Text(
                    text = stringResource(R.string.tap_to_customize),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    fontSize = 12.sp,
                    letterSpacing = 0.25.sp,
                )
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SettingsOverlay(
                preferences = preferences,
                settingsStore = settingsStore,
                currentMotivation = motivation,
                onClose = { controlsVisible = false },
            )
        }
    }
}

@Composable
private fun SettingsOverlay(
    preferences: ClockPreferences,
    settingsStore: SettingsStore,
    currentMotivation: MotivationEntry,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val closeDescription = stringResource(R.string.close_settings)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f)),
    ) {
        val landscape = maxWidth > maxHeight
        val sheetWidth = if (maxWidth < 410.dp) maxWidth else 410.dp
        val sheetHeight = maxHeight * 0.82f
        val dismissAreaModifier = if (landscape) {
            Modifier
                .fillMaxHeight()
                .width(maxWidth - sheetWidth)
                .align(Alignment.CenterStart)
        } else {
            Modifier
                .fillMaxWidth()
                .height(maxHeight - sheetHeight)
                .align(Alignment.TopCenter)
        }
        Box(
            modifier = dismissAreaModifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onClose,
                )
                .semantics {
                    contentDescription = closeDescription
                },
        )
        Surface(
            modifier = Modifier
                .then(
                    if (landscape) {
                        Modifier.fillMaxHeight().width(sheetWidth).align(Alignment.CenterEnd)
                    } else {
                        Modifier.fillMaxWidth().height(sheetHeight).align(Alignment.BottomCenter)
                    },
                ),
            color = Color(0xFF121115),
            contentColor = Color(0xFFF7F2F6),
            shape = if (landscape) {
                RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp)
            } else {
                RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            },
            shadowElevation = 24.dp,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_list")
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.2.sp,
                            )
                            Text(
                                text = stringResource(R.string.customize),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(onClick = onClose)
                                .semantics { contentDescription = closeDescription },
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("×", fontSize = 26.sp, fontWeight = FontWeight.Light)
                            }
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.clock_style))
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.testTag("style_picker"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(ClockStyle.entries, key = { it.name }) { style ->
                            StyleTile(
                                style = style,
                                selected = style == preferences.style,
                                onClick = { settingsStore.setStyle(style) },
                            )
                        }
                    }
                }

                if (preferences.style == ClockStyle.MUSE) {
                    item {
                        SectionTitle(stringResource(R.string.motivation_feed))
                        Spacer(Modifier.height(10.dp))
                        ChoiceRow(
                            choices = MotivationCategory.entries,
                            selected = preferences.motivationCategory,
                            label = { stringResource(it.labelRes) },
                            onSelect = settingsStore::setMotivationCategory,
                        )
                        Text(
                            text = stringResource(R.string.motivation_rotation_hint),
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp),
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, currentMotivation.sourceUrl.toUri()),
                                    )
                                } catch (_: ActivityNotFoundException) {
                                    Toast.makeText(context, currentMotivation.sourceTitle, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.view_source),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.accent_color))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AccentChoice.entries.forEach { choice ->
                            val selected = choice == preferences.accent
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(choice.seed).copy(alpha = if (selected) 1f else 0.48f))
                                    .selectable(
                                        selected = selected,
                                        role = Role.RadioButton,
                                        onClick = { settingsStore.setAccent(choice) },
                                    )
                                    .semantics {
                                        contentDescription = choice.name.lowercase().replaceFirstChar(Char::uppercase)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Box(
                                        Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1A1519)),
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.display))
                    Spacer(Modifier.height(8.dp))
                    SettingSwitch(
                        label = stringResource(R.string.show_seconds),
                        checked = preferences.showSeconds,
                        onCheckedChange = settingsStore::setShowSeconds,
                    )
                    SettingSwitch(
                        label = stringResource(R.string.show_date),
                        checked = preferences.showDate,
                        onCheckedChange = settingsStore::setShowDate,
                    )
                    SettingSwitch(
                        label = stringResource(R.string.show_battery),
                        checked = preferences.showBattery,
                        onCheckedChange = settingsStore::setShowBattery,
                    )
                    SettingSwitch(
                        label = stringResource(R.string.keep_screen_on),
                        checked = preferences.keepScreenOn,
                        onCheckedChange = settingsStore::setKeepScreenOn,
                    )
                    SettingSwitch(
                        label = stringResource(R.string.burn_in_protection),
                        checked = preferences.burnInProtection,
                        onCheckedChange = settingsStore::setBurnInProtection,
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = Color.White.copy(alpha = 0.055f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                text = stringResource(R.string.low_power_pick),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                            )
                            Text(
                                text = stringResource(R.string.low_power_hint),
                                modifier = Modifier.padding(top = 5.dp),
                                color = Color.White.copy(alpha = 0.68f),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.time_format))
                    Spacer(Modifier.height(10.dp))
                    ChoiceRow(
                        choices = TimeFormatPreference.entries,
                        selected = preferences.timeFormat,
                        label = { stringResource(it.labelRes) },
                        onSelect = settingsStore::setTimeFormat,
                    )
                }

                item {
                    SectionTitle(stringResource(R.string.brightness))
                    Spacer(Modifier.height(10.dp))
                    ChoiceRow(
                        choices = BrightnessMode.entries,
                        selected = preferences.brightness,
                        label = { stringResource(it.labelRes) },
                        onSelect = settingsStore::setBrightness,
                    )
                }

                item {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.09f))
                    Spacer(Modifier.height(18.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    R.string.screensaver_hint,
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.open_screensaver_settings),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = stringResource(R.string.screensaver_hint),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.58f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
    )
}

@Composable
private fun StyleTile(
    style: ClockStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = when (style) {
        ClockStyle.PEBBLE -> Color(0xFF191825) to Color(0xFFFFB7C5)
        ClockStyle.FLIP -> Color(0xFF292625) to Color(0xFFF5EEE7)
        ClockStyle.EDITORIAL -> Color(0xFFF0EDE5) to Color(0xFF1E1B18)
        ClockStyle.ORBIT -> Color(0xFF071C1D) to Color(0xFF9EE6CF)
        ClockStyle.SOLAR -> Color(0xFF98CAE4) to Color(0xFFFFE3A3)
        ClockStyle.MUSE -> Color(0xFF24203C) to Color(0xFFFFB7C5)
        ClockStyle.NOIR -> Color.Black to Color(0xFFFFB7C5)
    }
    Surface(
        modifier = Modifier
            .width(116.dp)
            .height(104.dp)
            .testTag("style_${style.name}")
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        color = if (selected) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.055f),
        shape = RoundedCornerShape(18.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.first),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (style == ClockStyle.ORBIT) "◷" else "10:08",
                    color = colors.second,
                    fontSize = if (style == ClockStyle.ORBIT) 28.sp else 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(style.labelRes),
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 6.dp),
                color = Color.White.copy(alpha = if (selected) 1f else 0.7f),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                uncheckedBorderColor = Color.White.copy(alpha = 0.2f),
            ),
        )
    }
}

@Composable
private fun <T> ChoiceRow(
    choices: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(choices) { choice ->
            val isSelected = choice == selected
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(choice) },
                    ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.07f)
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    Color.White.copy(alpha = 0.78f)
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(choice),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
