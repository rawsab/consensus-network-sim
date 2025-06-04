import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
from matplotlib.widgets import Button

# Step 1: Read the data from the specified file and sort by SimTime
# Read the string from LatestFileName.txt
with open('../../../../../log/LatestFileName.txt', 'r') as file:
    latest_run = file.read().strip()  # Read the file and remove any surrounding whitespace or newlines
print(latest_run)
# Construct the file path using the latest_run variable
file_path = f'../../../../../log/{latest_run}/ClientLog - {latest_run}.csv'

# Now you can use the file_path variable for further processing
print(f"The file path is: {file_path}")

data = pd.read_csv(file_path, sep='\t', header=None, names=['SimTime', 'ClientID', 'Event', 'Details'], skiprows=1)

# Sort the data by SimTime in ascending order
data.sort_values(by='SimTime', inplace=True)

# Step 2: Initialize all client states at the beginning
clients = {}
all_client_ids = data['ClientID'].unique()  # Get all unique client IDs

for client_id in all_client_ids:
    clients[client_id] = {'events': []}  # Initialize each client with an empty events list

# Initialize simulation variables
sim_times = sorted(data['SimTime'].unique())  # Unique SimTime values
current_sim_time = sim_times[0]
anim_running = True  # Animation state

# Step 3: Function to process and update client states
def process_event(event_group):
    changed_clients = []
    current_events = []  # Store events for the current SimTime
    for index, row in event_group.iterrows():
        client_id = row['ClientID']
        event = row['Event']
        details = row['Details']

        # Update client events based on the current event
        clients[client_id]['events'].append((event, details))
        changed_clients.append(client_id)

        # Add current event details to the list for the table
        current_events.append([client_id, event, details])
    
    return changed_clients, current_events

# Step 4: Function to visualize clients using NetworkX and update table
def visualize_clients(changed_clients, current_events):
    ax.clear()  # Clear the previous plot
    ax_table.clear()  # Clear the previous table

    G = nx.DiGraph()

    # Add nodes with attributes
    for client_id, details in clients.items():
        color = 'red' if client_id in changed_clients else 'lightblue'
        G.add_node(client_id, 
                   events=details['events'],
                   color=color)

    # Draw the graph
    pos = nx.spring_layout(G, seed=42)  # fixed seed for consistent layout
    node_colors = [G.nodes[node]['color'] for node in G.nodes()]
    node_labels = {node: f"ID: {node}\nEvents: {len(data['events'])}" for node, data in G.nodes(data=True)}
    
    nx.draw(G, pos, ax=ax, with_labels=True, labels=node_labels, node_size=1500, 
            node_color=node_colors, font_size=8, font_color='black', 
            font_weight='bold', edge_color='grey')
    ax.set_title(f"Client Events at SimTime: {current_sim_time}")

    # Add time bar
    fig.text(0.5, 0.02, f"Current SimTime: {current_sim_time}", ha="center", fontsize=12)

    # Update the table with current events
    table_data = [["Client ID", "Event Type", "Content"]] + current_events
    ax_table.axis('tight')
    ax_table.axis('off')
    table = ax_table.table(cellText=table_data, loc='center', cellLoc='center', colColours=["palegreen"]*3)
    table.auto_set_column_width(col=list(range(len(table_data[0]))))

# Step 5: Update function for FuncAnimation
def update(frame):
    global current_sim_time
    current_sim_time = sim_times[frame]
    
    # Filter data for the current sim time
    event_group = data[data['SimTime'] == current_sim_time]
    changed_clients, current_events = process_event(event_group)
    visualize_clients(changed_clients, current_events)

# Step 6: Setup plot for animation
fig, (ax, ax_table) = plt.subplots(1, 2, figsize=(14, 6), gridspec_kw={'width_ratios': [2, 1]})
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

