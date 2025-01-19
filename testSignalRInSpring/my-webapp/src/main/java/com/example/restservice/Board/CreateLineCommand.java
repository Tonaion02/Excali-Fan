package com.example.restservice.Board;

public class CreateLineCommand extends Command {
    
    public CreateLineCommand(String userId, String groupId, long timestamp, Line line) {
        super(userId, groupId, timestamp);

        this.line = line;
    }

    public Line line;
}
