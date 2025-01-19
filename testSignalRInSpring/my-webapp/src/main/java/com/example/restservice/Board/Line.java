package com.example.restservice.Board;

import java.util.List;





public class Line {
    
    public static class Pair {

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int first;
        public int second;
    }

    public Line(String color, List<Pair> points, String userId, long timestamp) {
        this.color = color;
        this.points = points;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object other) {

        if(other == this)
            return true;
        
        if(other instanceof Line) {
            Line otherLine = (Line) other;
            if(this.getId().equals(otherLine.getId()))
                return true;
        }

        return false;
    }

    // T: WARNING, we don't know if this implementation works exactly
    @Override
    public int hashCode() {
        final int idAsLong = (int)Long.parseLong(this.getId());
        return idAsLong;
    }

    public String getId() {
        String id = userId + Long.toString(timestamp);
        return id;
    }

    public String userId;
    public long timestamp;
    public String color;
    public List<Pair> points;
}