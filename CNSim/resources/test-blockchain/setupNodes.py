import tkinter as tk
from tkinter import ttk
from traceback import clear_frames

import customtkinter as ctk
from tkinter import messagebox
import os
import csv


class NetworkSimulatorConfigApp:
    def __init__(self, root, return_callback):
        self.root = root
        self.root.title("Network Simulator Configuration")
        self.return_callback = return_callback  # Store the callback

        self.num_nodes = tk.StringVar(value="0")
        self.network_throughput = tk.StringVar(value="0.0")
        self.default_epochs = tk.StringVar(value="1")
        self.default_behavior = tk.StringVar(value="Honest")
        self.default_target_node_id = tk.StringVar(value="0")
        self.default_connectivity_ratio = tk.StringVar(value="1.0")

        self.node_data = []
        self.epoch_data = {}

        self.setup_main_frame()
        self.show_page_1()

    def get_int(self, stringvar, default=0):
        try:
            return int(stringvar.get())
        except ValueError:
            return default

    def get_float(self, stringvar, default=0.0):
        try:
            return float(stringvar.get())
        except ValueError:
            return default

    def setup_main_frame(self):
        self.main_frame = ctk.CTkFrame(self.root, corner_radius=15, fg_color=("white", "gray13"))
        self.main_frame.pack(fill="both", expand=True, padx=20, pady=20)

    def show_page_1(self):
        self.clear_frame()
        header_label = ctk.CTkLabel(self.main_frame, text="Network Configuration",
                                    font=ctk.CTkFont(size=24, weight="bold"))
        header_label.pack(pady=(30, 20))

        input_frame = ctk.CTkFrame(self.main_frame, corner_radius=10, fg_color=("gray95", "gray17"))
        input_frame.pack(fill="x", padx=50, pady=10)

        self.create_input_field(input_frame, "Number of Nodes:", self.num_nodes)
        self.create_input_field(input_frame, "Network Throughput:", self.network_throughput)

        next_button = ctk.CTkButton(input_frame, text="Next", command=self.show_page_2, height=35,
                                    font=ctk.CTkFont(size=14), corner_radius=8)
        next_button.pack(pady=20)

    def create_input_field(self, parent, label_text, variable, state="normal"):
        container = ctk.CTkFrame(parent, fg_color="transparent")
        container.pack(fill="x", padx=20, pady=10)
        label = ctk.CTkLabel(container, text=label_text, font=ctk.CTkFont(size=14))
        label.pack(side="left")
        entry = ctk.CTkEntry(container, height=35, font=ctk.CTkFont(size=14), corner_radius=8, textvariable=variable,
                             state=state)
        entry.pack(side="right", padx=(20, 0))
        return entry

    def bind_mouse_wheel(self, widget):
        def _on_mousewheel(event):
            widget._parent_canvas.yview_scroll(-int(event.delta / 120), "units")

        # Bind the widget and all its children
        def _bind_recursive(w):
            w.bind("<MouseWheel>", _on_mousewheel)
            w.bind("<Button-4>", lambda e: widget._parent_canvas.yview_scroll(-1, "units"))
            w.bind("<Button-5>", lambda e: widget._parent_canvas.yview_scroll(1, "units"))
            for child in w.winfo_children():
                _bind_recursive(child)

        _bind_recursive(widget)

    def show_page_2(self):
        if self.get_int(self.num_nodes) <= 0 or self.get_float(self.network_throughput) <= 0:
            messagebox.showerror("Input Error", "Please enter valid values for nodes and throughput.")
            return

        self.update_config_file()
        self.clear_frame()
        self.node_data = []

        scrollable_frame = self.create_scrollable_frame()

        # Defaults card after nodes
        defaults_frame = ctk.CTkFrame(scrollable_frame, fg_color=("gray90", "gray20"), corner_radius=10)
        defaults_frame.pack(fill="x", padx=10, pady=5)
        defaults_frame.pack_configure(after=scrollable_frame.winfo_children()[0])

        header_frame = ctk.CTkFrame(defaults_frame, fg_color=("gray85", "gray25"), corner_radius=10)
        header_frame.pack(fill="x", padx=5, pady=5)
        ctk.CTkLabel(header_frame, text="Default Settings", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=5)

        content_frame = ctk.CTkFrame(defaults_frame, fg_color="transparent")
        content_frame.pack(fill="x", padx=10, pady=5)

        self.create_input_field(content_frame, "Default Number of Epochs:", self.default_epochs)

        behavior_container = ctk.CTkFrame(content_frame, fg_color="transparent")
        behavior_container.pack(fill="x", padx=20, pady=10)
        ctk.CTkLabel(behavior_container, text="Default Behavior:", font=ctk.CTkFont(size=14)).pack(side="left")
        behavior_combo = ctk.CTkComboBox(behavior_container, values=["Honest", "Malicious", "Fluctuating"],
                                         variable=self.default_behavior, height=35, font=ctk.CTkFont(size=14), state="readonly")
        behavior_combo.pack(side="right", padx=(20, 0))

        self.create_input_field(content_frame, "Default Target Node ID:", self.default_target_node_id)
        self.create_input_field(content_frame, "Default Connectivity Ratio:", self.default_connectivity_ratio)

        button_frame = ctk.CTkFrame(defaults_frame, fg_color="transparent")
        button_frame.pack(fill="x", padx=10, pady=5)
        apply_button = ctk.CTkButton(button_frame, text="Apply to All", command=self.apply_default_epochs,
                                     height=35, font=ctk.CTkFont(size=14), corner_radius=8)
        apply_button.pack(side="right", padx=10, pady=5)

        for i in range(self.get_int(self.num_nodes)):
            node_frame = ctk.CTkFrame(scrollable_frame, fg_color=("gray90", "gray20"), corner_radius=10)
            node_frame.pack(fill="x", padx=10, pady=5)

            header_frame = ctk.CTkFrame(node_frame, fg_color=("gray85", "gray25"), corner_radius=10)
            header_frame.pack(fill="x", padx=5, pady=5)
            ctk.CTkLabel(header_frame, text=f"Node {i + 1}", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=5)

            content_frame = ctk.CTkFrame(node_frame, fg_color="transparent")
            content_frame.pack(fill="x", padx=10, pady=5)

            epochs_container = ctk.CTkFrame(content_frame, fg_color="transparent")
            epochs_container.pack(fill="x", padx=20, pady=5)

            ctk.CTkLabel(epochs_container, text="Number of Epochs:", font=ctk.CTkFont(size=14)).pack(side="left")
            num_epochs = tk.StringVar(value=self.default_epochs.get())
            num_epochs.trace_add('write', lambda *args, i=i: self.update_epoch_data(i))

            entry = ctk.CTkEntry(epochs_container, height=35, font=ctk.CTkFont(size=14), corner_radius=8,
                                 textvariable=num_epochs)
            entry.pack(side="left", padx=(20, 10))

            config_button = ctk.CTkButton(epochs_container, text="Configure Epochs",
                                          command=lambda i=i: self.configure_epochs(i),
                                          height=35, font=ctk.CTkFont(size=14), corner_radius=8)
            config_button.pack(side="right", padx=10)

            self.node_data.append(num_epochs)
            self.epoch_data[i] = []

        # Save button at the bottom
        save_button = ctk.CTkButton(scrollable_frame, text="Save", command=self.save_config, height=35,
                                    font=ctk.CTkFont(size=14), corner_radius=8)
        save_button.pack(pady=20)

        self.bind_mouse_wheel(scrollable_frame)
        self.apply_default_epochs() # apply defaults at beginning

    def create_scrollable_frame(self):
        scrollable_container = ctk.CTkScrollableFrame(self.main_frame, corner_radius=10)
        scrollable_container.pack(fill="both", expand=True, padx=10, pady=10)
        return scrollable_container

    def configure_epochs(self, node_index):
        try:
            num_epochs = int(self.node_data[node_index].get())
            if num_epochs <= 0:
                messagebox.showerror("Input Error", "Please enter a valid number of epochs.")
                return
        except ValueError:
            messagebox.showerror("Input Error", "Please enter a valid number for epochs.")
            return

        epoch_window = ctk.CTkToplevel(self.root)
        epoch_window.protocol("WM_DELETE_WINDOW", lambda: self.save_epoch_config(node_index, epoch_window))
        epoch_window.title(f"Configure Epochs for Node {node_index + 1}")
        epoch_window.geometry("600x400")

        scrollable_frame = ctk.CTkScrollableFrame(epoch_window, corner_radius=10)
        scrollable_frame.pack(fill="both", expand=True, padx=20, pady=20)

        for epoch in range(num_epochs):
            epoch_frame = ctk.CTkFrame(scrollable_frame, fg_color=("gray90", "gray20"), corner_radius=10)
            epoch_frame.pack(fill="x", padx=10, pady=5)

            header_frame = ctk.CTkFrame(epoch_frame, fg_color=("gray85", "gray25"), corner_radius=10)
            header_frame.pack(fill="x", padx=5, pady=5)
            ctk.CTkLabel(header_frame, text=f"Epoch {epoch + 1}", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=5)

            content_frame = ctk.CTkFrame(epoch_frame, fg_color="transparent")
            content_frame.pack(fill="x", padx=10, pady=5)

            behavior_var = self.epoch_data[node_index][epoch]["behavior"]
            if isinstance(behavior_var, str):
                behavior_var = tk.StringVar(value=behavior_var)
                self.epoch_data[node_index][epoch]["behavior"] = behavior_var

            target_node_var = self.epoch_data[node_index][epoch]["target_node_id"]
            if isinstance(target_node_var, (int, str)):
                target_node_var = tk.StringVar(value=str(target_node_var))
                self.epoch_data[node_index][epoch]["target_node_id"] = target_node_var

            connectivity_ratio_var = self.epoch_data[node_index][epoch]["connectivity_ratio"]
            if isinstance(connectivity_ratio_var, (float, str)):
                connectivity_ratio_var = tk.StringVar(value=str(connectivity_ratio_var))
                self.epoch_data[node_index][epoch]["connectivity_ratio"] = connectivity_ratio_var

            behavior_container = ctk.CTkFrame(content_frame, fg_color="transparent")
            behavior_container.pack(fill="x", padx=20, pady=5)
            ctk.CTkLabel(behavior_container, text="Behavior:", font=ctk.CTkFont(size=14)).pack(side="left")
            behavior_combo = ttk.Combobox(behavior_container, values=["Honest", "Malicious", "Fluctuating"],
                                             textvariable=behavior_var, height=35, font=ctk.CTkFont(size=14), state="readonly")
            behavior_combo.pack(side="right", padx=(20, 0))
            behavior_combo.bind("<<ComboboxSelected>>", lambda e, i=node_index, ep=epoch: self.update_fields(i, ep))

            target_container = ctk.CTkFrame(content_frame, fg_color="transparent")
            target_container.pack(fill="x", padx=20, pady=5)
            ctk.CTkLabel(target_container, text="Target Node ID:", font=ctk.CTkFont(size=14)).pack(side="left")
            target_entry = ctk.CTkEntry(target_container, height=35, font=ctk.CTkFont(size=14), corner_radius=8,
                                        textvariable=target_node_var)
            target_entry.pack(side="right", padx=(20, 0))

            connectivity_container = ctk.CTkFrame(content_frame, fg_color="transparent")
            connectivity_container.pack(fill="x", padx=20, pady=5)
            ctk.CTkLabel(connectivity_container, text="Connectivity Ratio:", font=ctk.CTkFont(size=14)).pack(
                side="left")
            connectivity_entry = ctk.CTkEntry(connectivity_container, height=35, font=ctk.CTkFont(size=14),
                                              corner_radius=8,
                                              textvariable=connectivity_ratio_var)
            connectivity_entry.pack(side="right", padx=(20, 0))

            self.epoch_data[node_index][epoch]["target_node_entry"] = target_entry
            self.epoch_data[node_index][epoch]["connectivity_ratio_entry"] = connectivity_entry

            self.update_fields(node_index, epoch)

        done_button = ctk.CTkButton(scrollable_frame, text="Done",
                                    command=lambda: self.save_epoch_config(node_index, epoch_window),
                                    height=35, font=ctk.CTkFont(size=14), corner_radius=8)
        done_button.pack(pady=20)
        self.bind_mouse_wheel(scrollable_frame)

    def apply_default_epochs(self):
        try:
            default_value = self.default_epochs.get()
            for num_epochs in self.node_data:
                num_epochs.set(default_value)
            self.apply_defaults_to_epoch_data()
        except ValueError:
            messagebox.showerror("Input Error", "Default epochs must be a valid number.")

    def apply_defaults_to_epoch_data(self):
        for node_index in range(self.get_int(self.num_nodes)):
            num_epochs = self.get_int(self.node_data[node_index])
            self.epoch_data[node_index] = []
            for epoch in range(num_epochs):
                behavior_var = tk.StringVar(value=self.default_behavior.get())
                target_node_var = tk.StringVar(value=self.default_target_node_id.get())
                connectivity_ratio_var = tk.StringVar(value=self.default_connectivity_ratio.get())

                self.epoch_data[node_index].append({
                    "behavior": behavior_var,
                    "target_node_id": target_node_var,
                    "connectivity_ratio": connectivity_ratio_var
                })

    def update_epoch_data(self, node_index):
        try:
            num_epochs = int(self.node_data[node_index].get())
            current_epochs = len(self.epoch_data[node_index])

            if num_epochs > current_epochs:
                for epoch in range(current_epochs, num_epochs):
                    behavior_var = tk.StringVar(value=self.default_behavior.get())
                    target_node_var = tk.StringVar(value=self.default_target_node_id.get())
                    connectivity_ratio_var = tk.StringVar(value=self.default_connectivity_ratio.get())

                    self.epoch_data[node_index].append({
                        "behavior": behavior_var,
                        "target_node_id": target_node_var,
                        "connectivity_ratio": connectivity_ratio_var
                    })
            elif num_epochs < current_epochs:
                self.epoch_data[node_index] = self.epoch_data[node_index][:num_epochs]
        except ValueError:
            pass

    def update_fields(self, node_index, epoch_index):
        epoch_info = self.epoch_data[node_index][epoch_index]
        behavior = epoch_info["behavior"].get()

        target_entry = epoch_info["target_node_entry"]
        connectivity_entry = epoch_info["connectivity_ratio_entry"]

        if behavior == "Honest":
            target_entry.configure(state="disabled", fg_color=("gray80", "gray30"))
            connectivity_entry.configure(state="disabled", fg_color=("gray80", "gray30"))
        elif behavior == "Malicious":
            target_entry.configure(state="normal", fg_color=("white", "gray17"))
            connectivity_entry.configure(state="disabled", fg_color=("gray80", "gray30"))
        elif behavior == "Fluctuating":
            target_entry.configure(state="disabled", fg_color=("gray80", "gray30"))
            connectivity_entry.configure(state="normal", fg_color=("white", "gray17"))

    def save_epoch_config(self, node_index, epoch_window):
        try:
            num_epochs = int(self.node_data[node_index].get())
            for epoch in range(num_epochs):
                epoch_info = self.epoch_data[node_index][epoch]
                # epoch_info["behavior"] = epoch_info["behavior"].get()
                # don't need to update a bound variable
                # epoch_info["target_node_id"] = epoch_info["target_node_id"].get()
                # epoch_info["connectivity_ratio"] = epoch_info["connectivity_ratio"].get()
                int(epoch_info["target_node_id"].get())
                float(epoch_info["connectivity_ratio"].get())
            epoch_window.destroy()
        except ValueError:
            messagebox.showerror("Input Error", "Please ensure all numeric fields contain valid numbers.")

    def update_config_file(self):
        config_path = os.path.join("..", "config.txt")
        try:
            with open(config_path, "r") as file:
                lines = file.readlines()

            max_nodes_value = self.get_int(self.num_nodes) + 1
            with open(config_path, "w") as file:
                for line in lines:
                    if line.startswith("net.numOfNodes"):
                        file.write(f"net.numOfNodes = {self.get_int(self.num_nodes)}\n")
                    elif line.startswith("net.throughputMean"):
                        file.write(f"net.throughputMean = {self.get_float(self.network_throughput)}f\n")
                    elif line.startswith("sim.maxNodes"):
                        file.write(f"sim.maxNodes = {max_nodes_value}\n")
                    else:
                        file.write(line)
        except FileNotFoundError:
            messagebox.showerror("File Error", "The config.txt file was not found.")
        except Exception as e:
            messagebox.showerror("Error", f"An error occurred: {e}")

    def save_config(self):
        try:
            for node_index in range(self.get_int(self.num_nodes)):
                for epoch_index in range(len(self.epoch_data[node_index])):
                    epoch_info = self.epoch_data[node_index][epoch_index]
                    epoch_info["behavior"] = epoch_info["behavior"].get()
                    epoch_info["target_node_id"] = int(epoch_info["target_node_id"].get())
                    epoch_info["connectivity_ratio"] = float(epoch_info["connectivity_ratio"].get())

                node_epochs = self.epoch_data.get(node_index, [])
                with open(f"node_{node_index + 1}_config.csv", "w", newline='') as csvfile:
                    csvwriter = csv.writer(csvfile)
                    csvwriter.writerow(["Epoch", "Behavior", "Target Node ID", "Connectivity Ratio"])
                    for epoch_index, epoch in enumerate(node_epochs):
                        csvwriter.writerow(
                            [epoch_index + 1, epoch["behavior"], epoch["target_node_id"], epoch["connectivity_ratio"]])

            messagebox.showinfo("Success", "Configuration saved successfully!")
            self.main_frame.destroy()
            self.return_callback()
        except ValueError:
            messagebox.showerror("Input Error", "Please ensure all numeric fields contain valid numbers.")

    def clear_frame(self):
        for widget in self.main_frame.winfo_children():
            widget.destroy()


if __name__ == "__main__":
    root = ctk.CTk()
    app = NetworkSimulatorConfigApp(root)
    ctk.set_appearance_mode("dark")
    ctk.set_default_color_theme("blue")
    root.mainloop()