package enigma.controllers;

import enigma.dto.*;
import enigma.services.ConfigService;
import enigma.sessions.SessionData;
import enigma.sessions.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigurationController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private SessionManager sessionManager;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getMachineConfig(
            @RequestParam("sessionID") String sessionID,
            @RequestParam(value = "verbose", defaultValue = "false") boolean verbose) {

        MachineConfigDTO config = configService.getFullConfig(sessionID, verbose);
        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }
        return ResponseEntity.ok(config);
    }

    @PutMapping(value = "/automatic", produces = "text/plain")
    public ResponseEntity<String> setAutomaticConfig(@RequestParam("sessionID") String sessionID) {
        SessionData session = sessionManager.getSession(sessionID);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: Session not found.");
        }

        session.getEngine().codeAutomatic();
        return ResponseEntity.ok("Automatic code setup completed successfully");
    }

    @PutMapping(value = "/manual", produces = "text/plain")
    public ResponseEntity<String> setManualConfig(@RequestBody ManualConfigDTO request) {
        SessionData session = sessionManager.getSession(request.sessionID);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: Unknown sessionID: " + request.sessionID);
        }

        ManualCodeResultDTO result = configService.setManualConfig(request);

        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Session failed.");
        }

        if (result.isSuccess()) {
            return ResponseEntity.ok("Manual code set successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(configService.getManualCodeErrorMessage(result.getErrorType(), session.getEngine()));
        }
    }

    @PutMapping(value = "/reset", produces = "text/plain")
    public ResponseEntity<String> resetConfig(@RequestParam("sessionID") String sessionID) {
        SessionData session = sessionManager.getSession(sessionID);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Session not found.");
        }

        if (session.getEngine().getOriginalCodeData() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: You cannot perform this action because no code has been set.");
        }

        session.getEngine().ResetCodeToOriginalCode();
        return ResponseEntity.ok("Reset code completed successfully");
    }
}