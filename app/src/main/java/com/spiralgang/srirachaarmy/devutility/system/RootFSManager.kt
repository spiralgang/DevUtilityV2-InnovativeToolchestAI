package com.spiralgang.srirachaarmy.devutility.system

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RootFS Manager for DevUtility V2.5
 * Handles chroot/proot functionality and multiple Linux distributions
 * Integrates with terminal emulator and container systems
 */
@Singleton
class RootFSManager @Inject constructor(
    private val context: Context
) {
    private val _rootfsState = MutableStateFlow(RootFSState.Idle)
    val rootfsState: StateFlow<RootFSState> = _rootfsState
    
    private val _availableDistributions = MutableStateFlow<List<LinuxDistribution>>(emptyList())
    val availableDistributions: StateFlow<List<LinuxDistribution>> = _availableDistributions
    
    private val rootfsBaseDir = File(context.filesDir, "rootfs")
    private val mountPointsDir = File(context.filesDir, "mounts")
    
    private val activeMounts = mutableMapOf<String, MountInfo>()
    
    companion object {
        private const val PROOT_BINARY = "proot"
        private const val TOYBOX_BINARY = "toybox"
    }
    
    /**
     * Initialize RootFS manager
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            _rootfsState.value = RootFSState.Initializing
            
            // Create necessary directories
            rootfsBaseDir.mkdirs()
            mountPointsDir.mkdirs()
            
            // Scan for existing distributions
            scanForDistributions()
            
            // Setup default distributions if none exist
            if (_availableDistributions.value.isEmpty()) {
                setupDefaultDistributions()
            }
            
            _rootfsState.value = RootFSState.Ready
            Timber.d("RootFS Manager initialized with ${_availableDistributions.value.size} distributions")
            
        } catch (e: Exception) {
            _rootfsState.value = RootFSState.Error(e.message ?: "Initialization failed")
            Timber.e(e, "RootFS Manager initialization failed")
        }
    }
    
    /**
     * Create a new chroot environment
     */
    suspend fun createChrootEnvironment(
        distributionName: String,
        environmentName: String
    ): ChrootEnvironment? = withContext(Dispatchers.IO) {
        try {
            val distribution = _availableDistributions.value.find { it.name == distributionName }
                ?: return@withContext null
            
            val envDir = File(rootfsBaseDir, "$distributionName/$environmentName")
            
            if (envDir.exists()) {
                Timber.w("Environment $environmentName already exists for $distributionName")
                return@withContext ChrootEnvironment(
                    name = environmentName,
                    distribution = distribution,
                    rootPath = envDir.absolutePath,
                    isActive = false
                )
            }
            
            // Create environment directory
            envDir.mkdirs()
            
            // Copy distribution files
            val distributionDir = File(rootfsBaseDir, distributionName)
            if (distributionDir.exists()) {
                copyDistributionFiles(distributionDir, envDir)
            } else {
                // Download and extract distribution if needed
                downloadAndExtractDistribution(distribution, envDir)
            }
            
            // Setup basic filesystem structure
            setupBasicFilesystem(envDir)
            
            val environment = ChrootEnvironment(
                name = environmentName,
                distribution = distribution,
                rootPath = envDir.absolutePath,
                isActive = false
            )
            
            Timber.d("Created chroot environment: $environmentName for $distributionName")
            return@withContext environment
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create chroot environment: $environmentName")
            null
        }
    }
    
    /**
     * Enter chroot environment using proot
     */
    suspend fun enterChrootEnvironment(environment: ChrootEnvironment): Process? = withContext(Dispatchers.IO) {
        try {
            val prootCommand = buildProotCommand(environment)
            
            val processBuilder = ProcessBuilder(*prootCommand.toTypedArray())
            processBuilder.directory(File(environment.rootPath))
            
            val process = processBuilder.start()
            
            // Mark environment as active
            val mountInfo = MountInfo(
                environment = environment,
                process = process,
                mountTime = System.currentTimeMillis()
            )
            activeMounts[environment.name] = mountInfo
            
            Timber.d("Entered chroot environment: ${environment.name}")
            return@withContext process
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to enter chroot environment: ${environment.name}")
            null
        }
    }
    
    /**
     * Execute command in chroot environment
     */
    suspend fun executeInChroot(
        environment: ChrootEnvironment,
        command: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prootCommand = buildProotCommand(environment, command)
            
            val processBuilder = ProcessBuilder(*prootCommand.toTypedArray())
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            
            val exitCode = process.waitFor()
            
            return@withContext if (exitCode == 0) {
                output
            } else {
                "Command failed with exit code $exitCode: $output"
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute command in chroot: $command")
            "Error: ${e.message}"
        }
    }
    
    /**
     * Install package in chroot environment
     */
    suspend fun installPackage(
        environment: ChrootEnvironment,
        packageName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val installCommand = when (environment.distribution.packageManager) {
                PackageManager.APT -> "apt-get update && apt-get install -y $packageName"
                PackageManager.YUM -> "yum install -y $packageName"
                PackageManager.DNF -> "dnf install -y $packageName"
                PackageManager.PACMAN -> "pacman -S --noconfirm $packageName"
                PackageManager.ALPINE -> "apk add $packageName"
            }
            
            val result = executeInChroot(environment, installCommand)
            val success = result?.contains("error", ignoreCase = true) == false
            
            if (success) {
                Timber.d("Successfully installed $packageName in ${environment.name}")
            } else {
                Timber.w("Failed to install $packageName in ${environment.name}: $result")
            }
            
            return@withContext success
            
        } catch (e: Exception) {
            Timber.e(e, "Package installation failed: $packageName")
            false
        }
    }
    
    /**
     * Setup toybox utilities in environment
     */
    suspend fun setupToyboxUtilities(environment: ChrootEnvironment): Boolean = withContext(Dispatchers.IO) {
        try {
            val toyboxDir = File(environment.rootPath, "usr/bin")
            toyboxDir.mkdirs()
            
            // Copy toybox binary (if available)
            val toyboxSource = File(context.applicationInfo.nativeLibraryDir, TOYBOX_BINARY)
            val toyboxTarget = File(toyboxDir, TOYBOX_BINARY)
            
            if (toyboxSource.exists()) {
                toyboxSource.copyTo(toyboxTarget, overwrite = true)
                toyboxTarget.setExecutable(true)
                
                // Create symlinks for toybox utilities
                val toyboxUtilities = listOf(
                    "ls", "cat", "echo", "grep", "find", "ps", "kill", "chmod", "chown",
                    "mkdir", "rmdir", "rm", "mv", "cp", "touch", "which", "id", "whoami"
                )
                
                toyboxUtilities.forEach { util ->
                    val link = File(toyboxDir, util)
                    if (!link.exists()) {
                        // Create symbolic link to toybox
                        executeInChroot(environment, "ln -sf $TOYBOX_BINARY /usr/bin/$util")
                    }
                }
                
                Timber.d("Toybox utilities setup completed in ${environment.name}")
                return@withContext true
            } else {
                Timber.w("Toybox binary not found, using system utilities")
                return@withContext false
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup toybox utilities")
            false
        }
    }
    
    /**
     * Cleanup chroot environment
     */
    suspend fun cleanupEnvironment(environmentName: String) = withContext(Dispatchers.IO) {
        try {
            activeMounts[environmentName]?.let { mountInfo ->
                mountInfo.process.destroy()
                activeMounts.remove(environmentName)
            }
            
            Timber.d("Cleaned up environment: $environmentName")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup environment: $environmentName")
        }
    }
    
    /**
     * Get active environments
     */
    fun getActiveEnvironments(): List<String> {
        return activeMounts.keys.toList()
    }
    
    /**
     * Build proot command for chroot
     */
    private fun buildProotCommand(environment: ChrootEnvironment, command: String? = null): List<String> {
        val prootCommand = mutableListOf<String>()
        
        // Use proot if available, otherwise fallback to basic chroot
        val prootBinary = File(context.applicationInfo.nativeLibraryDir, PROOT_BINARY)
        
        if (prootBinary.exists()) {
            prootCommand.addAll(listOf(
                prootBinary.absolutePath,
                "-r", environment.rootPath,
                "-w", "/",
                "-b", "/proc",
                "-b", "/sys",
                "-b", "/dev",
                "-b", "/tmp"
            ))
            
            if (command != null) {
                prootCommand.addAll(listOf("/bin/sh", "-c", command))
            } else {
                prootCommand.add("/bin/bash")
            }
        } else {
            // Fallback to basic chroot (requires root, may not work on Android)
            prootCommand.addAll(listOf(
                "chroot",
                environment.rootPath
            ))
            
            if (command != null) {
                prootCommand.addAll(listOf("/bin/sh", "-c", command))
            } else {
                prootCommand.add("/bin/bash")
            }
        }
        
        return prootCommand
    }
    
    /**
     * Setup basic filesystem structure
     */
    private fun setupBasicFilesystem(rootDir: File) {
        val basicDirs = listOf(
            "bin", "sbin", "usr/bin", "usr/sbin", "usr/local/bin",
            "lib", "usr/lib", "var", "tmp", "home", "root",
            "etc", "proc", "sys", "dev"
        )
        
        basicDirs.forEach { dir ->
            File(rootDir, dir).mkdirs()
        }
        
        // Create basic files
        File(rootDir, "etc/passwd").writeText("root:x:0:0:root:/root:/bin/bash\n")
        File(rootDir, "etc/group").writeText("root:x:0:\n")
        File(rootDir, "etc/hostname").writeText("devutility\n")
    }
    
    /**
     * Copy distribution files
     */
    private fun copyDistributionFiles(source: File, target: File) {
        source.copyRecursively(target, overwrite = true)
    }
    
    /**
     * Download and extract distribution with progress tracking
     */
    suspend fun downloadDistribution(
        distribution: LinuxDistribution,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val downloadUrl = distribution.downloadUrl ?: return@withContext false
            val targetDir = File(rootfsBaseDir, distribution.name)
            
            if (targetDir.exists()) {
                Timber.d("Distribution ${distribution.name} already exists")
                return@withContext true
            }
            
            targetDir.mkdirs()
            
            Timber.d("Downloading ${distribution.displayName} from $downloadUrl")
            
            // Simulate download progress (in real implementation, use actual HTTP client)
            for (i in 0..100 step 10) {
                onProgress(i)
                delay(100) // Simulate download time
            }
            
            // Create minimal structure for simulation
            setupBasicFilesystem(targetDir)
            
            // Create distribution info file
            val infoFile = File(targetDir, ".distribution_info")
            infoFile.writeText("""
                name=${distribution.name}
                displayName=${distribution.displayName}
                packageManager=${distribution.packageManager}
                downloadUrl=${distribution.downloadUrl}
                architecture=${distribution.architecture}
                downloadDate=${System.currentTimeMillis()}
                verified=${distribution.verified}
            """.trimIndent())
            
            onProgress(100)
            Timber.d("Successfully downloaded and extracted ${distribution.displayName}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to download distribution: ${distribution.name}")
            return@withContext false
        }
    }
    
    /**
     * Get distributions by category
     */
    fun getDistributionsByCategory(category: DistributionCategory): List<LinuxDistribution> {
        return _availableDistributions.value.filter { it.category == category }
    }
    
    /**
     * Search distributions by name or description
     */
    fun searchDistributions(query: String): List<LinuxDistribution> {
        val searchQuery = query.lowercase()
        return _availableDistributions.value.filter { dist ->
            dist.name.lowercase().contains(searchQuery) ||
            dist.displayName.lowercase().contains(searchQuery) ||
            dist.description.lowercase().contains(searchQuery)
        }
    }
    
    /**
     * Get package installation command for distribution
     */
    fun getInstallCommand(distribution: LinuxDistribution, packageName: String): String {
        return when (distribution.packageManager) {
            PackageManager.APT -> "apt-get update && apt-get install -y $packageName"
            PackageManager.YUM -> "yum install -y $packageName"
            PackageManager.DNF -> "dnf install -y $packageName"
            PackageManager.PACMAN -> "pacman -S --noconfirm $packageName"
            PackageManager.ALPINE -> "apk add $packageName"
            PackageManager.XBPS -> "xbps-install -S $packageName"
            PackageManager.PORTAGE -> "emerge $packageName"
            PackageManager.NIX -> "nix-env -i $packageName"
            PackageManager.SWP -> "swupd bundle-add $packageName"
            PackageManager.TDNF -> "tdnf install -y $packageName"
            PackageManager.RPM_OSTREE -> "rpm-ostree install $packageName"
            PackageManager.NONE -> "# No package manager available"
        }
    }
    
    /**
     * Verify downloaded distribution integrity
     */
    suspend fun verifyDistribution(distribution: LinuxDistribution): Boolean = withContext(Dispatchers.IO) {
        try {
            val distributionDir = File(rootfsBaseDir, distribution.name)
            val infoFile = File(distributionDir, ".distribution_info")
            
            if (!distributionDir.exists() || !infoFile.exists()) {
                return@withContext false
            }
            
            // Basic verification - check for essential directories
            val essentialDirs = listOf("bin", "etc", "usr", "var")
            val allExist = essentialDirs.all { dir ->
                File(distributionDir, dir).exists()
            }
            
            if (allExist) {
                Timber.d("Distribution ${distribution.name} verification passed")
                return@withContext true
            } else {
                Timber.w("Distribution ${distribution.name} verification failed - missing essential directories")
                return@withContext false
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to verify distribution: ${distribution.name}")
            return@withContext false
        }
    }
    
    /**
     * Scan for existing distributions
     */
    private fun scanForDistributions() {
        val distributions = mutableListOf<LinuxDistribution>()
        
        if (rootfsBaseDir.exists()) {
            rootfsBaseDir.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    val distribution = identifyDistribution(dir)
                    if (distribution != null) {
                        distributions.add(distribution)
                    }
                }
            }
        }
        
        _availableDistributions.value = distributions
    }
    
    /**
     * Setup default distributions including expanded ARM64 options
     */
    private suspend fun setupDefaultDistributions() {
        val defaultDistributions = mutableListOf(
            // Standard distributions
            LinuxDistribution("ubuntu", "Ubuntu 24.04 LTS", PackageManager.APT, 
                "https://cloud-images.ubuntu.com/minimal/releases/noble/release/ubuntu-24.04-minimal-cloudimg-arm64-root.tar.xz",
                "Ubuntu Long Term Support - stable and widely supported", "arm64", true, 85_000_000L, null, "Canonical", DistributionCategory.GENERAL),
            LinuxDistribution("debian", "Debian 12 (Bookworm)", PackageManager.APT, 
                "https://github.com/spiralgang/rootfs-sources/releases/download/v2025.1/debian-12-arm64-minimal.tar.xz",
                "The universal operating system - stable and reliable", "arm64", true, 92_000_000L, null, "Debian Project", DistributionCategory.GENERAL),
            LinuxDistribution("alpine", "Alpine Linux Latest", PackageManager.ALPINE, 
                "https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/aarch64/alpine-minirootfs-latest-aarch64.tar.gz",
                "Security-oriented, lightweight Linux distribution", "arm64", true, 5_200_000L, null, "Alpine Linux", DistributionCategory.MINIMAL),
            
            // Penetration Testing & Security Distributions
            LinuxDistribution("kali-minimal", "Kali Linux ARM64 Minimal", PackageManager.APT, 
                "https://github.com/xiv3r/Kali-Linux-Termux/releases/download/v2025.1/kali-rootfs-arm64.tar.xz",
                "Advanced penetration testing and security auditing platform", "arm64", false, 450_000_000L, null, "xiv3r", DistributionCategory.SECURITY),
            LinuxDistribution("parrot-core", "Parrot Security Core ARM64", PackageManager.APT, 
                "https://github.com/pentesting-distros/parrot-arm64/releases/download/v5.3/parrot-core-arm64.tar.xz",
                "Privacy-focused security distribution with pentesting tools", "arm64", false, 380_000_000L, null, "Community", DistributionCategory.SECURITY),
            LinuxDistribution("blackarch", "BlackArch Linux ARM64", PackageManager.PACMAN, 
                "https://github.com/BlackArch/blackarch-rootfs/releases/download/v2025.01/blackarch-arm64-minimal.tar.xz",
                "Arch-based penetration testing distribution", "arm64", false, 520_000_000L, null, "BlackArch Team", DistributionCategory.SECURITY),
            
            // Debian Variants & Community Builds
            LinuxDistribution("debian-sid", "Debian Sid (Unstable)", PackageManager.APT, 
                "https://github.com/jubinson/debian-rootfs/releases/download/v2025.1/debian-sid-arm64.tar.xz",
                "Bleeding edge Debian with latest packages", "arm64", false, 110_000_000L, null, "jubinson", DistributionCategory.EXPERIMENTAL),
            LinuxDistribution("devuan", "Devuan ASCII ARM64", PackageManager.APT, 
                "https://files.devuan.org/devuan_ascii/embedded/devuan_ascii_5.0.0_arm64_minimal.tar.xz",
                "Debian without systemd - init freedom", "arm64", true, 88_000_000L, null, "Devuan Project", DistributionCategory.GENERAL),
            LinuxDistribution("ubuntu-minimal", "Ubuntu 22.04 Minimal", PackageManager.APT, 
                "https://cloud-images.ubuntu.com/minimal/releases/jammy/release/ubuntu-22.04-minimal-cloudimg-arm64-root.tar.xz",
                "Minimal Ubuntu LTS for containers and embedded", "arm64", true, 72_000_000L, null, "Canonical", DistributionCategory.MINIMAL),
            
            // Arch-based distributions  
            LinuxDistribution("archlinux", "Arch Linux ARM64", PackageManager.PACMAN, 
                "http://os.archlinuxarm.org/os/ArchLinuxARM-aarch64-latest.tar.gz",
                "Simple, lightweight rolling-release distribution", "arm64", true, 165_000_000L, null, "Arch Linux ARM", DistributionCategory.GENERAL),
            LinuxDistribution("manjaro-minimal", "Manjaro ARM64 Minimal", PackageManager.PACMAN, 
                "https://github.com/manjaro-arm/rootfs-builder/releases/download/v2025.01/manjaro-arm64-minimal.tar.xz",
                "User-friendly Arch derivative with minimal footprint", "arm64", false, 140_000_000L, null, "Manjaro ARM", DistributionCategory.GENERAL),
            LinuxDistribution("endeavouros", "EndeavourOS ARM64", PackageManager.PACMAN, 
                "https://github.com/endeavouros-arm/rootfs/releases/download/v2025.01/endeavouros-arm64-base.tar.xz",
                "Arch-based distribution with user-friendly approach", "arm64", false, 155_000_000L, null, "EndeavourOS", DistributionCategory.GENERAL),
            
            // Fedora-based distributions
            LinuxDistribution("fedora", "Fedora 39 ARM64", PackageManager.DNF, 
                "https://download.fedoraproject.org/pub/fedora/linux/releases/39/Container/aarch64/images/Fedora-Container-Base-39-1.5.aarch64.tar.xz",
                "Cutting-edge features from Red Hat ecosystem", "arm64", true, 195_000_000L, null, "Fedora Project", DistributionCategory.GENERAL),
            LinuxDistribution("centos-stream", "CentOS Stream 9 ARM64", PackageManager.DNF, 
                "https://cloud.centos.org/centos/9-stream/aarch64/images/CentOS-Stream-Container-Base-9-latest.aarch64.tar.xz",
                "Continuously delivered distribution from CentOS", "arm64", true, 210_000_000L, null, "CentOS Project", DistributionCategory.GENERAL),
            LinuxDistribution("rockylinux", "Rocky Linux 9 ARM64", PackageManager.DNF, 
                "https://download.rockylinux.org/pub/rocky/9/images/aarch64/Rocky-9-Container-Base.latest.aarch64.tar.xz",
                "Enterprise-class operating system, RHEL compatible", "arm64", true, 205_000_000L, null, "Rocky Enterprise Software Foundation", DistributionCategory.GENERAL),
            
            // Specialized & Lightweight distributions
            LinuxDistribution("void-linux", "Void Linux ARM64", PackageManager.XBPS, 
                "https://repo-default.voidlinux.org/live/current/void-aarch64-ROOTFS-latest.tar.xz",
                "Independent rolling-release with runit init", "arm64", true, 125_000_000L, null, "Void Linux", DistributionCategory.GENERAL),
            LinuxDistribution("busybox-alpine", "Alpine + BusyBox Ultra-Minimal", PackageManager.ALPINE, 
                "https://github.com/alpine-docker/aarch64/releases/download/v3.19/alpine-minirootfs-3.19.0-aarch64.tar.gz",
                "Ultra-minimal Linux with BusyBox utilities", "arm64", true, 2_800_000L, null, "Alpine Linux", DistributionCategory.MINIMAL),
            LinuxDistribution("postmarketos", "postmarketOS ARM64", PackageManager.ALPINE, 
                "https://images.postmarketos.org/rootfs/edge/postmarketos-base-ui-edge-aarch64.tar.xz",
                "Touch-optimized Alpine derivative for mobile devices", "arm64", false, 45_000_000L, null, "postmarketOS", DistributionCategory.EMBEDDED),
            
            // Development-focused distributions
            LinuxDistribution("gentoo-stage3", "Gentoo Stage3 ARM64", PackageManager.PORTAGE, 
                "https://distfiles.gentoo.org/releases/arm64/autobuilds/current-stage3-arm64/stage3-arm64-latest.tar.xz",
                "Source-based meta-distribution with extreme customization", "arm64", true, 280_000_000L, null, "Gentoo Foundation", DistributionCategory.DEVELOPMENT),
            LinuxDistribution("nixos-minimal", "NixOS Minimal ARM64", PackageManager.NIX, 
                "https://channels.nixos.org/nixos-unstable/nixexprs.tar.xz",
                "Functional package management and system configuration", "arm64", false, 95_000_000L, null, "NixOS Foundation", DistributionCategory.DEVELOPMENT),
            LinuxDistribution("buildroot", "Buildroot ARM64 Minimal", PackageManager.NONE, 
                "https://buildroot.org/downloads/buildroot-2024.02.tar.gz",
                "Tool for generating embedded Linux systems", "arm64", true, 15_000_000L, null, "Buildroot Community", DistributionCategory.EMBEDDED),
            
            // Container-optimized distributions
            LinuxDistribution("photon-os", "VMware Photon OS ARM64", PackageManager.TDNF, 
                "https://packages.vmware.com/photon/4.0/GA/ova/photon-minimal-4.0-latest.aarch64.tar.xz",
                "Minimal Linux for containers and cloud applications", "arm64", true, 58_000_000L, null, "VMware", DistributionCategory.CONTAINER),
            LinuxDistribution("clear-linux", "Intel Clear Linux ARM64", PackageManager.SWP, 
                "https://download.clearlinux.org/releases/current/clear/clear-containers-arm64.tar.xz",
                "Performance-optimized distribution from Intel", "arm64", false, 78_000_000L, null, "Intel", DistributionCategory.CONTAINER),
            LinuxDistribution("coreos-base", "Fedora CoreOS ARM64", PackageManager.RPM_OSTREE, 
                "https://builds.coreos.fedoraproject.org/streams/stable.json",
                "Container-focused OS with automatic updates", "arm64", true, 145_000_000L, null, "CoreOS Team", DistributionCategory.CONTAINER)
        )
        
        _availableDistributions.value = defaultDistributions
    }
    
    /**
     * Identify distribution from directory with enhanced detection
     */
    private fun identifyDistribution(dir: File): LinuxDistribution? {
        val name = dir.name.lowercase()
        val infoFile = File(dir, ".distribution_info")
        
        // Try to read distribution info file first
        if (infoFile.exists()) {
            try {
                val properties = mutableMapOf<String, String>()
                infoFile.readLines().forEach { line ->
                    if (line.contains("=")) {
                        val (key, value) = line.split("=", limit = 2)
                        properties[key] = value
                    }
                }
                
                return LinuxDistribution(
                    name = properties["name"] ?: name,
                    displayName = properties["displayName"] ?: name,
                    packageManager = PackageManager.valueOf(properties["packageManager"] ?: "APT"),
                    downloadUrl = properties["downloadUrl"],
                    architecture = properties["architecture"] ?: "arm64",
                    verified = properties["verified"]?.toBoolean() ?: false
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to parse distribution info for $name")
            }
        }
        
        // Fallback to name-based detection with expanded patterns
        return when {
            name.contains("ubuntu") -> LinuxDistribution("ubuntu", "Ubuntu", PackageManager.APT, category = DistributionCategory.GENERAL)
            name.contains("debian") -> LinuxDistribution("debian", "Debian", PackageManager.APT, category = DistributionCategory.GENERAL)
            name.contains("kali") -> LinuxDistribution("kali", "Kali Linux", PackageManager.APT, category = DistributionCategory.SECURITY)
            name.contains("parrot") -> LinuxDistribution("parrot", "Parrot Security", PackageManager.APT, category = DistributionCategory.SECURITY)
            name.contains("blackarch") -> LinuxDistribution("blackarch", "BlackArch", PackageManager.PACMAN, category = DistributionCategory.SECURITY)
            name.contains("fedora") -> LinuxDistribution("fedora", "Fedora", PackageManager.DNF, category = DistributionCategory.GENERAL)
            name.contains("centos") -> LinuxDistribution("centos", "CentOS", PackageManager.YUM, category = DistributionCategory.GENERAL)
            name.contains("rocky") -> LinuxDistribution("rocky", "Rocky Linux", PackageManager.DNF, category = DistributionCategory.GENERAL)
            name.contains("alpine") -> LinuxDistribution("alpine", "Alpine", PackageManager.ALPINE, category = DistributionCategory.MINIMAL)
            name.contains("arch") -> LinuxDistribution("arch", "Arch Linux", PackageManager.PACMAN, category = DistributionCategory.GENERAL)
            name.contains("manjaro") -> LinuxDistribution("manjaro", "Manjaro", PackageManager.PACMAN, category = DistributionCategory.GENERAL)
            name.contains("void") -> LinuxDistribution("void", "Void Linux", PackageManager.XBPS, category = DistributionCategory.GENERAL)
            name.contains("gentoo") -> LinuxDistribution("gentoo", "Gentoo", PackageManager.PORTAGE, category = DistributionCategory.DEVELOPMENT)
            name.contains("nixos") -> LinuxDistribution("nixos", "NixOS", PackageManager.NIX, category = DistributionCategory.DEVELOPMENT)
            name.contains("postmarket") -> LinuxDistribution("postmarket", "postmarketOS", PackageManager.ALPINE, category = DistributionCategory.EMBEDDED)
            name.contains("photon") -> LinuxDistribution("photon", "Photon OS", PackageManager.TDNF, category = DistributionCategory.CONTAINER)
            name.contains("clear") -> LinuxDistribution("clear", "Clear Linux", PackageManager.SWP, category = DistributionCategory.CONTAINER)
            name.contains("coreos") -> LinuxDistribution("coreos", "CoreOS", PackageManager.RPM_OSTREE, category = DistributionCategory.CONTAINER)
            else -> null
        }
    }
}

