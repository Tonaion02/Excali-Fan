// T: this will become the synced list
let listLines = []
// T: this is something similar to the forward list,
// in that we can cache the current operation.
// This is useful when a user is drawing a line during
// an update.
let defaultColor = "white";
let currentLine = {color: defaultColor,  userId: null, timestamp: null, points: []}
let currentColor = defaultColor;

const canvas = document.getElementById("drawingCanvas");
const canvasContext = canvas.getContext("2d");
// Settings of canvas (START)
// T: WARNING remember to set a fixed size for the
// canvas
canvasContext.canvas.width  = 2000;
canvasContext.canvas.height = 2000;
canvasContext.canvas.style.backgroundColor = "#121212";
// Settings of canvas (END)

// T: data that must be maintained
// T: WARNING probably name and this data are OUTDATED, review this part and the use of this data
const data = {
    username: '',
    userId: '',
    groupId: '',
    currentBoardStorageId: null
}

let boardStorageIdsConst = [];


const endPointForCreateLine = `/api/createLine`;
const endPointForDeleteLine = `/api/deleteLine`;



let isDrawing = false;
let isDeleting = false;
let isDoingAction = false;




const tollerance = 5;


// T: Debug visualization to display points of 
let debugVisualizePointsOfLine = false;
function debugDrawPointsOfLine(line, ctx) {

    if(!debugVisualizePointsOfLine)
        return;

    // T: TODO substitute with current color
    let precColor = ctx.strokeStyle;
    console.log("precColor: " + precColor);
    ctx.strokeStyle = "red";

    for(point in line.points) {
        point = line.points[point];

        ctx.beginPath();
        ctx.arc(point.first, point.second, 2, 0, 2*Math.PI);
        ctx.stroke();    
    }

    ctx.strokeStyle = currentColor;
}

// T: This function draw a line
function drawLine(line, ctx) {

    let first = true;
    let lx, ly;

    ctx.strokeStyle = line.color;

    for(point in line.points) {

        point = line.points[point]

        if(first) {
            first = false;
            lx = point.first;
            ly = point.second;

            ctx.beginPath();
        }
        else {
            ctx.moveTo(lx, ly);
            ctx.lineTo(point.first, point.second);

            lx = point.first;
            ly = point.second;
        }
    }

    // T: is important for performance reason to put a single stroke for line
    // T: probably the stroke is used to create vertex information, so if we
    // abuse this function, we don't batch data correctly.
    // T: WARNING we need this check because otherwise the ctx mantain the last
    // path in cache and try to draw it even if the line is empty.
    if(line.points.length > 0)
        ctx.stroke();


    debugDrawPointsOfLine(line, ctx);
}

// T: This function is used to update the screen
// when a new command is received
function update(ctx) {

    // T: clear the canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height)    
    
    // T: redraw every lines from the synced list
    for(let lineIndex in listLines) {
        let line = listLines[lineIndex]
        drawLine(line, ctx)
    }

    // T: redraw the current line that you are drawing, toretrieve the current state 
    // of the line drawn until now from the forward
    // T: we redraw the currentLine only in the case we aren't deleting a line
    if(!isDeleting)
        drawLine(currentLine, ctx)
}







