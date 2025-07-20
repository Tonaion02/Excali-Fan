"use strict";
//=============================================================================
//
// TO-DO:
// - Check differences between let and vars.
// - Move the fonts directory under ImGUI.
// - Change name to this file, something like: application.js.
// - Probably it's a good idea to use the code that is written here
// for handling shaders.
// - Move code of fragment and vertex shader in apposite files.
// - Move part of the loop in the setup function.
//=============================================================================
const canvas = document.querySelector("#canvas");





function create_shader(gl, type, source) 
{
    let shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    let success = gl.getShaderParameter(shader, gl.COMPILE_STATUS);
    if(success)
    {
        return shader;
    }

    console.log(gl.getShaderInfoLog(shader));
    gl.deleteShader(shader);
}

function create_program(gl, vertex_shader, fragment_shader)
{
    let program = gl.createProgram();
    gl.attachShader(program, vertex_shader);
    gl.attachShader(program, fragment_shader);
    gl.linkProgram(program);
    let success = gl.getProgramParameter(program, gl.LINK_STATUS);
    if(success)
    {
        return program;
    }

    console.log(gl.getProgramInfoLog(program));
    gl.deleteProgram(program);
}

function debug_retrieve_error(gl)
{
    const error = gl.getError();
    if (error !== gl.NO_ERROR) {
      console.error('WebGL error:', error);
    }
}

function assert_attrib_uniform_location(location)
{
  if(location == null || location == -1)
    throw new Error(invalid_value_attrib_location);
}










function setup()
{
    // const gl = gl_glob;
    const gl = Global_State.get().gl_glob;
    
    console.log(gl.getParameter(gl.VERSION));
    if(!gl)
    {   
        console.log("ERROR: WebGL isn't working");
        return;
    }

    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
    if (debugInfo) {
      const vendor = gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL);
      const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
      console.log('Vendor:', vendor);
      console.log('Renderer:', renderer);
    } else {
      console.log('WEBGL_debug_renderer_info not supported');
    }




    // T: Loading shaders (START)
    let vertex_shader_src = document.querySelector("#vertex_shader").text;
    let fragment_shader_src = document.querySelector("#fragment_shader").text;
    // T: Loading shaders (END)

    // T: Compiling shaders (START)
    let vertex_shader = create_shader(gl, gl.VERTEX_SHADER, vertex_shader_src);
    let fragment_shader = create_shader(gl, gl.FRAGMENT_SHADER, fragment_shader_src);
    // T: Compiling shaders (END)

    // T: Create program
    let program = create_program(gl, vertex_shader, fragment_shader);
    gl.useProgram(program);


    // T: lookup for uniform and attrib locations (START)
    global_state.position_attribute_location = gl.getAttribLocation(program, "a_position");
    assert_attrib_uniform_location(global_state.position_attribute_location);
    global_state.color_attribute_location = gl.getAttribLocation(program, "a_color");
    assert_attrib_uniform_location(global_state.color_attribute_location);

    global_state.resolution_uniform_location = gl.getUniformLocation(program, "u_resolution");
    assert_attrib_uniform_location(global_state.resolution_uniform_location);
    // T: lookup for uniform and attrib locations (END)

    // T: Create and select the vertex array
    let vao = gl.createVertexArray();
    gl.bindVertexArray(vao);

    // T: Create position buffer (START)
    let position_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, position_buffer);
    var positions = [
      100, 0,
      200, 0,
      100, 20,

      101, 21,
      201, 21,
      201, 1,
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);
    // T: Create position buffer (END)

    // T: Tell the attribute how to get data out of positionBuffer(ARRAY_BUFFER) (START)
    gl.enableVertexAttribArray(global_state.position_attribute_location);
    // T: 2 components per iteration
    let size = 2;          
    // T: the data is 32bit floats
    let type = gl.FLOAT;   
    // T: don't normalize the data
    let normalize = false; 
    // T: 0 = move forward size * sizeof(type) each iteration to get the next position
    let stride = 0;
    // T: start at the beginning of the buffer        
    let offset = 0;        
    gl.vertexAttribPointer(global_state.position_attribute_location, size, type, normalize, stride, offset);
    // T: Tell the attribute how to get data out of positionBuffer(ARRAY_BUFFER) (END)


    
    // T: Create color buffer (START)
    let color_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, color_buffer);
    // T: Create color buffer (END)

    // T: Tell the attribute how to get data out of color_buffer(ARRAY_BUFFER) (START)
    gl.enableVertexAttribArray(global_state.color_attribute_location);
    // T: 2 components per iteration
    let size_color = 4;          
    // T: the data is 32bit floats
    let type_color = gl.FLOAT;   
    // T: don't normalize the data
    let normalize_color = false; 
    // T: 0 = move forward size * sizeof(type) each iteration to get the next position
    let stride_color = 0;
    // T: start at the beginning of the buffer        
    let offset_color = 0;        
    gl.vertexAttribPointer(global_state.color_attribute_location, size_color, type_color, normalize_color, stride_color, offset_color);
    // T: Tell the attribute how to get data out of color_buffer(ARRAY_BUFFER) (END)



    // T: Helper function to make working the resize of the window
    webglUtils.resizeCanvasToDisplaySize(gl.canvas);
    // console.log("canvas size: (" + gl.canvas.width + " " + gl.canvas.height + ")");
    
    // T: Set uniform for resolution of canvas
    gl.uniform2f(global_state.resolution_uniform_location, gl.canvas.width, gl.canvas.height);

    // T: Tell WebGL how to convert from clip space to pixel
    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height);
}





function loop() 
{
    // const gl = gl_glob;
    const gl = Global_State.get().gl_glob;



    var colors = [
        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,

        global_state.triangle_color.x, global_state.triangle_color.y, global_state.triangle_color.z, 1.0, 
        global_state.triangle_color.x, global_state.triangle_color.y, global_state.triangle_color.z, 1.0, 
        global_state.triangle_color.x, global_state.triangle_color.y, global_state.triangle_color.z, 1.0, 
        // 0.0, 0.0, 1.0, 1.0,
        // 0.0, 0.0, 1.0, 1.0,
        // 0.0, 0.0, 1.0, 1.0,
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);




    gl.clearColor(clear_color.x, clear_color.y, clear_color.z, clear_color.w);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    



    // T: Draw the triangle (START)
    let primitiveType = gl.TRIANGLES;
    let offset_ = 0;
    let count = 6;
    gl.drawArrays(primitiveType, offset_, count);
    // T: Draw the triangle (END)




    
    // // T: NOTES: this gl is taken directly from ImGui_impl, so I don't know if i can inizialize
    // // WebGL in another point of the code    
    // var gl = gl_glob;
    // console.log(gl);        
    // if (gl) {
    //     gl.viewport(0, 0, gl.drawingBufferWidth, gl.drawingBufferHeight);
    //     gl.clearColor(clear_color.x, clear_color.y, clear_color.z, clear_color.w);
    //     gl.clear(gl.COLOR_BUFFER_BIT);
    //     //gl.useProgram(0); // You may want this if using this code in an OpenGL 3+ context where shaders may be bound
    // }



    // T: NOTES I don't know the purpose of this code
    // const ctx = ImGui_Impl.ctx;
    // if (ctx) {
    //     // ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    //     ctx.fillStyle = `rgba(${clear_color.x * 0xff}, ${clear_color.y * 0xff}, ${clear_color.z * 0xff}, ${clear_color.w})`;
    //     ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    // }
}





function index()
{
    setup();

    if (typeof window !== "undefined") 
    {
        window.requestAnimationFrame(loop);
    }        
}