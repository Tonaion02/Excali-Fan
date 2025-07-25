"use strict";
class Global_State
{
    // T: Public fields (START)
    field;
    
    canvas;
    gl_glob;

    position_attribute_location;
    color_attribute_location;
    resolution_uniform_location;
    translation_uniform_location;
    rotation_uniform_location;
    
    camera_transform_uniform_location;
    camera_movement_acceleration;
    camera_zoom;

    state_keys;

    buffer_camera_transform;
    triangle_color;
    triangle_translation;
    angle;
    position_buffer;
    color_buffer;

    paths;
    // T: Public fields (END)

    static get()
    {
        // T: OPTIMIZE: we can easily optimize this away splitting in two methods(init and get)
        if(! Global_State.#instance)
        {
            Global_State.#instance = new Global_State();
        }
            
        return Global_State.#instance;
    }

    constructor()
    {
        // T: Init the Global_State (START)
        this.field = 0;

        this.triangle_color = {x: 0, y: 0, z: 0};
        this.triangle_translation = [0, 0];
        this.angle = 0;

        this.buffer_camera_transform = new Float32Array(3 * 3);
        this.buffer_camera_transform[0] = 1.0;
        this.buffer_camera_transform[1] = 0.0;
        this.buffer_camera_transform[2] = 0.0;
        this.buffer_camera_transform[3] = 0.0;
        this.buffer_camera_transform[4] = 1.0;
        this.buffer_camera_transform[5] = 0.0;
        this.buffer_camera_transform[6] = 0.0;
        this.buffer_camera_transform[7] = 0.0;
        this.buffer_camera_transform[8] = 1.0;
        this.camera_movement_acceleration = 8.0;
        this.camera_zoom = 1.0;

        this.state_keys = {};

        this.paths = new Map();
        this.paths.set("path_to_fonts", "fonts");
        // T: Init the Global_State (END)
    }

    join_path(path_name, string_to_join)
    {
        let path = this.paths.get(path_name);
        if(path == undefined)
        {
            console.error(`Error: {path_name} is not mapped to a path`);
            return undefined;
        }

        return path + "/" + string_to_join;
    }

    static #instance;
}

const global_state = Global_State.get();