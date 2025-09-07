```markdown
# Puter API — concise reference (UBULITE)

This document consolidates the Puter.js utilities and UI functions referenced in our integration demo. It intentionally omits repeated tutorial pages and keeps one short example per API to make review and testing fast.

NOTE: This is a reference summary for engineering use — always confirm the canonical Puter docs (js.puter.com/v2) before production rollout.

## Utilities
- puter.print(value) — prints to Puter console/output.
- puter.randName() — returns a random demo name.
- puter.appID — property returning the current app ID.
- puter.env — read-only environment variables in Puter runtime.

## Drivers
- puter.drivers.call(interface, driver, method, args = {}) — low-level driver invocation.

## UI helpers (selected)
- puter.ui.prompt(message?, placeholder?) — returns Promise<string|null>.
- puter.ui.alert(message, buttons?) — modal alerts; resolves to pressed button value.
- puter.ui.contextMenu(options) — dynamic context menus.
- puter.ui.showOpenFilePicker()/showSaveFilePicker() — file pickers.
- puter.ui.showDirectoryPicker() — directory picker.
- puter.ui.createWindow(options) — create additional window; options: title,width,height,center,content,has_head,is_resizable,disable_parent_window,show_in_taskbar.
- puter.ui.*window control methods and setMenubar.
- puter.ui.wasLaunchedWithItems(), parentApp(), launchApp(), on(eventName, handler), onWindowClose(handler), getLanguage(), exit().

## Integration pattern
- Client-first flow: try Puter client, else call server fallback at /api/ai/chat.
- Server returns 502 if no OPENAI_API_KEY to allow user-pay client flows.

## Minimal snippets
- Client/server selection:
```js
if (window.FEAT_PUTER && typeof puter !== 'undefined') {
  await puter.ai.chat(prompt, { model });
} else {
  await fetch('/api/ai/chat', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({prompt,model}) });
}
```

## Testing checklist
- Verify client path, server 502 when key absent, server response when key present, UI behaviors.

## References
- Puter canonical docs: confirm js.puter.com/v2
- OpenAI docs: https://platform.openai.com/docs
```