package enigma.controllers;

import enigma.dto.ProcessResultDTO;
import enigma.engine.MachineProcessErrorType;
import enigma.engine.MachineProcessException;
import enigma.services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProcessController {

    @Autowired
    private ProcessService processService;

    @PostMapping(value = "/process", produces = "application/json")
    public ResponseEntity<?> processMessage(
            @RequestParam("sessionID") String sessionID,
            @RequestParam("input") String input) {

        try {
            ProcessResultDTO response = processService.process(sessionID, input);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (MachineProcessException e) {
            String errorMsg = (e.getErrorType() == MachineProcessErrorType.INVALID_INPUT_CHAR)
                    ? "input contains characters not in the machine's ABC."
                    : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMsg));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}