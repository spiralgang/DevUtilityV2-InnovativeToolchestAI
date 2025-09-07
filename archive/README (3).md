<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

# Hardened Ban/Allow Stack (privileged)

Scope
- Block “banned” domains/hosts/strings with OS-level teeth.
- Ensure outbound TLS trusts only org‑approved CAs and (optionally) pins SPKI.
- Constrain processes by policy (SELinux) and by syscalls (seccomp).
- Automate DNS marking -> ipset -> nftables drop.
- Provide client pinning configs (Android XML + curl).

Pipeline
1) DNS marking: dnsmasq tags banned domains into ipset "blocked4"/"blocked6".
2) ipset_sync.sh resolves and seeds IPs for immediate blocking (boot-time).
3) nftables-ban.nft drops any packet matching those sets (ingress/egress).
4) SELinux: label sensitive dirs as `ban_space_t`. Only `ban_enforcer_t` can touch; apps lack `allow` so reads fail.
5) seccomp: deny risky syscalls (mount, ptrace, keyctl, perf_event_open, bpf, kexec, module syscalls, unshare CLONE_NEW* etc.).
6) Trust: install org CA into OS trust, and pin where possible (Android XML, curl pinnedpubkey).

Operational model
- Privileged units (systemd) run on boot to keep sets fresh.
- All configs are declarative; changes are tracked in VCS. No secrets are committed.

References
- /reference vault (policy patterns, nftables/ipset, SELinux module patterns, seccomp baseline)
- nftables, dnsmasq, ipset, SELinux CIL/TE, seccomp‑bpf