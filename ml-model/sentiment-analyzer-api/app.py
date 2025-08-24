from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import numpy as np

app = Flask(__name__)
CORS(app)

MODEL_NAME = "SamLowe/roberta-base-go_emotions"

# Load tokenizer and model
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

# Load label names (from model config)
id2label = model.config.id2label

def softmax(x):
    e_x = np.exp(x - np.max(x))
    return e_x / e_x.sum()

@app.route('/predict', methods=['POST'])
def predict_emotion():
    data = request.get_json()
    text = data.get('text', '')

    if not text:
        return jsonify({'error': 'No text provided'}), 400

    # Tokenize
    inputs = tokenizer(text, return_tensors="pt", truncation=True)

    # Get model output
    with torch.no_grad():
        outputs = model(**inputs)

    logits = outputs.logits[0].numpy()
    probs = softmax(logits)

    # Get label scores dictionary
    emotion_scores = {id2label[i]: float(probs[i]) for i in range(len(probs))}

    # Sort and get dominant emotion
    sorted_emotions = sorted(emotion_scores.items(), key=lambda x: x[1], reverse=True)
    dominant_emotion = sorted_emotions[0]

    return jsonify({
        "dominant_emotion": {
            "label": dominant_emotion[0],
            "score": round(dominant_emotion[1], 4)
        },
        "all_emotions": {
            label: round(score, 4) for label, score in sorted_emotions[:5]  # Top 5
        }
    })

if __name__ == '__main__':
    app.run(port=8000, debug=True)

