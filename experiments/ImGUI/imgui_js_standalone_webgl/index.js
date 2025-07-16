//=============================================================================
//
// TO-DO:
// - Check differences between let and vars
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










function loop() 
{
    const gl = gl_glob;    
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
    let position_attribute_location = gl.getAttribLocation(program, "a_position");
    assert_attrib_uniform_location(position_attribute_location);
    let color_attribute_location = gl.getAttribLocation(program, "a_color");
    assert_attrib_uniform_location(color_attribute_location);

    let resolution_uniform_location = gl.getUniformLocation(program, "u_resolution");
    assert_attrib_uniform_location(resolution_uniform_location);
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
    gl.enableVertexAttribArray(position_attribute_location);
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
    gl.vertexAttribPointer(position_attribute_location, size, type, normalize, stride, offset);
    // T: Tell the attribute how to get data out of positionBuffer(ARRAY_BUFFER) (END)


    
    // T: Create color buffer (START)
    let color_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, color_buffer);
    var colors = [
        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,

        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,
        0.0, 0.0, 1.0, 1.0,
    ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
    // T: Create color buffer (END)

    // T: Tell the attribute how to get data out of color_buffer(ARRAY_BUFFER) (START)
    gl.enableVertexAttribArray(color_attribute_location);
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
    gl.vertexAttribPointer(color_attribute_location, size_color, type_color, normalize_color, stride_color, offset_color);
    // T: Tell the attribute how to get data out of color_buffer(ARRAY_BUFFER) (END)



    // T: Helper function to make working the resize of the window
    webglUtils.resizeCanvasToDisplaySize(gl.canvas);
    console.log("canvas size: (" + gl.canvas.width + " " + gl.canvas.height + ")");
    
    // T: Set uniform for resolution of canvas
    gl.uniform2f(resolution_uniform_location, gl.canvas.width, gl.canvas.height);

    // T: Tell WebGL how to convert from clip space to pixel
    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height);
    
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





// export function index()
function index()
{
    if (typeof window !== "undefined") {
        window.requestAnimationFrame(loop);
    }        
}





