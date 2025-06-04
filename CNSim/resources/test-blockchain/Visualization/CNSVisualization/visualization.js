// Network and state visualizations

const svg = d3.select("svg");
const g = svg.select('#visualization-group')  // group for all elements

let initialRun = true;

function loadVisualization(framesFile) {

    // svg.selectAll("*").remove();

    d3.json(framesFile).then(function(data) {

        console.log("frames.json data loaded successfully.");

        // ================================== MAIN VISUALIZATION ==================================

        const visualizerBox = document.querySelector('.visualizer-box');

        const width = 1200;
        const height = 1200;
        svg.attr("width", width).attr("height", height);

        widthSpacing = 1000;
        heightSpacing = 800;
        
        const markerRadius = 40;
        const nodeY = 50;
        clientY = heightSpacing * 0.375;
        accountY = heightSpacing * 0.625;
        const textMaxWidth = 120;

        const toggledClients = new Set();

        let currentMessageToggled = 2;
        // 0: Clients only
        // 1: Nodes only
        // 2: Clients and Nodes

        let currentTransform = d3.zoomIdentity;

        const simtimeDisplay = d3.select("#simtimeDisplay");

        const tooltip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("position", "absolute")
            .style("padding", "8px")
            .style("background-color", "#fff")
            .style("border", "1px solid #ccc")
            .style("border-radius", "4px")
            .style("visibility", "hidden")
            .style("font-size", "14px");

        const arrowTooltip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("position", "absolute")
            .style("padding", "8px")
            .style("background-color", "#fff")
            .style("border", "1px solid #ccc")
            .style("border-radius", "4px")
            .style("visibility", "hidden")
            .style("font-size", "14px");

        const zoom = d3.zoom()
            .scaleExtent([0.1, 2])
            .on("zoom", (event) => {
                currentTransform = event.transform;
                g.attr('transform', currentTransform);
            });

        svg.call(zoom)
            .on("wheel", (event) => {
                event.preventDefault();
                event.stopPropagation();
            }, { passive: false});

        // Initial zoom and position
        const initialScale = 0.8;
        const initialTranslateX = 40;
        const initialTranslateY = 20;
        const initialZoom = d3.zoomIdentity
            .translate(initialTranslateX, initialTranslateY)
            .scale(initialScale);
        svg.call(zoom.transform, initialZoom);

        // to format Content field of client to display
        function formatContent(content) {
            let formattedContent = "";
            let insideBrackets = false;

            for (let i = 0; i < content.length; i++) {
                const char = content[i];
                if (char === "[") {
                    insideBrackets = true;
                } else if (char === "]") {
                    insideBrackets = false;
                }

                formattedContent += char;
                if (char === "," && !insideBrackets) {
                    formattedContent += "<br>";
                }
            }

            return formattedContent;
        }

        // used to be wrapText, added background option
        function wrapTextWithBackground(text, width, padding, fillColor, borderColor = null) {
            text.each(function() {
                const textElement = d3.select(this);
        
                const words = textElement.text().split(/\s+/).reverse();
                let word;
                let line = [];
                let lineNumber = 0;
                const lineHeight = 1.1;
                const y = textElement.attr("y");
                const dy = parseFloat(textElement.attr("dy")) || 0;
                const x = textElement.attr("x");
        
                const group = textElement.node().parentNode;
                const wrapperGroup = d3.select(group).insert("g", ":first-child");
        
                let tspan = textElement.text(null).append("tspan").attr("x", x).attr("y", y).attr("dy", dy + "em");
        
                while (word = words.pop()) {
                    line.push(word);
                    tspan.text(line.join(" "));
                    if (tspan.node().getComputedTextLength() > width) {
                        line.pop();
                        tspan.text(line.join(" "));
                        line = [word];
                        tspan = textElement.append("tspan")
                            .attr("x", x)
                            .attr("y", y)
                            .attr("dy", ++lineNumber * lineHeight + dy + "em")
                            .text(word);
                    }
                }
        
                const bbox = textElement.node().getBBox();
                const rect = wrapperGroup.insert("rect", "text")
                    .attr("x", bbox.x - padding)
                    .attr("y", bbox.y - padding)
                    .attr("width", bbox.width + 2 * padding)
                    .attr("height", bbox.height + 2 * padding)
                    .attr("rx", 10)
                    .attr("ry", 10)
                    .attr("fill", fillColor);
        
                if (borderColor) {
                    rect.attr("stroke", borderColor)
                        .attr("stroke-width", 2);
                }
            });
        }    
        
        function updateVisualization(simTime) {
            const simData = data[simTime];
            // console.log("Data for simTime", simTime, ":", simData);  // Log simData in console for debugging

            simtimeDisplay.text(`SimTime: ${simTime}`);
        
            if (!simData || !simData.clients || !simData.nodes) {
                console.error(`No data found for SimTime: ${simTime}`);
                return;
            }
        
            const clients = Object.values(simData.clients);

            if (initialRun) {
                clients.forEach(client => toggledClients.add(client.ID));  // Show all clients accounts by default
                initialRun = false;
            }

            // sort nodes by ID
            const nodes = Object.values(simData.nodes).sort((a, b) => {
                const aId = parseInt(a.NodeId.replace('node-', ''), 10);
                const bId = parseInt(b.NodeId.replace('node-', ''), 10);
                return aId - bId;
            });
        
            g.selectAll(".node-marker").remove();
            g.selectAll(".client-marker").remove();
            g.selectAll(".arrow-line").remove();
            g.selectAll(".tooltip").remove();

            const firstClientX = 100;
            const lastClientX = firstClientX + widthSpacing; 
            const clientCount = clients.length;
            const clientSpacing = clientCount > 1 ? (lastClientX - firstClientX) / (clientCount - 1) : 0;

            const firstNodeX = 100;
            const lastNodeX = firstNodeX + widthSpacing;
            const nodeCount = nodes.length;
            const nodeSpacingAdjusted = nodeCount > 1 ? (lastNodeX - firstNodeX) / (nodeCount - 1) : 0;

            d3.selectAll(".arrow-line").remove();
            d3.selectAll(".arrow-line-hitbox").remove();
            arrowTooltip.style("visibility", "hidden");

            const clientMarkers = g.selectAll(".client-marker")
            .data(clients)
            .enter().append("g")
            .attr("class", "client-marker")
            .attr("transform", (d, i) => `translate(${firstClientX + i * clientSpacing}, ${clientY})`);
        
            clientMarkers.append("circle")
                .attr("r", markerRadius)
                .attr("fill", "#FDDEDF")
                .on("click", toggleAccounts)  // Toggle accounts on click
                .on("mouseover", function(event, d) {   // show Content on hover
                    const formattedContent = formatContent(d.Content);
                    tooltip.style("visibility", "visible")
                        .html(`Client ${d.ID}:<br>${formattedContent}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px")
                        .style("color", "#D77171")
                        .style("background-color", "#fef5f5")
                        .style("border-color", "#FDDEDF")
                        .style("border-width", "2px");
                })
                .on("mousemove", function(event) {
                    tooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px");
                })
                .on("mouseout", function() {
                    tooltip.style("visibility", "hidden");
                });
        
            clientMarkers.append("image")
                .attr("xlink:href", "assets/graph-icons/client-icon.svg")
                .attr("width", 40)
                .attr("height", 40)
                .attr("x", -20)
                .attr("y", -20)
                .style("fill", "#FA7171")
                .attr("class", "client-icon")
                .on("click", toggleAccounts)  // Toggle accounts on click
                .on("mouseover", function(event, d) {   // show Content on hover
                    const formattedContent = formatContent(d.Content);
                    tooltip.style("visibility", "visible")
                        .html(`Client ${d.ID}:<br>${formattedContent}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px")
                        .style("color", "#D77171")
                        .style("background-color", "#fef5f5")
                        .style("border-color", "#FDDEDF")
                        .style("border-width", "2px");
                })
                .on("mousemove", function(event) {
                    tooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px");
                })
                .on("mouseout", function() {
                    tooltip.style("visibility", "hidden");
                });

            clientMarkers.append("text")
                .attr("x", 0)
                .attr("y", markerRadius + 10)
                .attr("text-anchor", "middle")
                .attr("dy", "1em")
                .attr("width", textMaxWidth)
                .text(d => `Client ${d.ID}: ${d.Type}`)
                .call(wrapTextWithBackground, textMaxWidth, 10, "white", "#ededed");

            const nodeMarkers = g.selectAll(".node-marker")
                .data(nodes)
                .enter().append("g")
                .attr("class", "node-marker")
                .attr("transform", (d, i) => `translate(${firstNodeX + i * nodeSpacingAdjusted}, ${nodeY})`);
            
            nodeMarkers.append("circle")
                .attr("r", markerRadius)
                .attr("fill", "#ECEAFB")
                .on("mouseover", function(event, d) {   // show Epoch on hover
                    tooltip.style("visibility", "visible")
                        .html(`Node ${d.NodeId}: Epoch ${d.Epoch}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px")
                        .style("color", "#6553B4")
                        .style("background-color", "#f9f9fe")
                        .style("border-color", "#ECEAFB")
                        .style("border-width", "2px");
                })
                .on("mousemove", function(event) {
                    tooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px");
                })
                .on("mouseout", function() {
                    tooltip.style("visibility", "hidden");
                });
        
            nodeMarkers.append("image")
                .attr("xlink:href", "assets/graph-icons/node-icon.svg")
                .attr("width", 40)
                .attr("height", 40)
                .attr("x", -20)
                .attr("y", -20)
                .style("stroke", "#6553EF")
                .attr("class", "client-icon")
                .on("mouseover", function(event, d) {   // show Epoch on hover
                    tooltip.style("visibility", "visible")
                        .html(`Node ${d.NodeId}: Epoch ${d.Epoch}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px")
                        .style("color", "#6553B4")
                        .style("background-color", "#f9f9fe")
                        .style("border-color", "#ECEAFB")
                        .style("border-width", "2px");
                })
                .on("mousemove", function(event) {
                    tooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX + 10) + "px");
                })
                .on("mouseout", function() {
                    tooltip.style("visibility", "hidden");
                });

            nodeMarkers.append("text")
                .attr("x", 0)
                .attr("y", markerRadius + 10)
                .attr("text-anchor", "middle")
                .attr("dy", "1em")
                .attr("width", textMaxWidth)
                .text(d => `Node ${d.NodeId}: ${d.EventType}`)
                .call(wrapTextWithBackground, textMaxWidth, 10, "white", "#ededed");

                if (currentMessageToggled === 0 || currentMessageToggled === 2) {
                    nodes.forEach(node => {
                        // Get the list of Client IDs this node is connected to
                        const connectedClients = node.ClientIds ? Object.keys(node.ClientIds) : [];
                    
                        // Draw paths to each connected client
                        connectedClients.forEach(clientId => {
                            const client = clients.find(c => c.ID === parseInt(clientId, 10));
                            if (client) {
                                const clientX = clients.indexOf(client) * clientSpacing + firstClientX;
                                const nodeX = nodes.indexOf(node) * nodeSpacingAdjusted + firstClientX;
                    
                                const nodeRectElement = d3.select(`.node-marker rect`).node();
                                const nodeBBox = nodeRectElement.getBBox();
                                const nodeLabelHeight = nodeBBox.height;
                    
                                // Main path
                                const mainPath = g.append("path")
                                    .attr("class", `arrow-line node-path-${node.NodeId}`)  // Add a class with NodeId for selection
                                    .attr("d", `M${nodeX},${nodeY + markerRadius + nodeLabelHeight} 
                                                C${nodeX},${((nodeY + clientY) / 2)}, 
                                                ${clientX},${((nodeY + clientY) / 2)}, 
                                                ${clientX},${clientY - markerRadius}`)
                                    .attr("stroke", "#6554F0")
                                    .attr("fill", "none")
                                    .attr("stroke-width", 2)
                                    .attr("marker-end", "url(#arrow)")
                                    .attr("opacity", 0.2);
                    
                                // Invisible hitbox path for easier hover
                                g.append("path")
                                    .attr("class", `arrow-line-hitbox node-path-hitbox-${node.NodeId}`)
                                    .attr("d", `M${nodeX},${nodeY + markerRadius + nodeLabelHeight} 
                                                C${nodeX},${((nodeY + clientY) / 2)}, 
                                                ${clientX},${((nodeY + clientY) / 2)}, 
                                                ${clientX},${clientY - markerRadius}`)
                                    .attr("stroke", "transparent") 
                                    .attr("fill", "none")
                                    .attr("stroke-width", 20)
                                    .on("mouseover", function(event) {
                                        if (currentMessageToggled === 1) return;

                                        mainPath.attr("opacity", 1).attr("stroke-width", 3);
                
                                        // Get the Type and Details from the client
                                        const clientType = client.Type || "N/A";
                                        const clientDetails = client.Content || "N/A";
                
                                        arrowTooltip.style("visibility", "visible")
                                            .html(`Type: ${clientType}<br>Details: ${clientDetails}`)
                                            .style("top", (event.pageY - 10) + "px")
                                            .style("left", (event.pageX + 10) + "px")
                                            .style("color", "#6553B4")
                                            .style("background-color", "#f9f9fe")
                                            .style("border-color", "#ECEAFB")
                                            .style("border-width", "2px");
                                    })
                                    .on("mousemove", function(event) {
                                        if (currentMessageToggled === 1) return;
                                        arrowTooltip.style("top", (event.pageY - 10) + "px")
                                            .style("left", (event.pageX + 10) + "px");
                                    })
                                    .on("mouseout", function() {
                                        if (currentMessageToggled === 1) return;
                                        mainPath.attr("opacity", 0.2).attr("stroke-width", 2);
                                        arrowTooltip.style("visibility", "hidden");
                                    });
                            }
                        });
                    });
                }                
            
            nodeMarkers.on("mouseover", function(event, d) {
                d3.selectAll(`.node-path-${d.NodeId}`).attr("opacity", 1).attr("stroke-width", 3);
            })
            .on("mouseout", function(event, d) {
                d3.selectAll(`.node-path-${d.NodeId}`).attr("opacity", 0.2).attr("stroke-width", 2);
            });            

            // Ensure that accounts of toggled clients are shown and updated for current simtime
            clients.forEach(d => {
                if (toggledClients.has(d.ID)) {
                    const clientX = clients.indexOf(d) * clientSpacing + 100;
                    showAccounts(d, clientX, simData);
                }
            });

            if (currentMessageToggled === 1 || currentMessageToggled === 2) {
                clients.forEach(client => {
                    const connectedNodes = client.NodeIds ? Object.keys(client.NodeIds) : [];
            
                    connectedNodes.forEach(nodeId => {
                        const node = nodes.find(n => n.NodeId === nodeId);
                        if (node) {
                            const clientX = clients.indexOf(client) * clientSpacing + firstClientX;
                            const nodeX = nodes.indexOf(node) * nodeSpacingAdjusted + firstNodeX;
            
                            const clientTopY = clientY - markerRadius;
            
                            const nodeRectElement = d3.select(`.node-marker rect`).node();
                            const nodeBBox = nodeRectElement.getBBox();
                            const nodeLabelHeight = nodeBBox.height;
            
                            const nodeBottomY = nodeY + markerRadius + nodeLabelHeight;
            
                            const mainPath = g.append("path")
                                .attr("class", `arrow-line client-path-${client.ID}`)
                                .attr("d", `M${clientX},${clientTopY}
                                            C${clientX},${(clientTopY + nodeBottomY) / 2},
                                            ${nodeX},${(clientTopY + nodeBottomY) / 2},
                                            ${nodeX},${nodeBottomY}`)
                                .attr("stroke", "#FA7171")
                                .attr("fill", "none")
                                .attr("stroke-width", 2)
                                .attr("marker-end", "url(#arrow)")
                                .attr("opacity", 0.2);
            
                            // Invisible hitbox path for easier hover
                            g.append("path")
                                .attr("class", `arrow-line-hitbox client-path-hitbox-${client.ID}`)
                                .attr("d", `M${clientX},${clientTopY}
                                            C${clientX},${(clientTopY + nodeBottomY) / 2},
                                            ${nodeX},${(clientTopY + nodeBottomY) / 2},
                                            ${nodeX},${nodeBottomY}`)
                                .attr("stroke", "transparent")
                                .attr("fill", "none")
                                .attr("stroke-width", 20)
                                .on("mouseover", function(event) {
                                    if (currentMessageToggled === 0) return;
                                    
                                    mainPath.attr("opacity", 1).attr("stroke-width", 3);
            
                                    // Get the Type and Details from the node
                                    const nodeType = client.NodeIds[nodeId]?.Type || "N/A";
                                    const nodeDetails = client.NodeIds[nodeId]?.Details || "N/A";
            
                                    arrowTooltip.style("visibility", "visible")
                                        .html(`Type: ${nodeType}<br>Details: ${nodeDetails}`)
                                        .style("top", (event.pageY - 10) + "px")
                                        .style("left", (event.pageX + 10) + "px")
                                        .style("color", "#D77171")
                                        .style("background-color", "#fef5f5")
                                        .style("border-color", "#FDDEDF")
                                        .style("border-width", "2px");
                                })
                                .on("mousemove", function(event) {
                                    if (currentMessageToggled === 0) return;
                                    arrowTooltip.style("top", (event.pageY - 10) + "px")
                                        .style("left", (event.pageX + 10) + "px");
                                })
                                .on("mouseout", function() {
                                    if (currentMessageToggled === 0) return;
                                    mainPath.attr("opacity", 0.2).attr("stroke-width", 2);
                                    arrowTooltip.style("visibility", "hidden");
                                });
                        }
                    });
                });
            }            
        
            clientMarkers.on("mouseover", function(event, d) {
                d3.selectAll(`.client-path-${d.ID}`).attr("opacity", 1).attr("stroke-width", 3);
            })
            .on("mouseout", function(event, d) {
                d3.selectAll(`.client-path-${d.ID}`).attr("opacity", 0.2).attr("stroke-width", 2);
            });
            

            function toggleAccounts(event, d) {
                const clientX = +d3.select(this.parentNode).attr("transform").match(/\d+/g)[0];  // get client X pos

                if (toggledClients.has(d.ID)) {
                    hideAccounts(d);
                    toggledClients.delete(d.ID);
                    console.log("Client accounts hidden:", d.ID);
                } else {
                    showAccounts(d, clientX, simData);
                    toggledClients.add(d.ID);
                    console.log("Client accounts displayed:", d.ID);
                }
            }

            function showAccounts(d, clientX, simData) {
                g.selectAll(`.accounts-group-${d.ID}`).remove();  // refresh

                const accounts = Object.values(simData.clients[d.ID].accounts);
                const accountGroup = g.append("g").attr("class", `accounts-group-${d.ID}`);

                const clientTextElement = d3.select(`.client-marker rect`).node();
                const clientBBox = clientTextElement.getBBox();
                const clientLabelHeight = clientBBox.height;

                accountGroup.selectAll(".line")
                    .data(accounts)
                    .enter().append("line")
                    .attr("x1", clientX)
                    .attr("y1", clientY + markerRadius + clientLabelHeight)
                    .attr("x2", (d, i) => clientX + i * 80 - (accounts.length - 1) * 40)
                    .attr("y2", accountY - markerRadius)
                    .attr("stroke", "black")
                    .attr("stroke-width", 3)
                    .attr("stroke-dasharray", "2,2");

                const accountMarkers = accountGroup.selectAll(".account-marker")
                    .data(accounts)
                    .enter().append("g")
                    .attr("class", "account-marker")
                    .attr("transform", (d, i) => `translate(${clientX + i * 80 - (accounts.length - 1) * 40}, ${accountY})`);

                accountMarkers.append("circle")
                    .attr("r", markerRadius)
                    .attr("fill", "#DFECFB");

                accountMarkers.append("image")
                    .attr("xlink:href", "assets/graph-icons/account-icon.svg")
                    .attr("width", 40)
                    .attr("height", 40)
                    .attr("x", -20)
                    .attr("y", -20)
                    .style("fill", "#0767F8")
                    .attr("class", "client-icon");

                accountMarkers.append("text")
                    .attr("x", 0)
                    .attr("y", markerRadius + 10)
                    .attr("text-anchor", "middle")
                    .attr("dy", "1em")
                    .attr("width", textMaxWidth)
                    .text(d => `Account ${d.ID}: Balance: ${d.Balance}, State: ${d.State}`)
                    .call(wrapTextWithBackground, textMaxWidth, 10, "white", "#ededed");
            }

            function hideAccounts(d) {
                g.selectAll(`.accounts-group-${d.ID}`).remove();
            }
        }

        let frameIndex = 0;
        const simTimes = Object.keys(data);

        let isPlaying = false;
        let animationTimeout;
        let animationSpeed = 1000;

        function step() {   // func to loop through simTime frames
            if (isPlaying) {
                updateVisualization(simTimes[frameIndex]);
                updateStateVisualization(stateSimTimes[frameIndex]);
                frameIndex = frameIndex + 1
                if (frameIndex >= simTimes.length) {
                    frameIndex = 0;
                }
                animationTimeout = setTimeout(step, animationSpeed);
            }
        }

        function reloadFrames() {
            console.log("Visualization refresh requested.");
            d3.json("frames.json").then(function(newData) {
                g.selectAll("*").remove();
                data = newData;  // update data
                frameIndex = 0;  // reset simTime
                updateVisualization(frameIndex);
                updateStateVisualization(frameIndex);
                alert('Visualization reset and data reloaded successfully!');
                console.log("Visualization reset and data reloaded successfully.");
            }).catch(function(error) {
                console.error('Error loading frames:', error);
                alert('Error reloading frames.');
            });
        }

        window.reloadFrames = reloadFrames; // make global

    // ================================== STATE VISUALIZATION ==================================

        const stateSvg = d3.select("#state-visualization");
        const stateGroup = stateSvg.select('#state-visualization-group');  // Group for all elements

        stateRadius = 250;  // Radius for circular layout
        const stateWidth = 1200;
        const stateHeight = 1200;
        const stateMarkerRadius = 40;

        const stateSimtimeDisplay = d3.select("#stateSimtimeDisplay");

        let stateCurrentTransform = d3.zoomIdentity;

        stateSvg.attr("width", stateWidth).attr("height", stateHeight);

        const stateCenterX = stateWidth / 2;
        const stateCenterY = stateHeight / 2;

        const stateTooltip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("position", "absolute")
            .style("padding", "8px")
            .style("background-color", "#fff")
            .style("border", "1px solid #ccc")
            .style("border-radius", "4px")
            .style("visibility", "hidden")
            .style("font-size", "14px");

        const stateZoom = d3.zoom()
            .scaleExtent([0.1, 2])
            .on("zoom", (event) => {
                stateCurrentTransform = event.transform;
                stateGroup.attr('transform', stateCurrentTransform);
            });

        stateSvg.call(stateZoom)
            .on("wheel", (event) => {
                event.preventDefault();
                event.stopPropagation();
            }, { passive: false});

        // Initial zoom and position
        const stateInitialScale = 0.7;
        const stateInitialTranslateX = -180;
        const stateInitialTranslateY = -190;
        const initialStateZoom = d3.zoomIdentity
            .translate(stateInitialTranslateX, stateInitialTranslateY)
            .scale(stateInitialScale);
        stateSvg.call(stateZoom.transform, initialStateZoom);

        function updateStateVisualization(simTime) {
            const simData = data[simTime];
            stateSimtimeDisplay.text(`SimTime: ${simTime}`);

            if (!simData || !simData.nodes) {
                console.error(`No node data found for SimTime: ${simTime}`);
                return;
            }

            const nodes = Object.values(simData.nodes).sort((a, b) => {
                const aId = parseInt(a.NodeId.replace('node-', ''), 10);
                const bId = parseInt(b.NodeId.replace('node-', ''), 10);
                return aId - bId;
            });
            
            const nodeIds = nodes.map(node => node.NodeId);

            // Clear previous visualization
            stateGroup.selectAll("*").remove();

            // Position nodes in a circle
            const angleStep = (2 * Math.PI) / nodeIds.length;
            const positions = nodeIds.map((_, i) => {
                const angle = i * angleStep;
                return {
                    x: stateCenterX + stateRadius * Math.cos(angle),
                    y: stateCenterY + stateRadius * Math.sin(angle)
                };
            });

            // Draw nodes
            const nodeElements = stateGroup.selectAll(".node-marker")
                .data(nodes)
                .enter().append("g")
                .attr("class", "node-marker")
                .attr("transform", (d, i) => `translate(${positions[i].x}, ${positions[i].y})`);


            nodeElements.append("circle")
                .attr("r", stateMarkerRadius)
                .attr("fill", "#ECEAFB")
                .on("mouseover", function(event, d) {  // Show tooltip on hover
                    stateTooltip.style("visibility", "visible")
                        .html(`Node ${d.NodeId}<br>Type: ${d.StateType || "N/A"}<br>Details: ${d.StateDetails || "N/A"}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX - stateTooltip.node().offsetWidth - 10) + "px")  // Position on the left
                        .style("color", "#6553B4")
                        .style("background-color", "#f9f9fe")
                        .style("border-color", "#ECEAFB")
                        .style("border-width", "2px")
                        .style("max-width", "600px")
                        .style("white-space", "normal");
                })
                .on("mousemove", function(event) {
                    stateTooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX - stateTooltip.node().offsetWidth - 10) + "px");  // Position on the left
                })            
                .on("mouseout", function() {
                    stateTooltip.style("visibility", "hidden");
                })

            nodeElements.append("image")
                .attr("xlink:href", "assets/graph-icons/node-icon.svg")
                .attr("width", 40)
                .attr("height", 40)
                .attr("x", -20)
                .attr("y", -20)
                .on("mouseover", function(event, d) {  // Show tooltip on hover
                    stateTooltip.style("visibility", "visible")
                        .html(`Node ${d.NodeId}<br>Type: ${d.StateType || "N/A"}<br>Details: ${d.StateDetails || "N/A"}`)
                        .style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX - stateTooltip.node().offsetWidth - 10) + "px")  // Position on the left
                        .style("color", "#6553B4")
                        .style("background-color", "#f9f9fe")
                        .style("border-color", "#ECEAFB")
                        .style("border-width", "2px")
                        .style("max-width", "600px")
                        .style("white-space", "normal");
                })
                .on("mousemove", function(event) {
                    stateTooltip.style("top", (event.pageY - 10) + "px")
                        .style("left", (event.pageX - stateTooltip.node().offsetWidth - 10) + "px");  // Position on the left
                })            
                .on("mouseout", function() {
                    stateTooltip.style("visibility", "hidden");
                });

            nodeElements.append("text")
                .attr("x", 0)
                .attr("y", stateMarkerRadius + 15)
                .attr("text-anchor", "middle")
                .attr("dy", "0.35em")
                .text(d => d.NodeId)
                .call(wrapTextWithBackground, textMaxWidth, 10, "white", "#ededed");

            // Draw connections
            nodes.forEach((node, i) => {
                const sourcePos = positions[i];
                node.NodesList.forEach(targetId => {
                    const targetIndex = nodeIds.indexOf(targetId);
                    
                    if (targetIndex === i) {
                        const mainPath = stateGroup.append("path")
                            .attr("class", `self-loop connection-${node.NodeId}`)
                            .attr("d", `M${sourcePos.x},${sourcePos.y - stateMarkerRadius} 
                                        a60,80 0 1,0 0,${stateMarkerRadius * 2}`)
                            .attr("stroke", "#6554F0")
                            .attr("fill", "none")
                            .attr("stroke-width", 2)
                            .attr("marker-end", "url(#arrow)")
                            .attr("opacity", 0.2);
                            
                        stateGroup.append("path")
                            .attr("class", `self-loop-hitbox connection-hitbox-${node.NodeId}`)
                            .attr("d", `M${sourcePos.x},${sourcePos.y - stateMarkerRadius} 
                                        a60,80 0 1,0 0,${stateMarkerRadius * 2}`)
                            .attr("stroke", "transparent")
                            .attr("fill", "none")
                            .attr("stroke-width", 20)
                            .on("mouseover", function() {
                                mainPath.attr("opacity", 1).attr("stroke-width", 3);
                            })
                            .on("mouseout", function() {
                                mainPath.attr("opacity", 0.2).attr("stroke-width", 2);
                            });

                    } else if (targetIndex >= 0) {
                        const targetPos = positions[targetIndex];
                        const dx = targetPos.x - sourcePos.x;
                        const dy = targetPos.y - sourcePos.y;
                        const distance = Math.sqrt(dx * dx + dy * dy);

                        const shortenDistance = 40;
                        const x2Short = targetPos.x - (dx / distance) * shortenDistance;
                        const y2Short = targetPos.y - (dy / distance) * shortenDistance;
                    
                        const mainPath = stateGroup.append("line")
                            .attr("class", `connection-${node.NodeId}`)
                            .attr("x1", sourcePos.x)
                            .attr("y1", sourcePos.y)
                            .attr("x2", x2Short)
                            .attr("y2", y2Short)
                            .attr("stroke", "#6554F0")
                            .attr("stroke-width", 2)
                            .attr("marker-end", "url(#arrow)")
                            .attr("opacity", 0.2);

                        stateGroup.append("line")
                            .attr("class", `connection-hitbox-${node.NodeId}`)
                            .attr("x1", sourcePos.x)
                            .attr("y1", sourcePos.y)
                            .attr("x2", x2Short)
                            .attr("y2", y2Short)
                            .attr("stroke", "transparent")
                            .attr("stroke-width", 20)
                            .on("mouseover", function() {
                                mainPath.attr("opacity", 1).attr("stroke-width", 3);
                            })
                            .on("mouseout", function() {
                                mainPath.attr("opacity", 0.2).attr("stroke-width", 2);
                            });
                    }
                });
            });

            nodeElements.on("mouseover", function(event, d) {
                d3.selectAll(`.connection-${d.NodeId}`).attr("opacity", 1).attr("stroke-width", 3);
            })
            .on("mouseout", function(event, d) {
                d3.selectAll(`.connection-${d.NodeId}`).attr("opacity", 0.2).attr("stroke-width", 2);
            });
        }

        // Initialize arrow marker
        stateSvg.append("defs").append("marker")
            .attr("id", "arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 5)
            .attr("refY", 0)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr("fill", "#6554F0");

        const stateSimTimes = Object.keys(data);

        // ==============*** UI Buttons ***============== 

        d3.select("#playButton").on("click", function() {
            console.log("Visualization playing from simTime:", simTimes[frameIndex]);
            if (!isPlaying) {
                isPlaying = true;
                step();
            }
        });

        d3.select("#pauseButton").on("click", function() {
            console.log("Visualization paused.");
            if (isPlaying) {
                isPlaying = false;
                clearTimeout(animationTimeout);
            }
        });

        d3.select("#restartButton").on("click", function() {
            console.log("Visualization restarted.");
            isPlaying = true;
            frameIndex = 0;
            clearTimeout(animationTimeout);
            step();
        });

        const speedSlider = d3.select("#speedSlider");
        const speedValueDisplay = document.getElementById("speedValue");

        speedSlider.on("input", function() {
            animationSpeed = +this.value;
            speedValueDisplay.innerHTML = `${animationSpeed}ms`;
            console.log("Step delay set to:", animationSpeed);
        });

        d3.select("#stepBackButton").on("click", function() {
            isPlaying = false;
            clearTimeout(animationTimeout);
            frameIndex = (frameIndex - 1 + simTimes.length) % simTimes.length;
            updateVisualization(simTimes[frameIndex]);
            updateStateVisualization(stateSimTimes[frameIndex]);
            console.log("Stepped back to simTime:", simTimes[frameIndex]);
        });

        d3.select("#stepForwardButton").on("click", function() {
            isPlaying = false;
            clearTimeout(animationTimeout);
            frameIndex = (frameIndex + 1) % simTimes.length;
            updateVisualization(simTimes[frameIndex]);
            updateStateVisualization(stateSimTimes[frameIndex]);
            console.log("Stepped forward to simTime:", simTimes[frameIndex]);
        });

        d3.select("#showAllAccountsButton").on("click", function() {        
            const simData = data[simTimes[frameIndex]];
            const clients = Object.values(simData.clients);

            toggledClients.clear();
            clients.forEach(client => {
                toggledClients.add(client.ID);
            });

            updateVisualization(simTimes[frameIndex]);
            console.log("All client accounts displayed.");
        });

        d3.select("#hideAllAccountsButton").on("click", function() {
            toggledClients.clear();
            svg.selectAll("[class^=accounts-group-]").remove();
            console.log("All client accounts hidden.");
        });

        const messageSvgPaths = [
            "assets/menu-icons/client-only.svg",
            "assets/menu-icons/node-only.svg",
            "assets/menu-icons/client-node.svg"
        ];

        function toggleMessageDisplayed() {
            const iconElement = d3.select("#toggleMessagesIcon");
            const svgPath = messageSvgPaths[currentMessageToggled];

            iconElement.html("");

            d3.xml(svgPath).then(svgData => {
                iconElement.node().append(svgData.documentElement);
            }).catch(error => {
                console.error(`Error loading SVG: ${svgPath}`, error);
            });
        }

        d3.select("#toggleMessages").on("click", () => {
            currentMessageToggled = (currentMessageToggled + 1) % messageSvgPaths.length;
            toggleMessageDisplayed();
            updateVisualization(simTimes[frameIndex]);
            if (currentMessageToggled === 0) {
                console.log("Message arrows displayed: Client messages only");
            }
            else if (currentMessageToggled === 1) {
                console.log("Message arrows displayed: Node messages only");
            }
            else {
                console.log("Message arrows displayed: Client and Node messages");
            }
        });

        toggleMessageDisplayed();

        
        d3.select("#goToSimtime").on("click", function() {
            const simTimeValue = parseInt(d3.select("#simTime").property("value"), 10);
        
            if (!isNaN(simTimeValue) && simTimeValue >= 0) {
                console.log("Inputted SimTime value:", simTimeValue);
                goToSimTime(simTimeValue);
            } else {
                alert("Please enter a valid SimTime value.");
                console.error("Invalid SimTime value (", simTimeValue, ") entered.");
            }
        });
        
        function goToSimTime(simTimeValue) {
            let targetSimTime = simTimes.includes(simTimeValue)
                ? simTimeValue
                : findClosestSimTime(simTimeValue);
        
            updateVisualization(targetSimTime);
            updateStateVisualization(targetSimTime);
            frameIndex = simTimes.indexOf(targetSimTime);
            console.log("Visualization set to SimTime:", targetSimTime);
        }
        
        function findClosestSimTime(simTimeValue) {
            return simTimes.reduce((a, b) => 
                Math.abs(b - simTimeValue) < Math.abs(a - simTimeValue) ? b : a
            );
        }

        d3.select("#resetZoom").on("click", function() {
            svg.transition()
                .duration(600)
                .call(zoom.transform, initialZoom);
            console.log("Network visualization zoom reset.");

            stateSvg.transition()
                .duration(600)
                .call(stateZoom.transform, initialStateZoom);
            console.log("State visualization zoom reset.");
        });

        d3.select("#setWidth").on("click", function() {
            const widthValue = parseInt(d3.select("#width").property("value"), 10);
            setWidth(widthValue);
            d3.select("#curValues").text(`Width = ${widthSpacing}, Height = ${heightSpacing}, Radius = ${stateRadius}`);
        });

        d3.select("#setHeight").on("click", function() {
            const heightValue = parseInt(d3.select("#height").property("value"), 10);
            setHeight(heightValue);
            d3.select("#curValues").text(`Width = ${widthSpacing}, Height = ${heightSpacing}, Radius = ${stateRadius}`);
        });

        d3.select("#setRadius").on("click", function() {
            const radiusValue = parseInt(d3.select("#radius").property("value"), 10);
            setRadius(radiusValue);
            d3.select("#curValues").text(`Width = ${widthSpacing}, Height = ${heightSpacing}, Radius = ${stateRadius}`);
        });

        d3.select("#applyLayout").on("click", function() {
            const widthValue = parseInt(d3.select("#width").property("value"), 10);
            const heightValue = parseInt(d3.select("#height").property("value"), 10);
            const radiusValue = parseInt(d3.select("#radius").property("value"), 10);
            if (widthValue) {
                setWidth(widthValue);
            }
            if (heightValue) {
                setHeight(heightValue);
            }
            if (radiusValue) {
                setRadius(radiusValue);
            }
            d3.select("#curValues").text(`Width = ${widthSpacing}, Height = ${heightSpacing}, Radius = ${stateRadius}`);
        });

        d3.select("#resetLayout").on("click", function() {
            setWidth(1000);
            setHeight(800);
            setRadius(250);
            console.log("Layout reset to default values.");
            d3.select("#curValues").text(`Width = ${widthSpacing}, Height = ${heightSpacing}, Radius = ${stateRadius}`);
        });

        function setWidth(widthValue){
            if (!isNaN(widthValue) && widthValue >= 0) {
                widthSpacing = widthValue;
                console.log("Network visualization width set to:", widthValue);
                updateVisualization(simTimes[frameIndex]);
            }
            else {
                alert("Please enter a valid width value.");
                console.log("Invalid width value entered:", widthValue);
            }
        }

        function setHeight(heightValue){
            if (!isNaN(heightValue) && heightValue >= 0) {
                heightSpacing = heightValue;
                clientY = heightSpacing * 0.375;
                accountY = heightSpacing * 0.625;
                console.log("Network visualization height set to:", heightValue);
                updateVisualization(simTimes[frameIndex]);
            }
            else {
                alert("Please enter a valid height value.");
                console.log("Invalid height value entered:", heightValue);
            }
        }

        function setRadius(radiusValue){
            if (!isNaN(radiusValue) && radiusValue >= 0) {
                stateRadius = radiusValue;
                console.log("State visualization radius set to:", radiusValue);
                updateStateVisualization(stateSimTimes[frameIndex]);
            }
            else {
                alert("Please enter a valid radius value.");
                console.log("Invalid radius value entered:", radiusValue);
            }
        }

        d3.select("#captureFrame").on("click", function() {
            console.log("Capturing current network visualization frame...");
            saveSvgAsImage('main-visualization');

            const stateElementDiplay = d3.select("#state").style("display");
            if (stateElementDiplay !== "none") {
                console.log("Capturing current state visualization frame...");
                saveSvgAsImage('state-visualization');
            }
        });

        function saveSvgAsImage(svgId) {
            const svgElement = document.getElementById(svgId);
        
            if (!svgElement) {
                console.error(`SVG element with id '${svgId}' not found.`);
                return;
            }
        
            const svgData = new XMLSerializer().serializeToString(svgElement);
            const svgBlob = new Blob([svgData], { type: "image/svg+xml;charset=utf-8" });
            const url = URL.createObjectURL(svgBlob);
        
            const canvas = document.createElement("canvas");
            canvas.width = svgElement.clientWidth;
            canvas.height = svgElement.clientHeight;
            const context = canvas.getContext("2d");
        
            const img = new Image();
            img.onload = function () {
                context.drawImage(img, 0, 0);
                URL.revokeObjectURL(url);
        
                const imgURI = canvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
        
                const downloadLink = document.createElement("a");
                downloadLink.href = imgURI;
                downloadLink.download = "network-visualization.png";
                document.body.appendChild(downloadLink);
                downloadLink.click();
                document.body.removeChild(downloadLink);
            };
        
            img.src = url;
            if (svgId === 'main-visualization') console.log("Network visualization frame captured and saved as image.");
            if (svgId === 'state-visualization') console.log("State visualization frame captured and saved as image.");
        }

        // ==============*** Initialization ***==============

        svg.append("defs").append("marker")
            .attr("id", "arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 5)
            .attr("refY", 0)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr("fill", "black");

        step();

        updateVisualization(simTimes[frameIndex]);
        updateStateVisualization(stateSimTimes[frameIndex]);

    }).catch(function(error) {
        console.error("Error loading JSON data:", error);
        console.log("Error loading JSON data from", framesFile, ":", error);
    });
}


let defaultFramesFile = "frames.json";
// let defaultFramesFile = "frames/2024.11.27 14.58.17.json";

loadVisualization(defaultFramesFile);

function fetchArchiveFolders() {
    console.log('Fetching archive folders...');
    fetch('http://127.0.0.1:5000/list-folders', {
        method: 'GET',
        mode: 'cors',
        headers: {
            'Content-Type': 'application/json',
        },
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(folderNames => {
        const archiveList = document.querySelector('.archive-list');
        archiveList.innerHTML = '';

        folderNames.forEach(folderName => {
            const archiveElement = document.createElement('div');
            archiveElement.className = 'archive-item';
            archiveElement.textContent = folderName;

            const uniqueID = `archive-${folderName}`;
            archiveElement.id = uniqueID;

            archiveElement.addEventListener('click', () => {
                svg.selectAll("*").remove();
                const framePath = `frames/${folderName}.json`;
                console.log('Archive selected:', folderName);
                loadVisualization(framePath);
            });

            archiveList.appendChild(archiveElement);
        });

        console.log('Archive folders fetched successfully.');
    })
    .catch(error => {
        console.error('Error fetching folder names:', error);
        alert('There was a problem fetching the archive folders.');
    });
}

document.addEventListener('DOMContentLoaded', function() {
    fetchArchiveFolders();
});