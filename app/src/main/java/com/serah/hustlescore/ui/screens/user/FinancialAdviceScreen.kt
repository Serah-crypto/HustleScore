package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.data.bot.askFinancialBot
import com.serah.hustlescore.models.ChatMessage
import com.serah.hustlescore.models.FinancialAdvice
import com.serah.hustlescore.models.Priority
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun FinancialAdviceScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    // ── Theme-aware colours ───────────────────────────────────────────────────
    val backgroundColor  = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F6F9)
    val cardColor        = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val headerColor      = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val tabBgColor       = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFF3F4F6)
    val primaryText      = if (isDarkMode) Color.White       else Color(0xFF111827)
    val secondaryText    = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val chatBotBubbleBg  = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val inputBarBg       = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val quickChipBg      = if (isDarkMode) Color(0xFF1A3A25) else Color(0xFF1E8449).copy(alpha = 0.1f)

    val HustleGreen = Color(0xFF1E8449)

    var advice        by remember { mutableStateOf<List<FinancialAdvice>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }
    var selectedTab   by remember { mutableStateOf(0) }
    var chatMessages  by remember { mutableStateOf(listOf(
        ChatMessage("Habari! 👋 I'm HustleBot, your personal Kenyan financial advisor!\n\nAsk me anything about:\n• 💰 Saving & budgeting tips\n• 📱 M-Pesa, M-Shwari, Fuliza\n• 🏦 SACCOs & bank loans\n• 📊 Improving your HustleScore\n• 🇰🇪 Kenyan financial services", isUser = false)
    )) }
    var userInput     by remember { mutableStateOf("") }
    var isBotTyping   by remember { mutableStateOf(false) }
    val chatListState = rememberLazyListState()
    val scope         = rememberCoroutineScope()

    val quickQuestions = listOf(
        "How do I improve my HustleScore?",
        "What is M-Shwari?",
        "How do SACCOs work?",
        "Best way to save money in Kenya?",
        "How does Fuliza work?"
    )

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transactions: List<Transaction> = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    if (transactions.isNotEmpty()) {
                        val score = HustleScoreEngine.calculate(transactions)
                        advice = HustleScoreEngine.getAdvice(score)
                    }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) chatListState.animateScrollToItem(chatMessages.size - 1)
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isBotTyping) return
        val question = text.trim(); userInput = ""; isBotTyping = true
        chatMessages = chatMessages + ChatMessage(question, isUser = true)
        scope.launch {
            val reply = askFinancialBot(question, chatMessages)
            chatMessages = chatMessages + ChatMessage(reply, isUser = false)
            isBotTyping = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize().background(backgroundColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HustleGreen)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        // ── Tab Header ────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth().background(headerColor).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Financial Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryText)
            Text("Advice & AI Assistant", fontSize = 13.sp, color = secondaryText)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(tabBgColor).padding(4.dp)) {
                listOf("💡 Advice", "🤖 HustleBot").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == index) HustleGreen else Color.Transparent)
                            .then(Modifier.clickableNoRipple { selectedTab = index })
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = if (selectedTab == index) Color.White else secondaryText)
                    }
                }
            }
        }

        // ── Tab Content ───────────────────────────────────────────────────────
        when (selectedTab) {

            // ── Advice Tab ────────────────────────────────────────────────────
            0 -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Gold Banner
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))).padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f)), Alignment.Center) {
                                    Text("✨", fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("AI-Powered Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Based on your M-Pesa activity", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (advice.isEmpty()) {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(cardColor),
                            border = BorderStroke(2.dp, if (isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB))) {
                            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💡", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No advice yet", fontWeight = FontWeight.SemiBold, color = primaryText)
                                Text("Upload your M-Pesa SMS to get personalized advice.", color = secondaryText, fontSize = 13.sp)
                            }
                        }
                    } else {
                        advice.forEach { tip ->
                            val priorityColor = when (tip.priority) {
                                Priority.HIGH   -> Color(0xFFDC2626)
                                Priority.MEDIUM -> Color(0xFFF59E0B)
                                Priority.LOW    -> Color(0xFF16A34A)
                            }
                            val priorityLabel = when (tip.priority) {
                                Priority.HIGH -> "High Priority"; Priority.MEDIUM -> "Medium"; Priority.LOW -> "Tip"
                            }
                            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(cardColor)) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(HustleGreen.copy(alpha = 0.1f)), Alignment.Center) {
                                        Text(tip.icon, fontSize = 24.sp)
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(tip.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                                color = primaryText, modifier = Modifier.weight(1f))
                                            Surface(shape = RoundedCornerShape(8.dp), color = priorityColor.copy(alpha = 0.1f)) {
                                                Text(priorityLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(tip.description, color = secondaryText, fontSize = 12.sp, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Kenyan Resources
                    Card(shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFF0FDF4))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("🇰🇪 Kenyan Financial Resources", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HustleGreen)
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "M-Shwari"    to "KCB savings & micro-loans via M-Pesa",
                                "KCB M-Pesa"  to "Higher credit limits, up to KSh 1M",
                                "Fuliza"      to "Overdraft facility on M-Pesa",
                                "Equity TING" to "Group savings and loan products",
                                "SACCO"       to "Join a co-op for affordable credit"
                            ).forEach { (name, desc) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp))
                                    .background(cardColor).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(HustleGreen))
                                    Spacer(Modifier.width(10.dp))
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = primaryText)
                                    Spacer(Modifier.width(6.dp))
                                    Text(desc, fontSize = 11.sp, color = secondaryText)
                                }
                            }
                        }
                    }

                    // CTA to chatbot
                    Card(shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1A1E2E) else Color(0xFFEFF6FF))) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Have a question?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryText)
                                Text("Chat with HustleBot for personalized financial advice.", fontSize = 12.sp, color = secondaryText)
                            }
                            Button(onClick = { selectedTab = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                                shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                                Text("Chat", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ── Chatbot Tab ───────────────────────────────────────────────────
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    LazyColumn(state = chatListState, modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                        items(chatMessages) { msg ->
                            ChatBubble(msg, HustleGreen, primaryText, chatBotBubbleBg)
                        }
                        if (isBotTyping) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(HustleGreen), Alignment.Center) {
                                        Text("🤖", fontSize = 14.sp)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Card(shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                                        colors = CardDefaults.cardColors(chatBotBubbleBg)) {
                                        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            repeat(3) {
                                                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(secondaryText))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (chatMessages.size <= 1) {
                        Column(Modifier.fillMaxWidth().background(inputBarBg).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("Quick questions:", fontSize = 11.sp, color = secondaryText, modifier = Modifier.padding(bottom = 6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(quickQuestions) { q ->
                                    Surface(shape = RoundedCornerShape(20.dp), color = quickChipBg,
                                        modifier = Modifier.clickableNoRipple { sendMessage(q) }) {
                                        Text(q, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 11.sp, color = HustleGreen, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().background(inputBarBg).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = userInput, onValueChange = { userInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask a financial question...", fontSize = 13.sp, color = secondaryText) },
                            shape = RoundedCornerShape(24.dp), maxLines = 3, enabled = !isBotTyping,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = HustleGreen,
                                unfocusedBorderColor = if (isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB),
                                focusedTextColor     = primaryText,
                                unfocusedTextColor   = primaryText,
                                unfocusedContainerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
                                focusedContainerColor   = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
                            ))
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { sendMessage(userInput) },
                            enabled = userInput.isNotBlank() && !isBotTyping,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                                .background(if (userInput.isNotBlank() && !isBotTyping) HustleGreen else if (isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB))) {
                            Icon(Icons.Default.Send, "Send",
                                tint = if (userInput.isNotBlank() && !isBotTyping) Color.White else secondaryText,
                                modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Chat Bubble ──────────────────────────────────────────────────────────────
@Composable
fun ChatBubble(msg: ChatMessage, hustleGreen: Color, primaryText: Color, botBubbleBg: Color) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom) {
        if (!msg.isUser) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(hustleGreen), Alignment.Center) {
                Text("🤖", fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
        Card(
            shape = if (msg.isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(containerColor = if (msg.isUser) hustleGreen else botBubbleBg),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(msg.text, modifier = Modifier.padding(12.dp), fontSize = 13.sp,
                color = if (msg.isUser) Color.White else primaryText, lineHeight = 19.sp)
        }
        if (msg.isUser) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE5E7EB)), Alignment.Center) {
                Text("👤", fontSize = 14.sp)
            }
        }
    }
}



// ─── Helper extension ─────────────────────────────────────────────────────────
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick))