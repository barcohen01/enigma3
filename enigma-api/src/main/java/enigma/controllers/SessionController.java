package enigma.controllers;

import enigma.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> openSession(@RequestBody Map<String, String> body) {
        String machineName = body.get("machine");

        try {
            String sessionID = sessionService.createSession(machineName);
            return ResponseEntity.ok(Map.of("sessionID", sessionID));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> closeSession(@RequestParam("sessionID") String sessionID) {
        try {
            sessionService.closeSession(sessionID);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}