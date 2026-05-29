package com.serah.hustlescore.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.data.NotificationHelper
import com.serah.hustlescore.models.UserProfile
import com.serah.hustlescore.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ─── Color Palette ────────────────────────────────────────────────────────────

private val PageBg         = Color(0xFFF5F9F5)   // soft sage page background
private val SurfaceWhite   = Color(0xFFFFFFFF)   // card / field surface
private val SurfaceFocus   = Color(0xFFF1F8F1)   // focused field surface
private val BorderDefault  = Color(0xFFC8E6C9)   // subtle green border
private val BorderFocus    = Color(0xFF4CAF50)   // vivid green on focus
private val BrandGreen     = Color(0xFF2E7D32)   // primary action / icon
private val BrandGreenLight= Color(0xFF4CAF50)   // accent / focused icon
private val TextPrimary    = Color(0xFF1B4332)   // headings & input text
private val TextLabel      = Color(0xFF558B5E)   // field labels
private val TextPlaceholder= Color(0xFFA5C8A8)   // placeholder text
private val TextMuted      = Color(0xFF81C784)   // char counter / muted
private val IconTint       = Color(0xFF81C784)   // unfocused leading icons
private val ErrorRed       = Color(0xFFD32F2F)   // validation errors
private val HeroBg         = Color(0xFFE8F5E9)   // hero icon background
private val SectionIconBg  = Color(0xFFE8F5E9)   // section header icon bg
private val SuccessBg      = Color(0xFFF1F8F1)   // success banner bg
private val SuccessBorder  = Color(0xFFA5D6A7)   // success banner border

// ─── County List ─────────────────────────────────────────────────────────────