function setup() {    

    // T: retrieve the HTML element that represent the cursor in canvas
    const cursor = document.getElementById("circleCursor");

    // T: set some properties of the cursor (START)
    cursor.style.display = "block";
    cursor.style.width = `${tollerance * 2}px`;
    cursor.style.height = `${tollerance * 2}px`;

    // T: set the content of GroupLabel
    const currentGroupLabel = document.getElementById('current-group-label');
    currentGroupLabel.textContent = `GroupID corrente: ${data.groupId}`;
    // T: set some properties of the cursor (END)





    if (canvas.getContext) {

        const ctx = canvas.getContext("2d");


        let lastX = 0;
        let lastY = 0;

        // Settings of the pen (START)
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.strokeStyle = currentColor;
        // Settings of the pen (END)



        // Set EventListener for toolbar (START)
        const pencilContainer = document.getElementById('pencil-container');
        const eraserContainer = document.getElementById('eraser-container');
        const colorDropdown = document.getElementById('color-dropdown');

        pencilContainer.addEventListener("click", () => {
            console.log("pencil clicked");
            isDrawing = true;
            isDeleting = false;
        });

        eraserContainer.addEventListener("click", () => {
            console.log("eraser clicked");
            isDeleting = true;
            isDrawing = false;
        });
        // Set EventListener for toolbar (END)



        // T: Set like default boardStorageId the groupId (START)
        data.currentBoardStorageId = data.groupId;
        const boardStorageIdTextBox = document.getElementById("file-name");
        boardStorageIdTextBox.value = data.currentBoardStorageId;
        // T: Set like default boardStorageId the groupId (END)



        // T: Retrieve the list of boards (START) 
        let accessToken = retrieveToken();
        axios.post("https://rest-service-1735827345127.azurewebsites.net/api/listBoards", {}, {
            headers: {
              "Authorization": accessToken,
              "Content-Type": "application/json"
            }
          })
        .then(response => {
          console.log("Risposta:", response.status);
          boardStorageIdsConst = response.data;
          setupLoadBoardWindow();              
        })
        .catch(error => {
          console.error("Errore:", error);
        });
        // T: Retrieve the list of boards (END)



        // Set EventListener for mouse down (START)
        canvas.addEventListener('mousedown', 
            (e) => {
                // T: if you are deleting the contents of the board, ignore the mousedown
                isDoingAction = true;

                [lastX, lastY] = [e.offsetX, e.offsetY];
                
                ctx.beginPath();
                ctx.strokeStyle = currentColor;

                // Clear the current line and add the first point of the line
                currentLine = {color: currentColor, userId: data.userId, timestamp: null, points: []};

                currentLine.points.push({first: e.offsetX, second: e.offsetY});
            }
        );
        // Set EventListener for mouse down (END)

        // Set EventListener for mouse move (START)        
        canvas.addEventListener('mousemove',
            (e) => {
                
                if(isDoingAction) {
                    if(isDrawing) {
                        ctx.moveTo(lastX, lastY);
                        ctx.lineTo(e.offsetX, e.offsetY);
                        ctx.stroke();
                        [lastX, lastY] = [e.offsetX, e.offsetY];
        
                        currentLine.points.push({first: e.offsetX, second: e.offsetY});
                    } 
                    else if(isDeleting) {
                        
                        // T: check if the position of mouse is a point "around" a line
                        let result = isPointInLines({x: e.offsetX, y: e.offsetY}, listLines, tollerance);
                        let lineToDelete = result.lineToReturn;
                        let indexLineToDelete = result.indexLineToReturn;
                        let isOnCurrentLine = result.isPointOnCurrentLine;
                        
                        if(lineToDelete != null) {
                            // T: local deleting (START)
                            deleteLineFromListWithIndex(listLines, indexLineToDelete);
    
                            if(isOnCurrentLine)
                                currentLine = {color: currentColor,  userId: data.userId, timestamp: null, points: []};
                            // T: local deleting (END)
    
                            // T: remote deleting
                            sendDeleteLine(lineToDelete);
    
                            update(ctx);
                        }
                    }
                }
            }
        );
        // Set EventListener for mouse move (END)
        
        // Set EventListener for mouse up (START)
        canvas.addEventListener('mouseup', 
            () => {
                if (isDrawing && isDoingAction) {
                    // T: commented because we add the line to the listLines
                    // only when we receive the lines from a newMessage
                    // listLines.push(currentLine);

                    let timestamp = Date.now();

                    currentLine.timestamp = timestamp;
                    console.log(currentLine.timestamp);

                    let accessToken = retrieveToken();

                    axios.post(endPointForCreateLine, {
                      userId: data.userId,
                      groupId: data.groupId,
                      timestamp: timestamp,
                      line: currentLine,
                    },
                    {
                        headers: {
                            "Authorization": accessToken,
                            "Content-Type": "application/json",
                        }
                    }).then(resp => resp.data)
                }

                isDoingAction = false;
            }

            
        );
        // Set EventListener for mouse up (END)

        // Set EventListener for mouse that goes out of the canvas (START)
        // T: TODO to handle the cursor out of the window, send the line and put isDointAction to false
        canvas.addEventListener('mouseleave', () => {

        });
        // Set EventListener for mouse that goes out of the canvas (END)


    


        function receiveCreateLine(command) {
            console.log("receiveCreateLine is called");
            console.log(command.line)
            
            listLines.push(command.line)
            update(ctx)
        }

        // T: we don't perform update in this function, because is performed in deleteLineFromList
        // and we don't want to make multiple update contemporary (this can lead to some bugs) 
        function receiveDeleteLine(command) {
            console.log("receiveDeleteLine is called");
            
            // T: check if the line to delete is equal to the currentLine, in that case delete it
            // T: WARNING: it's necessary to clean first the currentLine because we call only an update in
            // deleteLineFromList
            if(currentLine.userId == command.userIdOfLine && currentLine.timestamp == command.timestampOfLine) {
                currentLine = {color: currentColor,  userId: data.userId, timestamp: null, points: []};
            }

            // T: remove the line from the listLines
            deleteLineFromList(listLines, command.userIdOfLine, command.timestampOfLine);
        }

        function sendDeleteLine(lineToDelete) {
            let timestamp = Date.now();
            
            let accessToken = retrieveToken();

            axios.post(endPointForDeleteLine, 
            {
                userId: data.userId,
                groupId: data.groupId,
                timestamp: timestamp,
                userIdOfLine: lineToDelete.userId,
                timestampOfLine: lineToDelete.timestamp,
            },
            {
                headers: {
                    "Authorization": accessToken,
                    "Content-Type": "application/json",
                }
            }
            ).then(resp => resp.data)
        }

        function deleteLineFromList(lines, userIdLineToDelete, timestampLineToDelete) {
            let indexLineToDelete = -1;
            
            for(let indexLine in lines) {
                let line = lines[indexLine];

                if(line.userId == userIdLineToDelete && line.timestamp == timestampLineToDelete) {
                    indexLineToDelete = indexLine;
                    break;
                }
            }

            if(indexLineToDelete >= 0) {
                lines.splice(indexLineToDelete, 1);
                update(ctx);            
            }
        }

        function deleteLineFromListWithIndex(lines, indexLine) {
            if(indexLine >= 0) {
                lines.splice(indexLine, 1);
            }
        }





        // T: Create connection to signalR (START)
        const connection = new signalR.HubConnectionBuilder()
        // .withUrl(`${apiBaseUrl}/signalr`)
        .withUrl(`/signalr?userId=` + data.userId)
        .withAutomaticReconnect()
        .configureLogging(signalR.LogLevel.Information)
        .build()
        // T: Create connection to signalR (END)

        // T: Set the listener to the receiveing message (START)
        connection.on('receiveCreateLine', receiveCreateLine);
        connection.on('receiveDeleteLine', receiveDeleteLine);
        // T: Set the listener to the receiveing message (END)

        // T: Started connection with Azure SignalR(START)
        connection.start()
        .then(() => 
            {
                console.log("Started connection")
                console.log("data.userId: " + data.userId)
                console.log("data.groupId: " + data.groupId)

                let accessToken = retrieveToken();

                // T: autojoin the group (START)
                fetch("https://rest-service-1735827345127.azurewebsites.net/api/addgroup?groupId=" + data.groupId + "&userId=" + data.userId,
                    {
                        method: "GET",
                        headers: {
                            "Authorization": accessToken,
                            "Content-Type": "application/json",
                        }
                    }        
                  ).
                then((response) => console.log("adding to group during login: " + response.status))
                // T: autojoin the group (END)
            })
        .catch(console.error)
        // T: Started connection with Azure SignalR(END)
    }
}

