package com.sahana.expense

import android.os.Bundle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.Manifest
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.widget.Toast
import android.app.Service
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.math.sin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow

enum class AppLanguage { ENGLISH, TAMIL }

private fun tr(language: AppLanguage, english: String, tamil: String): String =
    if (language == AppLanguage.TAMIL) tamil else english

private fun loadLanguage(context: Context): AppLanguage =
    if (context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("language", "en") == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH

private fun saveLanguage(context: Context, language: AppLanguage) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString("language", if (language == AppLanguage.TAMIL) "ta" else "en").apply()
}

enum class EntryType { INCOME, EXPENSE, TRANSFER }

data class MoneyEntry(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: EntryType,
    val category: String,
    val paymentMethod: String = "Cash",
    val time: Long = System.currentTimeMillis(),
    val photoUri: String? = null
)

private val Midnight = Color(0xFF0B1020)
private val Ink = Color(0xFF161A2B)
private val Violet = Color(0xFF6C5CE7)
private val VioletDeep = Color(0xFF4D3BCB)
private val Gold = Color(0xFFFFC857)
private val Mint = Color(0xFF35D0A0)
private val Coral = Color(0xFFFF6B7A)
private val CanvasBg = Color(0xFFF6F7FB)

data class AINAThemePreset(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val dark: Boolean
)

private val AINAThemes = listOf(
    AINAThemePreset("Midnight Purple", Color(0xFF7C5CFF), Color(0xFF4DE1B2), Color(0xFF080B16), Color(0xFF141A2A), true),
    AINAThemePreset("Ocean Blue", Color(0xFF1976D2), Color(0xFF22B8CF), Color(0xFFF2F8FF), Color.White, false),
    AINAThemePreset("Forest Green", Color(0xFF218739), Color(0xFF7BCB55), Color(0xFFF2FAF3), Color.White, false),
    AINAThemePreset("Sunset Orange", Color(0xFFE85D04), Color(0xFFFF9F1C), Color(0xFFFFF6EF), Color.White, false),
    AINAThemePreset("Royal Sapphire", Color(0xFF3157D5), Color(0xFF62A7FF), Color(0xFFEEF3FF), Color.White, false),
    AINAThemePreset("Rose Gold", Color(0xFFC75C7A), Color(0xFFE8A87C), Color(0xFFFFF5F7), Color.White, false),
    AINAThemePreset("Obsidian", Color(0xFFB5B5B5), Color(0xFF65D8C1), Color(0xFF050505), Color(0xFF151515), true),
    AINAThemePreset("Pearl White", Color(0xFF5A67D8), Color(0xFF38A169), Color(0xFFF9FAFC), Color.White, false),
    AINAThemePreset("Aurora", Color(0xFF8B5CF6), Color(0xFF14B8A6), Color(0xFFF3F1FF), Color.White, false),
    AINAThemePreset("Arctic", Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFFEFFBFF), Color.White, false),
    AINAThemePreset("Mint", Color(0xFF059669), Color(0xFF34D399), Color(0xFFEEFCF6), Color.White, false),
    AINAThemePreset("Coffee", Color(0xFF8B5E3C), Color(0xFFD6A66A), Color(0xFFFFF8F0), Color.White, false),
    AINAThemePreset("Cyber Violet", Color(0xFFA855F7), Color(0xFF22D3EE), Color(0xFF0A0710), Color(0xFF18111F), true),
    AINAThemePreset("Electric Blue", Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFF07111F), Color(0xFF111C2E), true),
    AINAThemePreset("Emerald Wealth", Color(0xFF059669), Color(0xFFA3E635), Color(0xFF06140F), Color(0xFF10251C), true),
    AINAThemePreset("Golden Wealth", Color(0xFFD69E2E), Color(0xFFF6E05E), Color(0xFF100D06), Color(0xFF1D180B), true),
    AINAThemePreset("Moonlight", Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFF111827), Color(0xFF1F2937), true),
    AINAThemePreset("Luxury AINA", Color(0xFFD4AF37), Color(0xFF8B5CF6), Color(0xFF08080B), Color(0xFF17151D), true),
    // Cinematic hero-inspired palettes with original colors; no character artwork.
    AINAThemePreset("Arc Reactor", Color(0xFF00CFFF), Color(0xFF8BE9FF), Color(0xFF041018), Color(0xFF0A202A), true),
    AINAThemePreset("Asgard Gold", Color(0xFFE7B84B), Color(0xFF7DD3FC), Color(0xFF0B101C), Color(0xFF1B2436), true),
    AINAThemePreset("Wakanda Night", Color(0xFF8B5CF6), Color(0xFFD6B36A), Color(0xFF090712), Color(0xFF1B1328), true),
    AINAThemePreset("Quantum Red", Color(0xFFFF3B4E), Color(0xFFFF9B6A), Color(0xFF140609), Color(0xFF281015), true),
    AINAThemePreset("Multiverse", Color(0xFF7C3AED), Color(0xFF06B6D4), Color(0xFF070514), Color(0xFF16112A), true)
)


// SAHANA Premium Theme Catalog — 120 original visual identities.
// Each preset is intentionally distinct by name, palette, mood and UI treatment.
// Artwork/character packs can be attached to these IDs without changing finance logic.
data class SahanaThemePack(
    val id: Int,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val dark: Boolean = true
)

val SahanaThemeCatalog100: List<SahanaThemePack> = listOf(
    SahanaThemePack(1,"Midnight Neon",Color(0xFF7C4DFF),Color(0xFF00D4FF),Color(0xFF070816),Color(0xFF12152A)),
    SahanaThemePack(2,"Ocean Pulse",Color(0xFF00A6FF),Color(0xFF00E5C3),Color(0xFF04131C),Color(0xFF0B202B)),
    SahanaThemePack(3,"Forest Ember",Color(0xFF35C76F),Color(0xFFFFB74D),Color(0xFF07140E),Color(0xFF10251A)),
    SahanaThemePack(4,"Royal Sapphire",Color(0xFF3D7BFF),Color(0xFF9B7BFF),Color(0xFF070A16),Color(0xFF111A34)),
    SahanaThemePack(5,"Rose Gold",Color(0xFFFF6F91),Color(0xFFFFC6A5),Color(0xFF16090E),Color(0xFF2A111A)),
    SahanaThemePack(6,"Aurora",Color(0xFF7CF7D4),Color(0xFFB98CFF),Color(0xFF071014),Color(0xFF122027)),
    SahanaThemePack(7,"Cyber Violet",Color(0xFFB04CFF),Color(0xFF00F0FF),Color(0xFF0D0615),Color(0xFF1B0E28)),
    SahanaThemePack(8,"Solar Flare",Color(0xFFFF7A00),Color(0xFFFFD54F),Color(0xFF160B03),Color(0xFF291705)),
    SahanaThemePack(9,"Arctic Glass",Color(0xFF69D2FF),Color(0xFFE5F7FF),Color(0xFF071116),Color(0xFF11242D)),
    SahanaThemePack(10,"Crimson Night",Color(0xFFFF3B4E),Color(0xFFFF8A65),Color(0xFF150508),Color(0xFF290C12))
) + listOf(
    "Galaxy","Nebula","Moonlight","Starlight","Deep Space","Comet","Eclipse","Meteor","Cosmic Blue","Cosmic Rose",
    "Rainforest","Jungle","Pine","Moss","Wild Earth","Tropical","Monsoon","Bamboo","Emerald","Sage",
    "Sunset","Sunrise","Golden Hour","Desert","Canyon","Volcano","Firefly","Autumn","Maple","Copper",
    "Cherry Blossom","Sakura Night","Lavender","Lilac","Peach Bloom","Rose Garden","Lotus","Jasmine","Hibiscus","Floral Dawn",
    "Ocean Mist","Coral Reef","Deep Sea","Lagoon","Wave Rider","Beach Sunset","Island","Pearl","Blue Lagoon","Sea Glass",
    "Cyber Red","Cyber Blue","Cyber Green","Cyber Gold","Synthwave","Retro Grid","Pixel Night","Arcade","Hologram","Quantum",
    "Racing Black","Racing Red","Racing Blue","Racing Gold","Turbo","Street Night","Drift","Formula","Chrome","Carbon",
    "Royal Gold","Royal Purple","Royal Emerald","Crown","Velvet","Diamond","Platinum","Obsidian","Onyx","Midnight Gold",
    "Temple Dawn","Temple Night","Tamil Heritage","Village Morning","Pongal","Deepavali","Karthigai","Monsoon Tamil","Palm Grove","Marudham",
    "Super Hero","Web Hero","Thunder Hero","Iron Tech","Galaxy Hero","Shadow Hero","Neon Hero","Cosmic Hero","Guardian","Legend",
    "Candy Pop","Cute Dream","Kawaii Night","Bunny Cloud","Panda Moon","Fox Fire","Cat Cafe","Magic Girl","Fairy Garden","Dreamland"
).mapIndexed { i, name ->
    val palettes = listOf(
        Triple(Color(0xFF7C4DFF), Color(0xFF00D4FF), Color(0xFF080A18)),
        Triple(Color(0xFF00BFA6), Color(0xFF64FFDA), Color(0xFF061411)),
        Triple(Color(0xFFFF4081), Color(0xFFFFC1E3), Color(0xFF180812)),
        Triple(Color(0xFFFF8F00), Color(0xFFFFD740), Color(0xFF170C02)),
        Triple(Color(0xFF2979FF), Color(0xFF40C4FF), Color(0xFF050E18)),
        Triple(Color(0xFFAB47BC), Color(0xFFE1BEE7), Color(0xFF120717))
    )
    val p = palettes[i % palettes.size]
    SahanaThemePack(i + 11, name, p.first, p.second, p.third, p.third.copy(alpha = 0.92f))
}

