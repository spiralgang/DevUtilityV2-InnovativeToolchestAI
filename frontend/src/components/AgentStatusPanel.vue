<template>
  <div
    class="agent-status-panel"
    role="region"
    :aria-label="`Agent Status Panel — ${environmentLabel}`"
  >
    <h3 class="heading">Agent Status Panel</h3>

    <p class="env" v-if="environmentLabel">Environment: <strong>{{ environmentLabel }}</strong></p>
    <p class="coord" v-if="coordinationLabel">Coordination: <strong>{{ coordinationLabel }}</strong></p>

    <!-- Accessible status: role=status with polite live region so assistive tech is informed -->
    <div
      class="status-badge"
      role="status"
      aria-live="polite"
      :class="statusClass"
    >
      <span class="visually-hidden">Agent status:</span>
      <span class="status-text" aria-hidden="true">{{ statusText }}</span>
    </div>
  </div>
</template>

<script>
/*
  AgentStatusPanel.vue
  - Production-quality rewrite of a small status widget.
  - Goals:
    * Accept initial values via props and normalize them.
    * Provide programmatic API for updates (updateStatus, setEnvironment, setCoordination).
    * Expose accessible semantics (region + status live region).
    * Themeable via CSS variables and respectful of prefers-reduced-motion.

  Usage:
    <AgentStatusPanel :initial-environment="'build_server'" :initial-status="'active'" :initial-coordination="'mixtral'" />

  Public methods (component instance):
    - updateStatus(value)         // accepts "active", "inactive", "degraded", etc.
    - setEnvironment(env)
    - setCoordination(coord)

  Design notes:
    - Keep logic minimal and auditable: string normalization + deterministic tier mapping.
    - No external dependencies so it's easy to unit test and integrate (Vuex/Pinia or sockets).
*/
export default {
  name: 'AgentStatusPanel',
  props: {
    initialEnvironment: {
      type: String,
      default: 'building_env'
    },
    initialStatus: {
      type: String,
      default: 'active'
    },
    initialCoordination: {
      type: String,
      default: 'mixtral'
    }
  },
  data() {
    return {
      environment: this.initialEnvironment || '',
      statusRaw: this.initialStatus || '',
      coordination: this.initialCoordination || ''
    }
  },
  computed: {
    environmentLabel() {
      return this.environment ? String(this.environment).replace(/[_-]+/g, ' ') : ''
    },
    coordinationLabel() {
      return this.coordination ? String(this.coordination).replace(/[_-]+/g, ' ') : ''
    },
    // Normalized, human-friendly status text
    statusText() {
      const s = this._normalizeStatus(this.statusRaw)
      if (s === 'active') return 'Active'
      if (s === 'idle') return 'Idle'
      if (s === 'degraded') return 'Degraded'
      if (s === 'offline') return 'Offline'
      return String(this.statusRaw || 'Unknown')
    },
    // CSS class mirrors semantic tier
    statusClass() {
      const s = this._normalizeStatus(this.statusRaw)
      return {
        active: s === 'active',
        idle: s === 'idle',
        degraded: s === 'degraded',
        offline: s === 'offline',
        unknown: !['active','idle','degraded','offline'].includes(s)
      }
    }
  },
  methods: {
    // Public API: update status; accepts "Active", "active", "ACTIVE", null, or other strings.
    updateStatus(value) {
      if (value == null) {
        this.statusRaw = ''
        return true
      }
      const v = String(value).trim()
      this.statusRaw = v
      return true
    },
    setEnvironment(env) {
      if (env == null) {
        this.environment = ''
        return true
      }
      this.environment = String(env).trim()
      return true
    },
    setCoordination(coord) {
      if (coord == null) {
        this.coordination = ''
        return true
      }
      this.coordination = String(coord).trim()
      return true
    },
    // Internal helper: normalize to a small set of canonical statuses
    _normalizeStatus(raw) {
      if (!raw && raw !== 0) return 'unknown'
      const s = String(raw).trim().toLowerCase()
      // Common synonyms
      if (['active','running','online','up'].includes(s)) return 'active'
      if (['idle','standby','paused','waiting'].includes(s)) return 'idle'
      if (['degraded','warn','warning','partial'].includes(s)) return 'degraded'
      if (['offline','down','stopped','terminated'].includes(s)) return 'offline'
      return s || 'unknown'
    }
  }
}
</script>

<style scoped>
:root {
  --panel-bg: #071027;
  --text: #e6eef6;
  --muted: #9aa4b2;
  --pad: 16px;
  --radius: 10px;
  --status-height: 2rem;
  --color-active: #16a34a;
  --color-idle: #0ea5a4;
  --color-degraded: #f59e0b;
  --color-offline: #ef4444;
  --transition: 220ms;
}

/* Container */
.agent-status-panel {
  background: var(--panel-bg);
  color: var(--text);
  padding: var(--pad);
  border-radius: calc(var(--radius) + 2px);
  max-width: 480px;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
  box-shadow: 0 2px 8px rgba(2,6,23,0.65);
}

/* Heading & meta */
.heading {
  margin: 0 0 8px 0;
  font-size: 1.05rem;
  font-weight: 600;
}
.env, .coord {
  margin: 0 0 8px 0;
  color: var(--muted);
  font-size: 0.92rem;
}

/* Status badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  height: var(--status-height);
  padding: 0 12px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.95rem;
  box-sizing: border-box;
  transition: background-color var(--transition) ease, color var(--transition) ease, transform var(--transition) ease;
}

/* Visual variants */
.status-badge.active {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-active) 82%, #08964a), var(--color-active));
  color: #032012;
}
.status-badge.idle {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-idle) 82%, #038f8e), var(--color-idle));
  color: #022021;
}
.status-badge.degraded {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-degraded) 82%, #d97706), var(--color-degraded));
  color: #2b1600;
}
.status-badge.offline {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-offline) 82%, #dc2626), var(--color-offline));
  color: #2b0b0b;
}
.status-badge.unknown {
  background: rgba(255,255,255,0.06);
  color: var(--muted);
}

/* Visually-hidden helper for screen readers */
.visually-hidden {
  position: absolute !important;
  height: 1px; width: 1px;
  overflow: hidden;
  clip: rect(1px, 1px, 1px, 1px);
  white-space: nowrap;
  border: 0;
  padding: 0;
  margin: -1px;
}

/* Respect reduced motion */
@media (prefers-reduced-motion: reduce) {
  .status-badge {
    transition: none;
  }
}
</style>

<!--
References:
- /reference (project canonical vault) — foundational UI & accessibility standards (see vault for project rules)
- WAI-ARIA Authoring Practices — progress & status patterns (https://www.w3.org/WAI/ARIA/apg/)
- MDN: prefers-reduced-motion (https://developer.mozilla.org/docs/Web/CSS/@media/prefers-reduced-motion)
- Vue style guide & accessibility recommendations (https://v3.vuejs.org/style-guide/)
-->