function isPointInLine(point, line, tollerance) {

    // T: Handle special case in which we have a single point (START)
    // if(line.points.length == 1)
    // {
    //     let distance = Math.sqrt((line.points[0].first - point.first) ** 2 + (line.potins[0].second - point.second) ** 2);
    //     if(distance < tollerance)
    //         return true
    // }
    // T: Handle special case in which we have a single point (END)



    precPoint = line.points[0]
    for(let indexPoint = 1; indexPoint < line.points.length; indexPoint++) {
        currentPoint = line.points[indexPoint];
        
        distance = pointToSegmentDistance(point, {x: precPoint.first, y: precPoint.second}, {x: currentPoint.first, y: precPoint.second});
        if(distance < tollerance) {
            return true;
        }

        precPoint = currentPoint;
    }

    return false;
}

function isPointInLines(point, lines, tollerance) {

    let lineToReturn = null;
    let indexLineToReturn = -1;

    for(indexLine in lines) {
        let line = lines[indexLine];

        if(isPointInLine(point, line, tollerance)) {
            console.log("point in line: " + line);

            line.color = "red";

            lineToReturn = line;
            indexLineToReturn = indexLine;
            break;
        }
    }

    let isPointOnCurrentLine = false;
    if(isPointInLine(point, currentLine, tollerance)) {
        console.log("point in currentline: " + currentLine);

        currentLine.color = "red";

        isPointOnCurrentLine = true;
        // T: find a way to delete the currentLine
    }

    return {lineToReturn: lineToReturn, indexLineToReturn: indexLineToReturn, isPointOnCurrentLine: isPointOnCurrentLine};
}



