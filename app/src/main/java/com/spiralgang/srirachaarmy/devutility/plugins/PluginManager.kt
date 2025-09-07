// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plugin system architecture inspired by IntelliJ IDEA/Visual Studio Code
 * Part of DevUtility V2.5 modular and extensible architecture
 */
@Singleton
class PluginManager @Inject constructor() {
    
    private val installedPlugins = mutableMapOf<String, Plugin>()
    private val pluginRegistry = mutableMapOf<String, PluginMetadata>()
    private val extensionPoints = mutableMapOf<String, ExtensionPoint>()
    private val activeExtensions = mutableMapOf<String, List<Extension>>()
    
    data class Plugin(
        val id: String,
        val metadata: PluginMetadata,
        val classLoader: ClassLoader?,
        val isEnabled: Boolean = true,
        val loadTime: Long = System.currentTimeMillis(),
        val extensions: List<Extension> = emptyList()
    )
    
    data class PluginMetadata(
        val id: String,
        val name: String,
        val version: String,
        val description: String,
        val author: String,
        val category: String,
        val dependencies: List<String> = emptyList(),
        val supportedLanguages: List<String> = emptyList(),
            val minAppVersion: String = DEFAULT_MIN_APP_VERSION, // Minimum supported application version
        val maxAppVersion: String? = null,
        val permissions: List<String> = emptyList(),
        val entryPoint: String, // Main class name
        val resources: List<String> = emptyList(),
        val configuration: Map<String, Any> = emptyMap()
    )
    
    data class ExtensionPoint(
        val id: String,
        val name: String,
        val description: String,
        val interfaceClass: String,
        val cardinality: Cardinality,
        val parameters: List<ParameterDefinition> = emptyList()
    )
    
    data class Extension(
        val id: String,
        val extensionPointId: String,
        val pluginId: String,
        val implementation: Any,
        val priority: Int = 0,
        val configuration: Map<String, Any> = emptyMap()
    )
    
    data class ParameterDefinition(
        val name: String,
        val type: String,
        val required: Boolean = true,
        val defaultValue: Any? = null,
        val description: String = ""
    )
    
    enum class Cardinality { ONE, MANY }
    
    enum class PluginStatus { INSTALLED, ENABLED, DISABLED, ERROR, LOADING }
    
    // Core extension points
    companion object {
        const val CODE_EDITOR_EXTENSION = "code_editor"
        const val LANGUAGE_SUPPORT_EXTENSION = "language_support"
        const val AI_PROVIDER_EXTENSION = "ai_provider"
        const val BUILD_TOOL_EXTENSION = "build_tool"
        const val VCS_EXTENSION = "version_control"
        const val DEBUGGER_EXTENSION = "debugger"
        const val FORMATTER_EXTENSION = "code_formatter"
        const val LINTER_EXTENSION = "code_linter"
        const val THEME_EXTENSION = "ui_theme"
        const val TOOL_WINDOW_EXTENSION = "tool_window"
    }
    
