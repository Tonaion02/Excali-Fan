package com.example.restservice.Board;

import java.util.List;
import java.util.ArrayList;

public class Board {

    public Board() {
        this.lines = new ArrayList<>();
    }

    public Board(List<Line> lines, String ownerUserId, String hostUserId) {
        this.lines = lines;
        this.ownerUserId = ownerUserId;
        this.hostUserId = hostUserId;
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

    public String getHostUserId() {
        return this.hostUserId;
    }

    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    public List<Line> lines;
    // T: It is the email of the user that created the board
    public String ownerUserId;
    // T: It is the userId(email + random number generated from client)
    public String hostUserId;
}