// T: function to compute the distance beetween a point:p and the segment 
// between two points v and w
function pointToSegmentDistance(p, v, w) {
    const l2 = (v.x - w.x) ** 2 + (v.y - w.y) ** 2;
    if (l2 === 0) return Math.hypot(p.x - v.x, p.y - v.y);
    let t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
    t = Math.max(0, Math.min(1, t));
    return Math.hypot(p.x - (v.x + t * (w.x - v.x)), p.y - (v.y + t * (w.y - v.y)));
}



function moveCursor(position, cursor) {

    let x = position.x - parseInt(tollerance) / 2;
    let y = position.y + parseInt(tollerance) / 2;

    cursor.style.left = `${x}px`;
    cursor.style.top = `${y}px`;
}




function addToGroup() {

    const currentGroupLabel = document.getElementById('current-group-label');
    const groupId = document.getElementById('group-name').value;
    
    data.groupId = groupId
    currentGroupLabel.textContent = `GroupID corrente: ${groupId}`;

    let accessToken = retrieveToken();

    fetch("https://rest-service-1735827345127.azurewebsites.net/api/addgroup?groupId=" + groupId + "&userId=" + data.userId,
        {
            method: "GET",
            headers: {
                "Authorization": accessToken,
                "Content-Type": "application/json",
            }
        }        
    ).
    then((response) => console.log("adding to group: " + response.status))
}



// T: Load Board (START)
// T: This function permits the loading of the board in the client
// and trigger the loading on the server of the board 
function loadBoard(boardId) {

    // Clear the current line and add the first point of the line
    currentLine = {color: currentColor, userId: data.userId, timestamp: null, points: []};


    let accessToken = retrieveToken();
    let email = extractEmailFromToken(accessToken);

    axios.post("https://rest-service-1735827345127.azurewebsites.net/api/loadBoard", { "blobName": boardId, "email": email}, 
    {
        headers: {
            "Authorization": accessToken,
            "Content-Type": "application/json"
        }
    })
    .then(response => {
        if(response != null) {
            console.log("Reponse for the load of the Board");
            console.log(response);
    
            let board = JSON.parse(response.data.boardJson);
            listLines = board.lines;
    
            console.log(response.data.boardJson);
    
            data.groupId = response.data.boardSessionId;
            
            console.log("UIIIIIIIIIIII");
    
            data.currentBoardStorageId = boardId;

            const boardStorageIdTextBox = document.getElementById("file-name");
            boardStorageIdTextBox.value = data.currentBoardStorageId;
    
            setupLoadBoardWindow();
            
            update(canvasContext);   
        }
     
    })
    .catch(error => {
        console.error("Errore:", error);
    });
}
// T: Load Board (END)



