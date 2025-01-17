// T: this will become the synced list
let listLines = []
listLines.push({color: "black", points: [{first: 10, second: 10}, {first: 100, second: 100}]})
// T: this is something similar to the forward list,
// in that we can cache the current operation.
// This is useful when a user is drawing a line during
// an update.
let currentLine = {color: null,  points: []}

const canvas = document.getElementById("canvas");

// T: data that must be stored
const data = {
    username: '',
    userId: '',
    groupId: '',
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
    ctx.stroke();
}

// T: This function is used to update the screen
// when a new command is received
function update() {

    const ctx = canvas.getContext("2d")

    // T: clear the canva
    ctx.clearRect(0, 0, canvas.width, canvas.height)    
    
    // T: redraw every lines from the synced list
    for(lineIndex in listLines) {
        let line = listLines[lineIndex]
        drawLine(line, ctx)
    }

    // T: retrieve the current state of the line drawn until now from the forward
    drawLine(currentLine, ctx)
    isDrawing = true
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

                // Clear the current line and add the first point of the line
                currentLine = [];
                // currentLine.push([lastX, lastY]);

                currentLine.push({first: e.offsetX, second: e.offsetY});
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
                    isPointInLines({x: e.offsetX, y: e.offsetY}, listLines, 5);
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

                    axios.post(`/api/messages`, {
                      userId: data.userId,
                      groupId: data.groupId,
                      timestamp: Date.now(),
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


    
        function newMessage(message) {
            console.log("newMessage is called");
            console.log(message.points)
            
            listLines.push(message.points)
            update()            
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
            
            connection.on('newMessage', newMessage)
            
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

        x1 = precPoint.first;
        y1 = precPoint.second;
        x2 = currentPoint.first;
        y2 = currentPoint.second;
        
        const distance = Math.abs((y2 - y1) * point.x - (x2 - x1) * point.y + x2 * y1 - y2 * x1) /
            Math.sqrt((y2 - y1) ** 2 + (x2 - x1) ** 2);
        if(distance < tollerance) {
            return true
        }

        precPoint = currentPoint;
    }
}

function isPointInLines(point, lines, tollerance) {

    for(indexLine in lines) {
        line = lines[indexLine];

        if(isPointInLine(point, line, tollerance)) {
            console.log("point in line")

            line.color = "red"
        }
    }
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