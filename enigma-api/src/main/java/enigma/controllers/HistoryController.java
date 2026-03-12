package enigma.controllers;

import enigma.dto.MessageLogDTO;
import enigma.services.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping(value = "/history", produces = "application/json")
    public ResponseEntity<?> getHistory(
            @RequestParam(value = "sessionID", required = false) String sessionID,
            @RequestParam(value = "machineName", required = false) String machineName) {

        if ((sessionID == null && machineName == null) || (sessionID != null && machineName != null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Exactly one of sessionID or machineName must be provided"));
        }

        try {
            Map<String, List<MessageLogDTO>> historyMap = historyService.getHistory(sessionID, machineName);
            return ResponseEntity.ok(historyMap);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}