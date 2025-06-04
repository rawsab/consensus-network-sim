import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
from matplotlib.widgets import Button

# Step 1: Read the data
file_path = 'output_data.tsv'
data = pd.read_csv(file_path, sep='\t')

# Step 2: Initialize account states
accounts = {}
sim_times = sorted(data['SimTime'].unique())  # Unique SimTime values
current_sim_time = sim_times[0]
anim_running = True  # Animation state

# Step 3: Function to update accounts based on events
def update_accounts(event_group):
    changed_accounts = []
    for index, row in event_group.iterrows():
        account_id = row[' AccountID']
        event = row[' Event']
        balance = row[' Balance']
        state = row[' State']
        nonce = row[' Nonce']

        # Initialize account if not already present
        if account_id not in accounts:
            accounts[account_id] = {'balance': 0, 'state': 'UNKNOWN', 'nonce': 0}

        # Check if the account is changing in this frame
        if (accounts[account_id]['balance'] != balance or 
            accounts[account_id]['state'] != state or 
            accounts[account_id]['nonce'] != nonce):
            changed_accounts.append(account_id)

        # Update the account based on the event
        accounts[account_id]['balance'] = balance
        accounts[account_id]['state'] = state
        accounts[account_id]['nonce'] = nonce
    
    return changed_accounts

# Step 4: Function to visualize accounts using NetworkX
def visualize_accounts(changed_accounts):
    ax.clear()  # Clear the previous plot
    G = nx.DiGraph()

    # Add nodes with attributes
    for account_id, details in accounts.items():
        color = 'red' if account_id in changed_accounts else 'lightblue'
        G.add_node(account_id, 
                   balance=details['balance'], 
                   state=details['state'], 
                   nonce=details['nonce'],
                   color=color)

    # Draw the graph
    pos = nx.spring_layout(G, seed=42)  # fixed seed for consistent layout
    node_colors = [G.nodes[node]['color'] for node in G.nodes()]
    node_labels = {node: f"ID: {node}\nBal: {data['balance']}\nState: {data['state']}\nNonce: {data['nonce']}" 
                   for node, data in G.nodes(data=True)}
    
    nx.draw(G, pos, ax=ax, with_labels=True, labels=node_labels, node_size=1500, 
            node_color=node_colors, font_size=8, font_color='black', 
            font_weight='bold', edge_color='grey')
    ax.set_title(f"Accounts State at SimTime: {current_sim_time}")

    # Add time bar
    # fig.text(0.5, 0.02, f"Current SimTime: {current_sim_time}", ha="center", fontsize=12)

# Step 5: Update function for FuncAnimation
def update(frame):
    global current_sim_time
    current_sim_time = sim_times[frame]
    
    # Filter data for the current sim time
    event_group = data[data['SimTime'] == current_sim_time]
    changed_accounts = update_accounts(event_group)
    visualize_accounts(changed_accounts)

# Step 6: Setup plot for animation
fig, ax = plt.subplots(figsize=(10, 6))
plt.subplots_adjust(bottom=0.2)  # Adjust subplot to make space for buttons

# Step 7: Create the animation
ani = FuncAnimation(fig, update, frames=len(sim_times), repeat=False, interval=1000)

# Step 8: Add Play/Pause Button
ax_play = plt.axes([0.45, 0.01, 0.1, 0.05])  # Position of the button below the plot
button = Button(ax_play, 'Pause')

def on_button_click(event):
    global anim_running
    if anim_running:
        ani.event_source.stop()
        button.label.set_text('Play')
    else:
        ani.event_source.start()
        button.label.set_text('Pause')
    anim_running = not anim_running

button.on_clicked(on_button_click)

# Step 9: Show plot with interactive controls
plt.show()

