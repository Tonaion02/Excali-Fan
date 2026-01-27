package com.example.restservice.Board;

public class CloseBoardCommand extends Command {
    
    public CloseBoardCommand(String userId, String groupId, long timestamp) {
        super(userId, groupId, timestamp);
    }
}