import tkinter as tk
import customtkinter as ctk
import random
import os
import csv
from tkinter import messagebox

class TransactionGenerator:
    def __init__(self, master, return_callback):
        self.master = master
        self.master.title("Transaction Generator")
        self.return_callback = return_callback  # Store the callback

        self.clients = 0
        self.check_interval = 0
        self.start_time = 0
        self.end_time = 0
        self.transactions = []

        self.setup_initial_frame()

    # Initial frame for inputting number of clients and checking penny jar interval
    def setup_initial_frame(self):
        # Main container frame
        self.frame1 = ctk.CTkFrame(
            self.master,
            corner_radius=15,
            fg_color=("white", "gray13")
        )
        self.frame1.pack(fill="both", expand=True, padx=20, pady=20)

        # Header
        header_label = ctk.CTkLabel(
            self.frame1,
            text="Transaction Generator Setup",
            font=ctk.CTkFont(size=24, weight="bold"),
            text_color=("gray10", "gray90")
        )
        header_label.pack(pady=(30, 20))

        # Input container
        input_frame = ctk.CTkFrame(
            self.frame1,
            corner_radius=10,
            fg_color=("gray95", "gray17")
        )
        input_frame.pack(fill="x", padx=50, pady=10)

        # Input fields
        labels = [
            "Enter number of clients:",
            "How often to check penny jar (seconds):",
            "Enter start time (seconds):",
            "Enter end time (seconds):"
        ]
        entries = [
            "clients_entry",
            "interval_entry",
            "start_time_entry",
            "end_time_entry"
        ]

        for i, (label_text, entry_name) in enumerate(zip(labels, entries)):
            self.create_input_field(input_frame, label_text, i)
            setattr(self, entry_name, self.entry_widget)

        # Next button
        self.next_button = ctk.CTkButton(
            input_frame,
            text="Next",
            command=self.go_to_transactions,
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        self.next_button.pack(pady=20)

    def create_input_field(self, parent, label_text, row):
        container = ctk.CTkFrame(
            parent,
            fg_color="transparent"
        )
        container.pack(fill="x", padx=20, pady=10)

        label = ctk.CTkLabel(
            container,
            text=label_text,
            font=ctk.CTkFont(size=14),
            text_color=("gray20", "gray80")
        )
        label.pack(side="left")

        self.entry_widget = ctk.CTkEntry(
            container,
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        self.entry_widget.pack(side="right", padx=(20, 0))

    def go_to_transactions(self):
        try:
            self.clients = int(self.clients_entry.get())
            self.check_interval = int(self.interval_entry.get())
            self.start_time = int(self.start_time_entry.get())
            self.end_time = int(self.end_time_entry.get())

            # Validate inputs
            if self.clients <= 0:
                raise ValueError("Number of clients must be positive.")
            if self.check_interval <= 0:
                raise ValueError("Interval must be positive.")
            if self.start_time < 0 or self.end_time < 0:
                raise ValueError("Start time and end time must be non-negative integers.")
            if self.start_time >= self.end_time:
                raise ValueError("Start time must be less than end time.")

            # Update CSV file
            self.update_csv_file()
            # Update configuration file
            self.update_config_file()

            self.frame1.pack_forget()
            self.setup_transaction_page()
        except ValueError as e:
            messagebox.showerror("Invalid Input", str(e))

    def update_csv_file(self):
        csv_path = os.path.join(os.path.dirname(os.getcwd()), 'pennycheck.csv')

        try:
            with open(csv_path, 'w', newline='') as csvfile:
                csvwriter = csv.writer(csvfile)
                currentTime = self.start_time
                while currentTime < self.end_time:
                    for client_id in range(1, self.clients + 1):
                        csvwriter.writerow([client_id, currentTime])
                    currentTime += self.check_interval
        except Exception as e:
            messagebox.showerror("Error", f"Failed to update CSV file: {str(e)}")

    def update_config_file(self):
        config_path = os.path.join(os.path.dirname(os.getcwd()), 'config.txt')
        num_of_nodes = None
        
        try:
            # Read the existing config to find the value of net.numOfNodes
            with open(config_path, 'r') as file:
                lines = file.readlines()

            # Find the value of net.numOfNodes
            for line in lines:
                if line.startswith("net.numOfNodes"):
                    num_of_nodes = int(line.split('=')[1].strip())
                    break
            
            if num_of_nodes is None:
                raise ValueError("net.numOfNodes not found in the config file.")

            # Write back the config with updated values
            with open(config_path, 'w') as file:
                for line in lines:
                    if line.startswith("net.numOfClients"):
                        file.write(f"net.numOfClients = {self.clients}\n")
                    elif line.startswith("testblockchain.checkPennyJar"):
                        file.write(f"testblockchain.checkPennyJar = {self.check_interval}\n")
                    elif line.startswith("sim.maxNodes"):
                        file.write(f"sim.maxNodes = {num_of_nodes + self.clients + 1}\n")
                    else:
                        file.write(line)
        except Exception as e:
            messagebox.showerror("Error", f"Failed to update config file: {str(e)}")

    def setup_transaction_page(self):
        self.frame2 = ctk.CTkFrame(
            self.master,
            corner_radius=15,
            fg_color=("white", "gray13")
        )
        self.frame2.pack(fill="both", expand=True, padx=20, pady=20)

        # Header (same as original)
        header = ctk.CTkLabel(
            self.frame2,
            text="Planned Transactions",
            font=ctk.CTkFont(size=24, weight="bold"),
            text_color=("gray10", "gray90")
        )
        header.pack(pady=(20, 10))

        # Transactions list frame
        list_frame = ctk.CTkFrame(
            self.frame2,
            corner_radius=10,
            fg_color=("gray95", "gray17")
        )
        list_frame.pack(fill="both", expand=True, padx=20, pady=10)

        # Replace CTkTextbox with a styled tk.Listbox
        self.transactions_listbox = tk.Listbox(
            list_frame,
            font=("Helvetica", 12),
            bg=("gray95" if ctk.get_appearance_mode() == "light" else "gray17"),
            fg=("gray10" if ctk.get_appearance_mode() == "light" else "gray90"),
            selectbackground="blue",
            selectforeground="white",
            height=15,
            width=80
        )
        self.transactions_listbox.pack(fill="both", expand=True, padx=20, pady=20)
        self.transactions_listbox.bind("<Double-1>", self.edit_transaction_popup)

        # Buttons frame
        button_frame = ctk.CTkFrame(
            self.frame2,
            fg_color="transparent"
        )
        button_frame.pack(fill="x", padx=20, pady=20)

        # Add and Generate buttons
        self.add_button = ctk.CTkButton(
            button_frame,
            text="Add Transaction List",
            command=lambda: self.open_transaction_popup(None),
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        self.add_button.pack(side="left", padx=5)

        self.generate_button = ctk.CTkButton(
            button_frame,
            text="Generate Transactions",
            command=self.generate_transactions,
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        self.generate_button.pack(side="right", padx=5)

    def open_transaction_popup(self, index):
        self.popup = ctk.CTkToplevel(self.master)
        self.popup.title("Add/Edit Transaction")
        self.popup.geometry("600x650")

        # Main popup frame
        popup_frame = ctk.CTkFrame(
            self.popup,
            corner_radius=15,
            fg_color=("white", "gray13")
        )
        popup_frame.pack(fill="both", expand=True, padx=20, pady=20)

        # Input fields container
        input_frame = ctk.CTkFrame(
            popup_frame,
            corner_radius=10,
            fg_color=("gray95", "gray17")
        )
        input_frame.pack(fill="x", padx=20, pady=10)

        self.create_input_field(input_frame, "Sender ID:", 0)
        self.sender_entry = self.entry_widget

        # Receiver type combo box
        self.create_combo_field(input_frame, "Receiver Type:", ["Specific", "Random"], 1)
        self.receiver_choice = self.combo_widget

        self.create_input_field(input_frame, "Receiver ID:", 2)
        self.receiver_entry = self.entry_widget

        self.create_input_field(input_frame, "Total Value:", 3)
        self.total_value_entry = self.entry_widget

        self.create_input_field(input_frame, "Number of Transactions:", 4)
        self.num_transactions_entry = self.entry_widget

        self.create_input_field(input_frame, "Time Interval (ms):", 5)
        self.interval_entry = self.entry_widget

        self.create_input_field(input_frame, "Starting Time (ms):", 6)
        self.start_time_entry = self.entry_widget

        if index is not None:
            self.populate_fields(index)

        # Save button
        save_button = ctk.CTkButton(
            input_frame,
            text="Save",
            command=lambda: self.save_transaction(index),
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        save_button.pack(pady=20)

    def create_combo_field(self, parent, label_text, values, row):
        container = ctk.CTkFrame(
            parent,
            fg_color="transparent"
        )
        container.pack(fill="x", padx=20, pady=10)

        label = ctk.CTkLabel(
            container,
            text=label_text,
            font=ctk.CTkFont(size=14),
            text_color=("gray20", "gray80")
        )
        label.pack(side="left")

        self.combo_widget = ctk.CTkComboBox(
            container,
            values=values,
            height=35,
            font=ctk.CTkFont(size=14),
            corner_radius=8
        )
        self.combo_widget.pack(side="right", padx=(20, 0))

    def populate_fields(self, index):
        transaction = self.transactions[index]
        self.sender_entry.insert(0, transaction['sender'])
        self.receiver_choice.set(transaction['receiver_type'])
        if transaction['receiver_type'] == "Specific":
            self.receiver_entry.insert(0, transaction['receiver_id'])
        self.total_value_entry.insert(0, transaction['total_value'])
        self.num_transactions_entry.insert(0, transaction['num_transactions'])
        self.interval_entry.insert(0, transaction['interval_ms'])
        self.start_time_entry.insert(0, transaction['start_time'])

    def save_transaction(self, index):
        try:
            sender = int(self.sender_entry.get())
            receiver_type = self.receiver_choice.get()
            receiver_id = int(self.receiver_entry.get()) if receiver_type == "Specific" else None
            total_value = float(self.total_value_entry.get())
            num_transactions = int(self.num_transactions_entry.get())
            interval_ms = int(self.interval_entry.get())
            start_time_ms = int(self.start_time_entry.get())

            if sender < 1 or (receiver_id is not None and receiver_id < 1):
                raise ValueError("Client IDs must be positive integers.")

            transaction_info = (f"Sender: {sender}, Receiver: {receiver_type} "
                                f"({receiver_id if receiver_id is not None else 'Random'}), "
                                f"Value: {total_value}, Transactions: {num_transactions}, "
                                f"Interval: {interval_ms} ms, Start: {start_time_ms} ms")

            if index is not None:
                self.transactions[index] = {
                    'sender': sender,
                    'receiver_type': receiver_type,
                    'receiver_id': receiver_id,
                    'total_value': total_value,
                    'num_transactions': num_transactions,
                    'interval_ms': interval_ms,
                    'start_time': start_time_ms
                }
                self.transactions_listbox.delete(index)
                self.transactions_listbox.insert(index, transaction_info)
            else:
                self.transactions_listbox.insert(tk.END, transaction_info)
                self.transactions.append({
                    'sender': sender,
                    'receiver_type': receiver_type,
                    'receiver_id': receiver_id,
                    'total_value': total_value,
                    'num_transactions': num_transactions,
                    'interval_ms': interval_ms,
                    'start_time': start_time_ms
                })

            self.popup.destroy()
        except ValueError as e:
            messagebox.showerror("Invalid Input", str(e))

    def edit_transaction_popup(self, event):
        index = self.transactions_listbox.curselection()[0]
        self.open_transaction_popup(index)

    def generate_transactions(self):
        try:
            final_transactions = []

            for txn in self.transactions:
                sender = txn['sender']
                receiver_type = txn['receiver_type']
                receiver_id = txn['receiver_id']
                total_value = txn['total_value']
                num_transactions = txn['num_transactions']
                interval_ms = txn['interval_ms']
                start_time = txn['start_time']

                value_per_transaction = total_value / num_transactions
                current_time = start_time

                for _ in range(num_transactions):
                    receiver = receiver_id if receiver_type == "Specific" else random.randint(1, self.clients)
                    final_transactions.append((sender, receiver, value_per_transaction, current_time))
                    current_time += interval_ms

            self.save_transactions(final_transactions)
            messagebox.showinfo("Success", "Transactions generated successfully!")
            self.return_callback()
        except ValueError as e:
            messagebox.showerror("Error", str(e))

    def save_transactions(self, transactions):
        with open("transactions.txt", "w") as file:
            for sender, receiver, amount, time in transactions:
                file.write(f"{sender},{receiver},{amount},{time}\n")

if __name__ == "__main__":
    root = ctk.CTk()
    app = TransactionGenerator(root)
    root.mainloop()

