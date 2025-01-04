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

    public Command(String userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String userId;
    public long timestamp;
    public List<Pair> points;
}