@Composable
fun AINATheme(themeIndex: Int = 0, darkOverride: Boolean? = null, content: @Composable () -> Unit) {
    val preset = AINAThemes[themeIndex.coerceIn(AINAThemes.indices)]
    val dark = darkOverride ?: preset.dark
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            primary = preset.primary,
            onPrimary = Color.White,
            secondary = preset.secondary,
            tertiary = Gold,
            background = preset.background,
            surface = preset.surface,
            onSurface = Color.White,
            surfaceVariant = preset.surface.copy(alpha = .9f),
            onSurfaceVariant = Color(0xFFB9BED0),
            error = Coral
        ) else lightColorScheme(
            primary = preset.primary,
            onPrimary = Color.White,
            secondary = preset.secondary,
            tertiary = Gold,
            background = preset.background,
            surface = preset.surface,
            onSurface = Ink,
            surfaceVariant = Color(0xFFEFF0F6),
            onSurfaceVariant = Color(0xFF697087),
            error = Coral
        ),
        typography = Typography(),
        content = content
    )
}

class MainActivity : FragmentActivity() {
    private var lockPromptShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (android.provider.Settings.canDrawOverlays(this) && quickAddEnabled(this)) {
                try {
                    val serviceIntent = Intent(this, QuickAddOverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent) else startService(serviceIntent)
                } catch (_: Exception) { }
            } else if (!getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("overlay_prompted", false)) {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean("overlay_prompted", true).apply()
                try {
                    startActivity(Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ))
                } catch (_: Exception) {
                    try { startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) } catch (_: Exception) { }
                }
            }
        }
        setContent { AINAApp(initialAddType = intent.getStringExtra("quick_add_type")) }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.provider.Settings.canDrawOverlays(this) && quickAddEnabled(this)) {
            try {
                val serviceIntent = Intent(this, QuickAddOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent) else startService(serviceIntent)
            } catch (_: Exception) { }
        }
        if (!lockPromptShown && getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("app_lock", false)) {
            lockPromptShown = true
            showBiometricLock()
        }
    }

    fun enableAndShowBiometricLock() {
        lockPromptShown = false
        showBiometricLock()
    }

    fun showBiometricLock() {
        val manager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Fingerprint / device lock is not available on this phone", Toast.LENGTH_LONG).show()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    finish()
                }
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock AINA")
            .setSubtitle("Protect your financial information")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AINAApp(initialAddType: String? = null) {
    var tab by remember { mutableIntStateOf(0) }
    var darkTheme by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var language by remember { mutableStateOf(loadLanguage(context)) }
    var themeIndex by remember { mutableIntStateOf(loadTheme(context)) }
    var showExplore by remember { mutableStateOf(false) }
    val entries = remember {
        mutableStateListOf<MoneyEntry>().apply {
            addAll(loadEntries(context))
        }
    }
    var showAdd by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf(EntryType.EXPENSE) }

    LaunchedEffect(initialAddType) {
        when (initialAddType) {
            "EXPENSE" -> { addType = EntryType.EXPENSE; showAdd = true }
            "INCOME" -> { addType = EntryType.INCOME; showAdd = true }
        }
    }
    var selectedEntry by remember { mutableStateOf<MoneyEntry?>(null) }
    var editingEntry by remember { mutableStateOf<MoneyEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<MoneyEntry?>(null) }

    AINATheme(themeIndex = themeIndex, darkOverride = darkTheme) {
    Scaffold(
        containerColor = if (darkTheme) Midnight else CanvasBg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AINALogo(modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("AINA", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(tr(language, "Your money, elevated.", "உங்கள் பணம், இன்னும் உயர்ந்த முறையில்."), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp) {
                    IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, tr(language, "Notifications", "அறிவிப்புகள்"), tint = Ink) }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 18.dp, color = Color.White) {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.Home, null) }, label = { Text(tr(language, "Home", "முகப்பு")) })
                    NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.Analytics, null) }, label = { Text(tr(language, "Insights", "பகுப்பாய்வு")) })
                    NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.MoreHoriz, null) }, label = { Text(tr(language, "More", "மேலும்")) })
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addType = EntryType.EXPENSE; showAdd = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(tr(language, "Add transaction", "பரிவர்த்தனை சேர்"), fontWeight = FontWeight.Bold) },
                containerColor = Ink,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(padding),
            label = "main"
        ) { selected ->
            when (selected) {
                0 -> HomeScreen(entries, language, { addType = EntryType.INCOME; showAdd = true }, { addType = EntryType.EXPENSE; showAdd = true }, { addType = EntryType.TRANSFER; showAdd = true }, { selectedEntry = it }, { showExplore = true })
                1 -> ReportsScreen(entries, language) { selectedEntry = it }
                else -> MoreScreen(language, onExplore = { showExplore = true })
            }
        }
    }

    if (showAdd) {
        AddEntryDialog(addType, { showAdd = false }) { title, amount, category, paymentMethod, timestamp, photoUri ->
            val storedPhoto = photoUri?.let { persistPhoto(context, it) }
            val entry = MoneyEntry(System.currentTimeMillis(), title, amount, addType, category, paymentMethod, timestamp, storedPhoto)
            entries.add(0, entry)
            saveEntries(context, entries)
            playTransactionFeedback(context, addType)
            showAdd = false
        }
    }

    if (selectedEntry != null) {
        EntryDetailDialog(
            entry = selectedEntry!!,
            onDismiss = { selectedEntry = null },
            onEdit = { editingEntry = it; selectedEntry = null },
            onDelete = { showDeleteConfirm = selectedEntry; selectedEntry = null }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete transaction?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("This transaction will be removed from AINA. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val target = showDeleteConfirm
                    if (target != null) {
                        entries.removeAll { it.id == target.id }
                        saveEntries(context, entries)
                    }
                    showDeleteConfirm = null
                }) { Text("Delete", color = Coral, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } }
        )
    }

    if (editingEntry != null) {
        EditEntryDialog(
            entry = editingEntry!!,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                val index = entries.indexOfFirst { it.id == updated.id }
                if (index >= 0) entries[index] = updated
                saveEntries(context, entries)
                editingEntry = null
            }
        )
    }

    if (showExplore) {
        ExploreDialog(
            entries = entries,
            darkTheme = darkTheme,
            themeIndex = themeIndex,
            language = language,
            initialActive = "Smart Insights",
            onLanguageChange = { language = it; saveLanguage(context, it) },
            onThemeChange = { newIndex -> themeIndex = newIndex; saveTheme(context, newIndex) },
            onToggleTheme = { darkTheme = !darkTheme },
            onRestore = { restored -> entries.clear(); entries.addAll(restored); saveEntries(context, entries) },
            onDismiss = { showExplore = false }
        )
    }
    }
}

