document.addEventListener("DOMContentLoaded", function () {
    const menuItem = document.getElementById("networkSetup");
    const runDriverButton = document.getElementById("runDriver");
    const generateFramesButton = document.getElementById("generateFrames");
    const refreshVisualizationButton = document.getElementById("refreshVisualization");
    const captureFrameButton = document.getElementById("captureFrame");
    const showConsoleButton = document.getElementById("showConsole");
    const closeConsoleButton = document.getElementById("closeConsole");
    const archiveButton = document.getElementById("archive");

    const toggleStateVisualizationButton = document.getElementById("toggleStateVisualization");
    const stateVisualization = document.getElementById("state");

    menuItem.addEventListener("click", function (event) {
        if (!event.target.classList.contains('sub-menu-item')) {
            toggleSubmenu(this);
        }
    });

    const submenuItems = document.querySelectorAll(".sub-menu-item");
    submenuItems.forEach((item) => {
        item.addEventListener("click", function (event) {
            event.stopPropagation();
            console.log("Submenu item clicked:", this.innerText);

            // POST request to run setup scripts
            if (this.innerText.includes("Clients Setup")) {
                sendClientSetupRequest();
            }
            if (this.innerText.includes("Nodes Setup")) {
                sendNodesSetupRequest();
            }
            if (this.innerText.includes("Full Configuration")) {
                sendFullConfigRequest();
            }
        });
    });

    toggleStateVisualizationButton.addEventListener("click", function () {
        const toggleStateIcon = document.getElementById("toggleStateIcon");
        const toggleStateText = document.getElementById("toggleStateText");

        if (stateVisualizationToggled) {
            stateVisualizationToggled = false;
            stateVisualization.style.display = "none";
            toggleStateText.innerText = "Show State Visualization"
            toggleStateIcon.classList.remove("fa-eye-slash");
            toggleStateIcon.classList.add("fa-eye");
            console.log("State visualization hidden.");
        }
        else {
            stateVisualizationToggled = true;
            stateVisualization.style.display = "flex";
            toggleStateText.innerText = "Hide State Visualization"
            toggleStateIcon.classList.remove("fa-eye");
            toggleStateIcon.classList.add("fa-eye-slash");
            console.log("State visualization shown.");
        }
    });

    runDriverButton.addEventListener("click", function () {
        sendCNSimDriverRequest();
    });

    generateFramesButton.addEventListener("click", function () {
        sendGenerateFramesRequest();
    });

    refreshVisualizationButton.addEventListener("click", function () {
        window.reloadFrames();
    });

    // Console logs
    const consoleLogDiv = document.getElementById("consoleLogs");
    const originalConsoleLog = console.log;

    console.log = function(...args) {
        originalConsoleLog.apply(console, args);

        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        
        const timestamp = `[${hours}:${minutes}.${seconds}]`;
        const message = `${timestamp} ${args.map(arg => (typeof arg === "object" ? JSON.stringify(arg) : arg)).join(" ")}`;

        const logEntry = document.createElement("div");
        logEntry.className = "log-entry";
        logEntry.textContent = message;
        consoleLogDiv.appendChild(logEntry);

        consoleLogDiv.scrollTop = consoleLogDiv.scrollHeight;
    };

    showConsoleButton.addEventListener("click", function () {
        if (!consoleLogsShown) {
            const visualizationSettings = document.getElementById("visualizationSettings");
            visualizationSettings.style.scale = "0.8";
            const consolePanel = document.getElementById("consolePanel");
            consolePanel.style.display = "flex";
            setTimeout(() => {
                consolePanel.style.opacity = "1";
                consolePanel.style.transform = "translate(0%, 0%)";
            }, 10);
            const showConsoleText = document.getElementById("showConsoleText");
            showConsoleText.innerText = "Hide Console Logs";
            consoleLogsShown = true;
            console.log("Console logs shown.");
        } else {
            const visualizationSettings = document.getElementById("visualizationSettings");
            const consolePanel = document.getElementById("consolePanel");
            consolePanel.style.opacity = "0";
            consolePanel.style.transform = "translate(10%, 0%)";
            setTimeout(() => {
                consolePanel.style.display = "none";
                visualizationSettings.style.scale = "1";
            }, 200);
            const showConsoleText = document.getElementById("showConsoleText");
            showConsoleText.innerText = "Show Console Logs";
            consoleLogsShown = false;
            console.log("Console logs hidden.");
        }
    });

    closeConsoleButton.addEventListener("click", function () {
        const visualizationSettings = document.getElementById("visualizationSettings");
        const consolePanel = document.getElementById("consolePanel");
        consolePanel.style.opacity = "0";
        consolePanel.style.transform = "translate(10%, 0%)";
        setTimeout(() => {
            consolePanel.style.display = "none";
            visualizationSettings.style.scale = "1";
        }, 200);
        const showConsoleText = document.getElementById("showConsoleText");
        showConsoleText.innerText = "Show Console Logs";
        consoleLogsShown = false;
        console.log("Console logs hidden.");
    });

    // Layout configuration
    const closeLayoutButton = document.getElementById("closeLayout");
    const configLayoutButton = document.getElementById("configLayout");

    configLayoutButton.addEventListener("click", function () {
        if (layoutConfigShown) {
            closeLayoutWindow();
        }
        else {
            if (archiveShown) { 
                closeArchiveWindow(); 
            }
            setTimeout(() => {
                const layout = document.getElementsByClassName("layout-window")[0];
                layout.style.display = "flex";
                setTimeout(() => {
                    layout.style.opacity = "1";
                    layout.style.transform = "translate(-50%, -50%)";
                }, 10);
                layoutConfigShown = true;
                console.log("Layout configuration window opened.");
            }, 300);
        }
    });

    closeLayoutButton.addEventListener("click", function () {
        closeLayoutWindow();
    });

    function closeLayoutWindow() {
        const layout = document.getElementsByClassName("layout-window")[0];
        layout.style.opacity = "0";
        layout.style.transform = "translate(-50%, -45%)";
        setTimeout(() => {
            layout.style.display = "none";
        }, 400);
        const widthTextbox = document.getElementById("width");
        const heightTextbox = document.getElementById("height");
        const radiusTextbox = document.getElementById("radius");
        widthTextbox.value = "";
        heightTextbox.value = "";
        radiusTextbox.value = "";
        layoutConfigShown = false;
        console.log("Layout configuration window closed.");
    }

    const viewLayoutButton = document.getElementById("viewLayout");

    viewLayoutButton.addEventListener("mouseover", function () {
        const layout = document.getElementsByClassName("layout-window")[0];
        layout.style.opacity = "0.3";
    });

    viewLayoutButton.addEventListener("mouseout", function () {
        const layout = document.getElementsByClassName("layout-window")[0];
        layout.style.opacity = "1";
    });

    const goToSimtimeButton = document.getElementById("goToSimtime");
    goToSimtimeButton.addEventListener("click", function () {
        const simTimeInput = document.getElementById("simTime");
        setTimeout(() => {
            simTimeInput.value = "";
        }, 10);
    });

    archiveButton.addEventListener("click", function () {
        if (archiveShown) {
            closeArchiveWindow();
        }
        else {
            if (layoutConfigShown) {
                closeLayoutWindow();
            }
            setTimeout (() => {
                const archive = document.getElementsByClassName("archive-window")[0];
                archive.style.display = "flex";
                setTimeout(() => {
                    archive.style.opacity = "1";
                    archive.style.transform = "translate(-50%, -50%)";
                }, 10);
                archiveShown = true;
            }, 300);
        }
    });

    const closeArchiveButton = document.getElementById("closeArchive");

    closeArchiveButton.addEventListener("click", function () {
        closeArchiveWindow();
    });

    function closeArchiveWindow() {
        const archive = document.getElementsByClassName("archive-window")[0];
        archive.style.opacity = "0";
        archive.style.transform = "translate(-50%, -45%)";
        setTimeout(() => {
            archive.style.display = "none";
        }, 400);
        archiveShown = false;
    }

    // fetchArchiveFolders();

});

