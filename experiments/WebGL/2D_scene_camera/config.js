"use strict";
//==============================================================================
//------------------------------------------------------------------------------
// PERSONAL VARIABLES IMGUI
//------------------------------------------------------------------------------
// T: Even if this variable are in debug_interface.js and are defined here, they
// can't be really used outside, due to a problem caused by this implementation
// of ImGUI for Javascript. 
// For major info read the description at the begin of debug_interface.js file.
var ImGui, ImGui_Impl;
//==============================================================================

var boolean_field, use_font;
var clear_color = {x: 0, y: 0, z: 0, w: 0}

var debug = true;