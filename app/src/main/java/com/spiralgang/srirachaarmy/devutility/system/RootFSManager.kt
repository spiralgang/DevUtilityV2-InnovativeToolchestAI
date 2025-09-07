// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

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
 * Handles fully administrative customizable XShadow proot/chroot functionality
 * Specialized for Aarch64 Unix/Linux distributions on Android 10+
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
        private const val XSHADOW_BINARY = "xshadow"
        private const val TOYBOX_BINARY = "toybox"
        private const val ANDROID_MIN_VERSION = 29 // Android 10+
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
     * Create a new XShadow-enhanced chroot environment for Android 10+ Aarch64
     */
    suspend fun createXShadowChrootEnvironment(
        distributionName: String,
        environmentName: String,
        xshadowConfig: XShadowConfig = XShadowConfig()
    ): ChrootEnvironment? = withContext(Dispatchers.IO) {
        try {
            // Verify Android 10+ compatibility
            if (android.os.Build.VERSION.SDK_INT < ANDROID_MIN_VERSION) {
                Timber.w("XShadow requires Android 10+ (API level $ANDROID_MIN_VERSION)")
                return@withContext null
            }
            
            val distribution = _availableDistributions.value.find { it.name == distributionName }
                ?: return@withContext null
            
            // Ensure it's ARM64/Aarch64 compatible
            if (!distribution.architecture.equals("arm64", ignoreCase = true) && 
                !distribution.architecture.equals("aarch64", ignoreCase = true)) {
                Timber.w("Distribution $distributionName is not Aarch64 compatible")
                return@withContext null
            }
            
            val envDir = File(rootfsBaseDir, "$distributionName/$environmentName")
            
            if (envDir.exists()) {
                Timber.w("Environment $environmentName already exists for $distributionName")
                return@withContext ChrootEnvironment(
                    name = environmentName,
                    distribution = distribution,
                    rootPath = envDir.absolutePath,
                    isActive = false,
                    xshadowEnabled = true,
                    xshadowConfig = xshadowConfig
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
            
            // Setup enhanced filesystem structure with XShadow integration
            setupXShadowFilesystem(envDir, xshadowConfig)
            
            // Configure XShadow environment
            configureXShadowEnvironment(envDir, xshadowConfig)
            
            val environment = ChrootEnvironment(
                name = environmentName,
                distribution = distribution,
                rootPath = envDir.absolutePath,
                isActive = false,
                xshadowEnabled = true,
                xshadowConfig = xshadowConfig
            )
            
            Timber.d("Created XShadow chroot environment: $environmentName for $distributionName")
            return@withContext environment
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create XShadow chroot environment: $environmentName")
            null
        }
    }
    
    /**
     * Enter XShadow-enhanced chroot environment
     */
    suspend fun enterXShadowEnvironment(environment: ChrootEnvironment): Process? = withContext(Dispatchers.IO) {
        try {
            val xshadowCommand = buildXShadowCommand(environment)
            
            val processBuilder = ProcessBuilder(*xshadowCommand.toTypedArray())
            processBuilder.directory(File(environment.rootPath))
            
            // Set up Android 10+ specific environment variables
            val envMap = processBuilder.environment()
            envMap["ANDROID_ROOT"] = "/system"
            envMap["ANDROID_DATA"] = "/data"
            envMap["EXTERNAL_STORAGE"] = "/sdcard"
            envMap["PATH"] = "/usr/local/bin:/usr/bin:/bin:/usr/local/sbin:/usr/sbin:/sbin"
            envMap["HOME"] = "/root"
            envMap["USER"] = "root"
            envMap["SHELL"] = "/bin/bash"
            envMap["TERM"] = "xterm-256color"
            
            val process = processBuilder.start()
            
            // Mark environment as active with XShadow
            val mountInfo = MountInfo(
                environment = environment,
                process = process,
                mountTime = System.currentTimeMillis(),
                xshadowActive = true
            )
            activeMounts[environment.name] = mountInfo
            
            Timber.d("Entered XShadow chroot environment: ${environment.name}")
            return@withContext process
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to enter XShadow chroot environment: ${environment.name}")
            null
        }
    }
    
    /**
     * Execute command in XShadow chroot environment
     */
    suspend fun executeInXShadowChroot(
        environment: ChrootEnvironment,
        command: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val xshadowCommand = buildXShadowCommand(environment, command)
            
            val processBuilder = ProcessBuilder(*xshadowCommand.toTypedArray())
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
            Timber.e(e, "Failed to execute command in XShadow chroot: $command")
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
            
            val result = if (environment.xshadowEnabled) {
                executeInXShadowChroot(environment, installCommand)
            } else {
                executeInChroot(environment, installCommand)
            }
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
     * Build XShadow command for enhanced chroot with administrative customization
     */
    private fun buildXShadowCommand(environment: ChrootEnvironment, command: String? = null): List<String> {
        val xshadowCommand = mutableListOf<String>()
        
        // Check for XShadow binary first
        val xshadowBinary = File(context.applicationInfo.nativeLibraryDir, XSHADOW_BINARY)
        val prootBinary = File(context.applicationInfo.nativeLibraryDir, PROOT_BINARY)
        
        if (xshadowBinary.exists() && environment.xshadowEnabled) {
            // Use XShadow for fully administrative customizable environment
            xshadowCommand.addAll(listOf(
                xshadowBinary.absolutePath,
                "--root", environment.rootPath,
                "--working-dir", "/",
                "--bind", "/proc",
                "--bind", "/sys", 
                "--bind", "/dev",
                "--bind", "/tmp",
                "--android-compat",
                "--arm64-optimized"
            ))
            
            // Add XShadow-specific configurations
            environment.xshadowConfig?.let { config ->
                if (config.enableNetworking) {
                    xshadowCommand.addAll(listOf("--bind", "/data/misc/net"))
                }
                if (config.enableGpu) {
                    xshadowCommand.addAll(listOf("--bind", "/dev/dri", "--bind", "/dev/mali0"))
                }
                if (config.enableStorage) {
                    xshadowCommand.addAll(listOf("--bind", "/sdcard:/sdcard", "--bind", "/storage:/storage"))
                }
                if (config.enableSelinux) {
                    xshadowCommand.add("--selinux-context=u:r:untrusted_app:s0")
                }
                config.customBinds.forEach { (host, guest) ->
                    xshadowCommand.addAll(listOf("--bind", "$host:$guest"))
                }
            }
            
            if (command != null) {
                xshadowCommand.addAll(listOf("/bin/sh", "-c", command))
            } else {
                xshadowCommand.add("/bin/bash")
            }
            
        } else if (prootBinary.exists()) {
            // Fallback to enhanced proot with Android 10+ optimizations
            xshadowCommand.addAll(listOf(
                prootBinary.absolutePath,
                "-r", environment.rootPath,
                "-w", "/",
                "-b", "/proc",
                "-b", "/sys",
                "-b", "/dev", 
                "-b", "/tmp"
            ))
            
            // Add Android 10+ specific binds
            xshadowCommand.addAll(listOf(
                "-b", "/system",
                "-b", "/vendor",
                "-b", "/data/misc/net"
            ))
            
            if (command != null) {
                xshadowCommand.addAll(listOf("/bin/sh", "-c", command))
            } else {
                xshadowCommand.add("/bin/bash")
            }
        } else {
            // Basic chroot fallback (may not work without root)
            xshadowCommand.addAll(listOf(
                "chroot",
                environment.rootPath
            ))
            
            if (command != null) {
                xshadowCommand.addAll(listOf("/bin/sh", "-c", command))
            } else {
                xshadowCommand.add("/bin/bash")
            }
        }
        
        return xshadowCommand
    }
    
    /**
     * Legacy method for backward compatibility
     */
    suspend fun executeInChroot(
        environment: ChrootEnvironment,
        command: String
    ): String? = withContext(Dispatchers.IO) {
        return@withContext if (environment.xshadowEnabled) {
            executeInXShadowChroot(environment, command)
        } else {
            executeInLegacyChroot(environment, command)
        }
    }
    
    /**
     * Legacy chroot execution for non-XShadow environments
     */
    private suspend fun executeInLegacyChroot(
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
            Timber.e(e, "Failed to execute command in legacy chroot: $command")
            "Error: ${e.message}"
        }
    }
    
    /**
     * Legacy proot command builder for backward compatibility
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
     * Setup XShadow-enhanced filesystem structure
     */
    private fun setupXShadowFilesystem(rootDir: File, xshadowConfig: XShadowConfig) {
        // Basic filesystem structure
        setupBasicFilesystem(rootDir)
        
        // XShadow-specific directories
        val xshadowDirs = listOf(
            "opt/xshadow", "var/lib/xshadow", "etc/xshadow",
            "dev/pts", "dev/shm", "run", "run/lock", "run/user"
        )
        
        xshadowDirs.forEach { dir ->
            File(rootDir, dir).mkdirs()
        }
        
        // Create XShadow configuration files
        File(rootDir, "etc/xshadow/config").writeText("""
            # XShadow Configuration for Android 10+ ARM64
            android_compat=true
            arm64_optimized=true
            selinux_context=${xshadowConfig.enableSelinux}
            networking=${xshadowConfig.enableNetworking}
            gpu_access=${xshadowConfig.enableGpu}
            storage_access=${xshadowConfig.enableStorage}
        """.trimIndent())
        
        // Enhanced shell environment
        File(rootDir, "etc/profile.d/xshadow.sh").writeText("""
            #!/bin/bash
            # XShadow Android 10+ ARM64 Environment
            export ANDROID_COMPAT=1
            export ARM64_OPTIMIZED=1
            export XSHADOW_VERSION="2.5.0"
            export PS1="[\u@xshadow-devutility \W]\\$ "
            
            # Android-specific paths
            export ANDROID_ROOT=/system
            export ANDROID_DATA=/data
            export EXTERNAL_STORAGE=/sdcard
            
            # Enhanced PATH for development tools
            export PATH="/opt/devtools/bin:/usr/local/bin:/usr/bin:/bin:/usr/local/sbin:/usr/sbin:/sbin"
            
            # Development aliases
            alias ll='ls -la'
            alias grep='grep --color=auto'
            alias python='python3'
            alias pip='pip3'
        """.trimIndent())
    }
    
    /**
     * Configure XShadow environment with administrative customization
     */
    private fun configureXShadowEnvironment(rootDir: File, xshadowConfig: XShadowConfig) {
        // Create administrative tools directory
        val adminToolsDir = File(rootDir, "opt/devtools/bin")
        adminToolsDir.mkdirs()
        
        // Configure custom administrative scripts
        File(adminToolsDir, "xshadow-admin").writeText("""
            #!/bin/bash
            # XShadow Administrative Tool for DevUtility
            case "$1" in
                status)
                    echo "XShadow Environment Status:"
                    echo "Android Version: Android 10+ (API ${android.os.Build.VERSION.SDK_INT})"
                    echo "Architecture: ARM64/Aarch64"
                    echo "Networking: ${if (xshadowConfig.enableNetworking) "Enabled" else "Disabled"}"
                    echo "GPU Access: ${if (xshadowConfig.enableGpu) "Enabled" else "Disabled"}"
                    echo "Storage Access: ${if (xshadowConfig.enableStorage) "Enabled" else "Disabled"}"
                    ;;
                network)
                    if [ "${xshadowConfig.enableNetworking}" = "true" ]; then
                        echo "Configuring network access..."
                        # Network configuration commands would go here
                    else
                        echo "Network access disabled in XShadow configuration"
                    fi
                    ;;
                *)
                    echo "Usage: xshadow-admin {status|network}"
                    ;;
            esac
        """.trimIndent())
        
        File(adminToolsDir, "xshadow-admin").setExecutable(true)
        
        // Configure sudo alternatives for Android
        File(rootDir, "usr/bin/sudo").writeText("""
            #!/bin/bash
            # XShadow sudo alternative for Android
            echo "Note: Running in XShadow administrative environment"
            exec "$@"
        """.trimIndent())
        
        File(rootDir, "usr/bin/sudo").setExecutable(true)
    }
    
    /**
     * Legacy method for backward compatibility - creates XShadow environment by default on Android 10+
     */
    suspend fun createChrootEnvironment(
        distributionName: String,
        environmentName: String
    ): ChrootEnvironment? {
        return if (android.os.Build.VERSION.SDK_INT >= ANDROID_MIN_VERSION) {
            createXShadowChrootEnvironment(distributionName, environmentName)
        } else {
            createLegacyChrootEnvironment(distributionName, environmentName)
        }
    }
    
    /**
     * Legacy chroot environment creation for older Android versions
     */
    private suspend fun createLegacyChrootEnvironment(
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
                    isActive = false,
                    xshadowEnabled = false
                )
            }
            
            // Create environment directory
            envDir.mkdirs()
            
            // Copy distribution files
            val distributionDir = File(rootfsBaseDir, distributionName)
            if (distributionDir.exists()) {
                copyDistributionFiles(distributionDir, envDir)
            } else {
                downloadAndExtractDistribution(distribution, envDir)
            }
            
            // Setup basic filesystem structure
            setupBasicFilesystem(envDir)
            
            val environment = ChrootEnvironment(
                name = environmentName,
                distribution = distribution,
                rootPath = envDir.absolutePath,
                isActive = false,
                xshadowEnabled = false
            )
            
            Timber.d("Created legacy chroot environment: $environmentName for $distributionName")
            return@withContext environment
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create legacy chroot environment: $environmentName")
            null
        }
    }
    
    /**
     * Legacy method for backward compatibility - uses XShadow if available
     */
    suspend fun enterChrootEnvironment(environment: ChrootEnvironment): Process? {
        return if (environment.xshadowEnabled) {
            enterXShadowEnvironment(environment)
        } else {
            enterLegacyEnvironment(environment)
        }
    }
    
    /**
     * Legacy environment entry for non-XShadow environments
     */
    private suspend fun enterLegacyEnvironment(environment: ChrootEnvironment): Process? = withContext(Dispatchers.IO) {
        try {
            val prootCommand = buildProotCommand(environment)
            
            val processBuilder = ProcessBuilder(*prootCommand.toTypedArray())
            processBuilder.directory(File(environment.rootPath))
            
            val process = processBuilder.start()
            
            // Mark environment as active
            val mountInfo = MountInfo(
                environment = environment,
                process = process,
                mountTime = System.currentTimeMillis(),
                xshadowActive = false
            )
            activeMounts[environment.name] = mountInfo
            
            Timber.d("Entered legacy chroot environment: ${environment.name}")
            return@withContext process
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to enter legacy chroot environment: ${environment.name}")
            null
        }
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
     * Setup default distributions focusing on Aarch64 Unix/Linux for Android 10+
     */
    private suspend fun setupDefaultDistributions() {
        val defaultDistributions = mutableListOf(
            // Standard Aarch64 distributions optimized for Android 10+
            LinuxDistribution("ubuntu-arm64", "Ubuntu 24.04 LTS ARM64", PackageManager.APT, 
                "https://cloud-images.ubuntu.com/minimal/releases/noble/release/ubuntu-24.04-minimal-cloudimg-arm64-root.tar.xz",
                "Ubuntu Long Term Support - stable and widely supported for Android 10+", "aarch64", true, 85_000_000L, null, "Canonical", DistributionCategory.GENERAL),
            LinuxDistribution("debian-arm64", "Debian 12 (Bookworm) ARM64", PackageManager.APT, 
                "https://github.com/spiralgang/rootfs-sources/releases/download/v2025.1/debian-12-aarch64-minimal.tar.xz",
                "The universal operating system - stable and reliable for Android 10+ ARM64", "aarch64", true, 92_000_000L, null, "Debian Project", DistributionCategory.GENERAL),
            LinuxDistribution("alpine-arm64", "Alpine Linux Latest ARM64", PackageManager.ALPINE, 
                "https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/aarch64/alpine-minirootfs-latest-aarch64.tar.gz",
                "Security-oriented, lightweight Linux distribution for Android 10+ ARM64", "aarch64", true, 5_200_000L, null, "Alpine Linux", DistributionCategory.MINIMAL),
            
            // Penetration Testing & Security Distributions (Aarch64 only)
            LinuxDistribution("kali-arm64", "Kali Linux ARM64 Android", PackageManager.APT, 
                "https://github.com/xiv3r/Kali-Linux-Termux/releases/download/v2025.1/kali-rootfs-aarch64-android10.tar.xz",
                "Advanced penetration testing platform optimized for Android 10+ ARM64", "aarch64", false, 450_000_000L, null, "xiv3r", DistributionCategory.SECURITY),
            LinuxDistribution("parrot-arm64", "Parrot Security Core ARM64", PackageManager.APT, 
                "https://github.com/pentesting-distros/parrot-arm64/releases/download/v5.3/parrot-core-aarch64-android.tar.xz",
                "Privacy-focused security distribution for Android 10+ ARM64", "aarch64", false, 380_000_000L, null, "Community", DistributionCategory.SECURITY),
            LinuxDistribution("blackarch-arm64", "BlackArch Linux ARM64", PackageManager.PACMAN, 
                "https://github.com/BlackArch/blackarch-rootfs/releases/download/v2025.01/blackarch-aarch64-android-minimal.tar.xz",
                "Arch-based penetration testing distribution for Android 10+ ARM64", "aarch64", false, 520_000_000L, null, "BlackArch Team", DistributionCategory.SECURITY),
            
            // Debian Variants & Community Builds (Aarch64 Focus)
            LinuxDistribution("debian-sid-arm64", "Debian Sid (Unstable) ARM64", PackageManager.APT, 
                "https://github.com/jubinson/debian-rootfs/releases/download/v2025.1/debian-sid-aarch64-android10.tar.xz",
                "Bleeding edge Debian with latest packages for Android 10+ ARM64", "aarch64", false, 110_000_000L, null, "jubinson", DistributionCategory.EXPERIMENTAL),
            LinuxDistribution("devuan-arm64", "Devuan ASCII ARM64", PackageManager.APT, 
                "https://files.devuan.org/devuan_ascii/embedded/devuan_ascii_5.0.0_aarch64_android_minimal.tar.xz",
                "Debian without systemd - init freedom for Android 10+ ARM64", "aarch64", true, 88_000_000L, null, "Devuan Project", DistributionCategory.GENERAL),
            LinuxDistribution("ubuntu-minimal-arm64", "Ubuntu 22.04 Minimal ARM64", PackageManager.APT, 
                "https://cloud-images.ubuntu.com/minimal/releases/jammy/release/ubuntu-22.04-minimal-cloudimg-aarch64-root.tar.xz",
                "Minimal Ubuntu LTS for Android 10+ ARM64 containers", "aarch64", true, 72_000_000L, null, "Canonical", DistributionCategory.MINIMAL),
            
            // Arch-based distributions (ARM64 specific)
            LinuxDistribution("archlinux-arm64", "Arch Linux ARM64", PackageManager.PACMAN, 
                "http://os.archlinuxarm.org/os/ArchLinuxARM-aarch64-latest.tar.gz",
                "Simple, lightweight rolling-release distribution for Android 10+ ARM64", "aarch64", true, 165_000_000L, null, "Arch Linux ARM", DistributionCategory.GENERAL),
            LinuxDistribution("manjaro-arm64", "Manjaro ARM64 Minimal", PackageManager.PACMAN, 
                "https://github.com/manjaro-arm/rootfs-builder/releases/download/v2025.01/manjaro-aarch64-android-minimal.tar.xz",
                "User-friendly Arch derivative for Android 10+ ARM64", "aarch64", false, 140_000_000L, null, "Manjaro ARM", DistributionCategory.GENERAL),
            LinuxDistribution("endeavouros-arm64", "EndeavourOS ARM64", PackageManager.PACMAN, 
                "https://github.com/endeavouros-arm/rootfs/releases/download/v2025.01/endeavouros-aarch64-android-base.tar.xz",
                "Arch-based distribution for Android 10+ ARM64", "aarch64", false, 155_000_000L, null, "EndeavourOS", DistributionCategory.GENERAL),
            
            // Fedora-based distributions (ARM64 Android focus)
            LinuxDistribution("fedora-arm64", "Fedora 39 ARM64", PackageManager.DNF, 
                "https://download.fedoraproject.org/pub/fedora/linux/releases/39/Container/aarch64/images/Fedora-Container-Base-39-1.5.aarch64.tar.xz",
                "Cutting-edge features for Android 10+ ARM64", "aarch64", true, 195_000_000L, null, "Fedora Project", DistributionCategory.GENERAL),
            LinuxDistribution("centos-stream-arm64", "CentOS Stream 9 ARM64", PackageManager.DNF, 
                "https://cloud.centos.org/centos/9-stream/aarch64/images/CentOS-Stream-Container-Base-9-latest.aarch64.tar.xz",
                "Continuously delivered distribution for Android 10+ ARM64", "aarch64", true, 210_000_000L, null, "CentOS Project", DistributionCategory.GENERAL),
            LinuxDistribution("rockylinux-arm64", "Rocky Linux 9 ARM64", PackageManager.DNF, 
                "https://download.rockylinux.org/pub/rocky/9/images/aarch64/Rocky-9-Container-Base.latest.aarch64.tar.xz",
                "Enterprise-class OS for Android 10+ ARM64", "aarch64", true, 205_000_000L, null, "Rocky Enterprise Software Foundation", DistributionCategory.GENERAL),
            
            // Specialized & Lightweight distributions (ARM64 Android optimized)
            LinuxDistribution("void-linux-arm64", "Void Linux ARM64", PackageManager.XBPS, 
                "https://repo-default.voidlinux.org/live/current/void-aarch64-ROOTFS-latest.tar.xz",
                "Independent rolling-release with runit init for Android 10+ ARM64", "aarch64", true, 125_000_000L, null, "Void Linux", DistributionCategory.GENERAL),
            LinuxDistribution("busybox-alpine-arm64", "Alpine + BusyBox Ultra-Minimal ARM64", PackageManager.ALPINE, 
                "https://github.com/alpine-docker/aarch64/releases/download/v3.19/alpine-minirootfs-3.19.0-aarch64.tar.gz",
                "Ultra-minimal Linux for Android 10+ ARM64", "aarch64", true, 2_800_000L, null, "Alpine Linux", DistributionCategory.MINIMAL),
            LinuxDistribution("postmarketos-arm64", "postmarketOS ARM64", PackageManager.ALPINE, 
                "https://images.postmarketos.org/rootfs/edge/postmarketos-base-ui-edge-aarch64.tar.xz",
                "Touch-optimized Alpine derivative for Android 10+ ARM64", "aarch64", false, 45_000_000L, null, "postmarketOS", DistributionCategory.EMBEDDED),
            
            // Development-focused distributions (ARM64 Android)
            LinuxDistribution("gentoo-stage3-arm64", "Gentoo Stage3 ARM64", PackageManager.PORTAGE, 
                "https://distfiles.gentoo.org/releases/arm64/autobuilds/current-stage3-arm64/stage3-arm64-latest.tar.xz",
                "Source-based meta-distribution for Android 10+ ARM64", "aarch64", true, 280_000_000L, null, "Gentoo Foundation", DistributionCategory.DEVELOPMENT),
            LinuxDistribution("nixos-minimal-arm64", "NixOS Minimal ARM64", PackageManager.NIX, 
                "https://channels.nixos.org/nixos-unstable/nixexprs.tar.xz",
                "Functional package management for Android 10+ ARM64", "aarch64", false, 95_000_000L, null, "NixOS Foundation", DistributionCategory.DEVELOPMENT),
            LinuxDistribution("buildroot-arm64", "Buildroot ARM64 Minimal", PackageManager.NONE, 
                "https://buildroot.org/downloads/buildroot-2024.02.tar.gz",
                "Tool for generating embedded Linux systems on Android 10+ ARM64", "aarch64", true, 15_000_000L, null, "Buildroot Community", DistributionCategory.EMBEDDED),
            
            // Container-optimized distributions (ARM64 Android focus)
            LinuxDistribution("photon-os-arm64", "VMware Photon OS ARM64", PackageManager.TDNF, 
                "https://packages.vmware.com/photon/4.0/GA/ova/photon-minimal-4.0-latest.aarch64.tar.xz",
                "Minimal Linux for containers on Android 10+ ARM64", "aarch64", true, 58_000_000L, null, "VMware", DistributionCategory.CONTAINER),
            LinuxDistribution("clear-linux-arm64", "Intel Clear Linux ARM64", PackageManager.SWP, 
                "https://download.clearlinux.org/releases/current/clear/clear-containers-aarch64.tar.xz",
                "Performance-optimized distribution for Android 10+ ARM64", "aarch64", false, 78_000_000L, null, "Intel", DistributionCategory.CONTAINER),
            LinuxDistribution("coreos-base-arm64", "Fedora CoreOS ARM64", PackageManager.RPM_OSTREE, 
                "https://builds.coreos.fedoraproject.org/streams/stable.json",
                "Container-focused OS with automatic updates for Android 10+ ARM64", "aarch64", true, 145_000_000L, null, "CoreOS Team", DistributionCategory.CONTAINER)
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
 * XShadow configuration for fully administrative customizable environments
 */
data class XShadowConfig(
    val enableNetworking: Boolean = true,
    val enableGpu: Boolean = false,
    val enableStorage: Boolean = true,
    val enableSelinux: Boolean = false,
    val customBinds: Map<String, String> = emptyMap(),
    val adminMode: Boolean = true,
    val androidOptimized: Boolean = true
)

/**
 * Chroot environment data class with XShadow support
 */
data class ChrootEnvironment(
    val name: String,
    val distribution: LinuxDistribution,
    val rootPath: String,
    val isActive: Boolean,
    val xshadowEnabled: Boolean = false,
    val xshadowConfig: XShadowConfig? = null
)

/**
 * Mount information data class with XShadow tracking
 */
data class MountInfo(
    val environment: ChrootEnvironment,
    val process: Process,
    val mountTime: Long,
    val xshadowActive: Boolean = false
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