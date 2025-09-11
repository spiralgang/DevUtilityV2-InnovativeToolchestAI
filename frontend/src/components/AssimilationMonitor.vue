<template>
  <div
    class="assimilation-monitor"
    role="region"
    :aria-label="`Assimilation Monitor — ${environmentLabel}`"
  >
    <h3 class="heading">Assimilation Monitor</h3>

    <p class="env" v-if="environmentLabel">Environment: <strong>{{ environmentLabel }}</strong></p>

    <!-- Accessible progress bar: exposes aria-valuenow/min/max -->
    <div
      class="assimilation-bar"
      role="progressbar"
      :aria-valuenow="assimilationPercent"
      aria-valuemin="0"
      aria-valuemax="100"
    >
      <div
        class="assimilation-fill"
        :style="{ width: assimilationPercent + '%' }"
        :class="statusClass"
      >
        <span class="visually-hidden">Assimilation</span>
        <span class="fill-label" aria-hidden="true">{{ assimilationPercent }}%</span>
      </div>
    </div>

    <p class="status-text" :class="statusClass">{{ statusText }}</p>
  </div>
</template>

<script>
/*
  AssimilationMonitor.vue
  - Production-quality rewrite of a small status widget.
  - Goals:
    * Accept numeric or percent inputs, normalize and clamp between 0-100.
    * Provide accessible progressbar semantics (WAI-ARIA).
    * Expose a simple API (props + update methods) to integrate with sockets or store.
    * Use CSS variables and prefers-reduced-motion for OS-level motion preferences.

  Usage:
    <AssimilationMonitor :initial-environment="'build_server'" :initial-assimilation="87" />
    or
    <AssimilationMonitor initial-assimilation="100%" />

  Public methods (component instance):
    - updateAssimilation(value) // accepts number or numeric string (e.g. "75" or "75%")
*/
export default {
  name: 'AssimilationMonitor',
  props: {
    initialEnvironment: {
      type: String,
      default: 'building_env'
    },
    initialAssimilation: {
      // Accept Number or String ("85" or "85%")
      type: [Number, String],
      default: 100
    }
  },
  data() {
    const parse = (v) => {
      if (v == null) return 0
      const s = String(v).trim().replace('%', '')
      const n = Number(s)
      return Number.isFinite(n) ? n : 0
    }
    return {
      environment: this.initialEnvironment || '',
      assimilationValue: parse(this.initialAssimilation)
    }
  },
  computed: {
    assimilationPercent() {
      // Ensure integer percentage clamped 0..100
      const n = Math.round(Number(this.assimilationValue) || 0)
      return Math.max(0, Math.min(100, n))
    },
    statusClass() {
      // returns CSS class name: 'high' | 'medium' | 'low'
      if (this.assimilationPercent >= 90) return 'high'
      if (this.assimilationPercent >= 50) return 'medium'
      return 'low'
    },
    statusText() {
      const p = this.assimilationPercent
      if (p >= 100) return 'Fully assimilated'
      if (p >= 90) return 'Healthy'
      if (p >= 50) return 'Degraded'
      return 'Critical'
    },
    environmentLabel() {
      // make label human-friendly; do not mutate original
      return this.environment ? this.environment.replace(/[_-]+/g, ' ') : ''
    }
  },
  methods: {
    // Public: accepts 42, "42", "42%", "  42 % "
    updateAssimilation(value) {
      const s = String(value).trim().replace('%', '')
      const n = Number(s)
      if (!Number.isFinite(n)) return false
      this.assimilationValue = Math.max(0, Math.min(100, n))
      return true
    },
    // Convenience: update environment label
    setEnvironment(env) {
      if (typeof env !== 'string') return false
      this.environment = env
      return true
    }
  }
}
</script>

<style scoped>
:root {
  /* sensible defaults — can be overridden by parent theme */
  --assimilation-height: 1.5rem;
  --bg: #0b1220;
  --bar-bg: #1e293b; /* dark slate */
  --fill-high: #16a34a; /* green */
  --fill-medium: #f59e0b; /* amber */
  --fill-low: #ef4444; /* red */
  --text-muted: #9aa4b2;
  --radius: 0.5rem;
  --transition-duration: 320ms;
}

/* Container */
.assimilation-monitor {
  background: var(--bg);
  color: #e6eef6;
  padding: 16px;
  border-radius: calc(var(--radius) + 0.25rem);
  max-width: 420px;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
  box-shadow: 0 1px 6px rgba(2,6,23,0.6);
}

/* Heading & meta */
.heading {
  margin: 0 0 8px 0;
  font-size: 1.05rem;
  font-weight: 600;
}

.env {
  margin: 0 0 12px 0;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* Progress bar shell */
.assimilation-bar {
  background: var(--bar-bg);
  border-radius: var(--radius);
  height: var(--assimilation-height);
  overflow: hidden;
  position: relative;
}

/* Fill element with smooth width transition and color variants */
.assimilation-fill {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  color: #021018;
  font-weight: 700;
  border-radius: var(--radius);
  transition: width var(--transition-duration) ease, background-color var(--transition-duration) ease;
  box-sizing: border-box;
}

/* Label inside fill */
.fill-label {
  font-size: 0.9rem;
  color: #021018;
}

/* Status text below bar */
.status-text {
  margin: 10px 0 0 0;
  font-size: 0.9rem;
  font-weight: 600;
}

/* Variants */
.assimilation-fill.high {
  background: linear-gradient(90deg, var(--fill-high), #10b981);
  color: #042012;
}
.assimilation-fill.medium {
  background: linear-gradient(90deg, var(--fill-medium), #fbbf24);
  color: #2b1600;
}
.assimilation-fill.low {
  background: linear-gradient(90deg, var(--fill-low), #ef4444cc);
  color: #2b0b0b;
}

/* Mirror status class to status-text for subtle color */
.status-text.high { color: var(--fill-high); }
.status-text.medium { color: var(--fill-medium); }
.status-text.low { color: var(--fill-low); }

/* Accessibility: visually-hidden helper */
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

/* Respect user's reduced motion preference */
@media (prefers-reduced-motion: reduce) {
  .assimilation-fill {
    transition: none;
  }
}
</style>

<!--
References:
- Primary style & component decisions: /reference vault
- Vue component style guidance: https://v3.vuejs.org/style-guide/ (composition compatible)
- WAI-ARIA progressbar pattern: https://www.w3.org/WAI/ARIA/apg/patterns/progressbar/
- prefers-reduced-motion guidance: https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-reduced-motion
- CSS variables & accessibility color contrast: https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties
-->