val KenyaCounties = listOf(
    "Nairobi", "Mombasa", "Kwale", "Kilifi", "Tana River", "Lamu", "Taita Taveta",
    "Garissa", "Wajir", "Mandera", "Marsabit", "Isiolo", "Meru", "Tharaka Nithi",
    "Embu", "Kitui", "Machakos", "Makueni", "Nyandarua", "Nyeri", "Kirinyaga",
    "Murang'a", "Kiambu", "Turkana", "West Pokot", "Samburu", "Trans Nzoia",
    "Uasin Gishu", "Elgeyo Marakwet", "Nandi", "Baringo", "Laikipia", "Nakuru",
    "Narok", "Kajiado", "Kericho", "Bomet", "Kakamega", "Vihiga", "Bungoma",
    "Busia", "Siaya", "Kisumu", "Homa Bay", "Migori", "Kisii", "Nyamira",
    "Nandi Hills", "Nyahururu"
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

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

    var fullNameError  by mutableStateOf<String?>(null)
    var phoneError     by mutableStateOf<String?>(null)
    var idNumberError  by mutableStateOf<String?>(null)
    var countyError    by mutableStateOf<String?>(null)
    var occupationError by mutableStateOf<String?>(null)
    var incomeError    by mutableStateOf<String?>(null)

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
        fullNameError = if (fullName.trim().length < 3) { valid = false; "Enter your full name" } else null
        phoneError    = if (!phone.matches(Regex("^[0-9]{10,12}$"))) { valid = false; "Enter a valid phone number" } else null
        idNumberError = if (idNumber.trim().length < 6) { valid = false; "Enter a valid ID number" } else null
        countyError   = if (selectedCounty.isBlank()) { valid = false; "Please select a county" } else null
        occupationError = if (occupation.trim().isBlank()) { valid = false; "Enter your occupation" } else null
        incomeError   = if (monthlyIncome.trim().isBlank() || monthlyIncome.toDoubleOrNull() == null) { valid = false; "Enter a valid income amount" } else null
        return valid
    }

    fun submitProfile() {
        if (!validate()) return
        val uid = auth.currentUser?.uid ?: run {
            _saveState.value = SaveState.Error("User not authenticated")
            return
        }
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
        db.child("Users").child(uid).child("profile")   // ✅ Fixed: capital U
            .setValue(profileData)
            .addOnSuccessListener {
                _saveState.value = SaveState.Success

                // ✅ Send profile completed notification
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                NotificationHelper.profileCompleted(uid)
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
    formViewModel: UserDetailFormViewModel = viewModel()
) {
    val saveState by formViewModel.saveState.collectAsState()
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
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Complete Your Profile",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = null,
                            tint = BrandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero card ──────────────────────────────────────────────────

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(0.5.dp, BorderDefault)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HeroBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            "Build Your Profile",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Your information helps us calculate your HustleScore.",
                            color = BrandGreenLight,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Personal Details ───────────────────────────────────────────

            FormSectionHeader("Personal Details", Icons.Default.Person)

            FormField(
                value = formViewModel.fullName,
                onValueChange = { formViewModel.fullName = it },
                label = "Full Name",
                placeholder = "e.g Jane Wanjiku",
                icon = Icons.Default.Badge,
                error = formViewModel.fullNameError
            )

            FormField(
                value = formViewModel.phone,
                onValueChange = { formViewModel.phone = it },
                label = "Phone Number",
                placeholder = "e.g 0712345678",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                error = formViewModel.phoneError
            )

            FormField(
                value = formViewModel.idNumber,
                onValueChange = { formViewModel.idNumber = it },
                label = "National ID Number",
                placeholder = "e.g 12345678",
                icon = Icons.Default.CreditCard,
                keyboardType = KeyboardType.Number,
                error = formViewModel.idNumberError
            )

            // ── County dropdown ────────────────────────────────────────────

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                Text(
                    "County",
                    color = TextLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                ExposedDropdownMenuBox(
                    expanded = countyDropdownExpanded,
                    onExpandedChange = { countyDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formViewModel.selectedCounty.ifBlank { "Select your county" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = countyDropdownExpanded
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = if (formViewModel.countyError != null) ErrorRed else IconTint
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        isError = formViewModel.countyError != null,
                        colors = lightFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = countyDropdownExpanded,
                        onDismissRequest = { countyDropdownExpanded = false },
                        modifier = Modifier
                            .background(SurfaceWhite)
                            .heightIn(max = 260.dp)
                    ) {
                        KenyaCounties.forEach { county ->
                            DropdownMenuItem(
                                text = { Text(county, color = TextPrimary, fontSize = 14.sp) },
                                onClick = {
                                    formViewModel.selectedCounty = county
                                    formViewModel.countyError    = null
                                    countyDropdownExpanded        = false
                                }
                            )
                        }
                    }
                }

                formViewModel.countyError?.let {
                    Text(it, color = ErrorRed, fontSize = 11.sp)
                }
            }

            // ── Financial Information ──────────────────────────────────────

            FormSectionHeader("Financial Information", Icons.Default.AccountBalance)

            FormField(
                value = formViewModel.occupation,
                onValueChange = { formViewModel.occupation = it },
                label = "Occupation",
                placeholder = "e.g Software Engineer",
                icon = Icons.Default.Work,
                error = formViewModel.occupationError
            )

            FormField(
                value = formViewModel.employer,
                onValueChange = { formViewModel.employer = it },
                label = "Employer / Business Name",
                placeholder = "e.g Safaricom PLC",
                icon = Icons.Default.Business
            )

            FormField(
                value = formViewModel.monthlyIncome,
                onValueChange = { formViewModel.monthlyIncome = it },
                label = "Monthly Income (KES)",
                placeholder = "e.g 45000",
                icon = Icons.Default.AttachMoney,
                keyboardType = KeyboardType.Number,
                error = formViewModel.incomeError
            )

            // ── About You ─────────────────────────────────────────────────

            FormSectionHeader("About You (Optional)", Icons.Default.Info)

            OutlinedTextField(
                value = formViewModel.bio,
                onValueChange = { if (it.length <= 300) formViewModel.bio = it },
                placeholder = {
                    Text("Tell us a little about yourself...", color = TextPlaceholder)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5,
                supportingText = {
                    Text(
                        "${formViewModel.bio.length}/300",
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp
                    )
                },
                colors = lightFieldColors()
            )

            Spacer(Modifier.height(4.dp))

            // ── Save / Loading / Success / Error ──────────────────────────

            AnimatedContent(targetState = saveState, label = "save_btn") { state ->

                when (state) {

                    is UserDetailFormViewModel.SaveState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandGreen)
                        }
                    }

                    is UserDetailFormViewModel.SaveState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SuccessBg),
                            border = BorderStroke(0.5.dp, SuccessBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = BrandGreen)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Profile saved successfully!",
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    is UserDetailFormViewModel.SaveState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                            border = BorderStroke(0.5.dp, Color(0xFFFFCDD2))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = ErrorRed)
                                Spacer(Modifier.width(8.dp))
                                Text(state.message, color = ErrorRed)
                            }
                        }
                    }

                    else -> {

                        Button(
                            onClick = { formViewModel.submitProfile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen,
                                contentColor   = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Save, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Save Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Shared field colors ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun lightFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor    = BorderDefault,
    focusedBorderColor      = BorderFocus,
    errorBorderColor        = ErrorRed,
    unfocusedTextColor      = TextPrimary,
    focusedTextColor        = TextPrimary,
    cursorColor             = BrandGreen,
    unfocusedContainerColor = SurfaceWhite,
    focusedContainerColor   = SurfaceFocus,
    errorContainerColor     = Color(0xFFFFF0F0),
    focusedLeadingIconColor   = BrandGreen,
    unfocusedLeadingIconColor = IconTint,
    errorLeadingIconColor     = ErrorRed,
    focusedTrailingIconColor  = BrandGreenLight,
    unfocusedTrailingIconColor= IconTint
)

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
private fun FormSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SectionIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = BorderDefault
        )
    }
}

// ─── Reusable form field ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Text(
            label,
            color = TextLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, color = TextPlaceholder)
            },
            leadingIcon = {
                Icon(
                    icon,
                    null,
                    tint = if (error != null) ErrorRed else IconTint
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null,
            supportingText = error?.let {
                { Text(it, color = ErrorRed, fontSize = 11.sp) }
            },
            colors = lightFieldColors()
        )
    }
}