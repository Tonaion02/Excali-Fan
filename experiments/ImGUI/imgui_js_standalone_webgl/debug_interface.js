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
    var imgui_demo_js_1, imgui_memory_editor_js_1, font, memory_editor, show_sandbox_window, show_gamepad_window, show_movie_window, f, counter, done, source, image_urls, image_url, image_element, image_gl_texture, video_urls, video_url, video_element, video_gl_texture, video_w, video_h, video_time_active, video_time, video_duration;
    // var ImGui, ImGui_Impl, imgui_demo_js_1, imgui_memory_editor_js_1, font, clear_color, memory_editor, show_sandbox_window, show_gamepad_window, show_movie_window, f, counter, done, source, image_urls, image_url, image_element, image_gl_texture, video_urls, video_url, video_element, video_gl_texture, video_w, video_h, video_time_active, video_time, video_duration;
    
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
            ImGui.ColorEdit3("triangle color", global_state.triangle_color);
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