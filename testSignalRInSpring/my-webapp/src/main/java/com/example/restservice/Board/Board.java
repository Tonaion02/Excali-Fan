package com.example.restservice.Board;

import java.util.List;
import java.util.ArrayList;

public class Board {

    public Board() {
        this.lines = new ArrayList<>();
    }

    public Board(List<Line> lines) {
        this.lines = lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Line> lines;
}