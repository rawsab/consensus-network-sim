import csv
from collections import defaultdict, Counter

def read_input_file(input_filename):
    """
    Reads the input TSV file and returns a list of records.
    """
    records = []
    with open(input_filename, 'r') as file:
        reader = csv.reader(file, delimiter='\t')
        for row in reader:
            records.append(row)
    return records

def write_output_file(output_filename, output_data):
    """
    Writes the output data to a TSV file.
    """
    with open(output_filename, 'w', newline='') as file:
        writer = csv.writer(file, delimiter='\t')
        for row in output_data:
            writer.writerow(row)

def find_consensus(records):
    """
    Processes the records to find and output consensus states.
    """
    nodes_state = {}
    consensus_history = []
    consensus_achieved = defaultdict(set)  # Tracks which states have reached consensus per account ID
    
    for record in records:
        # Read the data from each record
        sim_time, account_id, node, event, balance, state, nonce = record
        
        # Current node state
        current_state = (event, balance, state, nonce)
        
        # Initialize node if not already present
        if node not in nodes_state:
            nodes_state[node] = {}
        
        # Update the node's account state
        nodes_state[node][account_id] = current_state
        
        # Check for consensus if more than 2/3 of nodes have the same state
        account_state_counts = defaultdict(Counter)
        for node_states in nodes_state.values():
            for account, state in node_states.items():
                account_state_counts[account][state] += 1

        # Determine the total number of nodes
        total_nodes = len(nodes_state)
        required_consensus_count = (2 / 3) * total_nodes
        
        for account, state_count in account_state_counts.items():
            for state, count in state_count.items():
                if count > required_consensus_count:
                    # More than 2/3 nodes agree on this state for this account
                    if state not in consensus_achieved[account]:
                        # This is a new consensus that has not been recorded before
                        consensus_row = [sim_time, account] + list(state)
                        consensus_history.append(consensus_row)
                        consensus_achieved[account].add(state)  # Mark this state as having reached consensus
    
    return consensus_history

def main():
    # Input and Output file names
    with open('../../../../../log/LatestFileName.txt', 'r') as file:
        latest_run = file.read().strip()  # Read the file and remove any surrounding whitespace or newlines
    print(latest_run)
    # Construct the file path using the latest_run variable
    file_path = f'../../../../../log/{latest_run}/AccountLog - {latest_run}.csv'

    # Now you can use the file_path variable for further processing
    print(f"The file path is: {file_path}")
    input_filename = f'../../../../../log/{latest_run}/AccountLog - {latest_run}.csv'
    output_filename = 'output_data.tsv'
        
    # Read input records from file
    records = read_input_file(input_filename)
    
    # Find consensus
    consensus_data = find_consensus(records)
    
    # Write consensus data to output file
    write_output_file(output_filename, consensus_data)

if __name__ == "__main__":
    main()

