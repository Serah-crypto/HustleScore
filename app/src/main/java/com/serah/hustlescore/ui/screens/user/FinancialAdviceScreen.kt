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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.FinancialAdvice
import com.serah.hustlescore.models.Priority
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ── Chat Message Model ────────────────────────────────────────────────────────
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

// ── Chatbot API Call ──────────────────────────────────────────────────────────
suspend fun askFinancialBot(
    userMessage: String,
    history: List<ChatMessage>
): String = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.anthropic.com/v1/messages")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("x-api-key", "YOUR_API_KEY_HERE") // 🔑 Replace with your key
        connection.setRequestProperty("anthropic-version", "2023-06-01")
        connection.doOutput = true

        // Build conversation history
        val messages = JSONArray()
        history.filter { !it.isLoading }.takeLast(10).forEach { msg ->
            messages.put(JSONObject().apply {
                put("role", if (msg.isUser) "user" else "assistant")
                put("content", msg.text)
            })
        }
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        val body = JSONObject().apply {
            put("model", "claude-sonnet-4-20250514")
            put("max_tokens", 1000)
            put("system", """
                You are HustleBot, a friendly Kenyan financial advisor assistant inside the HustleScore app.
                
                Your role is to:
                - Answer financial questions from Kenyan users, especially about M-Pesa, mobile loans, SACCOs, and savings
                - Educate users about Kenyan financial services like M-Shwari, KCB M-Pesa, Fuliza, Equity, Co-op Bank, Faulu, and SACCOs
                - Give practical saving, budgeting, and investment tips relevant to Kenya
                - Help users understand their HustleScore and how to improve it
                - Explain concepts like credit scores, loan eligibility, and financial planning in simple Swahili-friendly English
                
                Rules:
                - ONLY answer financial questions. If asked about anything else, politely redirect to finance.
                - Keep answers concise, practical, and relevant to Kenya
                - Use simple language, avoid jargon
                - You can use occasional Swahili words like "Sawa", "Pesa", "Akiba" to feel local
                - Always encourage good financial habits
                
                If a question is not finance-related, respond: 
                "Samahani! I can only help with financial questions. Try asking me about saving, loans, M-Pesa, or your HustleScore! 💚"
            """.trimIndent())
            put("messages", messages)
        }

        connection.outputStream.use { it.write(body.toString().toByteArray()) }

        val responseCode = connection.responseCode
        if (responseCode == 200) {
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            json.getJSONArray("content")
                .getJSONObject(0)
                .getString("text")
        } else {
            val error = connection.errorStream?.bufferedReader()?.readText()
            "Sorry, I couldn't connect right now. Please try again. (Error $responseCode)"
        }
    } catch (e: Exception) {
        "Sorry, something went wrong: ${e.message}"
    }
}

