import os
import time
from groq import Groq

client = Groq(api_key=os.getenv("GROQ_API_KEY"))
MODEL = os.getenv("GROQ_MODEL", "llama-3.3-70b-versatile")

# track startup time for uptime calculation
START_TIME = time.time()

# track average response time
response_times = []

def call_groq(messages, max_tokens=1000):
    start = time.time()
    try:
        response = client.chat.completions.create(
            model=MODEL,
            messages=messages,
            temperature=0.3,
            max_tokens=max_tokens
        )
        elapsed = time.time() - start
        response_times.append(elapsed)
        if len(response_times) > 100:
            response_times.pop(0)
        return response.choices[0].message.content, False
    except Exception as e:
        print(f"Groq API error: {e}")
        return None, True

def get_avg_response_time():
    if not response_times:
        return 0
    return round(sum(response_times) / len(response_times), 3)