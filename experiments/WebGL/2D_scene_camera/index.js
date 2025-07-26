"use strict";
//=============================================================================
//-----------------------------------------------------------------------------
// 2D SCENE CAMERA
//-----------------------------------------------------------------------------
// This is the main file of this application.
// In this application the goal is to create a simple working camera that
// implement the zoom and movement in the scene.
//
//
//
//-----------------------------------------------------------------------------
// TO-DO:
// - Check differences between let and vars.
// - Change name to this file, something like: application.js.
// - Check how the size of the canvas is defined.
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

    console.error(gl.getShaderInfoLog(shader));
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

    console.error(gl.getProgramInfoLog(program));
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

function update_camera()
{
    // T: Reset camera info (START)
    global_state.buffer_camera_transform[0] = 1.0;
    global_state.buffer_camera_transform[4] = 1.0;
    global_state.buffer_camera_transform[6] = 0.0;
    global_state.buffer_camera_transform[7] = 0.0;
    // T: Reset camera info (END)

    global_state.buffer_camera_transform[6] = global_state.camera_translation[0];
    global_state.buffer_camera_transform[7] = global_state.camera_translation[1];

    // T: Zoom in/out camera (START)
    // T: NOTES: This function adjust the position of objects after the zoom.
    // T: To have a working zoom, we need to translate the position of the distance that
    // that the focus point(reference point, middle of the screen) is moved for scaling. 
    // T: TODO: Move this function in the class Camera
    function zoom()
    {
        global_state.buffer_camera_transform[0] = global_state.camera_zoom;
        global_state.buffer_camera_transform[4] = global_state.camera_zoom;
        global_state.buffer_camera_transform[6] = global_state.camera_translation[0] * global_state.camera_zoom;
        global_state.buffer_camera_transform[7] = global_state.camera_translation[1] * global_state.camera_zoom;
        let screen_center = [global_state.canvas.width / 2.0, global_state.canvas.height / 2.0];
        // console.log(screen_center);
        let screen_center_translated_for_zoom = [screen_center[0] * global_state.camera_zoom, screen_center[1] * global_state.camera_zoom];
        global_state.buffer_camera_transform[6] -= screen_center_translated_for_zoom[0] - screen_center[0];
        global_state.buffer_camera_transform[7] -= screen_center_translated_for_zoom[1] - screen_center[1];
    }
    // T: Zoom in/out camera (START)


    zoom();
}









// T: move these variables in the global state
var vertex_shader_src;
var fragment_shader_src;
var render_texture_vertex_shader_src;
var render_texture_fragment_shader_src;

// T: This is the loading function (START)
function load()
{
    // T: Loading of shaders (START)
    async function load_shaders()
    {
        vertex_shader_src = await fetch('shaders/vertex.glsl').then(res => res.text());
        fragment_shader_src = await fetch('shaders/fragment.glsl').then(res => res.text());
        render_texture_vertex_shader_src = await fetch('shaders/render_texture_vertex.glsl').then(res => res.text());
        render_texture_fragment_shader_src = await fetch('shaders/render_texture_fragment.glsl').then(res => res.text());
    }
    load_shaders();
    // T: Loading of shaders (END)



    Global_State.get().canvas = document.getElementsByTagName("canvas")[0];
    Global_State.get().gl_glob = document.getElementsByTagName("canvas")[0].getContext("webgl2", { alpha: false }) || 
                    document.getElementsByTagName("canvas")[0].getContext("webgl", { alpha: false }) || 
                    document.getElementsByTagName("canvas")[0].getContext("2d");







    if(debug == true)
    {
      console.log("Debug Mode");
    
      System.import("debug_interface")
      // T: NOTE: I discovered that the name "main" is not mandatory
      // in the past I thinked that this name is mandatory for whatever
      // reason, but it's not. 
      // The unique important thing is that the name must match with the
      // name that is exported in the file interface.js
      // PROBABLY every other method that is exported can work like that.
      .then(function (main) 
      { 
          main.default();
      })
      .catch(console.error)
    }
    else
    {
      console.log("Release Mode");

      index();
    }
}
// T: This is the loading function (END)





