let listLines = []
let currentLine = []

const data = {
    username: '',
    userId: '',
    newMessage: '',
    messages: [],
    ready: false
}





// T: make the prompt to retrieve the username (START)
data.username = prompt('Enter your username')
if (!data.username) {
  alert('No username entered. Reload page and try again.')
  throw 'No username entered'
} else {
  console.log("im setting username");
  document.getElementById("username").textContent = data.username;
}
// T: make the prompt to retrieve the username (END)





function drawLine(line, ctx) {

    let first = true;
    let lx, ly;

    for(point in line) {

        point = line[point]

        if(first) {
            first = false;
            lx = point.first;
            ly = point.second;

            ctx.beginPath();
        }
        else {
            ctx.moveTo(lx, ly);
            ctx.lineTo(point.first, point.second);

            ctx.stroke();

            lx = point.first;
            ly = point.second;
        }
    }

}



function draw() {

    const canvas = document.getElementById("canvas");
    
    if (canvas.getContext) {
        const ctx = canvas.getContext("2d");

        ctx.canvas.width  = window.innerWidth;
        ctx.canvas.height = window.innerHeight;
  
        let isDrawing = false;
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
                isDrawing = true;
                [lastX, lastY] = [e.offsetX, e.offsetY];

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
                if(!isDrawing) 
                    return;

                ctx.beginPath();
                ctx.moveTo(lastX, lastY);
                ctx.lineTo(e.offsetX, e.offsetY);
                ctx.stroke();
                [lastX, lastY] = [e.offsetX, e.offsetY];

                currentLine.push({first: e.offsetX, second: e.offsetY});
            }
        );
        // Set EventListener for mouse move (END)
        
        // Set EventListener for mouse up (START)
        canvas.addEventListener('mouseup', 
            () => {
                if (isDrawing) {
                    listLines.push(currentLine);

                    axios.post(`/api/messages`, {
                      userId: data.userId,
                      timestamp: Date.now(),
                      points: currentLine,
                    }).then(resp => resp.data)
                }

                isDrawing = false;
            }
        );
        // Set EventListener for mouse up (END)

        // Set EventListener for mouse that goes out of the canvas (START)
        canvas.addEventListener('mouseleave', () => {
            isDrawing = false;
        });
        // Set EventListener for mouse that goes out of the canvas (END)





        // ctx.beginPath();        
        // ctx.moveTo(0, 0);
        // ctx.lineTo(100, 0);
        // ctx.lineTo(200, 200);
        // ctx.closePath();
        // ctx.stroke();
    
        function newMessage(message) {
            console.log("newMessage is called");
            console.log(message.points)

            /* data.messages.unshift(message)
      
            const li = document.createElement("li") 
            li.textContent = "User " + message.sender + " : " + message.text
            document.getElementById("messages").appendChild(li) */
            
            listLines.push(message.points)
            drawLine(message.points, ctx)
        }

        fetch("https://rest-service-1735827345127.azurewebsites.net/api/templogin")
        .then((response) => response.json())
        .then((json) => { 
          console.log(json); 
          data.userId = json.userId;
        
          const connection = new signalR.HubConnectionBuilder()
          // .withUrl(`${apiBaseUrl}/signalr`)
          .withUrl(`/signalr?userId=` + data.userId)
          .withAutomaticReconnect()
          .configureLogging(signalR.LogLevel.Information)
          .build()
        
          connection.on('newMessage', newMessage)
        
          connection.start()
          .then(() => data.ready = true)
          .catch(console.error)
        })

    }
}
  
draw();

console.log("You can start to draw")