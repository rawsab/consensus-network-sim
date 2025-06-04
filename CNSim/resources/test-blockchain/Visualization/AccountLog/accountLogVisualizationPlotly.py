import pandas as pd
import networkx as nx
import plotly.graph_objects as go

file_path = 'output_data.tsv'
data = pd.read_csv(file_path, sep='\t')

accounts = {}
sim_times = sorted(data['SimTime'].unique())

def update_accounts(event_group):
    for index, row in event_group.iterrows():
        account_id = row[' AccountID']
        event = row[' Event']
        balance = row[' Balance']
        state = row[' State']
        nonce = row[' Nonce']

        if account_id not in accounts:
            accounts[account_id] = {
                'balance': 0, 
                'state': 'UNKNOWN', 
                'nonce': 0}

        accounts[account_id]['balance'] = balance
        accounts[account_id]['state'] = state
        accounts[account_id]['nonce'] = nonce

fig = go.Figure()

edge_trace = go.Scatter(
    x=[], 
    y=[], 
    line=dict(
        width=1, 
        color='#888'
    ), 
    mode='lines', 
    hoverinfo='none'
)

node_trace = go.Scatter(
    x=[], 
    y=[],
    mode='markers+text', 
    textposition="middle center", 
    hoverinfo='text',
    marker=dict(
        # color='#d9d9d9',
        size=140, 
        showscale=False
    ),
    textfont=dict(
        family="Roboto, sans-serif",
        size=14
    )
)

fig.add_trace(edge_trace)
fig.add_trace(node_trace)

def generate_frame(sim_time):
    event_group = data[data['SimTime'] == sim_time]
    update_accounts(event_group)
    
    G = nx.DiGraph()
    
    for account_id, details in accounts.items():
        G.add_node(account_id, balance=details['balance'], state=details['state'], nonce=details['nonce'])
    
    pos = nx.spring_layout(G, seed=42)  # test other seeds (def value = 42)

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
    text_colors = []
      
    for node in G.nodes():
        x, y = pos[node]
        node_x.append(x)
        node_y.append(y)
        node_info = f"ID: {node}<br>Balance: {G.nodes[node]['balance']}<br>State: {G.nodes[node]['state']}<br>Nonce: {G.nodes[node]['nonce']}"
        node_text.append(node_info)

        if node == event_group.iloc[-1][' AccountID']: 
            node_colors.append('#5e60ce') # highlighted node
            text_colors.append('white')
        else:
            node_colors.append('#d9d9d9') # default node
            text_colors.append('black')

    return go.Frame(
        data=[
            go.Scatter(x=edge_x, y=edge_y, mode='lines', line=dict(width=1, color='#888')), 
            go.Scatter(x=node_x, y=node_y, text=node_text, mode='markers+text',
                       marker=dict(color=node_colors),
                       textfont=dict(color=text_colors),
                       textposition="middle center")
        ],
        name=f"SimTime: {sim_time}",
        layout=go.Layout(
            title=f"Accounts State at SimTime: {sim_time}",
            title_font=dict(
                family="Roboto, sans-serif",
                size=24,
                color='white'
            ),
            font=dict(
                family="Roboto, sans-serif",
            ),
        )
    )

frames = [generate_frame(sim_time) for sim_time in sim_times]
fig.frames = frames

fig.update_layout(
    title="CNSim Account Log Visualization",
    title_font=dict(
        family="Roboto, sans-serif",
        size=24,
        color='white'
    ),
    paper_bgcolor='#161925',
    plot_bgcolor='#161925',
    
    xaxis=dict(range=[-1.5, 1.5], showticklabels=False, zeroline=False, showgrid=False),
    yaxis=dict(range=[-1.5, 1.5], showticklabels=False, zeroline=False, showgrid=False),
    
    # buttons
    updatemenus=[dict(
        type="buttons",
        buttons=[dict(label="Play Visualization",
                      method="animate",
                      args=[None, dict(frame=dict(duration=1500, redraw=True), fromcurrent=True)]
                      ),
                 dict(label="Pause Visualization",
                      method="animate",
                      args=[[None], dict(frame=dict(duration=0, redraw=False), mode="immediate")],
                      )
                 ],
        showactive=True,
        bgcolor='white',
        borderwidth=0,
    )],
)

fig.show()