if(debug)
System.register(["imgui-js", "imgui-impl-js", /*"./imgui_demo.js",*/ "imgui_memory_editor.js"], function (exports_1, context_1) {
    "use strict";
    
    // T: reset this line of code
    var imgui_demo_js_1, imgui_memory_editor_js_1, font, memory_editor, show_sandbox_window, show_gamepad_window, show_movie_window, f, counter, done, source, image_urls, image_url, image_element, image_gl_texture, video_urls, video_url, video_element, video_gl_texture, video_w, video_h, video_time_active, video_time, video_duration;
    // var ImGui, ImGui_Impl, imgui_demo_js_1, imgui_memory_editor_js_1, font, clear_color, memory_editor, show_sandbox_window, show_gamepad_window, show_movie_window, f, counter, done, source, image_urls, image_url, image_element, image_gl_texture, video_urls, video_url, video_element, video_gl_texture, video_w, video_h, video_time_active, video_time, video_duration;
    
    var __moduleName = context_1 && context_1.id;

    
    async function LoadArrayBuffer(url) {
        const response = await fetch(url);
        // console.log(response);
        return response.arrayBuffer();
    }
    
    async function main() {
        await ImGui.default();
        
        if (typeof window !== "undefined") {
            window.requestAnimationFrame(_init);
        } 
        /* else {
            await _init();
            for (let i = 0; i < 3; ++i) {
                _loop(1 / 60);
            }
            await _done();
        } */
    }

    function init_main()
    {
        ImGui.default();
        window.requestAnimationFrame(_init);
    }

    exports_1("default", main);
    // exports_1("default", init_main);

    async function AddFontFromFileTTF(url, size_pixels, font_cfg = null, glyph_ranges = null) {
        font_cfg = font_cfg || new ImGui.FontConfig();
        font_cfg.Name = font_cfg.Name || `${url.split(/[\\\/]/).pop()}, ${size_pixels.toFixed(0)}px`;
        const buffer = await LoadArrayBuffer(url);
        return ImGui.GetIO().Fonts.AddFontFromMemoryTTF(buffer, size_pixels, font_cfg, glyph_ranges);
    }

    async function _init() {
        const EMSCRIPTEN_VERSION = `${ImGui.bind.__EMSCRIPTEN_major__}.${ImGui.bind.__EMSCRIPTEN_minor__}.${ImGui.bind.__EMSCRIPTEN_tiny__}`;
        console.log("Emscripten Version", EMSCRIPTEN_VERSION);
        console.log("Total allocated space (uordblks) @ _init:", ImGui.bind.mallinfo().uordblks);

        ImGui.CHECKVERSION();
        ImGui.CreateContext();
        const io = ImGui.GetIO();
        ImGui.StyleColorsDark();

        io.Fonts.AddFontDefault();
        font = await AddFontFromFileTTF("fonts/Roboto-Medium.ttf", 14.0);
        ImGui.ASSERT(font !== null);

        if (typeof window !== "undefined") {
            const canvas = document.getElementsByTagName("canvas")[0];
            canvas.tabIndex = 1;
            canvas.style.position = "absolute";
            canvas.style.left = "0px";
            canvas.style.right = "0px";
            canvas.style.top = "0px";
            canvas.style.bottom = "0px";
            canvas.style.width = "100%";
            canvas.style.height = "100%";
            canvas.style.userSelect = "none";
            ImGui_Impl.Init(canvas);
        } else {
            ImGui_Impl.Init(null);
        }

        if (typeof window !== "undefined") {
            window.requestAnimationFrame(_loop);
        }
    }


    // Main loop
    function _loop(time) {

        ImGui_Impl.NewFrame(time);
        ImGui.NewFrame()

        // 2. Show a simple window that we create ourselves. We use a Begin/End pair to created a named window.
        {
            // T: NOTES: these static variables are commented here and then defined at the end of this file(WTF???)
            // static float f = 0.0f;
            // static int counter = 0;


            // T: Change font (START)
            if (use_font && font) {
                ImGui.PushFont(font);

                // T: this code is useless
                // ImGui.Text(`${font.GetDebugName()}`);
                // if (font.FindGlyphNoFallback(0x5929)) {
                //     ImGui.Text(`U+5929: \u5929`);
                // }
                
                // T: Remove the font 
                // ImGui.PopFont();
            }
            // T: Change font (END)


            ImGui.Begin("Window Title"); // Create a window called "Hello, world!" and append into it.
            ImGui.Text("Displaying some text"); // Display some text
            // T: NOTES: this code is useless
            // ImGui.Checkbox("Demo Window", (value = show_demo_window) => show_demo_window = value); // Edit bools storing our windows open/close state
            ImGui.Checkbox("checkbox", (value = boolean_field) => boolean_field = value); // Edit bools
            ImGui.Checkbox("use_font", (value = use_font) => use_font = value);


            // T: This code make possible that when we have the pointer on the checkbox
            // we create the tooltip writing on it a string
            if (ImGui.IsItemHovered()) {
                ImGui.BeginTooltip();
                ImGui.Text("Hello!");
                ImGui.EndTooltip();
            }

            ImGui.SliderFloat("float", (value = f) => f = value, 0.0, 1.0); // Edit 1 float using a slider from 0.0f to 1.0f
            ImGui.ColorEdit3("clear color", clear_color); // Edit 3 floats representing a color
            if (ImGui.Button("Increment counter")) // Buttons return true when clicked (NB: most widgets return true when edited/activated)
                counter++;
            ImGui.SameLine();
            ImGui.Text(`counter = ${counter}`);
            ImGui.Text(`Application average ${(1000.0 / ImGui.GetIO().Framerate).toFixed(3)} ms/frame (${ImGui.GetIO().Framerate.toFixed(1)} FPS)`);



            ImGui.End();
        }



        ImGui.EndFrame();
        // Rendering
        ImGui.Render();
        

        loop();


        // T: WARNING: removing this node give us a strange error (START)
        ImGui_Impl.RenderDrawData(ImGui.GetDrawData());
        if (typeof (window) !== "undefined") {
            window.requestAnimationFrame(_loop);
        }
        // T: WARNING: removing this node give us a strange error (END)
    }

    return {
        setters: [
            function (ImGui_1) {
                ImGui = ImGui_1;
            },
            function (ImGui_Impl_1) {
                ImGui_Impl = ImGui_Impl_1;
            },
            // function (imgui_demo_js_1_1) {
            //     imgui_demo_js_1 = imgui_demo_js_1_1;
            // },
            function (imgui_memory_editor_js_1_1) {
                imgui_memory_editor_js_1 = imgui_memory_editor_js_1_1;
            }
        ],

        // T: NOTES: some of the variables that are used in the code are defined directly here
        execute: function () {
            font = null;

            boolean_field = false;

            use_font = false;

            clear_color = new ImGui.Vec4(0.45, 0.55, 0.60, 1.00);
            memory_editor = new imgui_memory_editor_js_1.MemoryEditor();
            memory_editor.Open = false;
            show_sandbox_window = false;
            show_gamepad_window = false;
            show_movie_window = false;
            /* static */ f = 0.0;
            /* static */ counter = 0;
            done = false;
            source = [
                "ImGui.Text(\"Hello, world!\");",
                "ImGui.SliderFloat(\"float\",",
                "\t(value = f) => f = value,",
                "\t0.0, 1.0);",
                "",
            ].join("\n");
            image_urls = [
                "https://threejs.org/examples/textures/crate.gif",
                "https://threejs.org/examples/textures/sprite.png",
                "https://threejs.org/examples/textures/uv_grid_opengl.jpg",
            ];
            image_url = image_urls[0];
            image_element = null;
            image_gl_texture = null;
            video_urls = [
                "https://threejs.org/examples/textures/sintel.ogv",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
            ];
            video_url = video_urls[0];
            video_element = null;
            video_gl_texture = null;
            video_w = 640;
            video_h = 360;
            video_time_active = false;
            video_time = 0;
            video_duration = 0;
        }
    };
});