# Consensus Network Visualization Tool

A simulation and visualization platform for experimenting with consensus network protocols and modeling client/node interactions in distributed systems. Supports configurable client and node behavior (including malicious actors), blockchain structure modeling, and state visualization for real-time consensus tracking. Features a UI to explore account, node, and client dynamics under varied network configurations.

<img width="1920" height="1280" alt="cnsim_concept_art" src="https://github.com/user-attachments/assets/a9defe23-74ee-4b0e-bdeb-f135e5aac263" />

<!--
![cnsim_github](https://github.com/user-attachments/assets/0b315c89-bd58-48d5-9f06-d32ccf81843d)
-->

## Running the Project

To run the project on your local machine, navigate to the `consensus-network-sim/CNSim/resources/test-blockchain/Visualization/CNSVisualization` directory in the terminal and run the Flask server:

```bash
  python app.py
```

In a new terminal, run the web application:

```bash
  python -m http.server 8000
```

Then go to `localhost:8000` on your browser to use the tool.

## Contributors

- Rawsab Said
- Amirreza Radjou
