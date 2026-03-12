package enigma.services;

import enigma.dto.CodeData;
import enigma.dto.ProcessResultDTO;
import enigma.engine.Engine;
import enigma.entities.ProcessingEntity;
import enigma.repositories.ProcessingRepository;
import enigma.sessions.SessionData;
import enigma.sessions.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessService {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ProcessingRepository processingRepository;

    @Transactional
    public ProcessResultDTO process(String sessionID, String input) throws Exception {
        SessionData session = sessionManager.getSession(sessionID);
        if (session == null) {
            throw new IllegalArgumentException("Unknown sessionID: " + sessionID);
        }

        Engine engine = session.getEngine();

        if (engine.getOriginalCodeData() == null) {
            throw new IllegalStateException("Machine code is not set. Please set config first.");
        }

        CodeData currentStateBefore = engine.getCurrentCodeData();
        String codeStateString = formatCompactRotors(currentStateBefore);

        long startTime = System.nanoTime();
        String result = engine.process(input.toUpperCase());
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        ProcessingEntity processingEntry = new ProcessingEntity();
        processingEntry.setId(UUID.randomUUID());

        processingEntry.setMachine_id(session.getMachineUuid());

        processingEntry.setSession_id(sessionID);
        processingEntry.setInput(input.toUpperCase());
        processingEntry.setOutput(result);
        processingEntry.setCode(codeStateString);
        processingEntry.setTime(duration);

        processingRepository.save(processingEntry);

        ProcessResultDTO response = new ProcessResultDTO();
        response.output = result;
        response.currentRotorsPositionCompact = formatCompactRotors(engine.getCurrentCodeData());

        return response;
    }

    private String formatCompactRotors(CodeData codeData) {
        if (codeData == null) return "N/A";
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < codeData.machineWindowLetters.size(); i++) {
            parts.add(codeData.machineWindowLetters.get(i) +
                    "(" + codeData.stepsBetweenPeekWindowAndNotch.get(i) + ")");
        }
        return String.join(",", parts);
    }
}