@Composable
private fun AINALogo(modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Violet, VioletDeep))), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(25.dp))
        Box(Modifier.size(7.dp).align(Alignment.TopEnd).offset((-6).dp, 6.dp).clip(CircleShape).background(Gold))
    }
}

@Composable
private fun HomeScreen(entries: List<MoneyEntry>, language: AppLanguage, onIncome: () -> Unit, onExpense: () -> Unit, onTransfer: () -> Unit, onEntryClick: (MoneyEntry) -> Unit, onSaha: () -> Unit) {
    val context = LocalContext.current
    val uiLanguage = loadLanguage(context)
    val income = entries.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val expense = entries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense
    // Goals are intentionally independent from the cash balance. They are persisted
    // separately and are re-read whenever Home recomposes, so saved changes appear
    // here immediately after closing Explore.
    val savingsTarget = loadGoal(context)
    val savingsSaved = loadGoalSaved(context)
    val investmentTarget = loadInvestmentTarget(context)
    val investmentSaved = loadInvestmentSaved(context)
    val grouped = entries.sortedByDescending { it.time }.groupBy { dayKey(it.time) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp)
    ) {
        item { BalanceHero(balance, income, expense, language) }
        item { SahaHomeCard(language, onSaha) }

        if (savingsTarget > 0 || investmentTarget > 0) {
            item {
                GoalOverviewCard(
                    savingsTarget = savingsTarget,
                    savingsSaved = savingsSaved,
                    investmentTarget = investmentTarget,
                    investmentSaved = investmentSaved
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction(tr(uiLanguage, "Income", "வரவு"), Icons.Default.ArrowDownward, Mint, onIncome, Modifier.weight(1f))
                QuickAction(tr(uiLanguage, "Expense", "செலவு"), Icons.Default.ArrowUpward, Coral, onExpense, Modifier.weight(1f))
                QuickAction(tr(uiLanguage, "Transfer", "மாற்றம்"), Icons.Default.SwapHoriz, Violet, onTransfer, Modifier.weight(1f))
            }
        }
        item { SpendingOverview(income, expense) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr(uiLanguage, "Recent activity", "சமீபத்திய பரிவர்த்தனைகள்"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(tr(uiLanguage, "Grouped by transaction date", "தேதி வாரியாக"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (entries.isEmpty()) { item { EmptyState() } }
        grouped.entries.take(8).forEach { (day, dayEntries) ->
            item {
                Text(dayLabel(dayEntries.first().time), fontWeight = FontWeight.Bold, color = Violet, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
            }
            items(dayEntries.take(8), key = { it.id }) { EntryRow(it, onClick = onEntryClick) }
        }
    }
}

@Composable
private fun SahaHomeCard(language: AppLanguage, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10162A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(Violet, Color(0xFF22D3EE)))), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("AI", color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                Text(tr(language, "Your private money assistant", "உங்கள் தனிப்பட்ட பண உதவியாளர்"), color = Color.White, fontWeight = FontWeight.Bold)
                Text(tr(language, "Insights • Goals • Spending", "பகுப்பாய்வு • இலக்குகள் • செலவுகள்"), color = Color.White.copy(.62f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(.75f))
        }
    }
}

@Composable
private fun GoalOverviewCard(
    savingsTarget: Double,
    savingsSaved: Double,
    investmentTarget: Double,
    investmentSaved: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Violet.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Flag, null, tint = Violet)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Goals", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(
                        "Your progress, separate from cash balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (savingsTarget > 0) {
                GoalProgressRow(
                    title = "Savings",
                    saved = savingsSaved,
                    target = savingsTarget,
                    icon = Icons.Default.Savings
                )
            }

            if (investmentTarget > 0) {
                GoalProgressRow(
                    title = "Investment",
                    saved = investmentSaved,
                    target = investmentTarget,
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
private fun GoalProgressRow(
    title: String,
    saved: Double,
    target: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val progress = if (target > 0) (saved / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val remaining = (target - saved).coerceAtLeast(0.0)

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Violet, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                "${(progress * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = Violet
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp))
        )
        Row {
            Text(
                "${money(saved)} / ${money(target)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (remaining > 0) "${money(remaining)} left" else "Completed ✓",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else Violet
            )
        }
    }
}

@Composable
private fun BalanceHero(balance: Double, income: Double, expense: Double, language: AppLanguage) {
    val transition = rememberInfiniteTransition(label = "balanceWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase"
    )
    Box(
        Modifier.fillMaxWidth().heightIn(min = 245.dp).clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary)))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val wave = Path()
            val base = size.height * .73f
            wave.moveTo(0f, base)
            for (x in 0..size.width.toInt() step 8) {
                val y = base + sin(x / size.width * Math.PI.toFloat() * 2f + phase) * 15f +
                    sin(x / size.width * Math.PI.toFloat() * 4f - phase * .7f) * 7f
                wave.lineTo(x.toFloat(), y.toFloat())
            }
            wave.lineTo(size.width, size.height)
            wave.lineTo(0f, size.height)
            wave.close()
            drawPath(wave, Brush.linearGradient(listOf(Color.White.copy(.06f), Color.White.copy(.16f))))
            drawCircle(Gold.copy(.12f), radius = 110.dp.toPx(), center = Offset(size.width * .9f, 35.dp.toPx()))
        }
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = .10f)) {
                    Text(tr(language, "TOTAL BALANCE", "மொத்த இருப்பு"), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White.copy(.80f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("AI", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.2.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(money(balance), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (balance >= 0) tr(language, "You're on track this period", "இந்த காலத்தில் நீங்கள் சரியான பாதையில் இருக்கிறீர்கள்") else tr(language, "Review your spending", "உங்கள் செலவுகளை சரிபார்க்கவும்"), color = Color.White.copy(.72f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroMetric(tr(language, "Income", "வரவு"), income, Mint, Modifier.weight(1f))
                HeroMetric(tr(language, "Expenses", "செலவுகள்"), expense, Coral, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, amount: Double, color: Color, modifier: Modifier) {
    Surface(modifier, color = Color.White.copy(.09f), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(7.dp))
                Text(label, color = Color.White.copy(.72f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(money(amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier, color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(color.copy(.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(7.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SpendingOverview(income: Double, expense: Double) {
    val total = (income + expense).coerceAtLeast(1.0)
    val ratio = (expense / total).coerceIn(0.0, 1.0).toFloat()
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Cash flow", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Income vs. expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.TrendingUp, null, tint = Mint)
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFECEEF5))) {
                Box(Modifier.fillMaxWidth(ratio).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Brush.horizontalGradient(listOf(Coral, Violet))))
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Legend("Income", money(income), Mint)
                Legend("Expenses", money(expense), Coral)
            }
        }
    }
}

@Composable
private fun Legend(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEAFE))) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Violet.copy(.14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Savings, null, tint = Violet)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Build your money story", fontWeight = FontWeight.Bold)
                Text("Add your first income or expense to unlock insights.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Violet)
        }
    }
}

@Composable
private fun EntryRow(entry: MoneyEntry, onClick: (MoneyEntry) -> Unit = {}) {
    val isIncome = entry.type == EntryType.INCOME
    val isTransfer = entry.type == EntryType.TRANSFER
    val accent = when { isIncome -> Mint; isTransfer -> Violet; else -> Coral }
    val context = LocalContext.current
    val bitmap = remember(entry.photoUri) {
        entry.photoUri?.let {
            runCatching { context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }.getOrNull()
        }
    }
    Card(onClick = { onClick(entry) }, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = "Receipt photo", modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            } else {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(.11f)), contentAlignment = Alignment.Center) {
                    Icon(if (isIncome) Icons.Default.ArrowDownward else if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.ArrowUpward, null, tint = accent)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${entry.category} • ${entry.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDateTime(entry.time), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text((if (isIncome) "+" else if (isTransfer) "↔" else "-") + money(entry.amount), fontWeight = FontWeight.ExtraBold, color = if (isIncome) Color(0xFF159B74) else if (isTransfer) Violet else Ink)
                if (entry.photoUri != null) {
                    Text("Receipt attached", fontSize = 10.sp, color = Violet, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReportsScreen(entries: List<MoneyEntry>, language: AppLanguage, onEntryClick: (MoneyEntry) -> Unit) {
    var mode by remember { mutableStateOf("Month") }
    var selectedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val filtered = filteredEntries(entries, mode, selectedTime)
    val income = filtered.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val expense = filtered.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 110.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr(language, "Insights", "பகுப்பாய்வு"), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Text(tr(language, "Filter, review and export your money story", "உங்கள் பண விவரங்களை வடிகட்டி பார்க்கவும், PDF ஆக சேமிக்கவும்"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { savePdfToDownloads(context, filteredEntries(entries, mode, selectedTime), mode) }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("PDF") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Day", "Month", "All").forEach { option ->
                    FilterChip(selected = mode == option, onClick = { mode = option }, label = { Text(option) })
                }
                if (mode != "All") {
                    OutlinedButton(onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedTime }
                        DatePickerDialog(context, { _, y, m, d ->
                            selectedTime = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }.timeInMillis
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Text(if (mode == "Day") "📅 ${dayLabel(selectedTime)}" else "🗓️ ${monthLabel(selectedTime)}", maxLines = 1)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Income", money(income), Mint, Modifier.weight(1f))
                StatCard("Expense", money(expense), Coral, Modifier.weight(1f))
            }
        }
        item { ReportCard("Net balance", balance, Violet) }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Midnight)) {
                Column(Modifier.padding(22.dp)) {
                    Text("Financial pulse", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${filtered.size} transaction${if (filtered.size == 1) "" else "s"} in this view", color = Color.White.copy(.6f), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(18.dp)); MiniChart()
                }
            }
        }
        filtered.sortedByDescending { it.time }.groupBy { dayKey(it.time) }.forEach { (_, list) ->
            item { Text(dayLabel(list.first().time), fontWeight = FontWeight.Bold, color = Violet) }
            items(list, key = { it.id }) { EntryRow(it, onClick = onEntryClick) }
        }
        if (filtered.isEmpty()) item { EmptyState() }
    }
}

@Composable
private fun StatCard(title: String, value: String, accent: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun MiniChart() {
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        val points = listOf(.18f, .42f, .28f, .58f, .48f, .76f, .68f, .92f)
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = size.width * index / (points.lastIndex.toFloat())
            val y = size.height * (1f - value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Gold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, value ->
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(size.width * index / points.lastIndex.toFloat(), size.height * (1f - value)))
        }
    }
}

@Composable
private fun ReportCard(title: String, amount: Double, accent: Color) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(money(amount), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MoreScreen(language: AppLanguage, onExplore: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(tr(language, "More", "மேலும்"), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(tr(language, "Make AINA feel like yours", "AINA-வை உங்களுக்கு பிடித்தபடி அமைக்கவும்"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Midnight)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AINALogo(Modifier.size(46.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AINA PREMIUM", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                        Text("Clarity. Control. Confidence.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Premium reports, smart insights, exports, themes and savings goals.", color = Color.White.copy(.68f))
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onExplore, border = BorderStroke(1.dp, Color.White.copy(.2f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text(tr(language, "Explore", "ஆராயுங்கள்")) }
            }
        }
    }
}

@Composable
private fun ExploreDialog(
    entries: List<MoneyEntry>,
    darkTheme: Boolean,
    themeIndex: Int,
    language: AppLanguage,
    initialActive: String? = null,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onRestore: (List<MoneyEntry>) -> Unit,
    onDismiss: () -> Unit
) {
    var active by remember { mutableStateOf<String?>(initialActive) }
    var askSahaOpen by remember { mutableStateOf(false) }
    var sahaQuestion by remember { mutableStateOf("") }
    val context = LocalContext.current
    var goal by remember { mutableStateOf(loadGoal(context)) }
    var goalSaved by remember { mutableStateOf(loadGoalSaved(context)) }
    var goalText by remember { mutableStateOf(if (goal > 0) goal.toString() else "") }
    var goalSavedText by remember { mutableStateOf(if (goalSaved > 0) goalSaved.toString() else "") }
    var investmentTarget by remember { mutableStateOf(loadInvestmentTarget(context)) }
    var investmentSaved by remember { mutableStateOf(loadInvestmentSaved(context)) }
    var investmentTargetText by remember { mutableStateOf(if (investmentTarget > 0) investmentTarget.toString() else "") }
    var investmentSavedText by remember { mutableStateOf(if (investmentSaved > 0) investmentSaved.toString() else "") }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use { it.write(buildBackupJson(entries).toByteArray()) }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val restored = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()?.let(::parseBackupJson) ?: emptyList() }.getOrDefault(emptyList())
            if (restored.isNotEmpty()) onRestore(restored)
        }
    }
    val income = entries.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val expense = entries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense
    val suggestion = when {
        entries.isEmpty() -> "Start with today's income and expenses. AINA will build your personal money pattern."
        expense > income -> "Your recorded expenses are above income. Review the largest categories and set a daily spending limit."
        goal > 0 && goalSaved >= goal -> "🎉 Your savings goal is reached. You can now create the next milestone."
        investmentTarget > 0 && investmentSaved >= investmentTarget -> "📈 Your investment target is reached. Consider setting your next long-term milestone."
        goal > 0 -> "You have ₹${String.format(Locale.getDefault(), "%,.0f", (goal - goalSaved).coerceAtLeast(0.0))} left on your savings goal. Add actual savings contributions here; your balance is kept separate."
        investmentTarget > 0 -> "You have ₹${String.format(Locale.getDefault(), "%,.0f", (investmentTarget - investmentSaved).coerceAtLeast(0.0))} left on your investment target. Record contributions separately from your cash balance."
        balance >= 0 -> "Your recorded balance is positive. Set separate savings and investment targets to give your surplus a purpose."
        else -> "Your balance is negative for the recorded period. Review expenses before increasing your savings or investment target."
    }
    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(28.dp),
        title = { Column { Text("AINA Premium", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp); Text("Tools that make your money clearer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumAction(tr(language, "Settings", "அமைப்புகள்"), tr(language, "Preferences, security & app controls", "விருப்பங்கள், பாதுகாப்பு மற்றும் கட்டுப்பாடுகள்"), Icons.Default.Settings) { active = "Settings" }
                PremiumAction(tr(language, "App Lock", "ஆப் பூட்டு"), if (isAppLockEnabled(context)) "Fingerprint / device lock enabled" else "Protect AINA with biometrics", Icons.Default.Settings) { active = "App Lock" }
                PremiumAction(tr(language, "Smart Insights", "ஸ்மார்ட் பகுப்பாய்வு"), tr(language, "Personal spending suggestions", "உங்கள் செலவுக்கான பரிந்துரைகள்"), Icons.Default.AutoAwesome) { active = "Smart Insights" }
                PremiumAction(tr(language, "Themes", "தீம்கள்"), "${AINAThemes.size} premium themes • ${AINAThemes[themeIndex].name}", Icons.Default.Palette) { active = "Themes" }
                PremiumAction(tr(language, "Exports", "ஏற்றுமதி"), "CSV or filtered PDF reports", Icons.Default.FileDownload) { active = "Exports" }
                PremiumAction(tr(language, "Savings Goal", "சேமிப்பு இலக்கு"), if (goal > 0) "Target ₹${String.format(Locale.getDefault(), "%,.0f", goal)} • Saved ₹${String.format(Locale.getDefault(), "%,.0f", goalSaved)}" else "Set target + add savings separately", Icons.Default.Flag) { active = "Savings Goal" }
                PremiumAction(tr(language, "Investment Goal", "முதலீட்டு இலக்கு"), if (investmentTarget > 0) "Target ₹${String.format(Locale.getDefault(), "%,.0f", investmentTarget)} • Added ₹${String.format(Locale.getDefault(), "%,.0f", investmentSaved)}" else "Set an investment target separately", Icons.Default.TrendingUp) { active = "Investment Goal" }
                if (active == "Settings") {
                    PremiumInfoCard("Settings", "AINA • powered by AI. Transactions, dates, times and receipt photos stay on this device.")
                    PremiumInfoCard("Developer", "Srinivasan\nsrinimmb@gmail.com")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = soundEnabled(context), onClick = { setSoundEnabled(context, !soundEnabled(context)) }, label = { Text(if (soundEnabled(context)) "🔊 Sounds ON" else "🔇 Sounds OFF") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = vibrationEnabled(context), onClick = { setVibrationEnabled(context, !vibrationEnabled(context)) }, label = { Text(if (vibrationEnabled(context)) "📳 Vibration ON" else "📳 Vibration OFF") }, modifier = Modifier.weight(1f))
                    }
                    val quickAddOn = quickAddEnabled(context)
                    FilterChip(
                        selected = quickAddOn,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                                runCatching {
                                    context.startActivity(Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ))
                                }
                            } else {
                                setQuickAddEnabled(context, !quickAddOn)
                            }
                        },
                        label = { Text(if (quickAddOn) "⚡ Quick Add ON" else "⚡ Quick Add OFF") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Floating Expense / Income controls can be disabled anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(tr(language, "Language", "மொழி"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = language == AppLanguage.ENGLISH, onClick = { onLanguageChange(AppLanguage.ENGLISH) }, label = { Text("English") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = language == AppLanguage.TAMIL, onClick = { onLanguageChange(AppLanguage.TAMIL) }, label = { Text("தமிழ்") }, modifier = Modifier.weight(1f))
                    }
                    PremiumInfoCard("Graphics", "High graphics are enabled by default. Balance wave animation follows your selected theme.")
                    OutlinedButton(onClick = { backupPicker.launch("sahana-backup.json") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Backup data") }
                    OutlinedButton(onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Restore data") }
                }
                if (active == "App Lock") {
                    val enabled = isAppLockEnabled(context)
                    PremiumInfoCard("App Lock", if (enabled) "AINA is protected with fingerprint / device credentials." else "Turn on biometric or device-credential protection for your financial data.")
                    Button(onClick = {
                        if (enabled) {
                            setAppLockEnabled(context, false)
                            active = "App Lock"
                        } else {
                            setAppLockEnabled(context, true)
                            active = "App Lock"
                            (context as? MainActivity)?.enableAndShowBiometricLock()
                        }
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(if (enabled) "Disable App Lock" else "Enable App Lock & Test Now") }
                }
                if (active == "Smart Insights") {
                    PremiumInfoCard("AI • Smart Insights", suggestion)
                    PremiumAction("Ask AI", "Ask about spending, balance, goals or trends", Icons.Default.AutoAwesome) { askSahaOpen = true }
                }
                if (active == "Themes") {
                    PremiumInfoCard("AINA Themes", "Choose a visual identity. The balance wave, cards and controls adapt automatically.")
                    AINAThemes.forEachIndexed { index, preset ->
                        Surface(
                            onClick = { onThemeChange(index) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (index == themeIndex) preset.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (index == themeIndex) BorderStroke(2.dp, preset.primary) else null
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(listOf(preset.primary, preset.secondary))))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(preset.name, fontWeight = FontWeight.Bold)
                                    Text(if (preset.dark) "Premium dark" else "Professional light", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (index == themeIndex) Text("✓", color = preset.primary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    OutlinedButton(onClick = onToggleTheme, modifier = Modifier.fillMaxWidth()) {
                        Text(if (darkTheme) "Use light override" else "Use dark override")
                    }
                }
                if (active == "Exports") PremiumInfoCard("Exports", "Open Insights → choose Day, Month or All → tap PDF to create a shareable PDF. CSV export is also available below.")
                if (active == "Exports") {
                    OutlinedButton(onClick = {
                        val csv = buildCsv(entries); val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "AINA transactions"); putExtra(Intent.EXTRA_TEXT, csv) }; context.startActivity(Intent.createChooser(intent, "Export AINA CSV"))
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Export CSV") }
                }
                if (active == "Savings Goal") {
                    OutlinedTextField(goalText, { goalText = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Savings target ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(goalSavedText, { goalSavedText = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount actually saved ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        goal = goalText.toDoubleOrNull() ?: 0.0
                        goalSaved = goalSavedText.toDoubleOrNull() ?: 0.0
                        saveGoal(context, goal)
                        saveGoalSaved(context, goalSaved)
                        if (goal > 0) { scheduleGoalReminder(context, goal); requestNotificationPermission(context) } else cancelGoalReminder(context)
                        active = "Savings Goal"
                    }, modifier = Modifier.fillMaxWidth()) { Text("Save savings goal") }
                    if (goal > 0) {
                        val pct = ((goalSaved / goal).coerceIn(0.0, 1.0) * 100).toInt()
                        PremiumInfoCard("Savings progress", "₹${String.format(Locale.getDefault(), "%,.0f", goalSaved)} / ₹${String.format(Locale.getDefault(), "%,.0f", goal)} • $pct% • Balance is not used as saved amount.")
                    }
                }
                if (active == "Investment Goal") {
                    OutlinedTextField(investmentTargetText, { investmentTargetText = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Investment target ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(investmentSavedText, { investmentSavedText = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount invested so far ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        investmentTarget = investmentTargetText.toDoubleOrNull() ?: 0.0
                        investmentSaved = investmentSavedText.toDoubleOrNull() ?: 0.0
                        saveInvestmentTarget(context, investmentTarget)
                        saveInvestmentSaved(context, investmentSaved)
                        if (investmentTarget > 0) { scheduleInvestmentReminder(context, investmentTarget); requestNotificationPermission(context) } else cancelInvestmentReminder(context)
                        active = "Investment Goal"
                    }, modifier = Modifier.fillMaxWidth()) { Text("Save investment goal") }
                    if (investmentTarget > 0) {
                        val pct = ((investmentSaved / investmentTarget).coerceIn(0.0, 1.0) * 100).toInt()
                        PremiumInfoCard("Investment progress", "₹${String.format(Locale.getDefault(), "%,.0f", investmentSaved)} / ₹${String.format(Locale.getDefault(), "%,.0f", investmentTarget)} • $pct% • This is separate from AINA's cash balance.")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )

    if (askSahaOpen) {
        val incomeNow = entries.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
        val expenseNow = entries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
        val balanceNow = incomeNow - expenseNow
        val reply = sahaLocalReply(
            sahaQuestion,
            entries,
            incomeNow,
            expenseNow,
            balanceNow,
            goal,
            goalSaved,
            investmentTarget,
            investmentSaved
        )
        AlertDialog(
            onDismissRequest = { askSahaOpen = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("AI", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your private AINA money assistant", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = sahaQuestion,
                        onValueChange = { sahaQuestion = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ask AI") },
                        placeholder = { Text("Where did I spend most?") },
                        singleLine = false,
                        minLines = 2
                    )
                    PremiumInfoCard("AI", reply)
                }
            },
            confirmButton = { TextButton(onClick = { askSahaOpen = false }) { Text("Close") } }
        )
    }
}



private fun sahaLocalReply(
    question: String,
    entries: List<MoneyEntry>,
    income: Double,
    expense: Double,
    balance: Double,
    savingsTarget: Double,
    savingsSaved: Double,
    investmentTarget: Double,
    investmentSaved: Double
): String {
    val q = question.trim().lowercase(Locale.getDefault())
    if (q.isEmpty()) return "Ask me about your spending, income, balance, savings goal, investment goal, or trends. Your data stays on this device."

    val categoryTotals = entries.filter { it.type == EntryType.EXPENSE }
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    fun money(v: Double) = "₹${String.format(Locale.getDefault(), "%,.0f", v)}"

    return when {
        q.contains("most") || q.contains("highest") || q.contains("category") ->
            if (categoryTotals.isEmpty()) "I don't have enough expense data yet. Add a few expenses and ask me again."
            else "Your highest expense category is ${categoryTotals.first().first} at ${money(categoryTotals.first().second)}."

        q.contains("balance") || q.contains("left") ->
            "Your recorded balance is ${money(balance)}. Income is ${money(income)} and expenses are ${money(expense)}."

        q.contains("income") || q.contains("earn") ->
            "Your recorded income is ${money(income)}."

        q.contains("expense") || q.contains("spend") ->
            "Your recorded expenses total ${money(expense)}."

        q.contains("saving") || q.contains("goal") ->
            if (savingsTarget > 0) "Savings goal: ${money(savingsSaved)} saved out of ${money(savingsTarget)}. ${money((savingsTarget - savingsSaved).coerceAtLeast(0.0))} remains."
            else "You don't have a savings target yet. Set one in Savings Goal."

        q.contains("invest") ->
            if (investmentTarget > 0) "Investment goal: ${money(investmentSaved)} added out of ${money(investmentTarget)}. ${money((investmentTarget - investmentSaved).coerceAtLeast(0.0))} remains."
            else "You don't have an investment target yet. Set one in Investment Goal."

        q.contains("trend") || q.contains("month") ->
            "You have ${entries.size} recorded transactions. Income is ${money(income)}, expenses are ${money(expense)}, and the current recorded balance is ${money(balance)}."

        else ->
            "I can help with your balance, income, expenses, top spending category, savings goal, investment goal, and spending trends. Try: “Where did I spend most?”"
    }
}

@Composable
private fun PremiumInfoCard(title: String, message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) { Text(title, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun PremiumAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Violet.copy(.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Violet)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun buildCsv(entries: List<MoneyEntry>): String {
    val header = "Title,Amount,Type,Category,Payment Method,Date,Time,Receipt\n"
    return header + entries.joinToString("\n") { e ->
        val c = Calendar.getInstance().apply { timeInMillis = e.time }
        val date = String.format(Locale.getDefault(), "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH))
        val time = String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        "\"${e.title.replace("\"", "\"\"")}\",${e.amount},${e.type},\"${e.category.replace("\"", "\"\"")}\",$date,$time,${if (e.photoUri != null) "Yes" else "No"}"
    }
}


@Composable
private fun EntryDetailDialog(
    entry: MoneyEntry,
    onDismiss: () -> Unit,
    onEdit: (MoneyEntry) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(entry.photoUri) {
        entry.photoUri?.let { uri ->
            runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use(BitmapFactory::decodeStream) }.getOrNull()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Transaction details", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(entry.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    (if (entry.type == EntryType.INCOME) "+" else if (entry.type == EntryType.TRANSFER) "↔" else "-") + money(entry.amount),
                    fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (entry.type == EntryType.INCOME) Mint else if (entry.type == EntryType.TRANSFER) Violet else Coral
                )
                PremiumInfoCard("Category", entry.category)
                PremiumInfoCard("Account / payment", entry.paymentMethod)
                PremiumInfoCard("Date & time", formatDateTime(entry.time))
                if (bitmap != null) {
                    Image(
                        bitmap.asImageBitmap(),
                        contentDescription = "Receipt",
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(18.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                if (entry.photoUri != null) {
                    Text("Receipt attached", color = Violet, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Delete")
                }
                Button(onClick = { onEdit(entry) }, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Edit")
                }
            }
        }
    )
}

@Composable
private fun EditEntryDialog(
    entry: MoneyEntry,
    onDismiss: () -> Unit,
    onSave: (MoneyEntry) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(entry.title) }
    var amount by remember { mutableStateOf(entry.amount.toString()) }
    var category by remember { mutableStateOf(entry.category) }
    var paymentMethod by remember { mutableStateOf(entry.paymentMethod) }
    var timestamp by remember { mutableLongStateOf(entry.time) }
    var photoUri by remember { mutableStateOf(entry.photoUri) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = persistPhoto(context, uri.toString()) ?: photoUri
    }
    val cal = remember(timestamp) { Calendar.getInstance().apply { timeInMillis = timestamp } }
    val dateLabel = String.format(Locale.getDefault(), "%02d/%02d/%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR))
    val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Edit transaction", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(paymentMethod, { paymentMethod = it }, label = { Text("Account / payment method") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            timestamp = Calendar.getInstance().apply { timeInMillis = timestamp; set(y,m,d) }.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("📅 $dateLabel", fontSize = 12.sp) }
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            timestamp = Calendar.getInstance().apply { timeInMillis = timestamp; set(Calendar.HOUR_OF_DAY,h); set(Calendar.MINUTE,m) }.timeInMillis
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("🕐 $timeLabel", fontSize = 12.sp) }
                }
                OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(if (photoUri == null) "📷 Add / replace receipt" else "✓ Receipt attached")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                onClick = { onSave(entry.copy(title = title.trim(), amount = amount.toDouble(), category = category.ifBlank { "Other" }, paymentMethod = paymentMethod.ifBlank { "Cash" }, time = timestamp, photoUri = photoUri)) }
            ) { Text("Save changes", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryDialog(
    type: EntryType,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, Long, String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var fromAccount by remember { mutableStateOf("Cash") }
    var toAccount by remember { mutableStateOf("Bank") }
    var showPaymentMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }
    val categories = if (type == EntryType.INCOME) listOf("Salary", "Business", "Gift", "Refund", "Other") else listOf("Food", "Travel", "Bills", "Shopping", "Fuel", "Medical", "Entertainment", "Other")
    val paymentMethods = listOf("Cash", "UPI", "Bank", "Card", "Wallet")
    val accounts = listOf("Cash", "Bank", "UPI", "Card", "Wallet")
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    val selectedCalendar = remember(timestamp) { Calendar.getInstance().apply { timeInMillis = timestamp } }
    val dateLabel = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedCalendar.get(Calendar.DAY_OF_MONTH), selectedCalendar.get(Calendar.MONTH) + 1, selectedCalendar.get(Calendar.YEAR))
    val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE))

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(when (type) { EntryType.INCOME -> "Add income"; EntryType.TRANSFER -> "Transfer money"; else -> "Add expense" }, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount ₹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (type == EntryType.TRANSFER) {
                    ExposedDropdownMenuBox(expanded = showFromMenu, onExpandedChange = { showFromMenu = !showFromMenu }) {
                        OutlinedTextField(fromAccount, {}, readOnly = true, label = { Text("From account") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showFromMenu) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = showFromMenu, onDismissRequest = { showFromMenu = false }) {
                            accounts.forEach { a -> DropdownMenuItem(text = { Text(a) }, onClick = { fromAccount = a; showFromMenu = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = showToMenu, onExpandedChange = { showToMenu = !showToMenu }) {
                        OutlinedTextField(toAccount, {}, readOnly = true, label = { Text("To account") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showToMenu) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = showToMenu, onDismissRequest = { showToMenu = false }) {
                            accounts.forEach { a -> DropdownMenuItem(text = { Text(a) }, onClick = { toAccount = a; showToMenu = false }) }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = !showCategoryMenu }) {
                        OutlinedTextField(category, {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoryMenu) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) { categories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; showCategoryMenu = false }) } }
                    }
                    ExposedDropdownMenuBox(expanded = showPaymentMenu, onExpandedChange = { showPaymentMenu = !showPaymentMenu }) {
                        OutlinedTextField(paymentMethod, {}, readOnly = true, label = { Text("Payment method") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showPaymentMenu) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = showPaymentMenu, onDismissRequest = { showPaymentMenu = false }) { paymentMethods.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { paymentMethod = p; showPaymentMenu = false }) } }
                    }
                }

                Text("Transaction details", fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                val c = Calendar.getInstance().apply { timeInMillis = timestamp; set(y, m, d) }
                                timestamp = c.timeInMillis
                            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("📅 $dateLabel", fontSize = 12.sp) }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                val c = Calendar.getInstance().apply { timeInMillis = timestamp; set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
                                timestamp = c.timeInMillis
                            }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("🕐 $timeLabel", fontSize = 12.sp) }
                }

                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (photoUri == null) "📷 Add receipt / photo (optional)" else "✓ Receipt photo attached")
                }
                if (photoUri != null) {
                    val bitmap = remember(photoUri) {
                        runCatching { context.contentResolver.openInputStream(Uri.parse(photoUri!!))?.use(BitmapFactory::decodeStream) }.getOrNull()
                    }
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), contentDescription = "Selected receipt", modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0, onClick = {
                onSave(
                    title.trim(),
                    amount.toDouble(),
                    if (type == EntryType.TRANSFER) "$fromAccount → $toAccount" else category.ifBlank { if (type == EntryType.INCOME) "Income" else "Purchase" },
                    if (type == EntryType.TRANSFER) "Transfer" else paymentMethod,
                    timestamp,
                    photoUri
                )
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


private fun quickAddEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("quick_add_enabled", true)

private fun setQuickAddEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putBoolean("quick_add_enabled", enabled).apply()
    runCatching {
        val intent = Intent(context, QuickAddOverlayService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        } else {
            context.stopService(intent)
        }
    }
}

private fun soundEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("sound_enabled", true)

private fun setSoundEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("sound_enabled", enabled).apply()
}

private fun vibrationEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("vibration_enabled", true)

private fun setVibrationEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("vibration_enabled", enabled).apply()
}

private fun playTransactionFeedback(context: Context, type: EntryType) {
    if (soundEnabled(context)) {
        runCatching {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            tone.startTone(if (type == EntryType.INCOME) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP, 120)
            tone.release()
        }
    }
    if (vibrationEnabled(context)) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(45L)
            }
        }
    }
}

private fun loadTheme(context: Context): Int =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("theme_index", 0).coerceIn(0, AINAThemes.lastIndex)

private fun saveTheme(context: Context, index: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt("theme_index", index).apply()
}

private const val PREFS_NAME = "sahana_storage"
private const val ENTRIES_KEY = "entries_json"

private fun saveEntries(context: Context, entries: List<MoneyEntry>) {
    val array = org.json.JSONArray()
    entries.forEach { entry ->
        val obj = org.json.JSONObject()
            .put("id", entry.id)
            .put("title", entry.title)
            .put("amount", entry.amount)
            .put("type", entry.type.name)
            .put("category", entry.category)
            .put("paymentMethod", entry.paymentMethod)
            .put("time", entry.time)
        if (entry.photoUri != null) obj.put("photoUri", entry.photoUri) else obj.put("photoUri", org.json.JSONObject.NULL)
        array.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(ENTRIES_KEY, array.toString())
        .apply()
}

private fun loadEntries(context: Context): List<MoneyEntry> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(ENTRIES_KEY, null) ?: return emptyList()
    return runCatching {
        val array = org.json.JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    MoneyEntry(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        type = EntryType.valueOf(obj.getString("type")),
                        category = obj.getString("category"),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        time = obj.getLong("time"),
                        photoUri = if (obj.isNull("photoUri")) null else obj.getString("photoUri")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun persistPhoto(context: Context, sourceUri: String): String? {
    return runCatching {
        val source = Uri.parse(sourceUri)
        val dir = File(context.filesDir, "receipts").apply { mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source).use { input ->
            requireNotNull(input) { "Unable to read selected image" }
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    }.getOrNull()
}

private fun buildBackupJson(entries: List<MoneyEntry>): String {
    val array = org.json.JSONArray()
    entries.forEach { e ->
        array.put(org.json.JSONObject().put("id", e.id).put("title", e.title).put("amount", e.amount).put("type", e.type.name).put("category", e.category).put("paymentMethod", e.paymentMethod).put("time", e.time).put("photoUri", e.photoUri ?: org.json.JSONObject.NULL))
    }
    return org.json.JSONObject().put("app", "AINA").put("version", 2).put("entries", array).toString(2)
}

private fun parseBackupJson(raw: String): List<MoneyEntry> {
    val array = org.json.JSONObject(raw).getJSONArray("entries")
    return buildList { for (i in 0 until array.length()) {
        val o = array.getJSONObject(i)
        add(MoneyEntry(o.getLong("id"), o.getString("title"), o.getDouble("amount"), EntryType.valueOf(o.getString("type")), o.getString("category"), o.optString("paymentMethod", "Cash"), o.getLong("time"), if (o.isNull("photoUri")) null else o.getString("photoUri")))
    } }
}

private fun dayKey(timestamp: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestamp }
    return String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

private fun dayLabel(timestamp: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        dayKey(timestamp) == dayKey(today.timeInMillis) -> "Today"
        dayKey(timestamp) == dayKey(yesterday.timeInMillis) -> "Yesterday"
        else -> String.format(Locale.getDefault(), "%02d %s %04d", c.get(Calendar.DAY_OF_MONTH), c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()), c.get(Calendar.YEAR))
    }
}

private fun monthLabel(timestamp: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestamp }
    return String.format(Locale.getDefault(), "%s %04d", c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), c.get(Calendar.YEAR))
}

private fun filteredEntries(entries: List<MoneyEntry>, mode: String, selectedTime: Long): List<MoneyEntry> {
    if (mode == "All") return entries
    val selected = Calendar.getInstance().apply { timeInMillis = selectedTime }
    return entries.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.time }
        if (mode == "Day") {
            c.get(Calendar.YEAR) == selected.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR)
        } else {
            c.get(Calendar.YEAR) == selected.get(Calendar.YEAR) && c.get(Calendar.MONTH) == selected.get(Calendar.MONTH)
        }
    }
}

private fun isAppLockEnabled(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("app_lock", false)
private fun setAppLockEnabled(context: Context, enabled: Boolean) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("app_lock", enabled).apply() }

private fun loadGoal(context: Context): Double = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("goal", "0")?.toDoubleOrNull() ?: 0.0
private fun saveGoal(context: Context, goal: Double) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString("goal", goal.toString()).apply() }
private fun loadGoalSaved(context: Context): Double = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("goal_saved", "0")?.toDoubleOrNull() ?: 0.0
private fun saveGoalSaved(context: Context, amount: Double) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString("goal_saved", amount.toString()).apply() }
private fun loadInvestmentTarget(context: Context): Double = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("investment_target", "0")?.toDoubleOrNull() ?: 0.0
private fun saveInvestmentTarget(context: Context, amount: Double) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString("investment_target", amount.toString()).apply() }
private fun loadInvestmentSaved(context: Context): Double = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("investment_saved", "0")?.toDoubleOrNull() ?: 0.0
private fun saveInvestmentSaved(context: Context, amount: Double) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString("investment_saved", amount.toString()).apply() }

