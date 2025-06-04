import customtkinter as ctk
import subprocess
from setupClients import TransactionGenerator
from setupNodes import NetworkSimulatorConfigApp

# Set the default appearance and color theme
ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

# Global variables for frames
transaction_generator = None
network_simulator = None
main_frame = None

# Define configure_clients function
def configure_clients():
    global transaction_generator, main_frame
    main_frame.pack_forget()

    def return_to_main():
        if transaction_generator:
            if hasattr(transaction_generator, 'frame1'):
                transaction_generator.frame1.pack_forget()
            if hasattr(transaction_generator, 'frame2'):
                transaction_generator.frame2.pack_forget()
        main_frame.pack(fill="both", expand=True, padx=20, pady=20)

    transaction_generator = TransactionGenerator(root, return_to_main)

# Define configure_nodes function
def configure_nodes():
    global network_simulator, main_frame
    main_frame.pack_forget()

    def return_to_main():
        # if network_simulator: network_simulator.root.pack_forget()
        main_frame.pack(fill="both", expand=True, padx=20, pady=20)

    network_simulator = NetworkSimulatorConfigApp(root, return_to_main)

# Toggle theme function
def toggle_theme():
    current_theme = ctk.get_appearance_mode()
    ctk.set_appearance_mode("light" if current_theme == "Dark" else "dark")
    theme_switch.toggle()  # Adjust the switch state

# Initialize main window
root = ctk.CTk()
root.title("Configuration GUI")
root.geometry("700x600")

# Main frame container
frame = ctk.CTkFrame(root, corner_radius=15, fg_color=("white", "gray13"))
frame.pack(fill="both", expand=True, padx=20, pady=20)
main_frame = frame

# Header label
header_label = ctk.CTkLabel(
    frame, text="Configuration GUI", font=ctk.CTkFont(size=24, weight="bold"), text_color=("gray10", "gray90")
)
header_label.pack(pady=(30, 10))

# Description label
description_label = ctk.CTkLabel(
    frame, text="Select an option to configure", font=ctk.CTkFont(size=14), text_color=("gray20", "gray80")
)
description_label.pack(pady=(0, 30))

# Sub-frame for buttons
button_frame = ctk.CTkFrame(frame, corner_radius=10, fg_color=("gray95", "gray17"))
button_frame.pack(fill="x", padx=50, pady=10)

# Nodes button
nodes_button = ctk.CTkButton(
    button_frame, text="Configure Nodes", command=configure_nodes, height=40, font=ctk.CTkFont(size=14), corner_radius=8
)
nodes_button.pack(padx=20, pady=(20, 10), fill="x")

# Clients button
clients_button = ctk.CTkButton(
    button_frame, text="Configure Clients", command=configure_clients, height=40, font=ctk.CTkFont(size=14), corner_radius=8
)
clients_button.pack(padx=20, pady=10, fill="x")

# Theme toggle switch
theme_switch = ctk.CTkSwitch(
    button_frame, text="Dark Mode", command=toggle_theme, font=ctk.CTkFont(size=12), button_color=("blue", "blue")
)
theme_switch.pack(pady=20)
theme_switch.select()  # Set to dark mode

# Exit button
exit_button = ctk.CTkButton(
    frame, text="Exit", command=root.quit, height=35, font=ctk.CTkFont(size=13), text_color=("gray10", "white"),
    fg_color=("gray80", "gray30"), hover_color=("gray65", "gray40"), corner_radius=8
)
exit_button.pack(pady=10)

# Start event loop
root.mainloop()