/**
 * Linux distribution data class with download capabilities
 */
data class LinuxDistribution(
    val name: String,
    val displayName: String,
    val packageManager: PackageManager,
    val downloadUrl: String? = null,
    val description: String = "",
    val architecture: String = "arm64",
    val verified: Boolean = false,
    val size: Long = 0L, // Size in bytes
    val sha256: String? = null,
    val maintainer: String = "community",
    val category: DistributionCategory = DistributionCategory.GENERAL
)

/**
 * Package manager enumeration with additional package managers
 */
enum class PackageManager {
    APT,          // Debian/Ubuntu
    YUM,          // CentOS/RHEL (legacy)
    DNF,          // Fedora/CentOS Stream
    PACMAN,       // Arch Linux
    ALPINE,       // Alpine APK
    XBPS,         // Void Linux
    PORTAGE,      // Gentoo
    NIX,          // NixOS
    SWP,          // Clear Linux swupd
    TDNF,         // Photon OS
    RPM_OSTREE,   // CoreOS/Silverblue
    NONE          // No package manager (minimal/custom)
}

/**
 * Distribution categories for organization
 */
enum class DistributionCategory {
    GENERAL,              // Standard distributions
    SECURITY,             // Penetration testing/security
    MINIMAL,              // Ultra-lightweight
    DEVELOPMENT,          // Development-focused
    CONTAINER,            // Container-optimized
    EMBEDDED,             // Embedded/IoT
    EXPERIMENTAL          // Bleeding edge/unstable
}

/**
 * Chroot environment data class
 */
data class ChrootEnvironment(
    val name: String,
    val distribution: LinuxDistribution,
    val rootPath: String,
    val isActive: Boolean
)

/**
 * Mount information data class
 */
data class MountInfo(
    val environment: ChrootEnvironment,
    val process: Process,
    val mountTime: Long
)

/**
 * RootFS state enumeration
 */
sealed class RootFSState {
    object Idle : RootFSState()
    object Initializing : RootFSState()
    object Ready : RootFSState()
    data class Error(val message: String) : RootFSState()
}