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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
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
import com.mrfool.stilltime.model.WallpaperChoice
import com.mrfool.stilltime.model.WallpaperLayout
import com.mrfool.stilltime.model.WallpaperDim
import com.mrfool.stilltime.model.ClockTypography
import com.mrfool.stilltime.spotify.SpotifyController
import com.mrfool.stilltime.spotify.StandbyPlayer
import com.mrfool.stilltime.spotify.DevicePlayerController
import com.mrfool.stilltime.ui.components.SpotifyClock
import com.mrfool.stilltime.power.BurnInOffset
import com.mrfool.stilltime.power.rememberBatteryState
import com.mrfool.stilltime.power.rememberClockMoment
import com.mrfool.stilltime.ui.components.ClockFace
import com.mrfool.stilltime.ui.components.ClockReadout
import com.mrfool.stilltime.ui.components.ThemePreview
import com.mrfool.stilltime.ui.components.fontFamily
import com.mrfool.stilltime.util.TimeTextFormatter
import kotlinx.coroutines.delay

@Composable
fun StandbyScreen(
    preferences: ClockPreferences,
    settingsStore: SettingsStore,
    tickerActive: Boolean,
    showControls: Boolean,
    connectionActive: Boolean = tickerActive,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val secondsEnabled = preferences.showSeconds && preferences.style !in
        setOf(ClockStyle.SPOTIFY, ClockStyle.WALLPAPER, ClockStyle.CHROMA)
    val use24HourTime = TimeTextFormatter.uses24HourTime(
        preference = preferences.timeFormat,
        systemUses24HourTime = DateFormat.is24HourFormat(context),
    )
    val moment by rememberClockMoment(
        showSeconds = secondsEnabled,
        isActive = tickerActive,
        refreshSeconds = if (preferences.style == ClockStyle.MUSE) MotivationLibrary.ROTATION_SECONDS else 60L,
    )
    val battery by rememberBatteryState()
    val time = remember(moment, use24HourTime, secondsEnabled, locale) {
        TimeTextFormatter.time(moment, use24HourTime, secondsEnabled, locale)
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
        moment.toEpochSecond() / MotivationLibrary.ROTATION_SECONDS,
        preferences.motivationCategory,
    ) {
        MotivationLibrary.entryFor(
            context = context,
            category = preferences.motivationCategory,
            epochSecond = moment.toEpochSecond(),
        )
    }
    val accessibilityLabel = buildList {
        add(time.spoken)
        if (preferences.style == ClockStyle.MUSE) {
            add(motivation.text)
            add(motivation.attribution)
            add(motivation.sourceTitle)
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
        if (preferences.style == ClockStyle.SPOTIFY) {
            val controller: StandbyPlayer = remember(context, preferences.devicePlayer) {
                if (preferences.devicePlayer) DevicePlayerController(context) else SpotifyController(context)
            }
            val spotifyState by controller.state.collectAsState()
            val mediaReady by com.mrfool.stilltime.spotify.SpotifyMediaAccessService.ready.collectAsState()
            LaunchedEffect(controller, tickerActive, mediaReady) {
                if (tickerActive && preferences.devicePlayer) controller.connect(false)
            }
            DisposableEffect(controller, connectionActive) {
                controller.setActive(connectionActive)
                onDispose { controller.setActive(false) }
            }
            DisposableEffect(controller) { onDispose { controller.close() } }
            SpotifyClock(preferences, readout, spotifyState, tickerActive && !controlsVisible,
                showControls && !controlsVisible, { controller.connect() }, controller::previous,
                controller::togglePlayback, controller::next, { controlsVisible = true },
                Modifier.fillMaxSize().graphicsLayer {
                    translationX = offset.xFraction * burnInDistancePx
                    translationY = offset.yFraction * burnInDistancePx
                }, onUseDevicePlayer = if (preferences.devicePlayer) null else {
                    { settingsStore.setDevicePlayer(true) }
                }, onSeek = controller::seekTo, onShuffle = controller::toggleShuffle)
        } else {
        ClockFace(
            preferences = preferences,
            readout = readout,
            animationActive = tickerActive && !controlsVisible,
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
                            controlsVisible = true
                        }
                    } else {
                        Modifier
                    },
                ),
        )
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
        val sheetWidth = if (maxWidth < 440.dp) maxWidth else 440.dp
        val sheetHeight = maxHeight * 0.88f
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
            color = Color(0xFF111214),
            contentColor = Color(0xFFF7F2F6),
            shape = if (landscape) {
                RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp)
            } else {
                RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            },
            shadowElevation = 24.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = .08f)),
        ) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
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
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onClose)
                                .semantics { contentDescription = closeDescription },
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("×", fontSize = 26.sp, fontWeight = FontWeight.Light)
                            }
                        }
                    }
                HorizontalDivider(color = Color.White.copy(alpha = .07f))
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("settings_list"),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SectionTitle(stringResource(R.string.clock_style))
                        Text("${preferences.style.ordinal + 1} / ${ClockStyle.entries.size}",
                            color = Color(0xFF929398), fontSize = 10.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.testTag("style_picker"),
                        state = rememberLazyListState(initialFirstVisibleItemIndex = preferences.style.ordinal),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(ClockStyle.entries, key = { it.name }) { style ->
                            StyleTile(
                                style = style,
                                preferences = settingsStore.preferencesFor(style),
                                selected = style == preferences.style,
                                onClick = { settingsStore.setStyle(style) },
                            )
                        }
                    }
                }

                if (preferences.style == ClockStyle.FLIP) {
                    item {
                        SettingsCard {
                            SectionTitle("Motion")
                            SettingSwitch("Flip animation", preferences.flipAnimation, settingsStore::setFlipAnimation)
                        }
                    }
                }
                if (preferences.style == ClockStyle.CAT) {
                    item { SettingSwitch("Kitten animation", preferences.catMotion, settingsStore::setCatMotion) }
                }
                if (preferences.style == ClockStyle.WALLPAPER) {
                    item {
                        SectionTitle("Your wallpapers · ${WallpaperChoice.entries.size} included")
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.testTag("wallpaper_picker")) {
                            WallpaperChoice.entries.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { wallpaper ->
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = if (wallpaper == preferences.wallpaper) .18f else .06f))
                                    .selectable(selected = wallpaper == preferences.wallpaper, role = Role.RadioButton,
                                        onClick = { settingsStore.setWallpaper(wallpaper) })
                                    .testTag("wallpaper_${wallpaper.name}")) {
                                    Image(painterResource(wallpaper.resource), contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(76.dp), contentScale = ContentScale.Crop)
                                    Text(wallpaper.title, Modifier.padding(10.dp), color = Color.White, fontSize = 11.sp)
                                }
                            }
                            }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        SectionTitle("Composition")
                        ChoiceRow(WallpaperLayout.entries, preferences.wallpaperLayout, { it.title }, settingsStore::setWallpaperLayout)
                        Spacer(Modifier.height(12.dp))
                        SectionTitle("Wallpaper dimming")
                        ChoiceRow(WallpaperDim.entries, preferences.wallpaperDim, { it.title }, settingsStore::setWallpaperDim)
                    }
                }
                run {
                    item {
                        SectionTitle("Clock typography")
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ClockTypography.entries) { type ->
                                Surface(Modifier.width(112.dp).height(82.dp).clip(RoundedCornerShape(14.dp))
                                    .selectable(type == preferences.typography, role = Role.RadioButton, onClick = { settingsStore.setTypography(type) })
                                    .testTag("type_${type.name}"), color = Color(0xFF242529),
                                    border = if (type == preferences.typography) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                    shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("10:08", fontFamily = type.fontFamily(), fontSize = 25.sp, color = Color.White)
                                        Text(type.title, fontSize = 10.sp, color = Color(0xFFB5B6BB))
                                    }
                                }
                            }
                        }
                        if (preferences.style == ClockStyle.SPOTIFY) {
                            Spacer(Modifier.height(16.dp))
                            SectionTitle("Connection method")
                            ChoiceRow(listOf(false, true), preferences.devicePlayer,
                                { if (it) "Device player" else "Spotify App Remote" }, settingsStore::setDevicePlayer)
                            SettingSwitch("Scroll long song titles", preferences.spotifyMarquee, settingsStore::setSpotifyMarquee)
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
                        SettingSwitch("Aurora background", preferences.museMotion, settingsStore::setMuseMotion)
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
                                text = if (currentMotivation.category.inspiredBy != null) "About the inspiring series" else stringResource(R.string.view_source),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.accent_color))
                    SettingSwitch("Original theme colors", preferences.themeColors, settingsStore::setThemeColors)
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccentChoice.entries.chunked(5).forEach { paletteRow ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        paletteRow.forEach { choice ->
                            val selected = !preferences.themeColors && choice == preferences.accent
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
                    }
                    if (preferences.style !in setOf(ClockStyle.NOIR, ClockStyle.PANORAMA, ClockStyle.REDLINE, ClockStyle.CALENDAR, ClockStyle.CHROMA))
                        SettingSwitch("Color the clock digits", preferences.tintDigits, settingsStore::setTintDigits)
                }

                item {
                    SettingsCard {
                    SectionTitle(stringResource(R.string.display))
                    Spacer(Modifier.height(8.dp))
                    if (preferences.style !in setOf(ClockStyle.SPOTIFY, ClockStyle.WALLPAPER, ClockStyle.CHROMA)) SettingSwitch(
                        label = stringResource(R.string.show_seconds),
                        checked = preferences.showSeconds,
                        onCheckedChange = settingsStore::setShowSeconds,
                    )
                    if (preferences.style != ClockStyle.SPOTIFY) SettingSwitch(
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
                }
            }
                Row(Modifier.fillMaxWidth().background(Color(0xFF17181B)).padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(preferences.style.labelRes), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(onClick = onClose, shape = RoundedCornerShape(14.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
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
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(color = Color(0xFF1B1C20), shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .045f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) { content() }
    }
}

@Composable
private fun StyleTile(
    style: ClockStyle,
    preferences: ClockPreferences,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(168.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
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
            ThemePreview(style, preferences, Modifier.fillMaxWidth().height(86.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(style.labelRes),
                modifier = Modifier.weight(1f).padding(horizontal = 3.dp, vertical = 8.dp),
                color = Color.White.copy(alpha = if (selected) 1f else 0.7f),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
                if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
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
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, modifier = Modifier.weight(1f).padding(end = 12.dp),
            color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp)
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
                    .heightIn(min = 48.dp)
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
