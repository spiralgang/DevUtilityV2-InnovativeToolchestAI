#!/usr/bin/env python3
import json, os, sys
from datetime import datetime
from transformers import AutoTokenizer, AutoModelForCausalLM, Trainer, TrainingArguments
from datasets import load_dataset

def log_event(event_type, details):
    with open('logs/training_metrics.jsonl', 'a') as f:
        f.write(json.dumps({"ts": datetime.now().isoformat(), "event": event_type, "details": details}) + '\n')

def validate_dataset(dataset_path):
    with open(dataset_path, 'r') as f:
        content = f.read()
    dangerous = ['os.system', 'subprocess.run', 'eval(', 'exec(']
    for pattern in dangerous:
        if pattern in content:
            log_event("SECURITY_VIOLATION", {"pattern": pattern, "file": dataset_path})
            with open('logs/error.log', 'a') as f:
                f.write(f"{datetime.now().isoformat()} - Security violation: {pattern}\n")
            return False
    return True

def fine_tune_model(model_dir, dataset):
    try:
        tokenizer = AutoTokenizer.from_pretrained(model_dir)
        model = AutoModelForCausalLM.from_pretrained(model_dir)
        if tokenizer.pad_token is None:
            tokenizer.pad_token = tokenizer.eos_token

        def tokenize_function(examples):
            texts = [f"Input: {inp}\nOutput: {json.dumps(out)}\n" for inp, out in zip(examples['input'], examples['output'])]
            return tokenizer(texts, truncation=True, padding='max_length', max_length=512)

        tokenized_dataset = dataset.map(tokenize_function, batched=True)
        training_args = TrainingArguments(
            output_dir=f"{model_dir}/fine-tuned",
            num_train_epochs=1,
            per_device_train_batch_size=1,
            gradient_accumulation_steps=4,
            warmup_steps=10,
            logging_steps=10,
            save_strategy="no",
            dataloader_pin_memory=False
        )
        trainer = Trainer(model=model, args=training_args, train_dataset=tokenized_dataset, tokenizer=tokenizer)
        trainer.train()
        model.save_pretrained(model_dir)
        tokenizer.save_pretrained(model_dir)
        log_event("MODEL_FINETUNED", {"model": model_dir, "status": "success"})
        return True
    except Exception as e:
        log_event("MODEL_ERROR", {"model": model_dir, "error": str(e)})
        with open('logs/error.log', 'a') as f:
            f.write(f"{datetime.now().isoformat()} - Error fine-tuning {model_dir}: {str(e)}\n")
        return False

def main():
    dataset_path = 'datasets/classical_algo_comparison.jsonl'
    if not os.path.exists(dataset_path) or not validate_dataset(dataset_path):
        log_event("DATASET_ERROR", {"error": "Dataset missing or insecure"})
        sys.exit(1)

    dataset = load_dataset('json', data_files=dataset_path, split='train')
    with open('configs/model_manifest.json', 'r') as f:
        models = json.load(f)['models']

    success_count = 0
    for model in models:
        model_dir = f"models/{model['dir']}"
        if os.path.exists(model_dir) and fine_tune_model(model_dir, dataset):
            success_count += 1

    log_event("FINETUNING_SUMMARY", {"success": success_count, "total": len(models)})
    if success_count == 0:
        sys.exit(1)

if __name__ == "__main__":
    main()