// T: Save Board on Cloud (START)
// T: This function permits to save a bord in cloud
function saveOnCloud(boardSessionId, boardName)
{
    let accessToken = retrieveToken();
    let email = extractEmailFromToken(accessToken);

    axios.post("https://rest-service-1735827345127.azurewebsites.net/api/saveBoard", 
        { 
            "blobName": boardName, 
            "email": email,
            "boardSessionId": boardSessionId,
            "precBoardStorageId": data.currentBoardStorageId,
        }, 
        {
            headers: {
                "Authorization": accessToken,
                "Content-Type": "application/json"
            }
        })
        .then(response => {
            console.log("Response status: " + response.status);

            if(boardName !== data.currentBoardStorageId) {

                // T: Update the list of boardStorageIds (START)
                // T: Update the list of boardStorageIds with the new file name
                for(let boardStorageIdIndex in boardStorageIdsConst) {
                    let boardStorageId = boardStorageIdsConst[boardStorageIdIndex];

                    if(boardStorageId === data.currentBoardStorageId) {
                        boardStorageIdsConst[boardStorageIdIndex] = boardName;
                    }
                }
                // T: Update the list of boardStorageIds (END)

                // T: Update the current boardStorageId because it changed
                data.currentBoardStorageId = boardName;

                setupLoadBoardWindow();
            }
            else {
                // T: update the list of boardStorageIds (START)
                let found = false;
                for(let boardStorageIdIndex in boardStorageIdsConst) {
                    let boardStorageId = boardStorageIdsConst[boardStorageIdIndex];

                    if(boardStorageId === boardName) {
                         found = true;
                    }
                }

                if(found == true) {
                    boardStorageIdsConst.push(boardName);
                }
                // T: update the list of boardStorageIds (END)
            }
                
        })
        .catch(error => {
            console.error("Errore:", error);
        });
}
// T: Save Board on Cloud (END)



// T: setup the window to load the board (START)
function setupLoadBoardWindow() {

    const lodeBoardDropdown = document.getElementById('lode-board-dropdown');
    // T: WARNING really inefficient way to update the content
    // of this dropdown menu
    lodeBoardDropdown.innerHTML = "";



    boardStorageIdsConst.forEach(name => {

        if(name !== data.currentBoardStorageId)
        {
            const wrapper = document.createElement('div');
            wrapper.style.display = 'flex';
            wrapper.style.alignItems = 'center';
    
            const colorDiv = document.createElement('div');
            colorDiv.style.width = '4px';
            colorDiv.style.height = '17px';
            colorDiv.style.backgroundColor = '#8a87ff';
            colorDiv.style.marginRight = '5px';
            colorDiv.style.borderRadius = '4px';
    
            // Create the download button
            const downloadButton = document.createElement('button');
            downloadButton.style.backgroundColor = 'transparent';
            downloadButton.style.border = 'none';
            downloadButton.style.cursor = 'pointer';
            downloadButton.style.marginRight = '10px';
            downloadButton.style.width = '20px'; // Add some space between the buttons
    
            const downloadIcon = document.createElement('img');
            downloadIcon.src = 'logo-dw.png';
            downloadIcon.alt = 'Download';
            downloadIcon.style.height = '20px'; // Adjust the size as needed
    
            downloadButton.appendChild(downloadIcon);
    
            downloadButton.addEventListener('click', () => {
                alert(`Download ${name}`);
            });
    
            const button = document.createElement('button');
            button.textContent = name;
            button.style.flexGrow = '1'; 
            button.style.whiteSpace = 'nowrap';
            button.addEventListener('click', () => {
                loadBoard(name);
            });
    
            wrapper.appendChild(colorDiv);
            wrapper.appendChild(downloadButton); // Add the download button to the wrapper
            wrapper.appendChild(button);
            lodeBoardDropdown.appendChild(wrapper);
        }
    });
}
// T: setup the window to load the board (END)



// T: disabilitate the context menu
document.addEventListener("contextmenu", function(event) {
    event.preventDefault();
});

let saveOnCloudButton = document.getElementById("save-option-cloud-button");
saveOnCloudButton.addEventListener("click", () => {
    let fileNameTextBox = document.getElementById("file-name");
    let fileName = fileNameTextBox.value; 

    saveOnCloud(data.groupId, fileName);

    let windowFileManager = document.getElementById("save-modal");
    windowFileManager.style.display = "none";
});

let loginButton = document.getElementById("login")
loginButton.addEventListener('click', login)

let joinGroupButton = document.getElementById("add-group-button")
joinGroupButton.addEventListener('click', addToGroup)

console.log("You can start to draw")