@Composable
fun FinancialAdviceScreen(navController: NavController) {
    var advice by remember { mutableStateOf<List<FinancialAdvice>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Advice, 1 = Chatbot
    var chatMessages by remember { mutableStateOf(listOf(
        ChatMessage(
            "Habari! 👋 I'm HustleBot, your personal Kenyan financial advisor!\n\n" +
                    "Ask me anything about:\n" +
                    "• 💰 Saving & budgeting tips\n" +
                    "• 📱 M-Pesa, M-Shwari, Fuliza\n" +
                    "• 🏦 SACCOs & bank loans\n" +
                    "• 📊 Improving your HustleScore\n" +
                    "• 🇰🇪 Kenyan financial services",
            isUser = false
        )
    )) }
    var userInput by remember { mutableStateOf("") }
    var isBotTyping by remember { mutableStateOf(false) }
    val chatListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Quick question suggestions
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
                    val transactions: List<Transaction> = snapshot.children.mapNotNull {
                        it.getValue(Transaction::class.java)
                    }
                    if (transactions.isNotEmpty()) {
                        val score = HustleScoreEngine.calculate(transactions)
                        advice = HustleScoreEngine.getAdvice(score)
                    }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    // Scroll to bottom when new message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isBotTyping) return
        val question = text.trim()
        userInput = ""
        isBotTyping = true

        chatMessages = chatMessages + ChatMessage(question, isUser = true)

        scope.launch {
            val reply = askFinancialBot(question, chatMessages)
            chatMessages = chatMessages + ChatMessage(reply, isUser = false)
            isBotTyping = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HustleGreen)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        // ── Tab Header ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Financial Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Advice & AI Assistant", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(4.dp)
            ) {
                listOf("💡 Advice", "🤖 HustleBot").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == index) HustleGreen else Color.Transparent)
                            .then(Modifier.clickableNoRipple { selectedTab = index })
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (selectedTab == index) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // ── Tab Content ───────────────────────────────────────────────────────
        when (selectedTab) {

            // ── Advice Tab ────────────────────────────────────────────────────
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Gold Banner
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("✨", fontSize = 20.sp) }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("AI-Powered Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Based on your M-Pesa activity", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (advice.isEmpty()) {
                        Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, Color(0xFFE5E7EB))) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💡", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No advice yet", fontWeight = FontWeight.SemiBold)
                                Text("Upload your M-Pesa SMS to get personalized advice.", color = TextSecondary, fontSize = 13.sp)
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
                                Priority.HIGH   -> "High Priority"
                                Priority.MEDIUM -> "Medium"
                                Priority.LOW    -> "Tip"
                            }
                            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(HustleGreen.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(tip.icon, fontSize = 24.sp) }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(tip.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            Surface(shape = RoundedCornerShape(8.dp), color = priorityColor.copy(alpha = 0.1f)) {
                                                Text(priorityLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(tip.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Kenyan Resources
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🇰🇪 Kenyan Financial Resources", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HustleGreen)
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "M-Shwari"    to "KCB savings & micro-loans via M-Pesa",
                                "KCB M-Pesa"  to "Higher credit limits, up to KSh 1M",
                                "Fuliza"      to "Overdraft facility on M-Pesa",
                                "Equity TING" to "Group savings and loan products",
                                "SACCO"       to "Join a co-op for affordable credit"
                            ).forEach { (name, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(HustleGreen))
                                    Spacer(Modifier.width(10.dp))
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(6.dp))
                                    Text(desc, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // CTA to chatbot
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤖", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Have a question?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Chat with HustleBot for personalized financial advice.", fontSize = 12.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = { selectedTab = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) { Text("Chat", fontSize = 12.sp) }
                        }
                    }
                }
            }

            // ── Chatbot Tab ───────────────────────────────────────────────────
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Chat messages
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(chatMessages) { msg ->
                            ChatBubble(msg)
                        }
                        if (isBotTyping) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(HustleGreen),
                                        contentAlignment = Alignment.Center
                                    ) { Text("🤖", fontSize = 14.sp) }
                                    Spacer(Modifier.width(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(3) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(TextSecondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick questions
                    if (chatMessages.size <= 1) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Quick questions:", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(quickQuestions) { q ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = HustleGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.clickableNoRipple { sendMessage(q) }
                                    ) {
                                        Text(
                                            q,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 11.sp,
                                            color = HustleGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask a financial question...", fontSize = 13.sp) },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            enabled = !isBotTyping
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { sendMessage(userInput) },
                            enabled = userInput.isNotBlank() && !isBotTyping,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (userInput.isNotBlank() && !isBotTyping) HustleGreen else Color(0xFFE5E7EB))
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (userInput.isNotBlank() && !isBotTyping) Color.White else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Chat Bubble ───────────────────────────────────────────────────────────────
@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!msg.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HustleGreen),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 14.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Card(
            shape = if (msg.isUser)
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            else
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (msg.isUser) HustleGreen else Color.White
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                msg.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                color = if (msg.isUser) Color.White else Color(0xFF111827),
                lineHeight = 19.sp
            )
        }

        if (msg.isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 14.sp) }
        }
    }
}

// ── Helper extension ──────────────────────────────────────────────────────────
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    )

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FinancialAdviceScreenPreview() {
    HustleScoreTheme {
        FinancialAdviceScreen(navController = rememberNavController())
    }
}