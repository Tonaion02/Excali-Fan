// T: this will become the synced list
let listLines = []
// T: this is something similar to the forward list,
// in that we can cache the current operation.
// This is useful when a user is drawing a line during
// an update.
let currentLine = {color: "black",  userId: null, timestamp: null, points: []}

const canvas = document.getElementById("canvas");

// T: data that must be stored
const data = {
    username: '',
    userId: '',
    groupId: '',
}



const endPointForCreateLine = `/api/createLine`;
const endPointForDeleteLine = `/api/deleteLine`;





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
    ctx.stroke();
}

// T: This function is used to update the screen
// when a new command is received
function update(ctx) {

    // T: clear the canva
    ctx.clearRect(0, 0, canvas.width, canvas.height)    
    
    // T: redraw every lines from the synced list
    for(lineIndex in listLines) {
        let line = listLines[lineIndex]
        drawLine(line, ctx)
    }

    // T: retrieve the current state of the line drawn until now from the forward
    drawLine(currentLine, ctx)
    // isDrawing = true
}



function draw() {

    if (canvas.getContext) {
        const ctx = canvas.getContext("2d");

        ctx.canvas.width  = window.innerWidth;
        ctx.canvas.height = window.innerHeight;
  
        let isDrawing = false;
        let isDeleting = false;

        let lastX = 0;
        let lastY = 0;

        // Settings of the pen (START)
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.strokeStyle = 'black';
        // Settings of the pen (END)

        // Set EventListener for mouse down (START)
        canvas.addEventListener('mousedown', 
            (e) => {
                // T: if you are deleting the contents of the board, ignore the mousedown
                if(isDeleting)
                    return;

                isDrawing = true;
                [lastX, lastY] = [e.offsetX, e.offsetY];
                
                ctx.beginPath();
                ctx.strokeStyle = "black";

                // Clear the current line and add the first point of the line
                currentLine = {color: "black", userId: data.userId, timestamp: null, points: []};

                currentLine.points.push({first: e.offsetX, second: e.offsetY});
            }
        );
        // Set EventListener for mouse down (END)

        // Set EventListener for mouse move (START)        
        canvas.addEventListener('mousemove',
            (e) => {
                if(isDrawing) {
                    ctx.moveTo(lastX, lastY);
                    ctx.lineTo(e.offsetX, e.offsetY);
                    ctx.stroke();
                    [lastX, lastY] = [e.offsetX, e.offsetY];
    
                    currentLine.points.push({first: e.offsetX, second: e.offsetY});
                } 
                else if(isDeleting) {
                    // T: check if the position of mouse is a point "around" a line
                    let lineToReturn = isPointInLines({x: e.offsetX, y: e.offsetY}, listLines, 1);
                    if(lineToReturn != null) {

                        // T: TODO local deleting
                        
                        sendDeleteLine(lineToReturn);

                        update(ctx);
                    }
                }
            }
        );
        // Set EventListener for mouse move (END)
        
        // Set EventListener for mouse up (START)
        canvas.addEventListener('mouseup', 
            () => {
                if (isDrawing) {
                    // T: commented because we add the line to the listLines
                    // only when we receive the lines from a newMessage
                    // listLines.push(currentLine);

                    let timestamp = Date.now();

                    currentLine.timestamp = timestamp;

                    axios.post(endPointForCreateLine, {
                      userId: data.userId,
                      groupId: data.groupId,
                      timestamp: timestamp,
                      line: currentLine,
                    }).then(resp => resp.data)
                }

                isDrawing = false;
            }
        );
        // Set EventListener for mouse up (END)

        // Set EventListener for mouse that goes out of the canvas (START)
        canvas.addEventListener('mouseleave', () => {
            isDrawing = false;
            isDeleting = false;
        });
        // Set EventListener for mouse that goes out of the canvas (END)

        // T: Set EventListeners for the keys (START)
        window.addEventListener('keydown', (event) => {
            if(event.code == "KeyD") {
                isDeleting = true;
            }
        });

        window.addEventListener("keyup", (event) => {
            if(event.code == "KeyD") {
                isDeleting = false;
            }
        })
        // T: Set EventListeners for the keys (END)


    
        // function newMessage(message) {
        //     console.log("newMessage is called");
        //     console.log(message.line.points)
            
        //     listLines.push(message.line)
        //     update(ctx)            
        // }

        function receiveCreateLine(command) {
            console.log("receiveCreateLine is called");
            console.log(command.line.points)
            
            listLines.push(command.line)
            update(ctx)
        }

        function receiveDeleteLine(command) {
            console.log("receiveDeleteLine is called");
            
            // T: remove the line from the listLines
            deleteLineFromList(listLines, command.userIdOfLine, command.timestampOfLine);
            
            update(ctx);
        }

        function sendDeleteLine(lineToDelete) {
            let timestamp = Date.now();
            
            axios.post(endPointForDeleteLine, {
                userId: data.userId,
                groupId: data.groupId,
                timestamp: timestamp,
                userIdOfLine: lineToDelete.userId,
                timestampOfLine: lineToDelete.timestamp,
            }).then(resp => resp.data)
        }

        function deleteLineFromList(lines, indexLineToDelete) {
            let indexLineToDelete = -1;
            
            // for(let indexLine in lines) {
            //     let line = lines[indexLine];

            //     if(line.userId == userIdLineToDelete && line.timestamp == timestampLineToDelete)
            //         indexLineToDelete = indexLine;
            // }

            if(indexLineToDelete >= 0)
                lines.splice(indexLineToDelete, 1);
        }



        fetch("https://rest-service-1735827345127.azurewebsites.net/api/templogin")
        .then((response) => response.json())
        .then((json) => { 
            console.log(json); 
            data.userId = json.userId;

            // T: set the temporary userId
            document.getElementById("username").textContent = "User" + data.userId

            // T: write the code of the current groupId
            document.getElementById("groupId").textContent = data.userId
            
            const connection = new signalR.HubConnectionBuilder()
            // .withUrl(`${apiBaseUrl}/signalr`)
            .withUrl(`/signalr?userId=` + data.userId)
            .withAutomaticReconnect()
            .configureLogging(signalR.LogLevel.Information)
            .build()
            
            // T: Set the listener to the receiveing message (START)
            // connection.on('newMessage', newMessage)
            connection.on('receiveCreateLine', receiveCreateLine);
            connection.on('receiveDeleteLine', receiveDeleteLine);
            // T: Set the listener to the receiveing message (END)

            connection.start()
            .then(() => console.log("Started connection"))
            .catch(console.error)
        })
    }
}

