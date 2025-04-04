let listLines = []
let currentLine = []

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
                currentLine.push([lastX, lastY]);
            }
        );
        // Set EventListener for mouse down (END)

        // Set EventListener for mouse up (START)
        canvas.addEventListener('mousemove',
            (e) => {
                if(!isDrawing) 
                    return;

                ctx.beginPath();
                ctx.moveTo(lastX, lastY);
                ctx.lineTo(e.offsetX, e.offsetY);
                ctx.stroke();
                [lastX, lastY] = [e.offsetX, e.offsetY];

                currentLine.push([lastX, lastY]);
            }
        );
        
        canvas.addEventListener('mouseup', 
            () => {
                if (isDrawing) {
                    listLines.push(currentLine);
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
    }
}
  
draw();

console.log("Hello World")