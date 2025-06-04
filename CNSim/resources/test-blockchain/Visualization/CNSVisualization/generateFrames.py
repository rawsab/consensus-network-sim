import pandas as pd
import numpy as np
import json
import re
import math
import datetime
import time
import os

start_time = time.time()

# Load latest run file
with open('../../../../../log/LatestFileName.txt', 'r') as file:
    latest_run = file.read().strip()

def current_time():
    now = datetime.datetime.now()
    return now.strftime("%d-%m-%Y %H:%M:%S")

print(f"[{current_time()}] Latest run: {latest_run}")
print(f"[{current_time()}] Reading logs...")

# Define paths to logs
client_log_path = f'../../../../../log/{latest_run}/ClientLog - {latest_run}.csv'
account_log_path = f'../../../../../log/{latest_run}/AccountLog - {latest_run}.csv'
node_trx_event_log_path = f'../../../../../log/{latest_run}/NodeTrxEventLog - {latest_run}.csv'
state_message_log_path = f'../../../../../log/{latest_run}/StateMessageLog - {latest_run}.csv'
node_message_log_path = f'../../../../../log/{latest_run}/NodeMessageLog - {latest_run}.csv'
client_message_log_path = f'../../../../../log/{latest_run}/ClientMessageLog - {latest_run}.csv'

# Read logs
account_log = pd.read_csv(account_log_path, sep='\t', on_bad_lines='skip')
client_log = pd.read_csv(client_log_path, sep='\t', on_bad_lines='skip')
node_trx_event_log = pd.read_csv(node_trx_event_log_path, sep='\t', on_bad_lines='skip')
state_message_log = pd.read_csv(state_message_log_path, sep='\t', on_bad_lines='skip')
node_message_log = pd.read_csv(node_message_log_path, sep='\t', on_bad_lines='skip')
client_message_log = pd.read_csv(client_message_log_path, sep='\t', on_bad_lines='skip')

# Clean column names
account_log.columns = account_log.columns.str.strip()
client_log.columns = client_log.columns.str.strip()
node_trx_event_log.columns = node_trx_event_log.columns.str.strip()
state_message_log.columns = state_message_log.columns.str.strip()
node_message_log.columns = node_message_log.columns.str.strip()
client_message_log.columns = client_message_log.columns.str.strip()

print(f"[{current_time()}] Logs read successfully.")

frames = {}

def extract_account_ids(content):
    account_ids = re.findall(r'Account ID:\s*(\d+)', content)
    return [int(account_id) for account_id in account_ids]

client_simtimes = sorted(set(client_log['SimTime']))
account_simtimes = sorted(set(account_log['SimTime']))
node_simtimes = sorted(set(node_trx_event_log['SimTime']))
state_simtimes = sorted(set(state_message_log['SimTime']))
node_message_simtimes = sorted(set(node_message_log['SimTime']))
client_message_simtimes = sorted(set(client_message_log['SimTime']))

all_simtimes = sorted(set(client_simtimes).union(
    set(account_simtimes),
    set(node_simtimes),
    set(state_simtimes),
    set(node_message_simtimes),
    set(client_message_simtimes)
))

# Initialize frames for all simulation times
for simtime in all_simtimes:
    frames[int(simtime)] = {'clients': {}, 'nodes': {}}

print(f"[{current_time()}] Processing client and account data...")

# Get all unique client IDs from the client log
unique_client_ids = set(client_log['ClientID'].unique())

# Process clients and accounts data
for _, row in client_log.iterrows():
    simtime = int(row['SimTime'])
    client_id = int(row['ClientID'])
    content = row['Content']
    client_data = {
        'ID': client_id,
        'Type': row['Type'],
        'Content': content,
        'accounts': {},
        'NodeIds': []
    }

    # Extract account IDs from the content
    account_ids = extract_account_ids(content)
    for account_id in account_ids:
        matching_account = account_log[
            (account_log['SimTime'] <= simtime) & (account_log['AccountID'] == account_id)
        ]
        if not matching_account.empty:
            latest_account = matching_account.iloc[-1]
            account_data = {
                'ID': int(latest_account['AccountID']),
                'Balance': float(latest_account['Balance']),
                'State': latest_account['State']
            }
            client_data['accounts'][account_id] = account_data

    frames[simtime]['clients'][client_id] = client_data

print(f"[{current_time()}] Client and account data processed successfully.")

print(f"[{current_time()}] Syncing client accounts across SimTimes...")

# Initialize default accounts for all clients
default_account = {"ID": None, "Balance": None, "State": None}