function setup()
{
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




    
    // T: attach event listeners(START)
    document.addEventListener('keydown', (event) => 
    {
        global_state.state_keys[event.key] = true;
        // console.log(global_state.state_keys);

        if (global_state.state_keys['ArrowLeft'])
        {
            // global_state.buffer_camera_transform[6] += global_state.camera_movement_acceleration;
            global_state.camera_translation[0] += global_state.camera_movement_acceleration;
            // console.log('Left arrow pressed');
        } 
        if (global_state.state_keys['ArrowRight']) 
        {
            // global_state.buffer_camera_transform[6] -= global_state.camera_movement_acceleration;
            global_state.camera_translation[0] -= global_state.camera_movement_acceleration; 
            // console.log('Right arrow pressed');
        }
        if(global_state.state_keys["ArrowUp"])
        {
            global_state.camera_translation[1] += global_state.camera_movement_acceleration;
            // global_state.buffer_camera_transform[7] += global_state.camera_movement_acceleration;
            // console.log("up arrow pressed");
        }
        if(global_state.state_keys["ArrowDown"])
        {
            global_state.camera_translation[1] -= global_state.camera_movement_acceleration;
            // global_state.buffer_camera_transform[7] -= global_state.camera_movement_acceleration;
            // console.log("down arrow pressed");
        }
    });

    document.addEventListener('keyup', (event) => 
    {
        // console.log(event.key);
        global_state.state_keys[event.key] = false;
    });

    window.addEventListener('resize', () => {
        // console.log('Window resized to:', window.innerWidth, 'x', window.innerHeight);
        console.log('canvas resized to:', global_state.canvas.width, 'x', global_state.canvas.height);

        // T: NOTES: There is a little bit of code duplication here
        // T: Helper function to make working the resize of the window
        webglUtils.resizeCanvasToDisplaySize(gl.canvas);
    
        // T: Set uniform for resolution of canvas
        gl.uniform2f(global_state.resolution_uniform_location, gl.canvas.width, gl.canvas.height);

        // T: Tell WebGL how to convert from clip space to pixel
        gl.viewport(0, 0, gl.canvas.width, gl.canvas.height);
    });
    // T: attach event listeners(END)



    // T: Loading shaders (START)
    // let vertex_shader_src = document.querySelector("#vertex_shader").text;
    // let fragment_shader_src = document.querySelector("#fragment_shader").text;
    // console.log(vertex_shader_src);
    // console.log(fragment_shader_src);
    // T: Loading shaders (END)

    // T: Compiling shaders (START)
    // T: OPTIMIZE: After this, the string can be cleared, they aren't re-used and occupy potentially
    // a lot of space.
    let vertex_shader = create_shader(gl, gl.VERTEX_SHADER, vertex_shader_src);
    let fragment_shader = create_shader(gl, gl.FRAGMENT_SHADER, fragment_shader_src);
    let render_texture_vertex_shader = create_shader(gl, gl.VERTEX_SHADER, render_texture_vertex_shader_src);
    let render_texture_fragment_shader = create_shader(gl, gl.FRAGMENT_SHADER, render_texture_fragment_shader_src);
    // T: Compiling shaders (END)
    
    // T: Create programs (START)
    let program = create_program(gl, vertex_shader, fragment_shader);
    global_state.program = program;
    gl.useProgram(program);

    let render_texture_program = create_program(gl, render_texture_vertex_shader, render_texture_fragment_shader);
    global_state.render_texture_program = render_texture_program;
    // T: Create programs (END)


    // T: lookup for uniform and attrib locations (START)
    global_state.position_attribute_location = gl.getAttribLocation(program, "a_position");
    assert_attrib_uniform_location(global_state.position_attribute_location);
    global_state.color_attribute_location = gl.getAttribLocation(program, "a_color");
    assert_attrib_uniform_location(global_state.color_attribute_location);
    global_state.resolution_uniform_location = gl.getUniformLocation(program, "u_resolution");
    assert_attrib_uniform_location(global_state.resolution_uniform_location);
    global_state.camera_transform_uniform_location = gl.getUniformLocation(program, "u_camera_transform");
    assert_attrib_uniform_location(global_state.camera_transform_uniform_location);

    gl.useProgram(global_state.render_texture_program);
    global_state.render_texture_resolution_uniform_location = gl.getUniformLocation(global_state.render_texture_program, "u_resolution");
    assert_attrib_uniform_location(global_state.render_texture_resolution_uniform_location);
    global_state.render_texture_camera_transform_uniform_location = gl.getUniformLocation(global_state.render_texture_program, "u_camera_transform");
    assert_attrib_uniform_location(global_state.render_texture_camera_transform_uniform_location);
    global_state.coordinates_attribute_location = gl.getAttribLocation(global_state.render_texture_program, "a_coordinates");
    assert_attrib_uniform_location(global_state.coordinates_attribute_location);
    global_state.texcoord_attribute_location = gl.getAttribLocation(global_state.render_texture_program, "a_texcoord");
    assert_attrib_uniform_location(global_state.texcoord_attribute_location);
    // global_state.render_texture_image_uniform_location = gl.getUniformLocation(global_state.render_texture_program, "u_texture");
    // assert_attrib_uniform_location(global_state.render_texture_image_uniform_location);    
    
    gl.useProgram(global_state.program);
    // T: lookup for uniform and attrib locations (END)

    // T: Create and select the vertex array
    let vao = gl.createVertexArray();
    global_state.vao = vao;
    gl.bindVertexArray(vao);

    // T: Create position buffer (START)
    let position_buffer = gl.createBuffer();
    global_state.position_buffer = position_buffer;
    gl.bindBuffer(gl.ARRAY_BUFFER, position_buffer);
     var positions = [
      200, 0,
      300, 0,
      200, 20,

      1, 21,
      101, 21,
      101, 1,
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
    global_state.color_buffer = color_buffer;
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



    // T: Create vertex array to rendering textures (START)
    gl.useProgram(global_state.render_texture_program);

    global_state.render_texture_vao = gl.createVertexArray();
    gl.bindVertexArray(global_state.render_texture_vao);


    // T: Loading textures (START)
    async function load_textures()
    {
        const gl = global_state.gl_glob;
        global_state.texture = gl.createTexture();
        gl.activeTexture(gl.TEXTURE0 + 0);
        gl.bindTexture(gl.TEXTURE_2D, global_state.texture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, 1, 1, 0, gl.RGBA, gl.UNSIGNED_BYTE,
                new Uint8Array([0, 0, 255, 255]));

        global_state.texture_data = new Image();
        // T: WARNING: It's temporary, disable the cross origin when you find a better solution
        // to make this working.
        global_state.texture_data.crossOrigin = "anonymous";
        global_state.texture_data.src = "https://webgl2fundamentals.org/webgl/resources/f-texture.png";
        global_state.texture_data.addEventListener("load", function() {
            gl.bindTexture(gl.TEXTURE_2D, global_state.texture);
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, global_state.texture_data);
            gl.generateMipmap(gl.TEXTURE_2D);
        });
    }
    load_textures();
    // T: Loading textures (END)



    // T: Create buffer for coordinates to render textures
    global_state.render_texture_coordinates_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.render_texture_coordinates_buffer);

    // T: Tell the shaders how to get attributes out of render_texture_coordinates_buffer (START)
    stride = 16;
    
    gl.enableVertexAttribArray(global_state.coordinates_attribute_location);
    size = 2;
    type = gl.FLOAT;
    normalize = false;
    offset = 0;
    gl.vertexAttribPointer(global_state.coordinates_attribute_location, size, type, normalize, stride, offset);
    
    gl.enableVertexAttribArray(global_state.texcoord_attribute_location);
    size = 2;
    type = gl.FLOAT;
    normalize = false;
    offset = 8;
    gl.vertexAttribPointer(global_state.texcoord_attribute_location, size, type, normalize, stride, offset);
    // T: Tell the shaders how to get attributes out of render_texture_coordinates_buffer (END)
    // T: Create vertex array to rendering textures (END)



    gl.useProgram(global_state.program);

    // T: Set uniform for translation of elements of the scene (START)
    global_state.buffer_camera_transform[0] = 1.0;
    global_state.buffer_camera_transform[4] = 1.0;
    global_state.buffer_camera_transform[6] = 0.0;
    global_state.buffer_camera_transform[7] = 0.0;
    gl.uniformMatrix3fv(global_state.camera_transform_uniform_location, false, global_state.buffer_camera_transform);
    // T: Set uniform for translation of elements of the scene (END)

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



    gl.useProgram(global_state.program);
    gl.bindVertexArray(global_state.vao);

    // T: Set uniform for translation and scaling of matrix
    update_camera();
    gl.uniformMatrix3fv(global_state.camera_transform_uniform_location, false, global_state.buffer_camera_transform);
    gl.uniform2f(global_state.resolution_uniform_location, gl.canvas.width, gl.canvas.height);


    // T: OPTIMIZATION: I already made a split for the buffer, this is not really healthy for the
    // performance, I can always make a singular buffer, a single load and multiple draw calls
    // with different settings.
    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.position_buffer);
    var positions = [
      200, 0,
      300, 0,
      200, 20,

      1, 21,
      101, 21,
      101, 1,  
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.color_buffer);
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






    
    
    // T: Render textures (START)
    gl.useProgram(global_state.render_texture_program);
    gl.bindVertexArray(global_state.render_texture_vao);

    gl.uniformMatrix3fv(global_state.render_texture_camera_transform_uniform_location, false, global_state.buffer_camera_transform);
    gl.uniform2f(global_state.render_texture_resolution_uniform_location, gl.canvas.width, gl.canvas.height);


    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.render_texture_coordinates_buffer);
    var coordinates = [
        900, 200, 0, 0,
        960, 200, 1, 0,
        900, 259, 0, 1,
        
        900, 259, 0, 1,
        960, 259, 1, 1,
        960, 200, 1, 0, 
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coordinates), gl.STATIC_DRAW);



    
    // T: Draw the textures (START)
    primitiveType = gl.TRIANGLES;
    offset_ = 0;
    count = 6;
    gl.drawArrays(primitiveType, offset_, count);
    // T: Draw the textures (END)
    // T: Render textures (END)



    // T: Temporary to draw the point of the camera (START)
    gl.useProgram(global_state.program);
    gl.bindVertexArray(global_state.vao);

    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.position_buffer);
    var positions = [   
        960, 459.5,
        965, 459.5,
        960, 464.6,
        960, 464.6,
        965, 464.6,
        965, 459.5,    
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);
    
    
    gl.bindBuffer(gl.ARRAY_BUFFER, global_state.color_buffer);
    var colors = [
        1.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 0.0, 1.0,
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);

    console.log(global_state.buffer_camera_transform_position);

    gl.uniformMatrix3fv(global_state.camera_transform_uniform_location, false, global_state.buffer_camera_transform_position);    
    gl.uniform2f(global_state.resolution_uniform_location, gl.canvas.width, gl.canvas.height);

    primitiveType = gl.TRIANGLES;
    offset_ = 0;
    count = 6;
    gl.drawArrays(primitiveType, offset_, count);
    // T: Temporary to draw the point of the camera (END)
}

function index()
{
    setup();

    if (typeof window !== "undefined") 
    {
        window.requestAnimationFrame(loop);
    }        
}