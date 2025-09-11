<template>
  <div class="mobile-dashboard" role="application" aria-label="CODE-REAVER Mobile Agent Interface">
    <!-- Top bar -->
    <header class="topbar" role="banner">
      <button class="nav-btn" @click="toggleDrawer" aria-label="Open agents menu" :aria-expanded="drawerOpen">☰</button>
      <div class="title">CODE-REAVER • Mobile Ops</div>
      <div class="top-actions">
        <button class="action-btn" @click="refreshAllStatuses" aria-label="Refresh statuses">🔁</button>
        <button class="action-btn" @click="openEdgePanel" aria-label="Open quick actions">⋯</button>
      </div>
    </header>

    <!-- Slide-in left drawer (agents) -->
    <aside class="drawer left-drawer" :class="{ open: drawerOpen }" role="dialog" aria-modal="false" aria-label="Agents drawer">
      <div class="drawer-header">
        <strong>Agents</strong>
        <button class="close-btn" @click="toggleDrawer" aria-label="Close agents">✖</button>
      </div>
      <ul class="agent-list" role="list">
        <li
          v-for="agent in availableAgents"
          :key="agent.id"
          role="button"
          class="agent-row"
          :class="{ active: chosenAgent === agent.id }"
          @click="selectAgent(agent.id)"
          @keydown.enter.prevent="selectAgent(agent.id)"
          tabindex="0"
          :aria-pressed="chosenAgent === agent.id"
        >
          <div class="agent-left">
            <span class="agent-icon" aria-hidden="true">{{ agent.icon }}</span>
            <span class="agent-name">{{ agent.name }}</span>
          </div>
          <div class="agent-right">
            <span :class="agentStatuses[agent.id]?.online ? 'dot online' : 'dot offline'"></span>
          </div>
        </li>
      </ul>
    </aside>

    <!-- Edge panel (right-edge slide) for quick actions / accessibility tools -->
    <aside class="edge-panel" :class="{ open: edgePanelOpen }" role="complementary" aria-label="Quick actions panel" @keydown.esc="closeEdgePanel">
      <div class="edge-header">
        <strong>Quick Actions</strong>
        <button class="close-btn" @click="closeEdgePanel" aria-label="Close quick actions">✖</button>
      </div>
      <div class="edge-actions">
        <button class="edge-action" @click="toggleFloatWindow('accessibilityTools')" aria-label="Accessibility tools">
          ♿ Accessibility
        </button>
        <button class="edge-action" @click="toggleFloatWindow('network')" aria-label="Network diagnostics">
          📶 Network
        </button>
        <button class="edge-action" @click="openBottomSheet" aria-label="Open log sheet">
          🗒️ Open Logs
        </button>
        <button class="edge-action" @click="toggleFloatIconMenu" aria-label="Toggle floating actions">
          ✨ Floating Icons
        </button>
      </div>
      <div class="edge-footer">
        <label>
          <input type="checkbox" v-model="highContrast" /> High contrast
        </label>
        <label>
          <input type="checkbox" v-model="reducedMotionPreference" /> Reduce motion
        </label>
      </div>
    </aside>

    <!-- Main content stack -->
    <main class="content" role="main" @touchstart.passive="onTouchStart" @touchmove.passive="onTouchMove" @touchend.passive="onTouchEnd">
      <!-- Interaction header -->
      <section class="panel mode-panel" aria-label="Interaction Mode">
        <div class="modes" role="tablist" aria-label="Interaction modes">
          <button
            v-for="mode in modes"
            :key="mode"
            :class="{ active: selectedMode === mode }"
            @click="selectedMode = mode"
            role="tab"
            :aria-selected="selectedMode === mode"
            class="mode-pill"
          >
            {{ modeShort(mode) }}
          </button>
        </div>
        <div class="chosen-agent-inline">
          <strong>{{ getAgentIcon(chosenAgent) }} {{ getAgentName(chosenAgent) }}</strong>
          <span class="status-small" :class="agentStatuses[chosenAgent]?.online ? 'online' : 'offline'">
            {{ agentStatuses[chosenAgent]?.online ? 'Online' : 'Offline' }}
          </span>
        </div>
      </section>

      <!-- Prompt input -->
      <section v-if="selectedMode === 'prompt'" class="panel input-panel" aria-labelledby="prompt-label">
        <label id="prompt-label" class="sr-only">Prompt</label>
        <textarea
          id="prompt-input"
          v-model="prompt"
          placeholder="Tap to enter prompt (mobile-friendly)"
          rows="4"
          :disabled="loading"
          class="prompt-input"
          aria-required="true"
        ></textarea>
        <div class="row">
          <button class="primary-btn" @click="sendPrompt" :disabled="!prompt.trim() || loading" aria-label="Send prompt">Send</button>
          <button class="ghost-btn" @click="saveDraft" :disabled="!prompt.trim()" aria-label="Save draft">Save</button>
        </div>
      </section>

      <!-- File upload -->
      <section v-if="selectedMode === 'file'" class="panel input-panel" aria-labelledby="file-label">
        <label id="file-label" class="file-label" for="file-input">Attach file (code/docs)</label>
        <input id="file-input" ref="fileInput" type="file" @change="handleFileUpload" accept=".js,.ts,.vue,.py,.java,.kt,.md,.txt,.json" :disabled="loading" />
        <div class="file-row" v-if="selectedFile">
          <div class="file-meta">📄 {{ selectedFile.name }} • {{ formatFileSize(selectedFile.size) }}</div>
          <div class="row">
            <button class="primary-btn" @click="sendFile" :disabled="loading">Analyze</button>
            <button class="ghost-btn" @click="clearFile">Clear</button>
          </div>
        </div>
      </section>

      <!-- Status / Info -->
      <section v-if="selectedMode === 'status' || selectedMode === 'info'" class="panel info-panel">
        <div class="row">
          <button class="primary-btn" @click="getAgentInfo" :disabled="loading">{{ loading ? 'Loading…' : (selectedMode === 'status' ? 'Get Status' : 'Get Info') }}</button>
          <button class="ghost-btn" @click="refreshAllStatuses">Refresh</button>
        </div>
      </section>

      <!-- Response card -->
      <section v-if="response" class="panel response-panel" aria-live="polite" aria-atomic="true">
        <div class="resp-head">
          <div class="resp-left">
            <span class="agent-badge" :style="{ backgroundColor: getAgentColor(lastUsedAgent) }">{{ getAgentIcon(lastUsedAgent) }} {{ getAgentName(lastUsedAgent) }}</span>
            <small class="ts">{{ formatTimestamp(responseTimestamp) }}</small>
          </div>
          <div class="resp-actions">
            <button class="small-btn" @click="copyResponse" aria-label="Copy response">Copy</button>
            <button class="small-btn" @click="openBottomSheet" aria-label="Open response sheet">Open</button>
          </div>
        </div>

        <pre class="resp-body" v-if="typeof response === 'string'">{{ response }}</pre>
        <div v-else class="resp-json">
          <div v-for="(v,k) in response" :key="k" class="kv"><strong>{{ k }}:</strong> <code>{{ formatJsonValue(v) }}</code></div>
        </div>
      </section>

      <!-- Minimal history -->
      <section class="panel history-panel" aria-labelledby="history-heading">
        <div class="history-header">
          <strong id="history-heading">History</strong>
          <div>
            <button class="small-btn" @click="loadHistory">Refresh</button>
            <button class="small-btn" @click="clearHistory">Clear</button>
          </div>
        </div>
        <ul class="history-list" role="list">
          <li v-for="(h, idx) in interactionHistory" :key="h.timestamp || idx" class="history-row" role="listitem">
            <div class="h-left">
              <span class="h-agent">{{ getAgentIcon(h.agent) }}</span>
              <div class="h-meta"><div class="h-mode">{{ h.mode }}</div><small class="h-ts">{{ formatTimestamp(h.timestamp) }}</small></div>
            </div>
            <div class="h-right" :class="h.success ? 'ok' : 'fail'">{{ h.success ? 'OK' : 'ERR' }}</div>
          </li>
        </ul>
        <div v-if="interactionHistory.length === 0" class="muted">No interactions yet.</div>
      </section>
    </main>

    <!-- Floating action icons (FAB cluster) -->
    <div class="floating-icons" :class="{ open: floatIconsOpen }" role="navigation" aria-label="Floating actions">
      <button class="fab main-fab" @click="toggleFloatIconMenu" :aria-expanded="floatIconsOpen" :aria-controls="'fab-menu'">✦</button>
      <div id="fab-menu" class="fab-menu" v-if="floatIconsOpen">
        <button class="fab-item" @click="toggleFloatWindow('accessibilityTools')" aria-label="Open accessibility tools">♿</button>
        <button class="fab-item" @click="openBottomSheet" aria-label="Open logs">🗒️</button>
        <button class="fab-item" @click="toggleFloatWindow('quickNotes')" aria-label="Open quick notes">📝</button>
      </div>
    </div>

    <!-- Bottom sheet (drawer slide up) for logs / response detail -->
    <div class="bottom-sheet" :class="{ open: bottomSheetOpen }" role="dialog" aria-modal="true" aria-label="Bottom sheet" @keydown.esc="closeBottomSheet">
      <div class="sheet-handle" @click="closeBottomSheet" role="button" tabindex="0" aria-label="Close sheet">—</div>
      <div class="sheet-content">
        <h3>{{ bottomSheetTitle }}</h3>
        <div class="sheet-body">
          <pre v-if="sheetContent && typeof sheetContent === 'string'">{{ sheetContent }}</pre>
          <div v-else class="sheet-json" v-if="sheetContent">
            <div v-for="(v,k) in sheetContent" :key="k"><strong>{{ k }}:</strong><code>{{ formatJsonValue(v) }}</code></div>
          </div>
          <div v-if="!sheetContent" class="muted">No content.</div>
        </div>
      </div>
    </div>

    <!-- Floating windows (small accessible modals) -->
    <div v-if="floatWindowOpen" class="float-window-backdrop" @click.self="closeAllFloatWindows" role="presentation">
      <div class="float-window" role="dialog" aria-modal="true" :aria-label="floatWindowLabel" ref="floatWindow" @keydown.tab.prevent="trapFocus">
        <header class="float-header">
          <h4>{{ floatWindowTitle }}</h4>
          <button class="close-btn" @click="closeAllFloatWindows" aria-label="Close window">✖</button>
        </header>
        <div class="float-body">
          <component :is="floatWindowComponent" v-bind="floatWindowProps" />
        </div>
      </div>
    </div>

    <!-- Bottom navigation bar -->
    <nav class="bottom-nav" role="navigation" aria-label="Quick navigation">
      <button class="nav-action" @click="selectedMode='prompt'" :aria-pressed="selectedMode==='prompt'">✍️</button>
      <button class="nav-action" @click="selectedMode='file'" :aria-pressed="selectedMode==='file'">📎</button>
      <button class="nav-action" @click="selectedMode='status'" :aria-pressed="selectedMode==='status'">📡</button>
      <button class="nav-action" @click="toggleDrawer" aria-label="Open agents">🤖</button>
    </nav>
  </div>
