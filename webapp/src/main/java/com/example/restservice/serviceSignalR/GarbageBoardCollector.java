import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.restservice.serviceSignalR.*;

@Component
public class GarbageBoardCollector {

    private final SignalRContoller contoller;

    public GarbageBoardCollector(SignalRContoller contoller) {
        this.contoller = controller;
    }

    // T: This method runs each 5 seconds to collect the boards that are garbage
    @Scheduled(fixedDelay = 20000)
    public void garbage_collect() {
        System.out.println("Running GarbageBoardCollector at: " + System.currentTimeMillis());

        System.out.println("boards from GarbageBoardCollector point of view: " + contoller.boards);
    }
}