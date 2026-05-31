package com.serah.hustlescore.ui.screens.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.data.NotificationHelper
import com.serah.hustlescore.models.UserProfile
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ─── County List ─────────────────────────────────────────────────────────────
val KenyaCounties = listOf(
    "Nairobi","Mombasa","Kwale","Kilifi","Tana River","Lamu","Taita Taveta",
    "Garissa","Wajir","Mandera","Marsabit","Isiolo","Meru","Tharaka Nithi",
    "Embu","Kitui","Machakos","Makueni","Nyandarua","Nyeri","Kirinyaga",
    "Murang'a","Kiambu","Turkana","West Pokot","Samburu","Trans Nzoia",
    "Uasin Gishu","Elgeyo Marakwet","Nandi","Baringo","Laikipia","Nakuru",
    "Narok","Kajiado","Kericho","Bomet","Kakamega","Vihiga","Bungoma",
    "Busia","Siaya","Kisumu","Homa Bay","Migori","Kisii","Nyamira",
    "Nandi Hills","Nyahururu"
)

// ─── ViewModel (unchanged) ────────────────────────────────────────────────────
class UserDetailFormViewModel : ViewModel() {
    private val db   = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    var fullName       by mutableStateOf("")
    var phone          by mutableStateOf("")
    var idNumber       by mutableStateOf("")
    var selectedCounty by mutableStateOf("")
    var occupation     by mutableStateOf("")
    var employer       by mutableStateOf("")
    var monthlyIncome  by mutableStateOf("")
    var bio            by mutableStateOf("")

