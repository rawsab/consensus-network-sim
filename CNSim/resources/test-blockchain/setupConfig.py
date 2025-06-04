import tkinter as tk
from tkinter import ttk, messagebox
import os
import csv

class NetworkSimulatorConfigApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Network Simulator Configuration")
        self.root.geometry("800x600")  # Set the initial size of the main window
        self.current_page = 0

        # Set style
        self.style = ttk.Style()
        self.style.theme_use('clam')  # You can try 'default', 'classic', 'alt', 'clam', 'vista', 'xpnative'
        self.style.configure('TFrame', background='#ececec')
        self.style.configure('TLabel', background='#ececec', font=('Arial', 12))
        self.style.configure('TButton', background='#0066cc', foreground='#ffffff', font=('Arial', 12))
        self.style.configure('TEntry', font=('Arial', 12))
        self.style.configure('TCombobox', font=('Arial', 12))

        self.num_nodes = tk.IntVar()
        self.network_throughput = tk.DoubleVar()
        self.default_epochs = tk.IntVar(value=1)  # Default number of epochs
        self.default_behavior = tk.StringVar(value="Honest")
        self.default_target_node_id = tk.IntVar(value=0)
        self.default_connectivity_ratio = tk.DoubleVar(value=1.0)

        self.node_data = []
        self.epoch_data = {}

        self.setup_main_frame()
        self.show_page_1()

    def setup_main_frame(self):
        self.main_frame = ttk.Frame(self.root)
        self.main_frame.pack(fill=tk.BOTH, expand=True, padx=20, pady=20)

    def show_page_1(self):
        self.clear_frame()

        ttk.Label(self.main_frame, text="Number of Nodes:").grid(row=0, column=0, pady=5)
        ttk.Entry(self.main_frame, textvariable=self.num_nodes).grid(row=0, column=1, pady=5)

        ttk.Label(self.main_frame, text="Network Throughput:").grid(row=1, column=0, pady=5)
        ttk.Entry(self.main_frame, textvariable=self.network_throughput).grid(row=1, column=1, pady=5)

        ttk.Button(self.main_frame, text="Next", command=self.show_page_2).grid(row=2, column=1, pady=10)

    def show_page_2(self):
        if self.num_nodes.get() <= 0 or self.network_throughput.get() <= 0:
            messagebox.showerror("Input Error", "Please enter valid values for nodes and throughput.")
            return

        self.update_config_file()

        self.clear_frame()

        self.node_data = []

        # Create a canvas and a vertical scrollbar
        canvas = tk.Canvas(self.main_frame)
        scrollbar = ttk.Scrollbar(self.main_frame, orient="vertical", command=canvas.yview)
        scrollable_frame = ttk.Frame(canvas)

        scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(
                scrollregion=canvas.bbox("all")
            )
        )

        canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)

        # Default epochs setting
        ttk.Label(scrollable_frame, text="Default Number of Epochs:").grid(row=0, column=0, pady=5)
        ttk.Entry(scrollable_frame, textvariable=self.default_epochs).grid(row=0, column=1, pady=5)

        ttk.Label(scrollable_frame, text="Default Behavior:").grid(row=1, column=0, pady=5)
        behavior_combobox = ttk.Combobox(scrollable_frame, textvariable=self.default_behavior, values=["Honest", "Malicious", "Fluctuating"])
        behavior_combobox.grid(row=1, column=1, pady=5)

        ttk.Label(scrollable_frame, text="Default Target Node ID:").grid(row=2, column=0, pady=5)
        ttk.Entry(scrollable_frame, textvariable=self.default_target_node_id).grid(row=2, column=1, pady=5)

        ttk.Label(scrollable_frame, text="Default Connectivity Ratio:").grid(row=3, column=0, pady=5)
        ttk.Entry(scrollable_frame, textvariable=self.default_connectivity_ratio).grid(row=3, column=1, pady=5)

        ttk.Button(scrollable_frame, text="Apply to All", command=self.apply_default_epochs).grid(row=4, column=0, pady=5)

        for i in range(self.num_nodes.get()):
            node_frame = ttk.LabelFrame(scrollable_frame, text=f"Node {i+1}")
            node_frame.grid(row=i+5, column=0, padx=10, pady=5, sticky='ew')

            ttk.Label(node_frame, text="Number of Epochs:").grid(row=0, column=0, pady=5)
            num_epochs = tk.IntVar(value=self.default_epochs.get())  # Set default value
            num_epochs.trace_add('write', lambda *args, i=i: self.update_epoch_data(i))
            ttk.Entry(node_frame, textvariable=num_epochs).grid(row=0, column=1, pady=5)
            self.node_data.append(num_epochs)

            ttk.Button(node_frame, text="Configure Epochs", command=lambda i=i: self.configure_epochs(i)).grid(row=0, column=2, pady=5)

            # Initialize the epoch_data for the node
            self.epoch_data[i] = []

        ttk.Button(scrollable_frame, text="Save", command=self.save_config).grid(row=self.num_nodes.get() + 6, column=0, pady=10)

        canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

    def apply_default_epochs(self):
        default_value = self.default_epochs.get()
        for num_epochs in self.node_data:
            num_epochs.set(default_value)
        self.apply_defaults_to_epoch_data()

    def apply_defaults_to_epoch_data(self):
        for node_index in range(self.num_nodes.get()):
            num_epochs = self.node_data[node_index].get()
            self.epoch_data[node_index] = []
            for epoch in range(num_epochs):
                behavior_var = tk.StringVar(value=self.default_behavior.get())
                target_node_var = tk.IntVar(value=self.default_target_node_id.get())
                connectivity_ratio_var = tk.DoubleVar(value=self.default_connectivity_ratio.get())
                
                self.epoch_data[node_index].append({
                    "behavior": behavior_var,
                    "target_node_id": target_node_var,
                    "connectivity_ratio": connectivity_ratio_var
                })

    def update_epoch_data(self, node_index):
        num_epochs = self.node_data[node_index].get()
        current_epochs = len(self.epoch_data[node_index])
        
        if num_epochs > current_epochs:
            for epoch in range(current_epochs, num_epochs):
                behavior_var = tk.StringVar(value=self.default_behavior.get())
                target_node_var = tk.IntVar(value=self.default_target_node_id.get())
                connectivity_ratio_var = tk.DoubleVar(value=self.default_connectivity_ratio.get())
                
                self.epoch_data[node_index].append({
                    "behavior": behavior_var,
                    "target_node_id": target_node_var,
                    "connectivity_ratio": connectivity_ratio_var
                })
        elif num_epochs < current_epochs:
            self.epoch_data[node_index] = self.epoch_data[node_index][:num_epochs]

    def configure_epochs(self, node_index):
        num_epochs = self.node_data[node_index].get()
        if num_epochs <= 0:
            messagebox.showerror("Input Error", "Please enter a valid number of epochs.")
            return

        epoch_window = tk.Toplevel(self.root)
        epoch_window.title(f"Configure Epochs for Node {node_index+1}")
        epoch_window.geometry("600x400")  # Set the initial size of the epoch configuration window

        # Create a canvas and a vertical scrollbar for the epochs
        canvas = tk.Canvas(epoch_window)
        scrollbar = ttk.Scrollbar(epoch_window, orient="vertical", command=canvas.yview)
        scrollable_frame = ttk.Frame(canvas)

        scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(
                scrollregion=canvas.bbox("all")
            )
        )

        canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)

        for epoch in range(num_epochs):
            epoch_frame = ttk.LabelFrame(scrollable_frame, text=f"Epoch {epoch+1}")
            epoch_frame.grid(row=epoch, column=0, padx=10, pady=5)

            behavior_var = self.epoch_data[node_index][epoch]["behavior"]
            if isinstance(behavior_var, str):
                behavior_var = tk.StringVar(value=behavior_var)
                self.epoch_data[node_index][epoch]["behavior"] = behavior_var
            
            target_node_var = self.epoch_data[node_index][epoch]["target_node_id"]
            if isinstance(target_node_var, int):
                target_node_var = tk.IntVar(value=target_node_var)
                self.epoch_data[node_index][epoch]["target_node_id"] = target_node_var
            
            connectivity_ratio_var = self.epoch_data[node_index][epoch]["connectivity_ratio"]
            if isinstance(connectivity_ratio_var, float):
                connectivity_ratio_var = tk.DoubleVar(value=connectivity_ratio_var)
                self.epoch_data[node_index][epoch]["connectivity_ratio"] = connectivity_ratio_var

            ttk.Label(epoch_frame, text="Behavior:").grid(row=0, column=0, pady=5)
            behavior_combobox = ttk.Combobox(epoch_frame, textvariable=behavior_var, values=["Honest", "Malicious", "Fluctuating"])
            behavior_combobox.grid(row=0, column=1, pady=5)
            behavior_combobox.bind("<<ComboboxSelected>>", lambda e, i=node_index, ep=epoch: self.update_fields(i, ep))

            ttk.Label(epoch_frame, text="Target Node ID:").grid(row=1, column=0, pady=5)
            target_node_entry = ttk.Entry(epoch_frame, textvariable=target_node_var)
            target_node_entry.grid(row=1, column=1, pady=5)

            ttk.Label(epoch_frame, text="Connectivity Ratio:").grid(row=2, column=0, pady=5)
            connectivity_ratio_entry = ttk.Entry(epoch_frame, textvariable=connectivity_ratio_var)
            connectivity_ratio_entry.grid(row=2, column=1, pady=5)

            self.epoch_data[node_index][epoch]["target_node_entry"] = target_node_entry
            self.epoch_data[node_index][epoch]["connectivity_ratio_entry"] = connectivity_ratio_entry

            self.update_fields(node_index, epoch)

        ttk.Button(scrollable_frame, text="Done", command=lambda: self.save_epoch_config(node_index, epoch_window)).grid(row=num_epochs, column=0, pady=10)

        canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

    def update_fields(self, node_index, epoch_index):
        epoch_info = self.epoch_data[node_index][epoch_index]
        behavior = epoch_info["behavior"]
        if isinstance(behavior, tk.StringVar):
            behavior = behavior.get()

        if behavior == "Honest":
            epoch_info["target_node_entry"].config(state=tk.DISABLED)
            epoch_info["connectivity_ratio_entry"].config(state=tk.DISABLED)
        elif behavior == "Malicious":
            epoch_info["target_node_entry"].config(state=tk.NORMAL)
            epoch_info["connectivity_ratio_entry"].config(state=tk.DISABLED)
        elif behavior == "Fluctuating":
            epoch_info["target_node_entry"].config(state=tk.DISABLED)
            epoch_info["connectivity_ratio_entry"].config(state=tk.NORMAL)

    def save_epoch_config(self, node_index, epoch_window):
        # Collect and save data from epoch configuration window
        num_epochs = self.node_data[node_index].get()
        for epoch in range(num_epochs):
            epoch_info = self.epoch_data[node_index][epoch]
            epoch_info["behavior"] = epoch_info["behavior"].get()
            epoch_info["target_node_id"] = epoch_info["target_node_id"].get()
            epoch_info["connectivity_ratio"] = epoch_info["connectivity_ratio"].get()
        epoch_window.destroy()

    def update_config_file(self):
        config_path = os.path.join("..", "config.txt")
        try:
            with open(config_path, "r") as file:
                lines = file.readlines()

            max_nodes_value = self.num_nodes.get() + 1
            with open(config_path, "w") as file:
                for line in lines:
                    if line.startswith("net.numOfNodes"):
                        file.write(f"net.numOfNodes = {self.num_nodes.get()}\n")
                    elif line.startswith("net.throughputMean"):
                        file.write(f"net.throughputMean = {self.network_throughput.get()}f\n")
                    elif line.startswith("sim.maxNodes"):
                        file.write(f"sim.maxNodes = {max_nodes_value}\n")
                    else:
                        file.write(line)
        except FileNotFoundError:
            messagebox.showerror("File Error", "The config.txt file was not found.")
        except Exception as e:
            messagebox.showerror("Error", f"An error occurred: {e}")

    def save_config(self):
        for node_index in range(self.num_nodes.get()):
            for epoch_index in range(len(self.epoch_data[node_index])):
                epoch_info = self.epoch_data[node_index][epoch_index]
                if isinstance(epoch_info["behavior"], tk.StringVar):
                    epoch_info["behavior"] = epoch_info["behavior"].get()
                if isinstance(epoch_info["target_node_id"], tk.IntVar):
                    epoch_info["target_node_id"] = epoch_info["target_node_id"].get()
                if isinstance(epoch_info["connectivity_ratio"], tk.DoubleVar):
                    epoch_info["connectivity_ratio"] = epoch_info["connectivity_ratio"].get()

            node_epochs = self.epoch_data.get(node_index, [])
            with open(f"node_{node_index+1}_config.csv", "w", newline='') as csvfile:
                csvwriter = csv.writer(csvfile)
                csvwriter.writerow(["Epoch", "Behavior", "Target Node ID", "Connectivity Ratio"])
                for epoch_index, epoch in enumerate(node_epochs):
                    behavior = epoch["behavior"]
                    target_node_id = epoch["target_node_id"]
                    connectivity_ratio = epoch["connectivity_ratio"]
                    csvwriter.writerow([epoch_index + 1, behavior, target_node_id, connectivity_ratio])

        messagebox.showinfo("Success", "Configuration saved successfully!")

    def clear_frame(self):
        for widget in self.main_frame.winfo_children():
            widget.destroy()

if __name__ == "__main__":
    root = tk.Tk()
    app = NetworkSimulatorConfigApp(root)
    root.mainloop()

