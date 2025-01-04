package com.example.restservice.Board;

import java.util.List;
import java.util.ArrayList;

public class Board {

    public Board() {
        this.commands = new ArrayList<>();
    }

    public List<Command> commands;
}