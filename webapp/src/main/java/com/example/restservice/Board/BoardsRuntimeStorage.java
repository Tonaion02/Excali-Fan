package com.example.restservice.Board;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BoardsRuntimeStorage {

    public final ConcurrentHashMap<String, Board> boards;

    public BoardsRuntimeStorage() {
        this.boards = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Board> getBoards() {
        return boards;
    }
}