private const val GOAL_CHANNEL = "sahana_goal"
private const val GOAL_REQUEST = 7744
private const val INVESTMENT_REQUEST = 7745

private fun requestNotificationPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= 33 && context is Activity && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        context.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7745)
    }
}

private fun scheduleGoalReminder(context: Context, goal: Double) {
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, GoalReminderReceiver::class.java).putExtra("goal", goal)
    val pending = PendingIntent.getBroadcast(context, GOAL_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 21); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1) }
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
}

private fun scheduleInvestmentReminder(context: Context, target: Double) {
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, GoalReminderReceiver::class.java).putExtra("investment", true).putExtra("goal", target)
    val pending = PendingIntent.getBroadcast(context, INVESTMENT_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 0); if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1) }
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
}

private fun cancelInvestmentReminder(context: Context) {
    val intent = Intent(context, GoalReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(context, INVESTMENT_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
}

private fun cancelGoalReminder(context: Context) {
    val intent = Intent(context, GoalReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(context, GOAL_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
}

class GoalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val goal = intent?.getDoubleExtra("goal", loadGoal(context)) ?: loadGoal(context)
        val investment = intent?.getBooleanExtra("investment", false) ?: false
        if (goal <= 0) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(GOAL_CHANNEL, "SAHANA Savings Goals", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = Notification.Builder(context, GOAL_CHANNEL)
            .setSmallIcon(com.sahana.expense.R.drawable.sahana_launcher_logo)
            .setContentTitle(if (investment) "SAHANA • Investment goal" else "SAHANA • Savings goal")
            .setContentText(if (investment) "Investment target ₹${String.format(Locale.getDefault(), "%,.0f", goal)}. Update your invested amount separately from your cash balance." else "Savings target ₹${String.format(Locale.getDefault(), "%,.0f", goal)}. Update your saved amount separately from your cash balance.")
            .setAutoCancel(true)
            .build()
        manager.notify(GOAL_REQUEST, notification)
    }
}

private fun savePdfToDownloads(context: Context, entries: List<MoneyEntry>, mode: String) {
    try {
        val fileName = "veera-${mode.lowercase(Locale.getDefault())}-report-${System.currentTimeMillis()}.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AINA")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Unable to create PDF file")
            try {
                writePdf(context, uri, entries)
                val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(uri, done, null, null)
                Toast.makeText(context, "PDF saved in Downloads/AINA", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AINA")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            writePdf(context, Uri.fromFile(file), entries)
            Toast.makeText(context, "PDF saved in AINA Documents", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "PDF export failed: ${e.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
    }
}

private fun writePdf(context: Context, uri: Uri, entries: List<MoneyEntry>) {
    var document: PdfDocument? = null
    try {
        document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(22, 26, 43)
            textSize = 22f
            isFakeBoldText = true
        }

        canvas.drawText("AINA - Financial Report", margin, 50f, paint)
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Generated ${formatDateTime(System.currentTimeMillis())}", margin, 70f, paint)

        var y = 102f
        var pageNumber = 1
        val rows = entries.sortedByDescending { it.time }.take(500)

        rows.forEach { entry ->
            if (y > 805f) {
                document!!.finishPage(page)
                pageNumber++
                val nextInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(nextInfo)
                canvas = page.canvas
                y = 50f
            }
            val line = "${formatDateTime(entry.time)} | ${entry.type} | ${entry.title.take(28)} | Rs.${String.format(Locale.getDefault(), "%,.2f", entry.amount)}"
            canvas.drawText(line, margin, y, paint)
            y += 16f
        }

        document.finishPage(page)
        if (uri.scheme == "file") {
            FileOutputStream(File(uri.path!!)).use { output -> document.writeTo(output) }
        } else {
            context.contentResolver.openOutputStream(uri)?.use { output -> document.writeTo(output) }
                ?: throw IllegalStateException("Unable to open the selected file")
        }
    } finally {
        runCatching { document?.close() }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = timestamp }
    val date = String.format(Locale.getDefault(), "%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR))
    val time = String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    return "$date • $time"
}

private fun money(value: Double): String = "₹" + String.format(Locale.getDefault(), "%,.2f", value)


/**
 * Persistent quick-add rail.
 *
 * It stays on the screen after AINA is closed so a user can quickly start
 * an Expense or Income entry from anywhere. The service only shows two
 * small buttons on the right edge and launches AINA when tapped.
 */
class QuickAddOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var rail: View? = null

    override fun onCreate() {
        super.onCreate()
        if (!quickAddEnabled(this)) {
            stopSelf()
            return
        }
        createChannel()
        val notification = Notification.Builder(this, "sahana_quick_add")
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setContentTitle("AINA Quick Add")
            .setContentText("Quickly add an expense or income")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(4101, notification)
        }
        showRail()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sahana_quick_add",
                "AINA Quick Add",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Quick add controls for AINA"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun button(text: String, color: Int, type: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(android.graphics.Color.WHITE)
            textSize = 25f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(color)
                shape = GradientDrawable.OVAL
            }
            elevation = 8f
            setPadding(0, 0, 0, 0)
            contentDescription = if (type == "EXPENSE") "Add expense" else "Add income"
            setOnClickListener {
                val intent = Intent(this@QuickAddOverlayService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("quick_add_type", type)
                }
                startActivity(intent)
            }
        }
    }

    private fun showRail() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !android.provider.Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(5, 7, 5, 7)
            background = GradientDrawable().apply {
                setColor(0xF20D1222.toInt())
                cornerRadius = 34f
                setStroke(1, 0x55FFFFFF)
            }
            elevation = 18f
        }

        val expense = button("−", 0xFFE84C62.toInt(), "EXPENSE")
        val income = button("+", 0xFF19C98A.toInt(), "INCOME")
        container.addView(expense, LinearLayout.LayoutParams(46, 46).apply { bottomMargin = 7 })
        container.addView(income, LinearLayout.LayoutParams(46, 46))

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 4
            y = 0
        }

        rail = container
        try {
            windowManager?.addView(container, params)
            // Subtle premium breathing animation.
            container.animate()
                .alpha(0.94f)
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(900)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction {
                    container.animate()
                        .alpha(0.88f)
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(900)
                        .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                        .start()
                }.start()
        } catch (_: Exception) {
            rail = null
        }
    }

    override fun onDestroy() {
        try {
            rail?.let { windowManager?.removeView(it) }
        } catch (_: Exception) { }
        rail = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