# Ensure all clients are initialized for each SimTime
for simtime in sorted(frames.keys()):
    for client_id in unique_client_ids:
        client_id = int(client_id)  # Ensure client_id is a Python int
        if client_id not in frames[simtime]["clients"]:
            frames[simtime]["clients"][client_id] = {
                "ID": client_id,
                "Type": None,
                "Content": None,
                "accounts": {},
                "NodeIds": []
            }

# Initialize previous accounts
previous_accounts = {int(client_id): default_account.copy() for client_id in unique_client_ids}

for simtime in sorted(frames.keys()):
    current_accounts = {}

    for client_id, client_data in frames[simtime]["clients"].items():
        client_id = int(client_id)

        current_account = previous_accounts.get(client_id, default_account.copy())

        matching_account = account_log[
            (account_log['SimTime'] == simtime) & (account_log['AccountID'] == client_id)
        ]
        if not matching_account.empty:
            latest_account = matching_account.iloc[-1]
            current_account["ID"] = int(latest_account["AccountID"])
            current_account["Balance"] = float(latest_account["Balance"])
            current_account["State"] = latest_account["State"]

        current_accounts[client_id] = current_account

    for client_id in unique_client_ids:
        client_id = int(client_id)
        if client_id not in current_accounts:
            current_accounts[client_id] = previous_accounts.get(client_id, default_account.copy())

    for client_id, account in current_accounts.items():
        client_id = int(client_id)
        frames[simtime]["clients"][client_id]["accounts"] = {account["ID"]: account}

    previous_accounts = current_accounts.copy()

print(f"[{current_time()}] Client accounts synced successfully.")
print(f"[{current_time()}] Processing node data...")

# Process nodes data from NodeTrxEventLog
all_node_ids = set(node_trx_event_log['NodeId'].unique())

# Adjust node data to store multiple ClientIds per node
for _, row in node_trx_event_log.iterrows():
    simtime = int(row['SimTime'])
    node_id = row['NodeId']
    client_id = row['ClientId']

    if node_id not in frames[simtime]['nodes']:
        frames[simtime]['nodes'][node_id] = {
            'NodeId': node_id,
            'ClientIds': [],
            'EventType': row['EventType'],
            'Epoch': row['Epoch'],
            'Details': None if pd.isna(row['Details']) else row['Details'],
            'NodesList': [],
            'StateType': None,
            'StateDetails': None
        }
    
    # Add client_id to ClientIds list, avoiding duplicates
    if client_id not in frames[simtime]['nodes'][node_id]['ClientIds']:
        frames[simtime]['nodes'][node_id]['ClientIds'].append(client_id)

print(f"[{current_time()}] Node data processed successfully.")
print(f"[{current_time()}] Processing state message data...")

# Process StateMessageLog data for each simtime
unique_nodes = set(state_message_log['Sender']).union(set(state_message_log['Receiver']))

for simtime in state_simtimes:
    simtime_data = state_message_log[state_message_log['SimTime'] == simtime]

    for _, row in simtime_data.iterrows():
        sender = row['Sender']
        if sender not in frames[simtime]['nodes']:
            frames[simtime]['nodes'][sender] = {
                'NodeId': sender,
                'ClientIds': [],
                'EventType': None,
                'Epoch': None,
                'Details': None,
                'NodesList': [],
                'StateType': row['Type'],
                'StateDetails': row['Details']
            }
        frames[simtime]['nodes'][sender]['StateType'] = row['Type']
        frames[simtime]['nodes'][sender]['StateDetails'] = row['Details']

        # Append each receiver to the sender's NodesList for the current simtime
        if 'NodesList' not in frames[simtime]['nodes'][sender]:
            frames[simtime]['nodes'][sender]['NodesList'] = []
        frames[simtime]['nodes'][sender]['NodesList'].append(row['Receiver'])

# Process NodeMessageLog for NodeIds in clients
print(f"[{current_time()}] Processing NodeMessageLog for client NodeIds...")

for _, row in node_message_log.iterrows():
    simtime = int(row['SimTime'])
    sender = int(row['Sender'])  # Client ID
    receiver = row['Receiver']  # Node ID

    # Ensure the sender client exists in the frame
    if sender not in frames[simtime]['clients']:
        frames[simtime]['clients'][sender] = {
            'ID': sender,
            'Type': None,
            'Content': None,
            'accounts': {},
            'NodeIds': []
        }

    # Add the receiver NodeId to the sender's NodeIds list if not already present
    if receiver not in frames[simtime]['clients'][sender]['NodeIds']:
        frames[simtime]['clients'][sender]['NodeIds'].append(receiver)

