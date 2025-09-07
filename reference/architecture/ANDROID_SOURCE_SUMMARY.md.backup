# Android Source Summary â€” where /data, app sandboxing, mounts and permissions are implemented (AOSP)

This file summarizes the parts of the Android source tree (platform/superproject on cs.android.com) that control:
- mounting and device filesystems,
- storage daemon(s) that provide shared storage,
- app installation and data directory creation (ownership/UID),
- SELinux contexts and file_contexts that enforce access to /data and app sandboxes.

Use these links to inspect the code on cs.android.com (main branch). If a link opens a directory, explore files such as `*.c`, `*.cpp`, `*.java`, `*.rc`, and policy files.

1) init and mount logic (system/level early startup, fstab)
- Purpose: parse fstab, mount /data, create mount namespaces for services
- Location(s):
  - https://cs.android.com/android/platform/superproject/+/main:system/core/init
  - fs_mgr and mount helpers:
    - https://cs.android.com/android/platform/superproject/+/main:system/core/fs_mgr
  - init scripts and *.rc that mount partitions:
    - https://cs.android.com/android/platform/superproject/+/main:system/core/rootdir (device-specific)
    - Search `mount`/`mount_all`/`fstab` in `system/core/init` and `system/core/fs_mgr`

2) vold (Volume Daemon) and storage management
- Purpose: manage emulated storage, adoptable storage, create emulated user paths (e.g., /storage/emulated/0)
- Location:
  - https://cs.android.com/android/platform/superproject/+/main:system/vold
- Why it matters: vold provides and enforces user-facing mountpoints (sdcard, emulated), and exposes storage to apps (via FUSE or kernel loopback).

3) installd, package manager and data dir creation / ownership
- Purpose: set filesystem ownership/permissions when packages are installed; create /data/data/<pkg> and /data/user/<user>/<pkg>.
- Key code:
  - Package manager (assigns UIDs, coordinates install):
    - https://cs.android.com/android/platform/superproject/+/main:frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java
  - installd (helper that performs chown/chmod, etc. as root):
    - https://cs.android.com/android/platform/superproject/+/main:system/core/installd
  - Installer interface used by PackageManagerService to request filesystem changes
- Why it matters: these components enforce directory ownership (UID/GID), which is why chroot-root can't simply read app data.

4) SELinux policy and file contexts
- Purpose: SELinux labels and policies restrict cross-domain access. File contexts define labels applied to filesystem paths (including /data, app data dirs).
- Key places to inspect:
  - system wide policy:
    - https://cs.android.com/android/platform/superproject/+/main:system/sepolicy
  - app domain matching / app contexts:
    - https://cs.android.com/android/platform/superproject/+/main:system/sepolicy/private/seapp_contexts
  - file context rules:
    - search `file_contexts` within `system/sepolicy` and device overlays
- Why it matters: SELinux can block processes even running as root inside a different namespace. File contexts and domain rules are the main enforcement points.

5) Multi-user data layout and emulation
- Paths: `/data/data/<pkg>` historically, `/data/user/<user>/<pkg>` used for multi-user. Mapping logic is created by the package manager and installd (server side).
- Where to read:
  - Package lifecycle code in frameworks/base and system services (PackageManagerService, UserManager).
  - Storage manager and vold for emulated storage.

6) Useful search tips for cs.android.com
- To search for code that references `/data/user/0`:
  - https://cs.android.com/android/platform/superproject/+/main: (use the search box) or:
  - https://cs.android.com/search?q=%2Fdata%2Fuser%2F0
- To inspect SELinux app contexts quickly:
  - https://cs.android.com/android/platform/superproject/+/main:system/sepolicy/private/seapp_contexts
- To inspect installd:
  - https://cs.android.com/android/platform/superproject/+/main:system/core/installd

7) Practical implication for your recovery situation
- /data and app directories are protected by:
  - Linux permissions (uid/gid) set by installd / PackageManagerService actions at install time.
  - SELinux file contexts and policy (system/sepolicy).
  - Mount/namespace isolation (init, kernel mount options, PRoot/containers).
- Conclusion: Running `tar` inside your UserLAnd/PRoot chroot often cannot read host /data due to these protections. The reliable method is to run a host-root command (su on device host shell) or use a root-capable file manager (CX with root) to create an archive in shared storage (e.g., `/storage/emulated/0`).

8) Quick links (main branch)
- frameworks/base (PackageManagerService, installers):
  - https://cs.android.com/android/platform/superproject/+/main:frameworks/base/
- system/core/init:
  - https://cs.android.com/android/platform/superproject/+/main:system/core/init
- system/core/fs_mgr:
  - https://cs.android.com/android/platform/superproject/+/main:system/core/fs_mgr
- system/core/installd:
  - https://cs.android.com/android/platform/superproject/+/main:system/core/installd
- system/vold:
  - https://cs.android.com/android/platform/superproject/+/main:system/vold
- system/sepolicy:
  - https://cs.android.com/android/platform/superproject/+/main:system/sepolicy
- Search tips: use cs.android.com search for `data/user/0`, `installd`, `PackageManagerService`, `seapp_contexts`, `vold`, `fstab`.

References:
- AOSP code search: https://cs.android.com/android/platform/superproject/+/main:
- See the sections above for exact directories to open on cs.android.com.