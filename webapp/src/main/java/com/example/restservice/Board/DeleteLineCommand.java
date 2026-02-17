package com.example.restservice.Board;





public class DeleteLineCommand extends Command {

    public DeleteLineCommand(String userId, String groupId, long timestamp, String userIdOfLine, long timestampOfLine) {
        super(userId, groupId, timestamp);

        this.timestampOfLine = timestampOfLine;
        this.userIdOfLine = userIdOfLine;
    }

    public String userIdOfLine;
    public long timestampOfLine;
}