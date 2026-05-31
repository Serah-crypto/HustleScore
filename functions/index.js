const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

exports.askHustleBot = onCall(async (request) => {

    // ── 1. Auth check ────────────────────────────────────────────────────────
    if (!request.auth) {
        throw new HttpsError(
            "unauthenticated",
            "You must be logged in to use HustleBot."
        );
    }

    // ── 2. Rate limiting ─────────────────────────────────────────────────────
    const uid = request.auth.uid;
    const today = new Date().toISOString().split("T")[0];
    const rateLimitRef = admin.database().ref(`rateLimits/${uid}/${today}`);
    const snapshot = await rateLimitRef.once("value");
    const currentCount = snapshot.val() || 0;

    if (currentCount >= 20) {
        throw new HttpsError(
            "resource-exhausted",
            "You've reached your daily limit of 20 messages. Come back tomorrow!"
        );
    }
    await rateLimitRef.set(currentCount + 1);

    // ── 3. Validate input ────────────────────────────────────────────────────
    const { question, history } = request.data;
    if (!question || typeof question !== "string" || question.trim().length === 0) {
        throw new HttpsError("invalid-argument", "Question is required.");
    }

    // ── 4. Build messages ────────────────────────────────────────────────────
    const messages = [];
    if (Array.isArray(history)) {
        history.forEach((msg) => {
            messages.push({
                role: msg.isUser ? "user" : "assistant",
                content: msg.text,
            });
        });
    }
    messages.push({ role: "user", content: question.trim() });

    // ── 5. Call Anthropic API ────────────────────────────────────────────────
    const apiKey = process.env.ANTHROPIC_KEY; // ← reads from .env file

    const response = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "x-api-key": apiKey,
            "anthropic-version": "2023-06-01",
        },
        body: JSON.stringify({
            model: "claude-sonnet-4-20250514",
            max_tokens: 512,
            system: `You are HustleBot, a friendly and knowledgeable Kenyan financial advisor 
                     built into the HustleScore app. Your expertise includes M-Pesa, M-Shwari, 
                     Fuliza, KCB M-Pesa, SACCOs, budgeting and saving strategies for Kenyan 
                     households, and the HustleScore system. Keep answers concise and practical. 
                     Use KES/KSh for currency. Be warm and encouraging. Respond in Swahili if 
                     the user writes in Swahili, otherwise English.`,
            messages,
        }),
    });

    if (!response.ok) {
        throw new HttpsError("internal", "Failed to reach AI service.");
    }

    const result = await response.json();
    const reply = result.content?.[0]?.text ?? "Sorry, I couldn't generate a response.";
    return { reply };
});