print(f"[{current_time()}] NodeMessageLog processed successfully.")

print(f"[{current_time()}] Processing node messages...")

for simtime, frame_data in frames.items():
    for client_id, client_data in frame_data['clients'].items():
        updated_node_ids = {}
        for node_id in client_data['NodeIds']:
            matching_node_message = node_message_log[
                (node_message_log['SimTime'] == simtime) & (node_message_log['Receiver'] == node_id)
            ]
            if not matching_node_message.empty:
                latest_message = matching_node_message.iloc[-1]
                updated_node_ids[node_id] = {
                    'Type': latest_message['Type'] if pd.notna(latest_message['Type']) else None,
                    'Details': latest_message['Details'] if pd.notna(latest_message['Details']) else None
                }
            else:
                updated_node_ids[node_id] = {
                    'Type': None,
                    'Details': None
                }
        client_data['NodeIds'] = updated_node_ids
        
print(f"[{current_time()}] Node messages processed successfully.")

print(f"[{current_time()}] Initializing nodes in frames from ClientMessageLog...")

for _, row in client_message_log.iterrows():
    simtime = int(row['SimTime'])
    sender_node = row['Sender'] 
    
    if simtime not in frames:
        frames[simtime] = {'clients': {}, 'nodes': {}}
    
    # Ensure the sender node exists in the current SimTime's nodes
    if sender_node not in frames[simtime]['nodes']:
        frames[simtime]['nodes'][sender_node] = {
            'NodeId': sender_node,
            'ClientIds': {},
            'EventType': None,
            'Epoch': None,
            'Details': None,
            'NodesList': [],
            'StateType': None,
            'StateDetails': None
        }

print(f"[{current_time()}] Nodes initialized in frames from ClientMessageLog.")

print(f"[{current_time()}] Processing client messages...")

for simtime, frame_data in frames.items():
    for node_id, node_data in frame_data['nodes'].items():
        updated_client_ids = {}

        matching_client_messages = client_message_log[
            (client_message_log['SimTime'] == simtime) & (client_message_log['Sender'] == node_id)
        ]

        for _, message_row in matching_client_messages.iterrows():
            client_id = message_row['Receiver']
            updated_client_ids[client_id] = {
                'Type': message_row['Type'] if pd.notna(message_row['Type']) else None,
                'Details': message_row['Details'] if pd.notna(message_row['Details']) else None
            }

        node_data['ClientIds'] = updated_client_ids

print(f"[{current_time()}] Client messages processed successfully.")

# Add missing nodes and clients in each frame with default values
for simtime, frame_data in frames.items():
    for node in unique_nodes:
        if node not in frame_data['nodes']:
            frame_data['nodes'][node] = {
                'NodeId': node,
                'ClientIds': {},
                'EventType': None,
                'Epoch': None,
                'Details': None,
                'NodesList': [],
                'StateType': None,
                'StateDetails': None
            }
    for client_id in unique_client_ids:
        if client_id not in frame_data['clients']:
            frame_data['clients'][client_id] = {
                'ID': client_id,
                'Type': None,
                'Content': None,
                'accounts': {},
                'NodeIds': {}
            }

print(f"[{current_time()}] Nodes and clients added successfully.")
print(f"[{current_time()}] Serializing and writing data to frames.json...")

# Serialization function
def convert_types(obj):
    if isinstance(obj, dict):
        return {str(k): convert_types(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_types(i) for i in obj]
    elif isinstance(obj, pd._libs.tslibs.nattype.NaTType):
        return None
    elif isinstance(obj, pd.Timestamp):
        return str(obj)
    elif isinstance(obj, (np.integer, int)):
        return int(obj)
    elif isinstance(obj, (np.floating, float)):
        return float(obj)
    elif isinstance(obj, (int, float)):
        return None if math.isnan(obj) else obj
    elif isinstance(obj, str):
        return obj
    else:
        return obj

os.makedirs('framesArchive', exist_ok=True)

# set default frames for initial loading
output_path = f'frames.json'
with open(output_path, 'w') as json_file:
    json.dump(convert_types(frames), json_file, indent=2)

print(f"[{current_time()}] Data written to frames.json successfully.")

# Save data to archives
output_path = f'framesArchive/{latest_run}.json'
with open(output_path, 'w') as json_file:
    json.dump(convert_types(frames), json_file, indent=2)

print(f"[{current_time()}] Data successfully saved to archives.")

end_time = time.time()
elapsed_time = end_time - start_time
print(f"[{current_time()}] Frame generation completed in {elapsed_time:.2f} seconds.")

