/**
 * Minimal Express router: POST /api/ai/chat
 * - Uses server OPENAI_API_KEY when present (server fallback).
 * - Returns 502 when no server key so client can use Puter directly.
 *
 * Keep this module minimal and integrate into your existing Express app.
 * Security: apply auth, rate-limiting, and logging in production.
 */
const express = require('express');
const fetch = require('node-fetch');

module.exports = function registerPuterProxy(app) {
  const router = express.Router();

  router.post('/chat', express.json(), async (req, res) => {
    const { prompt, model = 'gpt-4o' } = req.body || {};
    if (!prompt) return res.status(400).json({ error: 'missing prompt' });

    const apiKey = process.env.OPENAI_API_KEY;
    if (!apiKey) {
      // Intentional: instruct client to use Puter if available
      return res.status(502).json({ error: 'No server-side API key configured; use Puter client or set OPENAI_API_KEY.' });
    }

    try {
      const payload = {
        model,
        messages: [{ role: 'user', content: prompt }],
        max_tokens: 800
      };
      const r = await fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${apiKey}`
        },
        body: JSON.stringify(payload)
      });

      if (!r.ok) {
        const txt = await r.text();
        return res.status(r.status).json({ error: txt });
      }

      const body = await r.json();
      const text = body.choices?.[0]?.message?.content ?? JSON.stringify(body);
      res.json({ text, raw: body });
    } catch (err) {
      res.status(500).json({ error: String(err) });
    }
  });

  app.use('/api/ai', router);
};