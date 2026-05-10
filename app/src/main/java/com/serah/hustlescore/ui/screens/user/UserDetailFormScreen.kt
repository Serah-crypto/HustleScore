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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.draw.clip
import com.serah.hustlescore.models.UserProfile


// ─── Counties List ─────────────────────────────────────────────────────────────

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

// ─── ViewModel ────────────────────────────────────────────────────────────────

class UserDetailFormViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // Form fields
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var idNumber by mutableStateOf("")
    var selectedCounty by mutableStateOf("")
    var occupation by mutableStateOf("")
    var employer by mutableStateOf("")
    var monthlyIncome by mutableStateOf("")
    var bio by mutableStateOf("")

    // Field errors
    var fullNameError by mutableStateOf<String?>(null)
    var phoneError by mutableStateOf<String?>(null)
    var idNumberError by mutableStateOf<String?>(null)
    var countyError by mutableStateOf<String?>(null)
    var occupationError by mutableStateOf<String?>(null)
    var incomeError by mutableStateOf<String?>(null)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    init {
        loadExistingProfile()
    }

    private fun loadExistingProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.child("users").child(uid).child("profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val p = snapshot.getValue(UserProfile::class.java) ?: return
                    fullName = p.fullName
                    phone = p.phone
                    idNumber = p.idNumber
                    selectedCounty = p.county
                    occupation = p.occupation
                    employer = p.employer
                    monthlyIncome = p.monthlyIncome
                    bio = p.bio
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validate(): Boolean {
        var valid = true
        fullNameError = if (fullName.trim().length < 3) { valid = false; "Enter your full name (min 3 chars)" } else null
        phoneError = if (!phone.matches(Regex("^[0-9]{10,12}$"))) { valid = false; "Enter a valid phone number" } else null
        idNumberError = if (idNumber.trim().length < 6) { valid = false; "Enter a valid ID number" } else null
        countyError = if (selectedCounty.isBlank()) { valid = false; "Please select a county" } else null
        occupationError = if (occupation.trim().isBlank()) { valid = false; "Enter your occupation" } else null
        incomeError = if (monthlyIncome.trim().isBlank() || monthlyIncome.toDoubleOrNull() == null) {
            valid = false; "Enter a valid income amount"
        } else null
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
            "uid" to uid,
            "fullName" to fullName.trim(),
            "email" to (auth.currentUser?.email ?: ""),
            "phone" to phone.trim(),
            "idNumber" to idNumber.trim(),
            "county" to selectedCounty,
            "occupation" to occupation.trim(),
            "employer" to employer.trim(),
            "monthlyIncome" to monthlyIncome.trim(),
            "bio" to bio.trim(),
            "profileComplete" to true
        )

        db.child("users").child(uid).child("profile")
            .setValue(profileData)
            .addOnSuccessListener { _saveState.value = SaveState.Success }
            .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Save failed") }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailFormScreen(
    NavController: androidx.navigation.NavHostController,
    formViewModel: UserDetailFormViewModel = viewModel()
) {
    val saveState by formViewModel.saveState.collectAsState()
    var countyDropdownExpanded by remember { mutableStateOf(false) }

    val primaryGreen = Color(0xFF00C853)
    val darkBg = Color(0xFF0D1117)
    val cardBg = Color(0xFF161B22)
    val mutedText = Color(0xFF8B949E)

    // Navigate back on success
    LaunchedEffect(saveState) {
        if (saveState is UserDetailFormViewModel.SaveState.Success) {
            kotlinx.coroutines.delay(1200)
            formViewModel.resetSaveState()
            NavController.navigate("profile") {
                popUpTo("user_detail_form") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { NavController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Header Banner ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF003D1F), Color(0xFF00C853).copy(0.3f))))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(40.dp))
                        Column {
                            Text("Build Your Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Your info helps us calculate your HustleScore", color = mutedText, fontSize = 12.sp)
                        }
                    }
                }

                // ── Section: Personal ────────────────────────────────────
                FormSectionHeader(title = "Personal Details", icon = Icons.Default.Person)

                FormField(
                    value = formViewModel.fullName,
                    onValueChange = { formViewModel.fullName = it },
                    label = "Full Name",
                    placeholder = "e.g. Jane Wanjiku",
                    icon = Icons.Default.Badge,
                    error = formViewModel.fullNameError
                )

                FormField(
                    value = formViewModel.phone,
                    onValueChange = { formViewModel.phone = it },
                    label = "Phone Number",
                    placeholder = "e.g. 0712345678",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    error = formViewModel.phoneError
                )

                FormField(
                    value = formViewModel.idNumber,
                    onValueChange = { formViewModel.idNumber = it },
                    label = "National ID Number",
                    placeholder = "e.g. 12345678",
                    icon = Icons.Default.CreditCard,
                    keyboardType = KeyboardType.Number,
                    error = formViewModel.idNumberError
                )

                // County Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("County", color = mutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    ExposedDropdownMenuBox(
                        expanded = countyDropdownExpanded,
                        onExpandedChange = { countyDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = formViewModel.selectedCounty.ifBlank { "Select your county" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countyDropdownExpanded) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (formViewModel.countyError != null) Color(0xFFFF5252) else mutedText) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (formViewModel.countyError != null) Color(0xFFFF5252) else Color(0xFF30363D),
                                focusedBorderColor = primaryGreen,
                                unfocusedTextColor = if (formViewModel.selectedCounty.isBlank()) mutedText else Color(0xFFCDD9E5),
                                focusedTextColor = Color.White,
                                cursorColor = primaryGreen,
                                unfocusedContainerColor = Color(0xFF161B22),
                                focusedContainerColor = Color(0xFF161B22)
                            ),
                            isError = formViewModel.countyError != null
                        )
                        ExposedDropdownMenu(
                            expanded = countyDropdownExpanded,
                            onDismissRequest = { countyDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF161B22)).heightIn(max = 250.dp)
                        ) {
                            KenyaCounties.forEach { county ->
                                DropdownMenuItem(
                                    text = { Text(county, color = Color(0xFFCDD9E5)) },
                                    onClick = {
                                        formViewModel.selectedCounty = county
                                        formViewModel.countyError = null
                                        countyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    formViewModel.countyError?.let {
                        Text(it, color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }

                // ── Section: Financial ────────────────────────────────────
                Spacer(Modifier.height(4.dp))
                FormSectionHeader(title = "Financial Information", icon = Icons.Default.AccountBalance)

                FormField(
                    value = formViewModel.occupation,
                    onValueChange = { formViewModel.occupation = it },
                    label = "Occupation",
                    placeholder = "e.g. Software Engineer",
                    icon = Icons.Default.Work,
                    error = formViewModel.occupationError
                )

                FormField(
                    value = formViewModel.employer,
                    onValueChange = { formViewModel.employer = it },
                    label = "Employer / Business Name",
                    placeholder = "e.g. Safaricom PLC",
                    icon = Icons.Default.Business
                )

                FormField(
                    value = formViewModel.monthlyIncome,
                    onValueChange = { formViewModel.monthlyIncome = it },
                    label = "Monthly Income (KES)",
                    placeholder = "e.g. 45000",
                    icon = Icons.Default.AttachMoney,
                    keyboardType = KeyboardType.Number,
                    error = formViewModel.incomeError
                )

                // ── Section: Bio ──────────────────────────────────────────
                Spacer(Modifier.height(4.dp))
                FormSectionHeader(title = "About You (Optional)", icon = Icons.Default.Info)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Short Bio", color = mutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = formViewModel.bio,
                        onValueChange = { if (it.length <= 300) formViewModel.bio = it },
                        placeholder = { Text("Tell us a little about yourself...", color = mutedText) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedBorderColor = primaryGreen,
                            unfocusedTextColor = Color(0xFFCDD9E5),
                            focusedTextColor = Color.White,
                            cursorColor = primaryGreen,
                            unfocusedContainerColor = Color(0xFF161B22),
                            focusedContainerColor = Color(0xFF161B22)
                        ),
                        supportingText = { Text("${formViewModel.bio.length}/300", color = mutedText, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) }
                    )
                }

                // ── Submit Button ─────────────────────────────────────────
                Spacer(Modifier.height(8.dp))

                AnimatedContent(targetState = saveState, label = "save_btn") { state ->
                    when (state) {
                        is UserDetailFormViewModel.SaveState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = primaryGreen, strokeWidth = 3.dp)
                            }
                        }
                        is UserDetailFormViewModel.SaveState.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF003D1F)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryGreen)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Profile saved! Redirecting...", color = primaryGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        is UserDetailFormViewModel.SaveState.Error -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0000)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFFF5252))
                                        Spacer(Modifier.width(8.dp))
                                        Text(state.message, color = Color(0xFFFF5252), fontSize = 13.sp)
                                    }
                                }
                                Button(
                                    onClick = { formViewModel.submitProfile() },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        else -> {
                            Button(
                                onClick = { formViewModel.submitProfile() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─── Reusable Form Components ─────────────────────────────────────────────────

@Composable
private fun FormSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(18.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Divider(modifier = Modifier.weight(2f), color = Color(0xFF30363D))
    }
}

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
    val primaryGreen = Color(0xFF00C853)
    val mutedText = Color(0xFF8B949E)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = mutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = mutedText) },
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = if (error != null) Color(0xFFFF5252) else mutedText, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = Color(0xFFFF5252), fontSize = 11.sp) } },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (error != null) Color(0xFFFF5252) else Color(0xFF30363D),
                focusedBorderColor = primaryGreen,
                errorBorderColor = Color(0xFFFF5252),
                unfocusedTextColor = Color(0xFFCDD9E5),
                focusedTextColor = Color.White,
                cursorColor = primaryGreen,
                unfocusedContainerColor = Color(0xFF161B22),
                focusedContainerColor = Color(0xFF161B22),
                errorContainerColor = Color(0xFF161B22)
            )
        )
    }
}