let stateVisualizationToggled = false;
let consoleLogsShown = false;
let layoutConfigShown = false;
let archiveShown = false;

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

        folderNames.sort((a, b) => a.localeCompare(b));

        folderNames.forEach(folderName => {
            const archiveElement = document.createElement('div');
            archiveElement.className = 'archive-item';
            archiveElement.textContent = folderName;

            archiveList.appendChild(archiveElement);
        });

        console.log('Archive folders fetched and sorted successfully.');
    })
    .catch(error => {
        console.error('Error fetching folder names:', error);
        alert('There was a problem fetching the archive folders.');
    });
}


function toggleSubmenu(menuItem) {
    const submenu = menuItem.querySelector(".sub-menu");
    const arrow = menuItem.querySelector(".arrowhead-icon");
    const submenuItems = submenu.querySelectorAll('.sub-menu-item');

    if (submenu.style.display === "none" || submenu.style.display === "") {
        submenu.style.display = "block";
        arrow.style.transform = "rotate(180deg)";

        submenuItems.forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            item.style.transition = `opacity 0.5s ease ${index * 0.1}s, transform 0.5s ease ${index * 0.1}s`;
            setTimeout(() => {
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, 10);
        });

        console.log("Network configuration submenu opened.");

    } else {
        submenuItems.forEach((item, index) => {
            item.style.transition = `opacity 0.4s ease ${index * 0.05}s, transform 0.4s ease ${index * 0.05}s`;
            item.style.opacity = '0';
            item.style.transform = 'translateY(-10px)';
            arrow.style.transform = "rotate(0deg)";

            if (index === submenuItems.length - 1) {
                setTimeout(() => {
                    submenu.style.display = "none";
                }, 400 + index * 100);
            }
        });

        console.log("Network configuration submenu closed.");
    }
}

