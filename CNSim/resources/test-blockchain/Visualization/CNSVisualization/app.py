from flask import Flask, jsonify, send_from_directory, abort
from flask_cors import CORS
import subprocess
import os

app = Flask(__name__)
CORS(app)

# Setup clients
@app.route('/run_setup_clients', methods=['POST'])
def run_setup_clients():
    try:
        subprocess.run(["python3", "../../setupClients.py"], check=True)
        return jsonify({"status": "success", "message": "Clients Setup script executed successfully."})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": f"Error running script: {str(e)}"}), 500

# Setup nodes
@app.route('/run_setup_nodes', methods=['POST'])
def run_setup_nodes():
    try:
        subprocess.run(["python3", "../../setupNodes.py"], check=True)
        return jsonify({"status": "success", "message": "Nodes Setup script executed successfully."})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": f"Error running script: {str(e)}"}), 500

# Full configuration setup
@app.route('/run_full_config', methods=['POST'])
def run_full_config():
    try:
        subprocess.run(["python3", "../../main.py"], check=True)
        return jsonify({"status": "success", "message": "main.py script executed successfully."})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": f"Error running script: {str(e)}"}), 500

# Run CNSim driver
@app.route('/run_cnsim_driver', methods=['POST'])
def run_cnsim_driver():
    try:
        subprocess.run(["python3", "../../../../../compile_and_run.py"], check=True)
        return jsonify({"status": "success", "message": "CNSim Driver script executed successfully."})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": f"Error running script: {str(e)}"}), 500

# Generate frames
@app.route('/run_generate_frames', methods=['POST'])
def run_generate_frames():
    try:
        subprocess.run(["python3", "generateFrames.py"], check=True)
        return jsonify({"status": "success", "message": "Frames generation script executed successfully."})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": f"Error running script: {str(e)}"}), 500


# get list of log folders
@app.route('/list-folders', methods=['GET'])
def list_folders():
    directory_path = '../../../../../log'
    folder_names = [folder for folder in os.listdir(directory_path) if os.path.isdir(os.path.join(directory_path, folder))]
    return jsonify(folder_names)


LOG_BASE_DIR = os.path.abspath('../../../../../log')

@app.route('/log/<path:subfolder>/frames.json', methods=['GET'])
def serve_frames(subfolder):
    full_path = os.path.join(LOG_BASE_DIR, subfolder)

    if not os.path.isdir(full_path):
        abort(404, description="Subfolder not found")
    frames_file = os.path.join(full_path, "frames.json")
    if not os.path.isfile(frames_file):
        abort(404, description="frames.json not found in the subfolder")

    return send_from_directory(full_path, "frames.json")


if __name__ == '__main__':
    app.run(debug=True)
