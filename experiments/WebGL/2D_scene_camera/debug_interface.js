//=============================================================================
//-----------------------------------------------------------------------------
// DEBUG INTERFACE FILE
// In this file is contained the code that run ImGUI like a debug interface. 
//-----------------------------------------------------------------------------
// 
// 
// 
// We can't really move this code as we want or write ImGUI code where we want
// because it seems that we need to have the code directly under 
// System.register to make it work. Currently we don't know how we can remove
// this, and we are not really interested in fix that. ImGUI in this project
// is used only as a debug interface and not more.
// In the future I don't exlcude to write my version of ImGUI for WebGL and
// to add all the feature that seems to miss.
//
//
//
// PRECONDITIONS
// To make it work some code of ImGUI must be runned during the drawing.
// For example, some code must be runned before the clear of OpenGL, some code
// after. To mix the code of ImGUI with the code of the application, we 
// injected the code of the application direclty in the code of ImGUI.
// In the "_setup" function of this file is called a "setup" function, that
// is the function that run the setup code for the application.
// In the "_loop" function of this file is called a "loop" function, that
// is the function that run the loop code for the application.
// 
// So, the ImGUI call the code for the rest of the application, but only in
// the "debug mode". When we are in "release mode" the code is 
//
//
// VISUALIZE AND MODIFY VARIABLES
// One of the most useful thing is how we can modify and visualize value
// thanks to ImGUI during the execution of applications. The problem is
// that all the variables that can be visualized or/and modified, must be
// declared like global variable as far as i know.
// The major part of variables are defined in the config.js file, the other
// are directly defined in the global_state, that is a singleton that I
// use to store the major part of the data.
//=============================================================================
if(debug)
System.register(["imgui-js", "imgui-impl-js", "imgui_memory_editor.js"], function (exports_1, context_1) {
    "use strict";
    
    // T: reset this line of code
    var imgui_memory_editor_js_1, font, memory_editor, f, counter;
    // var ImGui, ImGui_Impl, imgui_demo_js_1, imgui_memory_editor_js_1, font, clear_color, memory_editor, show_sandbox_window, show_gamepad_window, show_movie_window, f, counter, done, source, image_urls, image_url, image_element, image_gl_texture, video_urls, video_url, video_element, video_gl_texture, video_w, video_h, video_time_active, video_time, video_duration;
    
    // var translation;
    // var scale;

    var __moduleName = context_1 && context_1.id;

    
    async function LoadArrayBuffer(url) {
        const response = await fetch(url);
        return response.arrayBuffer();
    }
    
    async function main() {
        await ImGui.default();
        
        if (typeof window !== "undefined") {
            window.requestAnimationFrame(_init);
        } 
    }

    exports_1("default", main);

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
        const path_to_roboto = global_state.join_path("path_to_fonts", "Roboto-Medium.ttf");
        font = await AddFontFromFileTTF(path_to_roboto, 14.0);
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
            window.requestAnimationFrame(_setup);
        }
    }

    function _setup(time)
    {
        setup();

        if (typeof window !== "undefined") {
            window.requestAnimationFrame(_loop);
        }
    } 

    // Main loop
    function _loop(time) 
    {
        const canvas = document.getElementsByTagName("canvas")[0];

        ImGui_Impl.NewFrame(time);
        ImGui.NewFrame()

        {
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


            ImGui.Begin("Window Title"); 
            ImGui.Text(`translation (x = ${global_state.buffer_camera_transform[6]}, y = ${global_state.buffer_camera_transform[7]})`);
            ImGui.SliderFloat("camera zoom", (value = global_state.camera_zoom) => global_state.camera_zoom = value, 1.0, 100.0);
            global_state.buffer_camera_transform[0] = global_state.camera_zoom;
            global_state.buffer_camera_transform[4] = global_state.camera_zoom;            
            ImGui.SliderFloat("camera movement acceleration", (value = global_state.camera_movement_acceleration) => global_state.camera_movement_acceleration = value, 8.0, 20.0);
            ImGui.ColorEdit3("clear color", clear_color); 
            ImGui.ColorEdit3("triangle color", global_state.triangle_color);
            ImGui.Spacing();
            ImGui.Spacing();


            ImGui.Text("Displaying some text"); // T: Display some text
            ImGui.Checkbox("checkbox", (value = boolean_field) => boolean_field = value); // T: Edit bools
            ImGui.Checkbox("use_font", (value = use_font) => use_font = value);


            // T: This code make possible that when we have the pointer on the checkbox
            // we create the tooltip writing on it a string
            if (ImGui.IsItemHovered()) {
                ImGui.BeginTooltip();
                ImGui.Text("Hello!");
                ImGui.EndTooltip();
            }
            
            // ImGui.SliderFloat2("translation", translation, 0.0, 500.0);
            // // ImGui.SliderFloat2("scale", scale, 1.0, 100.0);
            // global_state.buffer_camera_transform[0] = scale.x;
            // global_state.buffer_camera_transform[4] = scale.x;
            // global_state.buffer_camera_transform[6] = -translation.x;
            // global_state.buffer_camera_transform[7] = translation.y;
            if (ImGui.Button("Increment counter")) 
                counter++;
            ImGui.SameLine();
            ImGui.Text(`counter = ${counter}`);
            ImGui.Text(`Application average ${(1000.0 / ImGui.GetIO().Framerate).toFixed(3)} ms/frame (${ImGui.GetIO().Framerate.toFixed(1)} FPS)`);



            // ImGui.End();

            // ImGui.Begin("aaaaaaaaaaa");

            // ImGui.End();
        }



        ImGui.EndFrame();
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

            function (imgui_memory_editor_js_1_1) {
                imgui_memory_editor_js_1 = imgui_memory_editor_js_1_1;
            }
        ],

        // T: Variables can be initialized here (START)
        execute: function () {
            font = null;

            boolean_field = false;

            use_font = false;

            // translation = new ImGui.Vec2(250.0 , 0.0);
            // scale = new ImGui.Vec2(50.0, 50.0);

            clear_color = new ImGui.Vec4(0.45, 0.55, 0.60, 1.00);
            memory_editor = new imgui_memory_editor_js_1.MemoryEditor();
            memory_editor.Open = false;
            
            f = 0.0;
            counter = 0;

        }
        // T: Variables can be initialized here (END)
    };
});