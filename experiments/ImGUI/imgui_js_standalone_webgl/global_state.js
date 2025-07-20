"use strict";
class Global_State
{
    // T: Public fields (START)
    field;
    
    gl_glob;

    position_attribute_location;
    color_attribute_location;
    resolution_uniform_location;

    triangle_color;
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
        // T: Init the Global_State
        this.field = 0;

        this.triangle_color = {x: 0, y: 0, z: 0};
    }



    static #instance;
}

const global_state = Global_State.get();