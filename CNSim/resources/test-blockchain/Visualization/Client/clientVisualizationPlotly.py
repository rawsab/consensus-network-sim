import pandas as pd
import networkx as nx
import plotly.graph_objects as go
from plotly.subplots import make_subplots

with open('../../../../../log/LatestFileName.txt', 'r') as file:
    latest_run = file.read().strip() 
# print(latest_run)
file_path = f'../../../../../log/{latest_run}/ClientLog - {latest_run}.csv'

print(f"The file path is: {file_path}")

data = pd.read_csv(file_path, sep='\t', header=None, names=['SimTime', 'ClientID', 'Event', 'Details'], skiprows=1)
data.sort_values(by='SimTime', inplace=True)

clients = {}
all_client_ids = data['ClientID'].unique()  

for client_id in all_client_ids:
    clients[client_id] = {'events': []}

sim_times = sorted(data['SimTime'].unique()) 

def process_event(event_group):
    current_events = [] 
    for _, row in event_group.iterrows():
        client_id = row['ClientID']
        event = row['Event']
        details = row['Details']

        clients[client_id]['events'].append((event, details))
        current_events.append([client_id, event, details])
    
    return current_events

fig = make_subplots(rows=1, cols=2, column_widths=[0.65, 0.35],
                    specs=[[{'type': 'scatter'}, {'type': 'table'}]])

edge_trace = go.Scatter(x=[], y=[], mode='lines', line=dict(width=1, color='#888'), hoverinfo='none')
node_trace = go.Scatter(x=[], y=[], mode='markers+text', textposition="middle center", hoverinfo='text',
                        marker=dict(size=140, showscale=False),
                        textfont=dict(family="Roboto, sans-serif", size=14))

fig.add_trace(edge_trace, row=1, col=1)
fig.add_trace(node_trace, row=1, col=1)

table_trace = go.Table(
    header=dict(values=['Client ID', 'Event Type', 'Content'],
                font=dict(size=14),
                align='left'),
    cells=dict(values=[[], [], []],
               align='left')
)
fig.add_trace(table_trace, row=1, col=2)

def generate_frame(sim_time):
    event_group = data[data['SimTime'] == sim_time]
    current_events = process_event(event_group)
    
    G = nx.DiGraph()

    for client_id, details in clients.items():
        G.add_node(client_id, color='lightblue', events=details['events'])

    pos = nx.spring_layout(G, seed=42)

    edge_x = []
    edge_y = []

    for edge in G.edges():
        x0, y0 = pos[edge[0]]
        x1, y1 = pos[edge[1]]
        edge_x += [x0, x1, None]
        edge_y += [y0, y1, None]

    node_x = []
    node_y = []
    node_text = []
    node_colors = []

    for node in G.nodes():
        x, y = pos[node]
        node_x.append(x)
        node_y.append(y)
        node_info = f"ID: {node}<br>Events: {len(G.nodes[node]['events'])}"
        node_text.append(node_info)
        node_colors.append(G.nodes[node]['color'])

    client_ids = [event[0] for event in current_events]
    event_types = [event[1] for event in current_events]
    details = [event[2] for event in current_events]

    return go.Frame(
        data=[
            go.Scatter(x=edge_x, y=edge_y, mode='lines', line=dict(width=1, color='#888')),
            go.Scatter(x=node_x, y=node_y, text=node_text, mode='markers+text',
                       marker=dict(color=node_colors, size=120),
                       textfont=dict(color='black'),
                       textposition="middle center"),
            go.Table(
                header=dict(values=['Client ID', 'Event Type', 'Content'],
                            font=dict(size=14),
                            align='left'),
                cells=dict(values=[client_ids, event_types, details],
                           align='left')
            )
        ],
        name=f"SimTime: {sim_time}",
        layout=go.Layout(
            title=f"Client Events at SimTime: {sim_time}",
            title_font=dict(family="Roboto, sans-serif", size=24, color='white'),
            font=dict(family="Roboto, sans-serif")
        )
    )

frames = [generate_frame(sim_time) for sim_time in sim_times]
fig.frames = frames

fig.update_layout(
    title="Client Events Visualization",
    title_font=dict(family="Roboto, sans-serif", size=24, color='white'),
    paper_bgcolor='#161925',
    plot_bgcolor='#161925',

    xaxis=dict(range=[-1.5, 1.5], showticklabels=False, zeroline=False, showgrid=False),
    yaxis=dict(range=[-1.5, 1.5], showticklabels=False, zeroline=False, showgrid=False),

    updatemenus=[dict(
        type="buttons",
        buttons=[
            dict(label="Play Visualization",
                 method="animate",
                 args=[None, dict(frame=dict(duration=500, redraw=True), fromcurrent=True)]),
            dict(label="Pause Visualization",
                 method="animate",
                 args=[[None], dict(frame=dict(duration=0, redraw=False), mode="immediate")])
        ],
        showactive=True,
        bgcolor='white',
        borderwidth=0,
    )],
    sliders=[{
        'steps': [
            {
                'args': [
                    [f"SimTime: {sim_time}"],
                    {'frame': {'duration': 0, 'redraw': True}, 'mode': 'immediate'}
                ],
                'label': str(sim_time),
                'method': 'animate'
            } for sim_time in sim_times
        ],
        'transition': {'duration': 0},
        'x': 0.1,
        'y': -0.1,
        'currentvalue': {
            'font': {'size': 14},
            'prefix': 'SimTime: ',
            'visible': True,
            'xanchor': 'right'
        },
        'len': 0.9
    }]
)

fig.show()
