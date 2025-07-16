// T: this is a global variable in ImGui, you can directly extract that from the interface.js file
// and then you can use that where you want.
// T: probably i will put all this global variable in a global_variable files, probably config 
// can be something like that.
var ImGui, ImGui_Impl;
var boolean_field, use_font, clear_color;

var imgui_init;
var imgui_start_frame;
var imgui_end_frame;

function here() {

    // T: this is a perfect example of how you can directly put code for ImGui out of that file
    const io = ImGui.GetIO();
    io.Fonts.AddFontDefault();
}

var debug = true;