</template>

<script>
/*
  Enhanced AgentDashboardMobile.vue
  - Mobile-first + feature upgrades:
    * Floating windows (accessible modals) with focus trap
    * Right-edge panel for quick-access actions
    * Floating action icons (FAB cluster)
    * Bottom-sheet slide-up for logs / detailed responses
    * High-contrast and reduced-motion toggles
    * Edge-swipe gesture handlers (lightweight) to open edge panel or drawer
    * Accessibility: ARIA attributes, role semantics, keyboard handlers, live regions
  - Design goals: Android 10 compatibility, small memory footprint, graceful fallbacks
  - Integrates with ../utils/agentApi.js
  - See /reference vault for standards and justification (accessibility, touch targets, reduced-motion)
*/

import agentApi from '../utils/agentApi.js';

const NETWORK_TIMEOUT = 12000;

export default {
  name: 'AgentDashboardMobile',
  data() {
    return {
      drawerOpen: false,
      edgePanelOpen: false,
      floatIconsOpen: false,
      bottomSheetOpen: false,
      floatWindowOpen: false,
      floatWindowComponent: null,
      floatWindowProps: {},
      floatWindowTitle: '',
      floatWindowLabel: '',
      bottomSheetTitle: '',
      sheetContent: null,

      chosenAgent: 'deepseek',
      modes: ['prompt', 'file', 'status', 'info'],
      selectedMode: 'prompt',
      prompt: '',
      selectedFile: null,
      loading: false,
      response: null,
      lastUsedAgent: null,
      responseTimestamp: null,
      availableAgents: [],
      agentStatuses: {},
      interactionHistory: [],

      // preferences
      highContrast: false,
      reducedMotionPreference: false,

      // simple edge gesture tracking
      touchStartX: 0,
      touchStartY: 0,
      touchEndX: 0,
      touchEndY: 0
    };
  },
  computed: {
    bottomSheetOpenClass() { return this.bottomSheetOpen ? 'open' : ''; }
  },
  mounted() {
    this.loadAvailableAgents();
    this.refreshAllStatuses();
    this.loadHistory();

    // restore simple preferences from localStorage if available
    try {
      const prefs = JSON.parse(localStorage.getItem('agentMobilePrefs') || '{}');
      this.highContrast = !!prefs.highContrast;
      this.reducedMotionPreference = !!prefs.reducedMotionPreference;
    } catch {}

    // apply high contrast class if enabled
    if (this.highContrast) document.documentElement.classList.add('high-contrast');
  },
  watch: {
    highContrast(val) {
      try {
        if (val) document.documentElement.classList.add('high-contrast');
        else document.documentElement.classList.remove('high-contrast');
        localStorage.setItem('agentMobilePrefs', JSON.stringify({ highContrast: this.highContrast, reducedMotionPreference: this.reducedMotionPreference }));
      } catch {}
    },
    reducedMotionPreference(val) {
      try {
        localStorage.setItem('agentMobilePrefs', JSON.stringify({ highContrast: this.highContrast, reducedMotionPreference: this.reducedMotionPreference }));
      } catch {}
    }
  },
  methods: {
    // ------------------ UI controls ------------------
    toggleDrawer() { this.drawerOpen = !this.drawerOpen; if (this.drawerOpen) this.edgePanelOpen = false; },
    openEdgePanel() { this.edgePanelOpen = true; this.drawerOpen = false; },
    closeEdgePanel() { this.edgePanelOpen = false; },
    toggleFloatIconMenu() { this.floatIconsOpen = !this.floatIconsOpen; },
    openBottomSheet(content = null, title = 'Details') {
      this.sheetContent = content || (typeof this.response === 'string' ? this.response : this.response || null);
      this.bottomSheetTitle = title;
      this.bottomSheetOpen = true;
      // small focus management
      document.activeElement?.blur();
    },
    closeBottomSheet() { this.bottomSheetOpen = false; },

    toggleFloatWindow(name) {
      // lightweight registry for small windows - component fallback to built-in content
      const registry = {
        accessibilityTools: {
          title: 'Accessibility Tools',
          component: {
            template: `<div>
                        <p><strong>High contrast:</strong> <button @click="$emit('toggle-contrast')">Toggle</button></p>
                        <p><strong>Reduce motion:</strong> <button @click="$emit('toggle-reduced')">Toggle</button></p>
                       </div>`
          },
          props: {}
        },
        network: {
          title: 'Network Diagnostics',
          component: {
            template: `<div><p>Ping: simulated</p><p>Latency: simulated</p></div>`
          },
          props: {}
        },
        quickNotes: {
          title: 'Quick Notes',
          component: {
            template: `<div><textarea style="width:100%;height:120px" placeholder="Quick note..."></textarea></div>`
          },
          props: {}
        }
      };
      const item = registry[name];
      if (!item) return;
      this.floatWindowTitle = item.title;
      this.floatWindowLabel = item.title;
      this.floatWindowComponent = item.component;
      this.floatWindowProps = item.props || {};
      this.floatWindowOpen = true;
      this.floatIconsOpen = false;
      this.$nextTick(() => { this.trapFocus(); });
    },
    closeAllFloatWindows() {
      this.floatWindowOpen = false;
      this.floatWindowComponent = null;
      this.floatWindowProps = {};
    },

    trapFocus(e) {
      // minimal focus trap: ensure float window is focused when open
      if (!this.floatWindowOpen) return;
      const el = this.$refs.floatWindow;
      if (el && typeof el.focus === 'function') el.focus();
    },

    // ------------------ Agent / API ------------------
    loadAvailableAgents() {
      try {
        this.availableAgents = agentApi.getAvailableAgents() || [];
        if (!this.availableAgents.find(a => a.id === this.chosenAgent) && this.availableAgents.length) {
          this.chosenAgent = this.availableAgents[0].id;
        }
      } catch (err) {
        this.availableAgents = [];
        this._setError(`Failed to load agents: ${err.message || err}`);
      }
    },

    async refreshAllStatuses() {
      if (!this.availableAgents.length) return;
      const tasks = this.availableAgents.map(a => this._withTimeout(() => agentApi.getAgentStatus(a.id), NETWORK_TIMEOUT)
        .then(s => ({ id: a.id, status: s }))
        .catch(e => ({ id: a.id, status: { online: false, error: String(e?.message || e) } })));
      try {
        const results = await Promise.all(tasks);
        results.forEach(r => { this.$set(this.agentStatuses, r.id, r.status); });
      } catch (err) {
        this._setError(`Status refresh error: ${err.message || err}`);
      }
    },

    selectAgent(id) { this.chosenAgent = id; this.drawerOpen = false; this.edgePanelOpen = false; },

    async sendPrompt() {
      if (!this.prompt || !this.prompt.trim()) return;
      this.loading = true; this._clearError();
      try {
        const payload = { agent: this.chosenAgent, prompt: this.prompt.trim(), mode: 'prompt' };
        const result = await this._withTimeout(() => agentApi.sendPromptToAgent(payload), NETWORK_TIMEOUT);
        this._recordResponse(result);
        this._pushHistory({ agent: this.chosenAgent, mode: 'prompt', prompt: this.prompt, success: true, result });
        this.prompt = ''; // avoid reopening keyboard
      } catch (err) {
        this._setError(err.message || String(err)); this._pushHistory({ agent: this.chosenAgent, mode: 'prompt', prompt: this.prompt, success: false, error: err.message || String(err) });
      } finally { this.loading = false; }
    },

    handleFileUpload(e) { const f = e.target.files && e.target.files[0]; this.selectedFile = f || null; },

    async sendFile() {
      if (!this.selectedFile) return;
      this.loading = true; this._clearError();
      try {
        const text = await this._readFileAsText(this.selectedFile);
        const payload = { agent: this.chosenAgent, prompt: `Analyze ${this.selectedFile.name}`, fileContent: text, mode: 'file' };
        const result = await this._withTimeout(() => agentApi.sendPromptToAgent(payload), NETWORK_TIMEOUT);
        this._recordResponse(result);
        this._pushHistory({ agent: this.chosenAgent, mode: 'file', prompt: this.selectedFile.name, success: true, result });
        this.clearFile();
      } catch (err) {
        this._setError(err.message || String(err)); this._pushHistory({ agent: this.chosenAgent, mode: 'file', prompt: this.selectedFile?.name || '', success: false, error: err.message || String(err) });
      } finally { this.loading = false; }
    },

    clearFile() { this.selectedFile = null; if (this.$refs.fileInput) this.$refs.fileInput.value = null; },

    async getAgentInfo() {
      this.loading = true; this._clearError();
      try {
        const result = await this._withTimeout(() => agentApi.sendPromptToAgent({ agent: this.chosenAgent, mode: this.selectedMode, prompt: '' }), NETWORK_TIMEOUT);
        this._recordResponse(result);
        this._pushHistory({ agent: this.chosenAgent, mode: this.selectedMode, prompt: '', success: true, result });
      } catch (err) {
        this._setError(err.message || String(err)); this._pushHistory({ agent: this.chosenAgent, mode: this.selectedMode, prompt: '', success: false, error: err.message || String(err) });
      } finally { this.loading = false; }
    },

    // ------------------ History & persistence ------------------
    loadHistory() {
      try { this.interactionHistory = JSON.parse(localStorage.getItem('agentLogsMobile') || '[]'); } catch { this.interactionHistory = []; }
    },
    _pushHistory(entry) {
      const record = { agent: entry.agent || this.chosenAgent, mode: entry.mode || 'unknown', prompt: entry.prompt || '', timestamp: new Date().toISOString(), success: !!entry.success, result: entry.result || null, error: entry.error || null };
      this.interactionHistory.unshift(record);
      if (this.interactionHistory.length > 50) this.interactionHistory.length = 50;
      try { localStorage.setItem('agentLogsMobile', JSON.stringify(this.interactionHistory)); } catch {}
    },
    clearHistory() { try { localStorage.removeItem('agentLogsMobile'); } catch {} this.interactionHistory = []; },

    // ------------------ Utilities ------------------
    _readFileAsText(file) { return new Promise((resolve, reject) => { const reader = new FileReader(); reader.onload = e => resolve(e.target.result); reader.onerror = () => reject(new Error('File read failed')); reader.readAsText(file); }); },
    _withTimeout(fnOrPromise, ms) { const p = typeof fnOrPromise === 'function' ? fnOrPromise() : fnOrPromise; let timeout; const t = new Promise((_, reject) => { timeout = setTimeout(() => reject(new Error('Network timeout')), ms); }); return Promise.race([p.finally(() => clearTimeout(timeout)), t]); },

    _recordResponse(result) { this.response = result.reply || result.result || result; this.lastUsedAgent = this.chosenAgent; this.responseTimestamp = new Date().toISOString(); },

    _setError(msg) { this.response = null; this.lastUsedAgent = null; this.responseTimestamp = null; this.error = msg; console.warn(msg); },
    _clearError() { this.error = null; },

    getAgentColor(id) { return agentApi.AGENT_CAPABILITIES?.[id]?.color || '#444'; },
    getAgentIcon(id) { return agentApi.AGENT_CAPABILITIES?.[id]?.icon || '🤖'; },
    getAgentName(id) { return agentApi.AGENT_CAPABILITIES?.[id]?.name || id || 'Agent'; },

    formatTimestamp(ts) { try { return new Date(ts).toLocaleString(); } catch { return ts; } },
    formatFileSize(bytes) { if (!Number.isFinite(bytes) || bytes === 0) return '0 B'; const units = ['B','KB','MB','GB']; const i = Math.floor(Math.log(bytes) / Math.log(1024)); return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`; },
    formatJsonValue(v) { try { return typeof v === 'object' ? JSON.stringify(v, null, 2) : String(v); } catch { return String(v); } },

    copyResponse() { try { const text = typeof this.response === 'string' ? this.response : JSON.stringify(this.response, null, 2); navigator.clipboard?.writeText(text); } catch {} },

    saveDraft() { if (!this.prompt || !this.prompt.trim()) return; try { const drafts = JSON.parse(localStorage.getItem('agentDraftsMobile') || '[]'); drafts.unshift({ prompt: this.prompt, ts: new Date().toISOString() }); if (drafts.length > 10) drafts.length = 10; localStorage.setItem('agentDraftsMobile', JSON.stringify(drafts)); } catch {} },

    modeShort(m) { const map = { prompt: 'PROMPT', file: 'FILE', status: 'STATUS', info: 'INFO' }; return map[m] || m.toUpperCase(); },

    // ------------------ Edge swipe (lightweight) ------------------
    onTouchStart(e) { const t = e.touches && e.touches[0]; if (!t) return; this.touchStartX = t.clientX; this.touchStartY = t.clientY; },
    onTouchMove(e) { const t = e.touches && e.touches[0]; if (!t) return; this.touchEndX = t.clientX; this.touchEndY = t.clientY; },
    onTouchEnd() {
      const dx = this.touchEndX - this.touchStartX;
      const dy = Math.abs(this.touchEndY - this.touchStartY);
      if (Math.abs(dx) > 80 && dy < 60) {
        if (dx > 0) { /* swipe right */ this.toggleDrawer(); }
        else { /* swipe left */ this.openEdgePanel(); }
      }
      this.touchStartX = this.touchEndX = this.touchStartY = this.touchEndY = 0;
    }
  }
};
</script>

<style scoped>
:root {
  --bg: #0e0e0e;
  --panel: #161616;
  --accent: #00c853;
  --muted: #9aa0a6;
  --danger: #ff5252;
  --radius: 12px;
  --touch: 48px;
}

/* High contrast mode override */
:root.high-contrast, :root.high-contrast * {
  filter: none;
  color-scheme: dark;
}
.high-contrast body, .high-contrast .panel { background: #000 !important; color: #fff !important; }

/* Base layout */
.mobile-dashboard { background: var(--bg); color: #fff; min-height: 100vh; display:flex; flex-direction:column; }
.topbar { display:flex; align-items:center; justify-content:space-between; padding:8px 12px; background:linear-gradient(180deg,#0b0b0b,#121212); border-bottom:1px solid rgba(255,255,255,0.04); z-index:50; position:relative; }
.nav-btn, .action-btn { background:transparent; border:none; color:var(--accent); font-size:1.1rem; width:var(--touch); height:var(--touch); display:inline-flex; align-items:center; justify-content:center; }
.title { font-weight:700; font-size:1rem; text-align:center; flex:1; }

/* Drawers & edge panel */
.drawer { position:fixed; top:0; left:0; width:86%; max-width:420px; height:100vh; background:var(--panel); padding:12px; transform:translateX(-110%); transition:transform 220ms ease; z-index:60; box-shadow:6px 0 20px rgba(0,0,0,0.6); }
.drawer.open { transform:translateX(0); }
.left-drawer .drawer-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.edge-panel { position:fixed; top:0; right:0; width:86%; max-width:420px; height:100vh; background:var(--panel); padding:12px; transform:translateX(110%); transition:transform 220ms ease; z-index:60; box-shadow:-6px 0 20px rgba(0,0,0,0.6); }
.edge-panel.open { transform:translateX(0); }
.edge-actions { display:flex; flex-direction:column; gap:10px; margin-top:8px; }
.edge-action { padding:10px; border-radius:8px; background:rgba(255,255,255,0.02); border:1px solid rgba(255,255,255,0.02); color:var(--muted); }
.edge-header { display:flex; justify-content:space-between; align-items:center; }

/* Content */
.content { padding:12px; display:flex; flex-direction:column; gap:12px; margin-bottom:60px; }
.panel { background: rgba(255,255,255,0.02); border-radius:10px; padding:10px; }
.mode-panel .modes { display:flex; gap:8px; overflow:auto; padding-bottom:6px; }
.mode-pill { padding:8px 12px; border-radius:999px; background:transparent; border:1px solid rgba(255,255,255,0.04); color:var(--muted); min-height:40px; }
.mode-pill.active { background:var(--accent); color:#000; border-color:var(--accent); }
.chosen-agent-inline { display:flex; justify-content:space-between; align-items:center; margin-top:8px; }

/* Inputs */
.prompt-input { width:100%; padding:10px; border-radius:8px; background:#0b0b0b; color:#fff; border:1px solid rgba(255,255,255,0.04); min-height:80px; resize:vertical; font-size:14px; }
.primary-btn { background:var(--accent); color:#000; border:none; padding:10px 14px; border-radius:8px; min-height:44px; font-weight:700; }
.ghost-btn { background:transparent; color:var(--accent); border:1px solid rgba(255,255,255,0.04); padding:10px 12px; border-radius:8px; min-height:44px; }

/* Floating icons (FAB) */
.floating-icons { position:fixed; right:16px; bottom:72px; display:flex; flex-direction:column; gap:8px; z-index:70; }
.main-fab { width:56px; height:56px; border-radius:50%; background:var(--accent); color:#000; border:none; font-size:20px; box-shadow:0 6px 18px rgba(0,0,0,0.4); }
.fab-menu { display:flex; flex-direction:column; gap:8px; margin-bottom:8px; }
.fab-item { width:48px; height:48px; border-radius:50%; background:rgba(255,255,255,0.04); color:#fff; border:none; }

/* Bottom sheet (slide-up) */
.bottom-sheet { position:fixed; left:0; right:0; bottom:0; transform:translateY(110%); transition:transform 260ms ease; background:var(--panel); border-top-left-radius:12px; border-top-right-radius:12px; z-index:80; box-shadow:0 -8px 24px rgba(0,0,0,0.6); max-height:70vh; overflow:auto; }
.bottom-sheet.open { transform:translateY(0); }
.sheet-handle { width:100%; text-align:center; padding:6px 0; cursor:pointer; color:var(--muted); }
.sheet-content { padding:12px; }

/* Floating window modal */
.float-window-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.45); display:flex; align-items:center; justify-content:center; z-index:90; }
.float-window { width:92%; max-width:640px; background:var(--panel); border-radius:10px; box-shadow:0 12px 40px rgba(0,0,0,0.7); outline:none; max-height:80vh; overflow:auto; }
.float-header { display:flex; justify-content:space-between; align-items:center; padding:12px; border-bottom:1px solid rgba(255,255,255,0.03); }
.float-body { padding:12px; }

/* Response / history */
.response-panel .resp-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.agent-badge { padding:6px 8px; border-radius:8px; color:#000; font-weight:700; display:inline-block; }
.resp-body { background:#0a0a0a; padding:10px; border-radius:8px; color:var(--accent); max-height:240px; overflow:auto; }
.history-list { list-style:none; padding:0; margin:8px 0 0 0; max-height:180px; overflow:auto; }
.history-row { display:flex; justify-content:space-between; padding:8px 6px; border-bottom:1px solid rgba(255,255,255,0.02); }

/* small misc */
.sr-only { position:absolute !important; clip:rect(1px,1px,1px,1px); height:1px; width:1px; overflow:hidden; white-space:nowrap; border:0; padding:0; margin:-1px;}
.small-btn { background:transparent; border:1px solid rgba(255,255,255,0.04); color:var(--muted); padding:6px 8px; border-radius:8px; }
.close-btn { background:transparent; border:none; color:var(--muted); font-size:1.05rem; }

/* reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .drawer, .edge-panel, .bottom-sheet, .floating-icons, .float-window { transition: none !important; }
}
</style>
