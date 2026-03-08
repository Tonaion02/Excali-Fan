package com.example.restservice.serviceSignalR;
 
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import java.time.Instant;
import java.time.Duration;





@Component
public class GarbageBoardCollector {

    private final SignalRController controller;
    // T: This is the constant that is used after how much time in 
    // seconds after which the board is closed.
    private final long maxHostInactiveTime = 300;

    public GarbageBoardCollector(SignalRController controller) {
        this.controller = controller;
    }

    // T: This method runs each 5 seconds to collect the boards that are garbage
    @Scheduled(fixedDelay = 20000)
    public void garbage_collect() {
        // T: DEBUG
        System.out.println("Running GarbageBoardCollector at: " + System.currentTimeMillis());
        System.out.println("boards from GarbageBoardCollector point of view: " + controller.boards);

        if(controller.boards != null)
        {
            for (Map.Entry<String, Board> entry: controller.boards.boards.entrySet())
            {
                String boardId = entry.getKey();
                String board = entry.getValue();



                if(board != null)
                {
                    synchronized(board)
                    {
                        Instant current_time = Instant.now();
                        Duration elapsed_time = Duration.between(board.instantLastMod, current_time);

                        // T: DEBUG
                        System.out.println("board: " + boardId + " seconds passed: " + elapsed_time.getSeconds());
                        if(elapsed_time.getSeconds() >= maxHostInactiveTime)
                        {
                            // T: DEBUG
                            System.out.println("Closing board: " + boardId);
                        }
                    }
                }
            }
        }
    }
}