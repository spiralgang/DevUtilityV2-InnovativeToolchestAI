```markdown
# nnimm v2 — AI training guide (concise, practical)

Purpose
- Provide a minimal, safe dataset & prompt-template guide so helpers can prepare training data to fine-tune or build retrieval-augmented prompts for UBULITE features.

Data format (recommended)
- JSONL, one example per line. Each line: {"prompt": "...", "response":"...", "meta":{...}}
- Example:
  {"prompt":"Summarize this repo in 3 lines: <repo_readme_text>","response":"UBULITE is...","meta":{"source":"repo_readme","date":"2025-08-31","license":"MIT"}}

Fields to include
- prompt — user-facing input or instruction.
- response — high-quality target output (developer-authored).
- meta — structured metadata: source, author, date (ISO8601), license, redaction_level.

Quality rules (short)
- Keep responses <= 512 tokens for fast fine-tuning iterations.
- No PII — scrub emails, tokens, secrets.
- Prefer deterministic/concise answers. Avoid hallucinated facts.
- Use neutral, technical tone matching UBULITE docs style.

Prompt templates (examples)
- Summarize:
  Prompt: "Summarize the following text in 2–3 sentences: <<<text>>>"
- Convert to code:
  Prompt: "Given this requirement, produce a minimal Node Express handler: <<<requirement>>>"
- Explain change:
  Prompt: "Explain why this change fixes the bug and list possible regressions."

Data size & splits
- Small experiments: 1k–5k examples.
- Useful validation split: 10% validation, 10% test.
- Keep a separate human-reviewed eval set (100–500 examples).

Privacy & compliance
- Remove or redact any file containing API keys, credentials, or user data.
- Add meta.redaction_level: "full" or "partial" when redaction applied.
- Keep license info in meta. Only include examples compatible with desired licensing.

Fine-tuning vs RAG
- Fine-tuning: use if you want model behavior changes (small curated dataset).
- RAG (retrieval): recommended when source knowledge must stay up-to-date (index docs and serve context).

Evaluation metrics (practical)
- BLEU / ROUGE for automated checks (coarse).
- Human pass/fail: correctness, hallucination, relevancy.
- Measure fallback rate: % of times client falls back from Puter to server.

How helpers should contribute
- Provide JSONL files in data/ with the naming pattern: data/nnimm_v2/*.jsonl
- Include a short contributor metadata file contributors.yaml listing name/email (or handle) and role.
- Run a smoke check script (e.g., node scripts/validate_jsonl.js) to ensure JSONL is valid.

Quick checklist for uploaders
- Remove secrets.
- Normalize whitespace.
- Include meta.source and meta.license.
- Provide 10–30 example prompts per contributor to start.

References & standards
- Internal /reference vault (project canonical standards).
- OpenAI fine-tuning & RAG docs (consult for model-specific details).
```