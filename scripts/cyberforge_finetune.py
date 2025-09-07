#!/usr/bin/env python3
"""
CODE-REAVER Cyberforge Fine-Tuning Script
Fine-tunes Hugging Face models with classical algorithm comparison dataset
"""

import json
import os
import sys
from datetime import datetime
from transformers import AutoTokenizer, AutoModelForCausalLM, Trainer, TrainingArguments
from datasets import load_dataset

def log_event(event_type, details):
    """Log events to training metrics"""
    timestamp = datetime.now().isoformat()
    log_entry = {
        "timestamp": timestamp,
        "event": event_type,
        "details": details
    }
    
    with open('logs/training_metrics.jsonl', 'a') as f:
        f.write(json.dumps(log_entry) + '\n')

def validate_dataset_security(dataset_path):
    """Security validation for dataset - prevent code injection"""
    print("[CYBERFORGE] Validating dataset security...")
    
    with open(dataset_path, 'r') as f:
        content = f.read()
    
    # Check for dangerous patterns
    dangerous_patterns = [
        'os.system', 'subprocess.run', 'subprocess.call', 'subprocess.Popen',
        'eval(', 'exec(', '__import__', 'open(', 'file(',
        'rm -rf', 'sudo', 'chmod 777'
    ]
    
    for pattern in dangerous_patterns:
        if pattern in content:
            error_msg = f"Security violation: Found dangerous pattern '{pattern}' in dataset"
            print(f"[CYBERFORGE ERROR] {error_msg}")
            log_event("SECURITY_VIOLATION", {"pattern": pattern, "file": dataset_path})
            with open('logs/error.log', 'a') as f:
                f.write(f"{datetime.now().isoformat()} - {error_msg}\n")
            return False
    
    print("[CYBERFORGE] Dataset security validation passed")
    return True

def load_and_prepare_dataset():
    """Load and prepare the classical algorithm comparison dataset"""
    dataset_path = 'datasets/classical_algo_comparison.jsonl'
    
    if not os.path.exists(dataset_path):
        error_msg = f"Dataset not found: {dataset_path}"
        print(f"[CYBERFORGE ERROR] {error_msg}")
        log_event("DATASET_ERROR", {"error": error_msg})
        with open('logs/error.log', 'a') as f:
            f.write(f"{datetime.now().isoformat()} - {error_msg}\n")
        return None
    
    # Security check
    if not validate_dataset_security(dataset_path):
        return None
    
    try:
        dataset = load_dataset('json', data_files=dataset_path, split='train')
        print(f"[CYBERFORGE] Loaded dataset with {len(dataset)} examples")
        log_event("DATASET_LOADED", {"examples": len(dataset), "file": dataset_path})
        return dataset
    except Exception as e:
        error_msg = f"Failed to load dataset: {str(e)}"
        print(f"[CYBERFORGE ERROR] {error_msg}")
        log_event("DATASET_ERROR", {"error": error_msg})
        with open('logs/error.log', 'a') as f:
            f.write(f"{datetime.now().isoformat()} - {error_msg}\n")
        return None

def fine_tune_model(model_dir, dataset):
    """Fine-tune a single model with the dataset"""
    try:
        print(f"[CYBERFORGE] Fine-tuning {model_dir}...")
        
        # Load model and tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_dir)
        model = AutoModelForCausalLM.from_pretrained(model_dir)
        
        # Add padding token if it doesn't exist
        if tokenizer.pad_token is None:
            tokenizer.pad_token = tokenizer.eos_token
        
        # Prepare training data
        def tokenize_function(examples):
            # Format input-output pairs for training
            texts = []
            for input_text, output_data in zip(examples['input'], examples['output']):
                # Convert output dict to string if needed
                if isinstance(output_data, dict):
                    output_text = json.dumps(output_data)
                else:
                    output_text = str(output_data)
                
                formatted_text = f"Input: {input_text}\nOutput: {output_text}\n"
                texts.append(formatted_text)
            
            return tokenizer(texts, truncation=True, padding='max_length', max_length=512)
        
        tokenized_dataset = dataset.map(tokenize_function, batched=True)
        
        # Training arguments - lightweight for mobile/resource constraints
        training_args = TrainingArguments(
            output_dir=f"{model_dir}/fine-tuned",
            num_train_epochs=1,  # Light training to avoid overfitting
            per_device_train_batch_size=1,  # Small batch for memory constraints
            gradient_accumulation_steps=4,  # Simulate larger batch
            warmup_steps=10,
            logging_steps=10,
            save_strategy="no",  # Don't save checkpoints to save space
            remove_unused_columns=False,
            dataloader_pin_memory=False,  # Reduce memory usage
        )
        
        # Initialize trainer
        trainer = Trainer(
            model=model,
            args=training_args,
            train_dataset=tokenized_dataset,
            tokenizer=tokenizer,
        )
        
        # Fine-tune
        print(f"[CYBERFORGE] Starting training for {model_dir}...")
        trainer.train()
        
        # Save fine-tuned model
        model.save_pretrained(model_dir)
        tokenizer.save_pretrained(model_dir)
        
        print(f"[CYBERFORGE] Successfully fine-tuned {model_dir}")
        log_event("MODEL_FINETUNED", {"model": model_dir, "status": "success"})
        
        return True
        
    except Exception as e:
        error_msg = f"Error fine-tuning {model_dir}: {str(e)}"
        print(f"[CYBERFORGE ERROR] {error_msg}")
        log_event("MODEL_ERROR", {"model": model_dir, "error": error_msg})
        with open('logs/error.log', 'a') as f:
            f.write(f"{datetime.now().isoformat()} - {error_msg}\n")
        return False

def main():
    """Main fine-tuning orchestration"""
    print("[CYBERFORGE] Starting model fine-tuning process...")
    
    # Load model manifest
    try:
        with open('configs/model_manifest.json', 'r') as f:
            manifest = json.load(f)
        models = manifest.get('models', [])
    except Exception as e:
        error_msg = f"Failed to load model manifest: {str(e)}"
        print(f"[CYBERFORGE ERROR] {error_msg}")
        log_event("MANIFEST_ERROR", {"error": error_msg})
        with open('logs/error.log', 'a') as f:
            f.write(f"{datetime.now().isoformat()} - {error_msg}\n")
        sys.exit(1)
    
    # Load dataset
    dataset = load_and_prepare_dataset()
    if dataset is None:
        print("[CYBERFORGE ERROR] Dataset loading failed, aborting fine-tuning")
        sys.exit(1)
    
    # Fine-tune each model
    success_count = 0
    for model_config in models:
        model_dir = f"models/{model_config['dir']}"
        
        if not os.path.exists(model_dir):
            print(f"[CYBERFORGE WARNING] Model directory not found: {model_dir}")
            continue
        
        if fine_tune_model(model_dir, dataset):
            success_count += 1
    
    # Final summary
    total_models = len([m for m in models if os.path.exists(f"models/{m['dir']}")])
    if success_count == total_models and total_models > 0:
        print(f"[CYBERFORGE] All {success_count} models successfully fine-tuned!")
        log_event("FINETUNING_COMPLETE", {"success": success_count, "total": total_models})
    else:
        print(f"[CYBERFORGE] Fine-tuning completed: {success_count}/{total_models} successful")
        log_event("FINETUNING_PARTIAL", {"success": success_count, "total": total_models})
        if success_count == 0:
            sys.exit(1)

if __name__ == "__main__":
    main()