function isPointInLine(point, line, tollerance) {
    precPoint = line.points[0]
    for(let indexPoint = 1; indexPoint < line.points.length; indexPoint++) {
        currentPoint = line.points[indexPoint];
        
        distance = pointToSegmentDistance(point, {x: precPoint.first, y: precPoint.second}, {x: currentPoint.first, y: precPoint.second});
        if(distance < tollerance) {
            return true
        }

        precPoint = currentPoint;
    }
}

function isPointInLines(point, lines, tollerance) {

    let indexLineToReturn = -1;

    for(indexLine in lines) {
        let line = lines[indexLine];

        if(isPointInLine(point, line, tollerance)) {
            console.log("point in line: " + line);

            line.color = "red";

            indexLineToReturn = indexLine;
        }
    }

    if(isPointInLine(point, currentLine, tollerance)) {
        console.log("point in currentline: " + currentLine);

        currentLine.color = "red";

        // T: find a way to delete the currentLine
    }

    return indexLineToReturn;
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





function addToGroup() {
    let groupId = document.getElementById("groupToAdd").value
    
    data.groupId = groupId
    document.getElementById("groupId").textContent = groupId

    fetch("https://rest-service-1735827345127.azurewebsites.net/api/addgroup?groupId=" + groupId + "&userId=" + data.userId).
    then((response) => console.log("adding to group: " + response.status))
}

// T: disabilitate the context menu
document.addEventListener("contextmenu", function(event) {
    event.preventDefault();
});
window.addEventListener('load', draw)

console.log("You can start to draw")