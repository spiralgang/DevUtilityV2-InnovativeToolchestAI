# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# -*- coding: utf-8 -*-
from __future__ import annotations
import asyncio, os, yaml
from typing import Dict, Any
from dataclasses import dataclass

from bot.local_brain import LocalBrain, BotView
from bot.llm import LLMConfig
from telegram import Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters, ContextTypes

CONFIG_PATH = os.environ.get("BOTS_CONFIG", "config/bots.yaml")
STATE_DIR = os.environ.get("STATE_DIR", "state")

def load_config() -> Dict[str, Any]:
    with open(CONFIG_PATH, "r", encoding="utf-8") as fh:
        return yaml.safe_load(fh)

def make_view(name: str, spec: Dict[str, Any]) -> BotView:
    llm = spec.get("llm", {})
    return BotView(
        name=name,
        allow_roots=spec.get("allow_roots", []),
        include_ext=spec.get("filters", {}).get("include_ext", []),
        exclude_glob=spec.get("filters", {}).get("exclude_glob", []),
        indices=spec.get("indices", []),
        llm=LLMConfig(
            provider=llm.get("provider", "none"),
            model=llm.get("model", ""),
            max_new_tokens=int(llm.get("max_new_tokens", 256)),
        ),
    )

async def handle_query(update: Update, context: ContextTypes.DEFAULT_TYPE, brain: LocalBrain):
    if not update.message:
        return
    q = update.message.text.strip()
    if not q:
        return
    await update.message.chat.send_action(action="typing")
    ans = brain.answer(q)
    await update.message.reply_text(ans[:4000])

async def main():
    cfg = load_config()
    bots = cfg.get("bots", {})
    tasks = []
    for name, spec in bots.items():
        token = spec.get("telegram_token", "")
        if not token:
            print(f"[!] Bot {name} has no token, skipping.")
            continue
        view = make_view(name, spec)
        brain = LocalBrain(STATE_DIR, view)
        app = Application.builder().token(token).concurrent_updates(True).build()

        async def msg(update: Update, context: ContextTypes.DEFAULT_TYPE, _brain=brain):
            await handle_query(update, context, _brain)

        app.add_handler(CommandHandler("start", lambda u,c: c.bot.send_message(chat_id=u.effective_chat.id, text=f"{name} ready. Ask me anything (local index).")))
        app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, msg))
        tasks.append(app.run_polling(stop_signals=None))  # run all in parallel

    if not tasks:
        print("[!] No bots configured.")
        return
    await asyncio.gather(*tasks)

if __name__ == "__main__":
    asyncio.run(main())

# References:
# - /reference vault
# - python-telegram-bot (async) docs