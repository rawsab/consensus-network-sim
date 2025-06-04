import os
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.widgets import Button
from matplotlib.colors import to_rgba
import matplotlib.patches as patches
import numpy as np

# Navigate to the directory containing LatestFileName.txt
current_directory = os.path.dirname(os.path.abspath(__file__))
log_directory = os.path.abspath(os.path.join(current_directory, '../../../log'))

# Read the latest file name
with open(os.path.join(log_directory, 'LatestFileName.txt'), 'r') as file:
    latest_directory_name = file.read().strip()

# Construct the path to the latest directory
latest_directory_path = os.path.join(log_directory, latest_directory_name)

# Find the CSV file starting with 'StateLog'
csv_file_name = None
for file_name in os.listdir(latest_directory_path):
    if file_name.startswith('StateLog') and file_name.endswith('.csv'):
        csv_file_name = file_name
        break

if csv_file_name is None:
    raise FileNotFoundError("No file starting with 'StateLog' found in the latest directory")

csv_file_path = os.path.join(latest_directory_path, csv_file_name)

# Load the CSV data
data = pd.read_csv(csv_file_path)

# Find unique nodes
nodes = data['NodeID'].unique()
num_nodes = len(nodes)

# Define state colors
state_colors = {
    'WAITING': 'grey',
    'INITIATED': 'blue',
    'WAITING_VALIDATE_SUBMISSIONS': 'purple',
    'FAULT_SUSPICION_INITIATED': 'orange',
    'VALIDATE_SUBMISSIONS': 'green',
    'WAITING_VALIDATE_HASH': 'pink',
    'FAULT_SUSPICION_VALIDATE_SUBMISSIONS': 'red',
    'VALIDATE_HASH': 'yellow',
    'COMPLETED': 'black',
    'FAULT_SUSPICION_VALIDATE_HASH': 'brown',
    'NODE_FAULT': 'darkred',
    'NETWORK_FAULT': 'darkblue'
}

# Create a figure for plotting
fig, ax = plt.subplots(figsize=(15, 8))
plt.subplots_adjust(right=0.75, bottom=0.25, top=0.85)  # Adjusted to make space for progress bar

# Calculate grid size for square-like node arrangement
grid_size = int(np.ceil(np.sqrt(num_nodes)))

# Create grid positions for nodes
node_positions = [(i % grid_size, i // grid_size) for i in range(num_nodes)]

# Initialize scatter plot with node positions
scatter = ax.scatter(
    [pos[0] for pos in node_positions],
    [pos[1] for pos in node_positions],
    color='grey', s=1000  # Increased node size
)

# Info box for displaying node details
info_box = plt.gcf().text(0.5, 0.02, '', ha='center', va='bottom', fontsize=12, bbox=dict(facecolor='white', alpha=0.5))

# Create variables to control animation
is_paused = False
current_frame = 0

# Initialize colors array and state array
colors = ['grey'] * num_nodes
states = ['WAITING'] * num_nodes

# Create a progress bar axis
progress_ax = plt.axes([0.1, 0.92, 0.8, 0.05], facecolor='none')
progress_ax.set_xlim(0, 1)
progress_ax.set_ylim(0, 1)
progress_ax.axis('off')

# Add a rectangle patch for the progress bar
progress_patch = patches.Rectangle((0, 0), 0, 1, transform=progress_ax.transAxes, color='blue')
progress_ax.add_patch(progress_patch)

# Add a border around the progress bar
border_patch = patches.Rectangle((0, 0), 1, 1, transform=progress_ax.transAxes, fill=False, edgecolor='black', linewidth=2)
progress_ax.add_patch(border_patch)

# Calculate total simulation time
start_sim_time = data['SimTime'].iloc[0]
end_sim_time = data['SimTime'].iloc[-1]
total_sim_time = end_sim_time - start_sim_time

# Add start and end labels
start_label = plt.text(0.1, 0.98, f'Start: {start_sim_time}', ha='left', va='center', transform=fig.transFigure, fontsize=10, fontweight='bold')
end_label = plt.text(0.9, 0.98, f'End: {end_sim_time}', ha='right', va='center', transform=fig.transFigure, fontsize=10, fontweight='bold')

# Update function for animation
def update(frame):
    global colors, states

    row = data.iloc[frame]
    sim_time = row['SimTime']
    node_id = row['NodeID']
    new_state = row['NewState']

    ax.set_title(f'Simulation Time: {sim_time}', fontsize=14, fontweight='bold')

    # Update the state for the specific node
    node_index = list(nodes).index(node_id)
    colors[node_index] = state_colors[new_state]
    states[node_index] = new_state

    scatter.set_facecolors([to_rgba(color) for color in colors])

    # Update progress bar
    progress = (sim_time - start_sim_time) / total_sim_time
    progress_patch.set_width(progress)

# Step function to manually go to the next frame
def step_forward(event):
    global current_frame
    if current_frame < len(data) - 1:
        current_frame += 1
        update(current_frame)

# Play/Pause function
def play_pause(event):
    global is_paused
    is_paused = not is_paused

# Initialize the plot with the first frame
update(current_frame)

# Add buttons for control
ax_step = plt.axes([0.8, 0.05, 0.1, 0.075])
ax_play_pause = plt.axes([0.6, 0.05, 0.1, 0.075])

btn_step = Button(ax_step, 'Step')
btn_step.on_clicked(step_forward)

btn_play_pause = Button(ax_play_pause, 'Play/Pause')
btn_play_pause.on_clicked(play_pause)

# Function to update the animation
def animate_func(i):
    global current_frame
    if not is_paused and current_frame < len(data) - 1:
        current_frame += 1
        update(current_frame)

# Click event function
def on_click(event):
    if event.inaxes == ax:
        cont, ind = scatter.contains(event)
        if cont:
            node_index = ind["ind"][0]
            node_id = nodes[node_index]
            current_state = states[node_index]
            info_box.set_text(f'Node: {node_id}\nState: {current_state}')

# Connect the click event
fig.canvas.mpl_connect('button_press_event', on_click)

# Add a legend explaining the colors
legend_elements = [
    plt.Line2D([0], [0], marker='o', color='w', markerfacecolor=color, markersize=10, label=state)
    for state, color in state_colors.items()
]
ax.legend(handles=legend_elements, loc='center left', bbox_to_anchor=(1, 0.5), title="Node States", fontsize=10)

ani = animation.FuncAnimation(fig, animate_func, frames=len(data), repeat=False)

plt.xlabel('Nodes', fontsize=12)
plt.ylabel('State', fontsize=12)
plt.xticks(range(num_nodes), nodes, rotation=45)
plt.show()

