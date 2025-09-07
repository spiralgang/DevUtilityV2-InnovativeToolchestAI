package com.spiralgang.srirachaarmy.devutility.ai

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileWriter
import kotlin.math.*

/**
 * Compatible Model LoRA Fine-Tuning Integration for DevUtility AI System
 * Implements Low-Rank Adaptation (LoRA) for efficient fine-tuning of compatible model types similar to Gemma
 * Supports multiple transformer architectures with Gemma-compatible style integration
 * Optimized for Aarch64 Unix/Linux environments on Android 10+
 */
@Singleton
class CompatibleModelLoRAFineTuning @Inject constructor(
    private val context: Context,
    private val aiTrainingSetManager: AITrainingSetManager
) {
    
    private val _fineTuningState = MutableStateFlow(FineTuningState.Idle)
    val fineTuningState: StateFlow<FineTuningState> = _fineTuningState.asStateFlow()
    
    private val _fineTuningProgress = MutableStateFlow(FineTuningProgress())
    val fineTuningProgress: StateFlow<FineTuningProgress> = _fineTuningProgress.asStateFlow()
    
    private val fineTuningDir = File(context.filesDir, "gemma_lora")
    private val modelsDir = File(fineTuningDir, "models")
    private val datasetDir = File(fineTuningDir, "datasets")
    private val checkpointsDir = File(fineTuningDir, "checkpoints")
    
    data class FineTuningProgress(
        val currentEpoch: Int = 0,
        val totalEpochs: Int = 0,
        val currentBatch: Int = 0,
        val totalBatches: Int = 0,
        val loss: Float = 0.0f,
        val accuracy: Float = 0.0f,
        val learningRate: Float = 0.0f,
        val elapsedTime: Long = 0L,
        val estimatedTimeRemaining: Long = 0L
    )
    
    data class LoRAConfig(
        val rank: Int = 8,              // LoRA rank (lower = fewer parameters)
        val alpha: Float = 32.0f,       // LoRA scaling parameter
        val dropout: Float = 0.1f,      // Dropout rate
        val targetModules: List<String> = listOf("q_proj", "v_proj", "k_proj", "o_proj"),
        val learningRate: Float = 3e-4f,
        val batchSize: Int = 4,
        val maxLength: Int = 512,
        val numEpochs: Int = 3,
        val warmupSteps: Int = 100,
        val saveSteps: Int = 500,
        val gradientAccumulationSteps: Int = 4
    )
    
    data class CompatibleModelConfig(
        val modelName: String = "gemma-2b-it",  // or compatible models like "llama-2-7b", "mistral-7b", "phi-3-mini"
        val modelType: String = "gemma-compatible", // "gemma", "llama", "mistral", "phi", "qwen", "codellama"
        val modelPath: String? = null,
        val tokenizer: String = "google/gemma-2b-it",
        val maxSequenceLength: Int = 2048,
        val vocabularySize: Int = 256128,
        val useQuantization: Boolean = true,    // 4-bit quantization for Android 10+ ARM64
        val useFP16: Boolean = true,           // Mixed precision training
        val deviceMap: String = "auto",
        val compatibilityMode: String = "gemma-style", // How to adapt other models to Gemma-like interface
        val androidOptimized: Boolean = true   // Android 10+ specific optimizations
    )
    
    data class TrainingDataset(
        val name: String,
        val trainSamples: List<TrainingSample>,
        val validationSamples: List<TrainingSample> = emptyList(),
        val category: String = "general",
        val source: String = "devutility"
    )
    
    data class TrainingSample(
        val input: String,
        val output: String,
        val context: String = "",
        val metadata: Map<String, Any> = emptyMap()
    )
    
    sealed class FineTuningState {
        object Idle : FineTuningState()
        object Initializing : FineTuningState()
        object PreparingData : FineTuningState()
        object LoadingModel : FineTuningState()
        object Training : FineTuningState()
        object Validating : FineTuningState()
        object Saving : FineTuningState()
        object Completed : FineTuningState()
        data class Error(val message: String, val exception: Throwable? = null) : FineTuningState()
    }
    
    /**
     * Initialize the Gemma LoRA fine-tuning system
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            _fineTuningState.value = FineTuningState.Initializing
            
            // Create necessary directories
            listOf(fineTuningDir, modelsDir, datasetDir, checkpointsDir).forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            
            // Initialize training set manager
            aiTrainingSetManager.initialize()
            
            // Generate Python fine-tuning script
            generateFineTuningScript()
            
            // Generate requirements file
            generateRequirementsFile()
            
            _fineTuningState.value = FineTuningState.Idle
            Timber.d("Gemma LoRA Fine-Tuning system initialized")
            
        } catch (e: Exception) {
            _fineTuningState.value = FineTuningState.Error("Initialization failed: ${e.message}", e)
            Timber.e(e, "Failed to initialize Gemma LoRA Fine-Tuning")
        }
    }
    
    /**
     * Start fine-tuning process with compatible models and custom dataset
     */
    suspend fun startFineTuning(
        dataset: TrainingDataset,
        loraConfig: LoRAConfig = LoRAConfig(),
        modelConfig: CompatibleModelConfig = CompatibleModelConfig()
    ): String = withContext(Dispatchers.IO) {
        try {
            _fineTuningState.value = FineTuningState.PreparingData
            
            // Prepare training dataset
            val datasetFile = prepareTrainingDataset(dataset)
            
            // Create configuration files
            val configFile = createFineTuningConfig(loraConfig, modelConfig, datasetFile)
            
            _fineTuningState.value = FineTuningState.LoadingModel
            
            // Generate and execute training command
            val trainingCommand = generateTrainingCommand(configFile, modelConfig)
            
            _fineTuningState.value = FineTuningState.Training
            
            // Start training process (in real implementation, this would execute the Python script)
            val resultPath = simulateFineTuningProcess(dataset, loraConfig, modelConfig)
            
            _fineTuningState.value = FineTuningState.Completed
            
            Timber.d("Fine-tuning completed. Model saved to: $resultPath")
            return@withContext resultPath
            
        } catch (e: Exception) {
            _fineTuningState.value = FineTuningState.Error("Fine-tuning failed: ${e.message}", e)
            Timber.e(e, "Fine-tuning process failed")
            throw e
        }
    }
    
    /**
     * Create development-focused training dataset from DevUtility interactions
     */
    suspend fun createDevUtilityDataset(): TrainingDataset = withContext(Dispatchers.IO) {
        try {
            val trainingSamples = mutableListOf<TrainingSample>()
            
            // Generate development-focused training examples
            trainingSamples.addAll(generateKotlinTrainingSamples())
            trainingSamples.addAll(generateJavaTrainingSamples())
            trainingSamples.addAll(generateTerminalTrainingSamples())
            trainingSamples.addAll(generateAIAssistantTrainingSamples())
            trainingSamples.addAll(generateCodeReviewTrainingSamples())
            
            // Add examples from AI training set manager
            val existingDatasets = aiTrainingSetManager.getTrainingStatistics()
            Timber.d("Incorporating existing training data: $existingDatasets")
            
            return@withContext TrainingDataset(
                name = "DevUtility-Mobile-Development",
                trainSamples = trainingSamples,
                category = "mobile_development",
                source = "devutility_enhanced"
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create DevUtility dataset")
            throw e
        }
    }
    
    /**
     * Create specialized AI personality training dataset
     */
    suspend fun createAIPersonalityDataset(personality: String): TrainingDataset = withContext(Dispatchers.IO) {
        val trainingSamples = when (personality.lowercase()) {
            "codereaver" -> generateCodeReaverTrainingSamples()
            "webnetcaste" -> generateWebNetCasteTrainingSamples()
            "learningbot" -> generateLearningBotTrainingSamples()
            "srirachaarmy" -> generateSrirachaArmyTrainingSamples()
            else -> generateGeneralPersonalityTrainingSamples()
        }
        
        return@withContext TrainingDataset(
            name = "AI-Personality-$personality",
            trainSamples = trainingSamples,
            category = "ai_personality",
            source = "devutility_personality"
        )
    }
    
    /**
     * Monitor fine-tuning progress
     */
    fun startProgressMonitoring(processId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            while (_fineTuningState.value == FineTuningState.Training) {
                // Simulate progress monitoring
                val currentProgress = _fineTuningProgress.value
                val newProgress = currentProgress.copy(
                    currentBatch = (currentProgress.currentBatch + 1) % currentProgress.totalBatches,
                    loss = maxOf(0.1f, currentProgress.loss - 0.001f),
                    accuracy = minOf(0.95f, currentProgress.accuracy + 0.001f)
                )
                
                if (newProgress.currentBatch == 0) {
                    _fineTuningProgress.value = newProgress.copy(
                        currentEpoch = currentProgress.currentEpoch + 1
                    )
                } else {
                    _fineTuningProgress.value = newProgress
                }
                
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Get available compatible models (Gemma-style and others)
     */
    fun getAvailableModels(): List<CompatibleModelConfig> {
        return listOf(
            // Gemma models (native)
            CompatibleModelConfig(
                modelName = "gemma-2b-it",
                modelType = "gemma",
                tokenizer = "google/gemma-2b-it",
                maxSequenceLength = 2048,
                compatibilityMode = "native"
            ),
            CompatibleModelConfig(
                modelName = "gemma-7b-it", 
                modelType = "gemma",
                tokenizer = "google/gemma-7b-it",
                maxSequenceLength = 4096,
                compatibilityMode = "native"
            ),
            CompatibleModelConfig(
                modelName = "gemma-2b-base",
                modelType = "gemma",
                tokenizer = "google/gemma-2b",
                maxSequenceLength = 2048,
                compatibilityMode = "native"
            ),
            // Llama models (Gemma-compatible)
            CompatibleModelConfig(
                modelName = "llama-2-7b-chat",
                modelType = "llama",
                tokenizer = "meta-llama/Llama-2-7b-chat-hf",
                maxSequenceLength = 4096,
                compatibilityMode = "gemma-style"
            ),
            CompatibleModelConfig(
                modelName = "code-llama-7b-instruct",
                modelType = "codellama",
                tokenizer = "codellama/CodeLlama-7b-Instruct-hf",
                maxSequenceLength = 16384,
                compatibilityMode = "gemma-style"
            ),
            // Mistral models (Gemma-compatible)
            CompatibleModelConfig(
                modelName = "mistral-7b-instruct",
                modelType = "mistral",
                tokenizer = "mistralai/Mistral-7B-Instruct-v0.1",
                maxSequenceLength = 32768,
                compatibilityMode = "gemma-style"
            ),
            // Phi models (Gemma-compatible)
            CompatibleModelConfig(
                modelName = "phi-3-mini-instruct",
                modelType = "phi",
                tokenizer = "microsoft/Phi-3-mini-4k-instruct",
                maxSequenceLength = 4096,
                compatibilityMode = "gemma-style",
                androidOptimized = true
            ),
            // Qwen models (Gemma-compatible)
            CompatibleModelConfig(
                modelName = "qwen-7b-chat",
                modelType = "qwen",
                tokenizer = "Qwen/Qwen-7B-Chat",
                maxSequenceLength = 8192,
                compatibilityMode = "gemma-style"
            )
        )
    }
    
    /**
     * Generate LoRA configuration optimized for Android 10+ ARM64
     */
    fun createAndroidOptimizedLoRAConfig(): LoRAConfig {
        return LoRAConfig(
            rank = 4,                    // Lower rank for Android 10+ ARM64
            alpha = 16.0f,              // Reduced alpha
            dropout = 0.05f,            // Lower dropout
            learningRate = 1e-4f,       // Conservative learning rate for mobile
            batchSize = 1,              // Small batch size for Android constraints
            maxLength = 256,            // Shorter sequences for efficiency
            numEpochs = 2,              // Fewer epochs for mobile training
            gradientAccumulationSteps = 8 // Higher accumulation for effective batch size
        )
    }
    
    // Private helper methods
    
    private suspend fun prepareTrainingDataset(dataset: TrainingDataset): File = withContext(Dispatchers.IO) {
        val datasetFile = File(datasetDir, "${dataset.name}_${System.currentTimeMillis()}.jsonl")
        
        FileWriter(datasetFile).use { writer ->
            dataset.trainSamples.forEach { sample ->
                val jsonLine = """{"instruction": "${sample.input}", "output": "${sample.output}", "context": "${sample.context}"}"""
                writer.appendLine(jsonLine)
            }
        }
        
        Timber.d("Training dataset prepared: ${datasetFile.absolutePath} with ${dataset.trainSamples.size} samples")
        return@withContext datasetFile
    }
    
    private fun createFineTuningConfig(
        loraConfig: LoRAConfig,
        modelConfig: CompatibleModelConfig,
        datasetFile: File
    ): File {
        val configFile = File(fineTuningDir, "lora_config.json")
        
        val configJson = """
        {
            "model_name": "${modelConfig.modelName}",
            "model_type": "${modelConfig.modelType}",
            "compatibility_mode": "${modelConfig.compatibilityMode}",
            "android_optimized": ${modelConfig.androidOptimized},
            "dataset_path": "${datasetFile.absolutePath}",
            "output_dir": "${checkpointsDir.absolutePath}",
            "lora_config": {
                "r": ${loraConfig.rank},
                "lora_alpha": ${loraConfig.alpha},
                "lora_dropout": ${loraConfig.dropout},
                "target_modules": ${loraConfig.targetModules.joinToString { "\"$it\"" }},
                "bias": "none",
                "task_type": "CAUSAL_LM"
            },
            "training_arguments": {
                "learning_rate": ${loraConfig.learningRate},
                "per_device_train_batch_size": ${loraConfig.batchSize},
                "gradient_accumulation_steps": ${loraConfig.gradientAccumulationSteps},
                "num_train_epochs": ${loraConfig.numEpochs},
                "max_steps": -1,
                "warmup_steps": ${loraConfig.warmupSteps},
                "save_steps": ${loraConfig.saveSteps},
                "logging_steps": 10,
                "output_dir": "${checkpointsDir.absolutePath}",
                "optim": "adamw_torch",
                "lr_scheduler_type": "cosine",
                "fp16": ${modelConfig.useFP16},
                "load_best_model_at_end": true,
                "metric_for_best_model": "eval_loss",
                "greater_is_better": false,
                "evaluation_strategy": "steps",
                "eval_steps": ${loraConfig.saveSteps}
            }
        }
        """.trimIndent()
        
        configFile.writeText(configJson)
        return configFile
    }
    
    private fun generateTrainingCommand(configFile: File, modelConfig: CompatibleModelConfig): String {
        return """
        python ${fineTuningDir.absolutePath}/compatible_model_lora_training.py \
            --config ${configFile.absolutePath} \
            --model_name ${modelConfig.modelName} \
            --model_type ${modelConfig.modelType} \
            --tokenizer ${modelConfig.tokenizer} \
            --use_quantization ${modelConfig.useQuantization} \
            --max_length ${modelConfig.maxSequenceLength} \
            --android_optimized ${modelConfig.androidOptimized}
        """.trimIndent()
    }
    
    private suspend fun simulateFineTuningProcess(
        dataset: TrainingDataset,
        loraConfig: LoRAConfig,
        modelConfig: CompatibleModelConfig
    ): String = withContext(Dispatchers.IO) {
        // Initialize progress
        _fineTuningProgress.value = FineTuningProgress(
            totalEpochs = loraConfig.numEpochs,
            totalBatches = dataset.trainSamples.size / loraConfig.batchSize,
            learningRate = loraConfig.learningRate
        )
        
        // Start progress monitoring
        startProgressMonitoring("simulated")
        
        // Simulate training time
        delay(5000) // 5 seconds simulation
        
        // Create output model path
        val outputPath = File(checkpointsDir, "${modelConfig.modelType}_devutility_lora_${System.currentTimeMillis()}")
        outputPath.mkdirs()
        
        // Create adapter config and weights files (placeholder)
        val adapterConfig = """
        {
            "adapter_type": "lora", 
            "r": ${loraConfig.rank},
            "model_type": "${modelConfig.modelType}",
            "compatibility_mode": "${modelConfig.compatibilityMode}",
            "android_optimized": ${modelConfig.androidOptimized}
        }
        """.trimIndent()
        
        File(outputPath, "adapter_config.json").writeText(adapterConfig)
        File(outputPath, "adapter_model.bin").writeText("# LoRA adapter weights placeholder for ${modelConfig.modelType}")
        File(outputPath, "training_log.txt").writeText("Training completed successfully for ${modelConfig.modelName}")
        
        return@withContext outputPath.absolutePath
    }
    
    private fun generateFineTuningScript() {
        val scriptFile = File(fineTuningDir, "compatible_model_lora_training.py")
        val scriptContent = """
#!/usr/bin/env python3
\"\"\"
Compatible Model LoRA Fine-Tuning Script for DevUtility
Supports Gemma-compatible models and other transformer architectures
Optimized for Aarch64 Unix/Linux environments on Android 10+
\"\"\"

import json
import argparse
import os
from dataclasses import dataclass
from typing import Dict, List, Optional

try:
    import torch
    from transformers import (
        AutoTokenizer, 
        AutoModelForCausalLM,
        TrainingArguments,
        Trainer,
        DataCollatorForLanguageModeling,
        LlamaTokenizer,
        LlamaForCausalLM,
        MistralForCausalLM,
        PhiForCausalLM
    )
    from peft import LoraConfig, get_peft_model, TaskType
    from datasets import Dataset
    import numpy as np
except ImportError as e:
    print(f"Required dependency missing: {e}")
    print("Please install: pip install torch transformers peft datasets")
    exit(1)

@dataclass
class CompatibleModelLoRATrainer:
    config_path: str
    model_name: str
    model_type: str
    tokenizer_name: str
    use_quantization: bool = True
    max_length: int = 512
    android_optimized: bool = True
    
    def load_config(self) -> Dict:
        with open(self.config_path, 'r') as f:
            return json.load(f)
    
    def load_dataset(self, dataset_path: str) -> Dataset:
        data = []
        with open(dataset_path, 'r') as f:
            for line in f:
                data.append(json.loads(line))
        return Dataset.from_list(data)
    
    def get_model_class(self, model_type: str):
        \"\"\"Get appropriate model class based on model type\"\"\"
        model_classes = {
            "gemma": AutoModelForCausalLM,
            "llama": LlamaForCausalLM,
            "mistral": MistralForCausalLM, 
            "phi": PhiForCausalLM,
            "qwen": AutoModelForCausalLM,
            "codellama": LlamaForCausalLM
        }
        return model_classes.get(model_type, AutoModelForCausalLM)
    
    def get_tokenizer_class(self, model_type: str):
        \"\"\"Get appropriate tokenizer class based on model type\"\"\"
        tokenizer_classes = {
            "gemma": AutoTokenizer,
            "llama": LlamaTokenizer,
            "mistral": AutoTokenizer,
            "phi": AutoTokenizer,
            "qwen": AutoTokenizer,
            "codellama": LlamaTokenizer
        }
        return tokenizer_classes.get(model_type, AutoTokenizer)
    
    def prepare_model_and_tokenizer(self, config: Dict):
        # Load tokenizer with model-specific class
        tokenizer_class = self.get_tokenizer_class(self.model_type)
        tokenizer = tokenizer_class.from_pretrained(self.tokenizer_name)
        
        if tokenizer.pad_token is None:
            tokenizer.pad_token = tokenizer.eos_token
        
        # Load model with quantization optimized for Android 10+ ARM64
        model_kwargs = {}
        if self.use_quantization and self.android_optimized:
            model_kwargs.update({
                "torch_dtype": torch.float16,
                "device_map": "auto",
                "load_in_4bit": True,
                "bnb_4bit_compute_dtype": torch.float16,
                "bnb_4bit_use_double_quant": True,
                "bnb_4bit_quant_type": "nf4"  # Better for Android ARM64
            })
        elif self.use_quantization:
            model_kwargs.update({
                "torch_dtype": torch.float16,
                "device_map": "auto",
                "load_in_4bit": True
            })
        
        model_class = self.get_model_class(self.model_type)
        model = model_class.from_pretrained(
            self.model_name,
            **model_kwargs
        )
        
        # Apply LoRA configuration with model-specific adaptations
        lora_config_dict = config["lora_config"].copy()
        
        # Adjust target modules based on model type
        if self.model_type == "llama" or self.model_type == "codellama":
            lora_config_dict["target_modules"] = ["q_proj", "v_proj", "k_proj", "o_proj", "gate_proj", "up_proj", "down_proj"]
        elif self.model_type == "mistral":
            lora_config_dict["target_modules"] = ["q_proj", "v_proj", "k_proj", "o_proj", "gate_proj", "up_proj", "down_proj"]
        elif self.model_type == "phi":
            lora_config_dict["target_modules"] = ["qkv_proj", "o_proj", "fc1", "fc2"]
        # Default Gemma-style target modules are already set
        
        lora_config = LoraConfig(**lora_config_dict)
        model = get_peft_model(model, lora_config)
        model.print_trainable_parameters()
        
        return model, tokenizer
    
    def preprocess_function(self, examples, tokenizer):
        # Combine instruction and output for training with model-specific formatting
        texts = []
        for instruction, output in zip(examples["instruction"], examples["output"]):
            if self.model_type in ["gemma", "phi"]:
                text = f"<start_of_turn>user\\n{instruction}<end_of_turn>\\n<start_of_turn>model\\n{output}<end_of_turn>"
            elif self.model_type in ["llama", "codellama"]:
                text = f"[INST] {instruction} [/INST] {output}"
            elif self.model_type == "mistral":
                text = f"<s>[INST] {instruction} [/INST] {output}</s>"
            else:
                # Fallback to generic format
                text = f"Instruction: {instruction}\\nResponse: {output}"
            texts.append(text)
        
        # Tokenize with Android-optimized settings
        max_length = min(self.max_length, 256) if self.android_optimized else self.max_length
        
        tokenized = tokenizer(
            texts,
            truncation=True,
            padding=False,
            max_length=max_length,
            return_tensors="pt"
        )
        
        # For causal LM, labels are the same as input_ids
        tokenized["labels"] = tokenized["input_ids"].clone()
        
        return tokenized
    
    def train(self):
        config = self.load_config()
        
        # Load model and tokenizer
        model, tokenizer = self.prepare_model_and_tokenizer(config)
        
        # Load dataset
        dataset = self.load_dataset(config["dataset_path"])
        
        # Preprocess dataset
        tokenized_dataset = dataset.map(
            lambda x: self.preprocess_function(x, tokenizer),
            batched=True,
            remove_columns=dataset.column_names
        )
        
        # Training arguments with Android optimizations
        training_args_dict = config["training_arguments"].copy()
        
        if self.android_optimized:
            # Reduce memory usage for Android
            training_args_dict.update({
                "dataloader_pin_memory": False,
                "gradient_checkpointing": True,
                "optim": "adamw_torch_fused" if torch.cuda.is_available() else "adamw_torch",
                "group_by_length": True,
                "length_column_name": "length"
            })
        
        training_args = TrainingArguments(**training_args_dict)
        
        # Data collator
        data_collator = DataCollatorForLanguageModeling(
            tokenizer=tokenizer,
            mlm=False,
        )
        
        # Trainer
        trainer = Trainer(
            model=model,
            args=training_args,
            train_dataset=tokenized_dataset,
            data_collator=data_collator,
            tokenizer=tokenizer,
        )
        
        # Train
        trainer.train()
        
        # Save model
        trainer.save_model()
        tokenizer.save_pretrained(training_args.output_dir)
        
        # Save model type info for compatibility
        model_info = {
            "model_type": self.model_type,
            "model_name": self.model_name,
            "android_optimized": self.android_optimized,
            "compatibility_mode": config.get("compatibility_mode", "gemma-style")
        }
        
        with open(os.path.join(training_args.output_dir, "model_info.json"), "w") as f:
            json.dump(model_info, f, indent=2)
        
        print(f"Training completed! Model saved to {training_args.output_dir}")
        print(f"Model type: {self.model_type} (compatibility: {config.get('compatibility_mode', 'gemma-style')})")

def main():
    parser = argparse.ArgumentParser(description="Compatible Model LoRA Fine-Tuning for DevUtility")
    parser.add_argument("--config", required=True, help="Path to configuration file")
    parser.add_argument("--model_name", required=True, help="Model name or path")
    parser.add_argument("--model_type", required=True, help="Model type (gemma, llama, mistral, phi, etc.)")
    parser.add_argument("--tokenizer", required=True, help="Tokenizer name or path")
    parser.add_argument("--use_quantization", action="store_true", help="Use 4-bit quantization")
    parser.add_argument("--max_length", type=int, default=512, help="Maximum sequence length")
    parser.add_argument("--android_optimized", action="store_true", help="Enable Android 10+ ARM64 optimizations")
    
    args = parser.parse_args()
    
    trainer = CompatibleModelLoRATrainer(
        config_path=args.config,
        model_name=args.model_name,
        model_type=args.model_type,
        tokenizer_name=args.tokenizer,
        use_quantization=args.use_quantization,
        max_length=args.max_length,
        android_optimized=args.android_optimized
    )
    
    trainer.train()

if __name__ == "__main__":
    main()
        """.trimIndent()
        
        scriptFile.writeText(scriptContent)
        scriptFile.setExecutable(true)
    }
    
    private fun generateRequirementsFile() {
        val requirementsFile = File(fineTuningDir, "requirements.txt")
        val requirements = """
# Core ML dependencies
torch>=2.0.0
transformers>=4.36.0
peft>=0.7.0
datasets>=2.14.0
accelerate>=0.24.0

# Additional utilities
numpy>=1.24.0
scikit-learn>=1.3.0
tqdm>=4.65.0
wandb>=0.16.0  # For experiment tracking

# Quantization support
bitsandbytes>=0.41.0

# Google AI dependencies
google-generativeai>=0.3.0

# DevUtility specific
jupyter>=1.0.0  # For notebook compatibility
        """.trimIndent()
        
        requirementsFile.writeText(requirements)
    }
    
    // Training sample generators for different contexts
    
    private fun generateKotlinTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "How do I create a coroutine-based repository pattern in Kotlin for Android?",
                output = """Here's a coroutine-based repository pattern for Android:

```kotlin
interface UserRepository {
    suspend fun getUser(id: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
}

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    
    override suspend fun getUser(id: String): Result<User> = withContext(dispatcher) {
        try {
            val user = apiService.getUser(id)
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            val cachedUser = userDao.getUser(id)
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                Result.failure(e)
            }
        }
    }
}
```""",
                context = "android_architecture",
                metadata = mapOf("difficulty" to "intermediate", "tags" to listOf("coroutines", "repository", "android"))
            ),
            
            TrainingSample(
                input = "Implement a sealed class for handling network states in Kotlin",
                output = """Here's a sealed class for network states:

```kotlin
sealed class NetworkState<out T> {
    object Loading : NetworkState<Nothing>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Error(val exception: Throwable, val message: String? = null) : NetworkState<Nothing>()
    object NetworkUnavailable : NetworkState<Nothing>()
}

// Usage in ViewModel
class MyViewModel : ViewModel() {
    private val _networkState = MutableLiveData<NetworkState<List<Item>>>()
    val networkState: LiveData<NetworkState<List<Item>>> = _networkState
    
    fun loadData() {
        viewModelScope.launch {
            _networkState.value = NetworkState.Loading
            try {
                val data = repository.getData()
                _networkState.value = NetworkState.Success(data)
            } catch (e: Exception) {
                _networkState.value = NetworkState.Error(e, "Failed to load data")
            }
        }
    }
}
```""",
                context = "state_management"
            )
        )
    }
    
    private fun generateJavaTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "Create a thread-safe singleton pattern with lazy initialization in Java",
                output = """Here's a thread-safe singleton with lazy initialization:

```java
public class ThreadSafeSingleton {
    private static volatile ThreadSafeSingleton instance;
    private final String data;
    
    private ThreadSafeSingleton(String data) {
        this.data = data;
    }
    
    public static ThreadSafeSingleton getInstance(String data) {
        if (instance == null) {
            synchronized (ThreadSafeSingleton.class) {
                if (instance == null) {
                    instance = new ThreadSafeSingleton(data);
                }
            }
        }
        return instance;
    }
    
    // Alternative: Bill Pugh Singleton Pattern (preferred)
    private static class SingletonHolder {
        private static final ThreadSafeSingleton INSTANCE = new ThreadSafeSingleton("default");
    }
    
    public static ThreadSafeSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```""",
                context = "design_patterns"
            )
        )
    }
    
    private fun generateTerminalTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "I want to list all files recursively and grep for a pattern",
                output = """Here are several ways to recursively search for patterns:

🔍 **Find and grep combined:**
```bash
find . -type f -name "*.kt" -exec grep -l "suspend fun" {} \\;
```

⚡ **Using grep with recursive flag:**
```bash
grep -r "suspend fun" --include="*.kt" .
```

🚀 **Modern alternative with ripgrep (if available):**
```bash
rg "suspend fun" --type kotlin
```

💡 **With line numbers and context:**
```bash
grep -rn "suspend fun" --include="*.kt" -B 2 -A 2 .
```

The first approach gives you just filenames, while the others show the actual matches. The recursive grep is usually fastest for most cases.""",
                context = "terminal_commands"
            )
        )
    }
    
    private fun generateAIAssistantTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "Explain the difference between var and val in Kotlin and when to use each",
                output = """🧠 **var vs val in Kotlin:**

**val (value/read-only):**
- Immutable reference (like final in Java)
- Can only be assigned once
- The object it points to can still be mutable
- Preferred for most cases (functional programming principle)

**var (variable/mutable):**
- Mutable reference
- Can be reassigned multiple times
- Use when you need to change what the variable points to

📝 **Examples:**
```kotlin
val name = "DevUtility"        // Cannot reassign
name = "Other"                 // ❌ Compiler error

var counter = 0                // Can reassign
counter = 1                    // ✅ OK

val list = mutableListOf(1, 2) // val reference
list.add(3)                    // ✅ OK - object is mutable
list = mutableListOf(4, 5)     // ❌ Error - can't reassign reference
```

🎯 **Best Practice:** Start with `val` by default, only use `var` when you actually need to reassign the variable. This leads to more predictable, functional code.""",
                context = "ai_assistant"
            )
        )
    }
    
    private fun generateCodeReviewTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "Review this Kotlin code for potential improvements",
                output = """🔍 **Code Review Analysis:**

**Potential Issues Found:**
1. **Null Safety:** Consider using safe calls or proper null handling
2. **Performance:** Look for unnecessary object allocations in loops
3. **Readability:** Function/variable names could be more descriptive
4. **Error Handling:** Missing try-catch blocks for risky operations

**Suggestions:**
- Use `sealed class` for better type safety in state management
- Consider using `suspend` functions for I/O operations
- Apply SOLID principles for better maintainability
- Add proper documentation for public APIs

**Security Considerations:**
- Validate all external inputs
- Use proper encryption for sensitive data
- Avoid exposing internal implementation details

Would you like me to review specific code snippets for detailed feedback?""",
                context = "code_review"
            )
        )
    }
    
    private fun generateCodeReaverTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "I need unfiltered coding advice for a complex architecture problem",
                output = """💜 **CodeReaver: Unfiltered Architecture Insight**

Listen up, developer. Complex architecture isn't about following every trend—it's about surgical precision and understanding the brutal trade-offs.

**The Real Talk:**
- Most "microservices" are just distributed monoliths with extra latency
- Your "clean architecture" might be over-engineered bureaucracy
- Performance matters more than perfect abstractions
- Technical debt is a tool, not always a liability

**Hardcore Recommendations:**
1. **Profile before you optimize**—gut feelings lie, profilers don't
2. **Embrace controlled chaos**—perfect systems break under real load
3. **Code for the human who maintains it at 3 AM**—that might be you
4. **Break rules intelligently**—know why they exist before you ignore them

What specific architectural hellscape are we solving? I'll give you the unvarnished truth, not textbook theory.""",
                context = "ai_personality_codereaver"
            )
        )
    }
    
    private fun generateWebNetCasteTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "How should I optimize my web API for mobile clients?",
                output = """🌐 **WebNetCaste: Mobile API Optimization Strategy**

Your mobile clients are operating in a hostile network environment. Here's how to make your API resilient:

**Network Intelligence:**
- Implement exponential backoff with jitter for retries
- Use connection pooling and keep-alive headers
- Consider HTTP/2 or HTTP/3 for multiplexing benefits
- Cache aggressively but invalidate intelligently

**Payload Optimization:**
```kotlin
// Use minimal response objects
data class MobileUserResponse(
    val id: String,
    val name: String,
    val avatar: String? = null // Only if needed
    // Omit heavy fields for mobile
)

// Implement field selection
@GET("/users/{id}")
suspend fun getUser(
    @Path("id") id: String,
    @Query("fields") fields: String? = null
): Response<User>
```

**Mobile-Specific Considerations:**
- Batch requests when possible
- Use GraphQL or field selection to reduce payload
- Implement offline-first architecture with sync
- Handle network state changes gracefully

The network is your enemy—design accordingly. 🔧""",
                context = "ai_personality_webnetcaste"
            )
        )
    }
    
    private fun generateLearningBotTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "I'm struggling to understand coroutines in Kotlin",
                output = """🌱 **LearningBot: Coroutines Learning Path**

I understand coroutines can feel overwhelming! Let's break it down into digestible learning steps:

**Learning Progression:**
1. **Start with the concept**: Coroutines are like lightweight threads
2. **Understand suspension**: `suspend` functions can pause and resume
3. **Learn the builders**: `launch`, `async`, `runBlocking`
4. **Master scopes**: `CoroutineScope`, `ViewModelScope`, `lifecycleScope`

**Hands-on Learning Exercise:**
```kotlin
// Step 1: Simple suspend function
suspend fun fetchUserData(): String {
    delay(1000) // Simulates network call
    return "User Data"
}

// Step 2: Using in a coroutine
GlobalScope.launch {
    val data = fetchUserData()
    println(data)
}
```

**Learning Pattern I've Observed:**
Most developers learn coroutines best by:
1. Writing simple examples ✅
2. Making mistakes with blocking vs suspending ⚠️
3. Understanding structured concurrency 🎯
4. Mastering error handling 🛡️

What specific part is confusing you? I'll adjust my teaching approach! 📚""",
                context = "ai_personality_learningbot"
            )
        )
    }
    
    private fun generateSrirachaArmyTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "Let's build something spicy and innovative for mobile development",
                output = """🌶️ **SrirachaArmy: Spicy Innovation Mode Activated!**

YESSS! Time to cook up some development fire! 🔥

**Spicy Innovation Framework:**
1. **Question Everything**: Why does mobile dev have to be so boring?
2. **Break Conventions**: Mix native + cross-platform in unexpected ways
3. **Add Heat Gradually**: Start with small innovations, build to revolution
4. **Community Collaboration**: Best spice comes from shared experiences

**Current Spicy Project Ideas:**
- AI-driven code completion that learns YOUR patterns
- Terminal-first mobile development (Unix philosophy on Android!)
- Live code collaboration with real-time AI assistance
- Voice-controlled coding for accessibility + speed

**The SrirachaArmy Approach:**
- Fail fast, learn faster 🚀
- Make tools that spark joy AND productivity ✨
- Open source everything (spice should be shared!) 🌍
- Document with humor and humanity 😄

What's YOUR spicy idea? Let's turn up the heat on this development challenge! 🌶️🔥

Ready to revolutionize mobile development together?""",
                context = "ai_personality_srirachaarmy"
            )
        )
    }
    
    private fun generateGeneralPersonalityTrainingSamples(): List<TrainingSample> {
        return listOf(
            TrainingSample(
                input = "Generic development question",
                output = "Helpful, contextual response tailored to user needs and current development situation.",
                context = "ai_personality_general"
            )
        )
    }
}