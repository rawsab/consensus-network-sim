import subprocess

# Fix script to support concurrent launching of Flask and HTTP servers

print("Launching Flask server...")
flask_process = subprocess.Popen(["python3", "app.py"])
print("Flask server launched successfully.")

print("Launching HTTP server...")
http_process = subprocess.Popen(["python3", "-m", "http.server", "8000"])
print("HTTP server launched successfully.")

try:
    flask_process.wait()
    http_process.wait()
except KeyboardInterrupt:
    print("Shutting down servers...")
    flask_process.terminate()
    http_process.terminate()
