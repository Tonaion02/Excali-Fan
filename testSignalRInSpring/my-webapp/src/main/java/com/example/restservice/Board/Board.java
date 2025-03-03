package com.example.restservice.Board;

import java.util.List;
import java.util.ArrayList;

public class Board {

    public Board() {
        this.lines = new ArrayList<>();
    }

    public Board(List<Line> lines, String ownerUserId) {
        this.lines = lines;
        this.ownerUserId = ownerUserId;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public List<Line> lines;
    public String ownerUserId;
}