function sendClientSetupRequest() {
    console.log('Client setup request initiated.');
    fetch('http://127.0.0.1:5000/run_setup_clients', {
        method: 'POST',
        mode: 'cors',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: 'setupClients' })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Client setup initiated:', data);
        alert('Client setup launched successfully.');
    })
    .catch(error => {
        console.error('Error with client setup request:', error);
        alert('There was a problem with the client setup request.');
    });
}

function sendNodesSetupRequest() {
    console.log('Nodes setup request initiated.');
    fetch('http://127.0.0.1:5000/run_setup_nodes', {
        method: 'POST',
        mode: 'cors',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: 'setupNodes' })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Nodes setup initiated:', data);
        alert('Nodes setup launched successfully.');
    })
    .catch(error => {
        console.error('Error with nodes setup request:', error);
        alert('There was a problem with the nodes setup request.');
    });
}

function sendFullConfigRequest() {
    console.log('Full configuration request initiated.');
    fetch('http://127.0.0.1:5000/run_full_config', {
        method: 'POST',
        mode: 'cors',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: 'setupFullConfig' })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Full configuration script initiated:', data);
        alert('Full configuration script launched successfully.');
    })
    .catch(error => {
        console.error('Error with full configuration request:', error);
        alert('There was a problem with the full configuration request.');
    });
}

function sendCNSimDriverRequest() {
    console.log('CNSim Driver request initiated.');
    fetch('http://127.0.0.1:5000/run_cnsim_driver', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: 'runCNSim' })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('CNSim Driver initiated:', data);
        alert('CNSim Driver launched successfully.');
    })
    .catch(error => {
        console.error('Error with CNSim Driver request:', error);
        console.log('Error with CNSim Driver request:', error);
        alert('There was a problem with the CNSim Driver request.');
    });
}

// function sendConfigSetupRequest() {
//     console.log('Configuration setup request initiated.');
//     fetch('http://127.0.0.1:5000/run_setup_config', {
//         method: 'POST',
//         mode: 'cors',
//         headers: {
//             'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({ action: 'setupConfig' })
//     })
//     .then(response => {
//         if (response.ok) {
//             return response.json();
//         } else {
//             throw new Error('Network response was not ok');
//         }
//     })
//     .then(data => {
//         console.log('Configuration setup initiated:', data);
//         alert('Configuration setup launched successfully.');
//     })
//     .catch(error => {
//         console.error('Error with configuration setup request:', error);
//         alert('There was a problem with the configuration setup request.');
//     });
// }

function sendGenerateFramesRequest() {
    console.log('Frame generation request initiated.');
    fetch('http://127.0.0.1:5000/run_generate_frames', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: 'generateFrames' })
    })
    .then(response => {
        if (response.ok) {
            return response.json(); // Assuming the response is JSON
        } else {
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Frames generation initiated:', data);
        alert('Frames generated successfully.');
    })
    .catch(error => {
        console.error('Error with frames generation request:', error);
        console.log('Error with frames generation request:', error);
        alert('There was a problem with the frames generation request.');
    });
}
