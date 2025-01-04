package com.example.restservice.Board;

import java.util.List;

public class Command {

    public static class Pair {

        public Pair(double first, double second) {
            this.first = first;
            this.second = second;
        }

        public double first;
        public double second;
    }

    public Command(String userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String userId;
    public long timestamp;
    public List<Pair> points;
}