class Global_State
{
    "use strict";

    // T: Public fields (START)
    field;
    // T: Public fields (END)



    static #instance;

    constructor()
    {
        if(Global_State.#instance)
            return Global_State.#instance;
        Global_State.#instance = this;

        // T: Init the Global_State
        field = 0;
    }
}