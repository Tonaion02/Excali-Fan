package com.example.restservice.Board;

import java.util.List;

public class Command {

    public static class Pair {

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int first;
        public int second;
    }

    public static class Line {

        public Line(String color) {
            this.color = color;
        }

        public String color;
        public List<Pair> points;
    }

    public Command(String userId, String groupId, long timestamp) {
        this.userId = userId;
        this.groupId = groupId;
        this.timestamp = timestamp;
    }

    public String userId;
    public String groupId;
    public long timestamp;
    public Line line;
}