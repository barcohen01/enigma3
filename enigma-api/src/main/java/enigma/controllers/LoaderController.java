package enigma.controllers;

import enigma.engine.EnigmaConfigurationException;
import enigma.services.LoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoaderController {

    @Autowired
    private LoaderService machineService;

    @PostMapping(value = "/load", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Map<String, Object>> loadMachine(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            String machineName = machineService.loadMachine(file);

            response.put("success", true);
            response.put("name", machineName);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (EnigmaConfigurationException e) {
            response.put("success", false);
            response.put("error", machineService.getErrorMessage(e.getErrorType()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}