    /**
     * Initialize plugin system
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Initializing Plugin Manager")
            
            // Register core extension points
            registerCoreExtensionPoints()
            
            // Load built-in plugins
            loadBuiltInPlugins()
            
            // Scan for external plugins
            scanForPlugins()
            
            Timber.d("Plugin Manager initialized with ${installedPlugins.size} plugins")
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin Manager initialization failed")
        }
    }
    
    /**
     * Install plugin from file or URL
     */
    suspend fun installPlugin(
        pluginPath: String,
        isUrl: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Installing plugin from: $pluginPath")
            
            // Download/load plugin package
            val pluginPackage = if (isUrl) {
                downloadPlugin(pluginPath)
            } else {
                loadPluginFromFile(pluginPath)
            }
            
            if (pluginPackage == null) {
                Timber.e("Failed to load plugin package")
                return@withContext false
            }
            
            // Validate plugin
            if (!validatePlugin(pluginPackage)) {
                Timber.e("Plugin validation failed")
                return@withContext false
            }
            
            // Check dependencies
            if (!checkDependencies(pluginPackage.metadata)) {
                Timber.e("Plugin dependency check failed")
                return@withContext false
            }
            
            // Install plugin
            val success = performPluginInstallation(pluginPackage)
            
            if (success) {
                Timber.d("Plugin installed successfully: ${pluginPackage.metadata.name}")
                installedPlugins[pluginPackage.metadata.id] = pluginPackage
                pluginRegistry[pluginPackage.metadata.id] = pluginPackage.metadata
            }
            
            return@withContext success
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin installation failed: $pluginPath")
            return@withContext false
        }
    }
    
    /**
     * Enable/disable plugin
     */
    suspend fun setPluginEnabled(pluginId: String, enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val plugin = installedPlugins[pluginId]
            if (plugin == null) {
                Timber.w("Plugin not found: $pluginId")
                return@withContext false
            }
            
            if (enabled && !plugin.isEnabled) {
                // Enable plugin
                val success = enablePlugin(plugin)
                if (success) {
                    installedPlugins[pluginId] = plugin.copy(isEnabled = true)
                    Timber.d("Plugin enabled: ${plugin.metadata.name}")
                }
                return@withContext success
            } else if (!enabled && plugin.isEnabled) {
                // Disable plugin
                val success = disablePlugin(plugin)
                if (success) {
                    installedPlugins[pluginId] = plugin.copy(isEnabled = false)
                    Timber.d("Plugin disabled: ${plugin.metadata.name}")
                }
                return@withContext success
            }
            
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to set plugin enabled state: $pluginId")
            return@withContext false
        }
    }
    
    /**
     * Uninstall plugin
     */
    suspend fun uninstallPlugin(pluginId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val plugin = installedPlugins[pluginId]
            if (plugin == null) {
                Timber.w("Plugin not found: $pluginId")
                return@withContext false
            }
            
            // Disable first if enabled
            if (plugin.isEnabled) {
                setPluginEnabled(pluginId, false)
            }
            
            // Remove from registry and installed plugins
            installedPlugins.remove(pluginId)
            pluginRegistry.remove(pluginId)
            
            // Clean up plugin files
            cleanupPluginFiles(plugin)
            
            Timber.d("Plugin uninstalled: ${plugin.metadata.name}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin uninstallation failed: $pluginId")
            return@withContext false
        }
    }
    
    /**
     * Get extensions for extension point
     */
    fun getExtensions(extensionPointId: String): List<Extension> {
        return activeExtensions[extensionPointId] ?: emptyList()
    }
    
    /**
     * Get single extension for extension point (when cardinality is ONE)
     */
    fun getExtension(extensionPointId: String): Extension? {
        val extensions = getExtensions(extensionPointId)
        return extensions.maxByOrNull { it.priority }
    }
    
    /**
     * Register new extension point
     */
    fun registerExtensionPoint(extensionPoint: ExtensionPoint) {
        extensionPoints[extensionPoint.id] = extensionPoint
        Timber.d("Extension point registered: ${extensionPoint.name}")
    }
    
    /**
     * Get all installed plugins
     */
    fun getInstalledPlugins(): List<Plugin> {
        return installedPlugins.values.toList()
    }
    
    /**
     * Get plugin by ID
     */
    fun getPlugin(pluginId: String): Plugin? {
        return installedPlugins[pluginId]
    }
    
    /**
     * Search plugins in registry
     */
    suspend fun searchPlugins(
        query: String,
        category: String? = null,
        language: String? = null
    ): List<PluginMetadata> = withContext(Dispatchers.IO) {
        return@withContext pluginRegistry.values
            .filter { plugin ->
                val matchesQuery = plugin.name.contains(query, ignoreCase = true) ||
                                plugin.description.contains(query, ignoreCase = true)
                val matchesCategory = category == null || plugin.category == category
                val matchesLanguage = language == null || plugin.supportedLanguages.contains(language)
                
                matchesQuery && matchesCategory && matchesLanguage
            }
            .sortedBy { it.name }
    }
    
    /**
     * Get plugin statistics
     */
    fun getPluginStatistics(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        stats["total_plugins"] = installedPlugins.size
        stats["enabled_plugins"] = installedPlugins.values.count { it.isEnabled }
        stats["disabled_plugins"] = installedPlugins.values.count { !it.isEnabled }
        
        val categoryDistribution = installedPlugins.values
            .groupBy { it.metadata.category }
            .mapValues { it.value.size }
        stats["category_distribution"] = categoryDistribution
        
        val languageSupport = installedPlugins.values
            .flatMap { it.metadata.supportedLanguages }
            .groupBy { it }
            .mapValues { it.value.size }
        stats["language_support"] = languageSupport
        
        stats["extension_points"] = extensionPoints.size
        stats["active_extensions"] = activeExtensions.values.sumOf { it.size }
        
        return stats
    }
    
    // Private helper methods
    private fun registerCoreExtensionPoints() {
        val coreExtensionPoints = listOf(
            ExtensionPoint(
                id = CODE_EDITOR_EXTENSION,
                name = "Code Editor",
                description = "Provides code editing capabilities",
                interfaceClass = "CodeEditor",
                cardinality = Cardinality.MANY
            ),
            ExtensionPoint(
                id = LANGUAGE_SUPPORT_EXTENSION,
                name = "Language Support",
                description = "Adds support for programming languages",
                interfaceClass = "LanguageSupport",
                cardinality = Cardinality.MANY
            ),
            ExtensionPoint(
                id = AI_PROVIDER_EXTENSION,
                name = "AI Provider",
                description = "Provides AI assistance capabilities",
                interfaceClass = "AIProvider",
                cardinality = Cardinality.MANY
            ),
            ExtensionPoint(
                id = BUILD_TOOL_EXTENSION,
                name = "Build Tool",
                description = "Integrates build tools",
                interfaceClass = "BuildTool",
                cardinality = Cardinality.MANY
            ),
            ExtensionPoint(
                id = FORMATTER_EXTENSION,
                name = "Code Formatter",
                description = "Formats code according to style guidelines",
                interfaceClass = "CodeFormatter",
                cardinality = Cardinality.MANY
            ),
            ExtensionPoint(
                id = THEME_EXTENSION,
                name = "UI Theme",
                description = "Provides UI themes and styling",
                interfaceClass = "UITheme",
                cardinality = Cardinality.MANY
            )
        )
        
        coreExtensionPoints.forEach { extensionPoint ->
            registerExtensionPoint(extensionPoint)
        }
    }
    
    private suspend fun loadBuiltInPlugins() = withContext(Dispatchers.IO) {
        // Load built-in plugins
        val builtInPlugins = listOf(
            createBuiltInPlugin("kotlin_support", "Kotlin Language Support", "kotlin"),
            createBuiltInPlugin("java_support", "Java Language Support", "java"),
            createBuiltInPlugin("python_support", "Python Language Support", "python"),
            createBuiltInPlugin("javascript_support", "JavaScript Language Support", "javascript"),
            createBuiltInPlugin("git_integration", "Git Integration", "vcs"),
            createBuiltInPlugin("gradle_support", "Gradle Build Tool", "build_tool")
        )
        
        builtInPlugins.forEach { plugin ->
            installedPlugins[plugin.id] = plugin
            pluginRegistry[plugin.id] = plugin.metadata
        }
        
        Timber.d("Loaded ${builtInPlugins.size} built-in plugins")
    }
    
    private fun createBuiltInPlugin(id: String, name: String, category: String): Plugin {
        val metadata = PluginMetadata(
            id = id,
            name = name,
            version = "2.5.0",
            description = "Built-in $name plugin",
            author = "SrirachaArmy",
            category = category,
            entryPoint = "com.spiralgang.srirachaarmy.devutility.plugins.builtin.${id}",
            permissions = emptyList()
        )
        
        return Plugin(
            id = id,
            metadata = metadata,
            classLoader = null, // Built-in plugins use app class loader
            isEnabled = true
        )
    }
    
    private suspend fun scanForPlugins() = withContext(Dispatchers.IO) {
        // Scan plugin directory for external plugins
        // In real implementation, this would scan file system
        Timber.d("Scanning for external plugins")
    }
    
    private suspend fun downloadPlugin(url: String): Plugin? = withContext(Dispatchers.IO) {
        try {
            // Simulate plugin download
            Timber.d("Downloading plugin from: $url")
            // In real implementation, this would download and parse the plugin
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Plugin download failed: $url")
            return@withContext null
        }
    }
    
    private suspend fun loadPluginFromFile(path: String): Plugin? = withContext(Dispatchers.IO) {
        try {
            // Simulate plugin loading from file
            Timber.d("Loading plugin from file: $path")
            // In real implementation, this would parse plugin manifest and load classes
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Plugin loading failed: $path")
            return@withContext null
        }
    }
    
    private fun validatePlugin(plugin: Plugin): Boolean {
        try {
            val metadata = plugin.metadata
            
            // Basic validation
            if (metadata.id.isBlank() || metadata.name.isBlank() || metadata.entryPoint.isBlank()) {
                Timber.e("Plugin validation failed: Missing required fields")
                return false
            }
            
            // Version compatibility check
            if (!isVersionCompatible(metadata.minAppVersion, metadata.maxAppVersion)) {
                Timber.e("Plugin validation failed: Version incompatibility")
                return false
            }
            
            // Permission validation
            if (!validatePermissions(metadata.permissions)) {
                Timber.e("Plugin validation failed: Invalid permissions")
                return false
            }
            
            Timber.d("Plugin validation successful: ${metadata.name}")
            return true
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin validation error")
            return false
        }
    }
    
    private fun checkDependencies(metadata: PluginMetadata): Boolean {
        return try {
            metadata.dependencies.all { dependency ->
                val dependentPlugin = installedPlugins[dependency]
                if (dependentPlugin != null && dependentPlugin.isEnabled) {
                    true
                } else {
                    Timber.e("Missing dependency: $dependency for plugin ${metadata.name}")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Dependency check failed")
            false
        }
    }
    
    private suspend fun performPluginInstallation(plugin: Plugin): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create plugin directory structure
            // Copy plugin files
            // Initialize plugin class loader
            // Register extensions
            
            Timber.d("Plugin installation completed: ${plugin.metadata.name}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin installation failed: ${plugin.metadata.name}")
            return@withContext false
        }
    }
    
    private suspend fun enablePlugin(plugin: Plugin): Boolean = withContext(Dispatchers.IO) {
        try {
            // Load plugin classes
            // Initialize plugin
            // Register extensions
            val extensions = loadPluginExtensions(plugin)
            extensions.forEach { extension ->
                registerExtension(extension)
            }
            
            Timber.d("Plugin enabled: ${plugin.metadata.name}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin enable failed: ${plugin.metadata.name}")
            return@withContext false
        }
    }
    
    private suspend fun disablePlugin(plugin: Plugin): Boolean = withContext(Dispatchers.IO) {
        try {
            // Unregister extensions
            plugin.extensions.forEach { extension ->
                unregisterExtension(extension)
            }
            
            // Cleanup plugin resources
            Timber.d("Plugin disabled: ${plugin.metadata.name}")
            return@withContext true
            
        } catch (e: Exception) {
            Timber.e(e, "Plugin disable failed: ${plugin.metadata.name}")
            return@withContext false
        }
    }
    
    private fun registerExtension(extension: Extension) {
        val currentExtensions = activeExtensions[extension.extensionPointId]?.toMutableList() ?: mutableListOf()
        currentExtensions.add(extension)
        currentExtensions.sortByDescending { it.priority }
        activeExtensions[extension.extensionPointId] = currentExtensions
        
        Timber.d("Extension registered: ${extension.id} for ${extension.extensionPointId}")
    }
    
    private fun unregisterExtension(extension: Extension) {
        val currentExtensions = activeExtensions[extension.extensionPointId]?.toMutableList()
        if (currentExtensions != null) {
            currentExtensions.removeAll { it.id == extension.id }
            activeExtensions[extension.extensionPointId] = currentExtensions
        }
        
        Timber.d("Extension unregistered: ${extension.id}")
    }
    
    private fun loadPluginExtensions(plugin: Plugin): List<Extension> {
        // In real implementation, this would load extensions from plugin manifest
        return emptyList()
    }
    
    private fun cleanupPluginFiles(plugin: Plugin) {
        // Clean up plugin files and directories
        Timber.d("Cleaning up plugin files: ${plugin.metadata.name}")
    }
    
    private fun isVersionCompatible(minVersion: String, maxVersion: String?): Boolean {
        // Simple version check - in real implementation would use proper version comparison
        val currentVersion = "2.5.0"
        return currentVersion >= minVersion && (maxVersion == null || currentVersion <= maxVersion)
    }
    
    private fun validatePermissions(permissions: List<String>): Boolean {
        val allowedPermissions = setOf(
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_NETWORK_STATE"
        )
        
        return permissions.all { it in allowedPermissions }
    }
    
    data class PluginPackage(
        val metadata: PluginMetadata,
        val files: Map<String, ByteArray>
    )
}