    var fullNameError   by mutableStateOf<String?>(null)
    var phoneError      by mutableStateOf<String?>(null)
    var idNumberError   by mutableStateOf<String?>(null)
    var countyError     by mutableStateOf<String?>(null)
    var occupationError by mutableStateOf<String?>(null)
    var incomeError     by mutableStateOf<String?>(null)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    sealed class SaveState {
        object Idle    : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    init { loadExistingProfile() }

    private fun loadExistingProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.child("users").child(uid).child("profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val p = snapshot.getValue(UserProfile::class.java) ?: return
                    fullName       = p.fullName
                    phone          = p.phone
                    idNumber       = p.idNumber
                    selectedCounty = p.county
                    occupation     = p.occupation
                    employer       = p.employer
                    monthlyIncome  = p.monthlyIncome
                    bio            = p.bio
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validate(): Boolean {
        var valid = true
        fullNameError   = if (fullName.trim().length < 3)                                          { valid = false; "Enter your full name"        } else null
        phoneError      = if (!phone.matches(Regex("^[0-9]{10,12}$")))                            { valid = false; "Enter a valid phone number"   } else null
        idNumberError   = if (idNumber.trim().length < 6)                                         { valid = false; "Enter a valid ID number"      } else null
        countyError     = if (selectedCounty.isBlank())                                            { valid = false; "Please select a county"       } else null
        occupationError = if (occupation.trim().isBlank())                                         { valid = false; "Enter your occupation"        } else null
        incomeError     = if (monthlyIncome.trim().isBlank() || monthlyIncome.toDoubleOrNull() == null) { valid = false; "Enter a valid income amount" } else null
        return valid
    }

    fun submitProfile() {
        if (!validate()) return
        val uid = auth.currentUser?.uid ?: run { _saveState.value = SaveState.Error("User not authenticated"); return }
        _saveState.value = SaveState.Loading
        val profileData = mapOf(
            "uid"             to uid,
            "fullName"        to fullName.trim(),
            "email"           to (auth.currentUser?.email ?: ""),
            "phone"           to phone.trim(),
            "idNumber"        to idNumber.trim(),
            "county"          to selectedCounty,
            "occupation"      to occupation.trim(),
            "employer"        to employer.trim(),
            "monthlyIncome"   to monthlyIncome.trim(),
            "bio"             to bio.trim(),
            "profileComplete" to true
        )
        db.child("Users").child(uid).child("profile")
            .setValue(profileData)
            .addOnSuccessListener {
                _saveState.value = SaveState.Success
                val uid2 = auth.currentUser?.uid ?: return@addOnSuccessListener
                NotificationHelper.profileCompleted(uid2)
            }
            .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Save failed") }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailFormScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    formViewModel: UserDetailFormViewModel = viewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val saveState  by formViewModel.saveState.collectAsState()

    // ── Theme-aware colours ───────────────────────────────────────────────────
    val pageBg          = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F9F5)
    val cardBg          = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val focusedFieldBg  = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFF1F8F1)
    val unfocusedFieldBg= if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val borderDefault   = if (isDarkMode) Color(0xFF2D6A3F) else Color(0xFFC8E6C9)
    val borderFocus     = Color(0xFF4CAF50)
    val brandGreen      = Color(0xFF2E7D32)
    val brandGreenLight = Color(0xFF4CAF50)
    val primaryText     = if (isDarkMode) Color.White       else Color(0xFF1B4332)
    val labelText       = if (isDarkMode) Color(0xFF81C784) else Color(0xFF558B5E)
    val placeholderText = if (isDarkMode) Color(0xFF4A6B4D) else Color(0xFFA5C8A8)
    val mutedText       = if (isDarkMode) Color(0xFF4A6B4D) else Color(0xFF81C784)
    val iconTint        = if (isDarkMode) Color(0xFF4A7C59) else Color(0xFF81C784)
    val errorRed        = Color(0xFFD32F2F)
    val heroBg          = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFE8F5E9)
    val sectionIconBg   = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFE8F5E9)
    val successBg       = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFF1F8F1)
    val successBorder   = if (isDarkMode) Color(0xFF2D6A3F) else Color(0xFFA5D6A7)
    val topBarBg        = if (isDarkMode) Color(0xFF1A1A1A) else Color.White

    // Field colours builder
    @Composable
    fun fieldColors(hasError: Boolean = false) = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor      = if (hasError) errorRed else borderDefault,
        focusedBorderColor        = if (hasError) errorRed else borderFocus,
        errorBorderColor          = errorRed,
        unfocusedTextColor        = primaryText,
        focusedTextColor          = primaryText,
        cursorColor               = brandGreen,
        unfocusedContainerColor   = unfocusedFieldBg,
        focusedContainerColor     = focusedFieldBg,
        errorContainerColor       = if (isDarkMode) Color(0xFF2E1A1A) else Color(0xFFFFF0F0),
        focusedLeadingIconColor   = brandGreen,
        unfocusedLeadingIconColor = iconTint,
        errorLeadingIconColor     = errorRed,
        focusedTrailingIconColor  = brandGreenLight,
        unfocusedTrailingIconColor= iconTint
    )

    var countyDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        if (saveState is UserDetailFormViewModel.SaveState.Success) {
            delay(1200)
            formViewModel.resetSaveState()
            navController.navigate(Routes.UserProfile.route) {
                popUpTo(Routes.UserDetailForm.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, tint = brandGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero card ──────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(cardBg), border = BorderStroke(0.5.dp, borderDefault)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(heroBg), Alignment.Center) {
                        Icon(Icons.Default.AccountCircle, null, tint = brandGreen, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("Build Your Profile", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.height(3.dp))
                        Text("Your information helps us calculate your HustleScore.",
                            color = brandGreenLight, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            // ── Personal Details ───────────────────────────────────────────
            FormSectionHeader("Personal Details", Icons.Default.Person, primaryText, sectionIconBg, brandGreen, borderDefault)

            DetailFormField(formViewModel.fullName, { formViewModel.fullName = it },
                "Full Name", "e.g Jane Wanjiku", Icons.Default.Badge, labelText, placeholderText,
                fieldColors(formViewModel.fullNameError != null), formViewModel.fullNameError, errorRed)

            DetailFormField(formViewModel.phone, { formViewModel.phone = it },
                "Phone Number", "e.g 0712345678", Icons.Default.Phone, labelText, placeholderText,
                fieldColors(formViewModel.phoneError != null), formViewModel.phoneError, errorRed,
                keyboardType = KeyboardType.Phone)

            DetailFormField(formViewModel.idNumber, { formViewModel.idNumber = it },
                "National ID Number", "e.g 12345678", Icons.Default.CreditCard, labelText, placeholderText,
                fieldColors(formViewModel.idNumberError != null), formViewModel.idNumberError, errorRed,
                keyboardType = KeyboardType.Number)

            // County Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("County", color = labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                ExposedDropdownMenuBox(expanded = countyDropdownExpanded, onExpandedChange = { countyDropdownExpanded = it }) {
                    OutlinedTextField(
                        value = formViewModel.selectedCounty.ifBlank { "Select your county" },
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countyDropdownExpanded) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null,
                                tint = if (formViewModel.countyError != null) errorRed else iconTint)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        isError = formViewModel.countyError != null,
                        colors = fieldColors(formViewModel.countyError != null)
                    )
                    ExposedDropdownMenu(expanded = countyDropdownExpanded,
                        onDismissRequest = { countyDropdownExpanded = false },
                        modifier = Modifier.background(if (isDarkMode) Color(0xFF1E1E1E) else Color.White).heightIn(max = 260.dp)) {
                        KenyaCounties.forEach { county ->
                            DropdownMenuItem(
                                text = { Text(county, color = primaryText, fontSize = 14.sp) },
                                onClick = {
                                    formViewModel.selectedCounty = county
                                    formViewModel.countyError    = null
                                    countyDropdownExpanded        = false
                                }
                            )
                        }
                    }
                }
                formViewModel.countyError?.let { Text(it, color = errorRed, fontSize = 11.sp) }
            }

            // ── Financial Information ──────────────────────────────────────
            FormSectionHeader("Financial Information", Icons.Default.AccountBalance, primaryText, sectionIconBg, brandGreen, borderDefault)

            DetailFormField(formViewModel.occupation, { formViewModel.occupation = it },
                "Occupation", "e.g Software Engineer", Icons.Default.Work, labelText, placeholderText,
                fieldColors(formViewModel.occupationError != null), formViewModel.occupationError, errorRed)

            DetailFormField(formViewModel.employer, { formViewModel.employer = it },
                "Employer / Business Name", "e.g Safaricom PLC", Icons.Default.Business,
                labelText, placeholderText, fieldColors())

            DetailFormField(formViewModel.monthlyIncome, { formViewModel.monthlyIncome = it },
                "Monthly Income (KES)", "e.g 45000", Icons.Default.AttachMoney, labelText, placeholderText,
                fieldColors(formViewModel.incomeError != null), formViewModel.incomeError, errorRed,
                keyboardType = KeyboardType.Number)

            // ── About You ─────────────────────────────────────────────────
            FormSectionHeader("About You (Optional)", Icons.Default.Info, primaryText, sectionIconBg, brandGreen, borderDefault)

            OutlinedTextField(
                value = formViewModel.bio,
                onValueChange = { if (it.length <= 300) formViewModel.bio = it },
                placeholder = { Text("Tell us a little about yourself...", color = placeholderText) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(14.dp), maxLines = 5,
                supportingText = {
                    Text("${formViewModel.bio.length}/300", color = mutedText,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp)
                },
                colors = fieldColors()
            )

            Spacer(Modifier.height(4.dp))

            // ── Save / Loading / Success / Error ──────────────────────────
            AnimatedContent(targetState = saveState, label = "save_btn") { state ->
                when (state) {
                    is UserDetailFormViewModel.SaveState.Loading -> {
                        Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            CircularProgressIndicator(color = brandGreen)
                        }
                    }
                    is UserDetailFormViewModel.SaveState.Success -> {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(successBg),
                            border = BorderStroke(0.5.dp, successBorder)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.CheckCircle, null, tint = brandGreen)
                                Spacer(Modifier.width(8.dp))
                                Text("Profile saved successfully!", color = brandGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is UserDetailFormViewModel.SaveState.Error -> {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(if (isDarkMode) Color(0xFF2E1A1A) else Color(0xFFFFF0F0)),
                            border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF6A2D2D) else Color(0xFFFFCDD2))) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = errorRed)
                                Spacer(Modifier.width(8.dp))
                                Text(state.message, color = errorRed)
                            }
                        }
                    }
                    else -> {
                        Button(onClick = { formViewModel.submitProfile() },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandGreen, contentColor = Color.White)) {
                            Icon(Icons.Default.Save, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun FormSectionHeader(
    title: String, icon: ImageVector,
    primaryText: Color, sectionIconBg: Color, brandGreen: Color, borderDefault: Color
) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(sectionIconBg), Alignment.Center) {
            Icon(icon, null, tint = brandGreen, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(Modifier.weight(1f), color = borderDefault)
    }
}

// ─── Reusable form field ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    labelText: Color,
    placeholderText: Color,
    colors: TextFieldColors,
    error: String? = null,
    errorRed: Color = Color(0xFFD32F2F),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = placeholderText) },
            leadingIcon = { Icon(icon, null, tint = if (error != null) errorRed else Color(0xFF81C784)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = errorRed, fontSize = 11.sp) } },
            colors = colors
        )
    }
}