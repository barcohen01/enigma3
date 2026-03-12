package enigma.services;

import enigma.dto.MessageLogDTO;
import enigma.engine.Engine;
import enigma.history.CodeHistoryEntry;
import enigma.sessions.SessionData;
import enigma.sessions.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    @Autowired
    private SessionManager sessionManager;

    public Map<String, List<MessageLogDTO>> getHistory(String sessionID, String machineName) {
        Map<String, List<MessageLogDTO>> historyMap = new LinkedHashMap<>();

        if (sessionID != null) {
            SessionData session = sessionManager.getSession(sessionID);
            if (session == null) {
                throw new NoSuchElementException("Unknown sessionID: " + sessionID);
            }
            fillMapFromEngine(historyMap, session.getEngine());
        } else {
            List<Engine> engines = sessionManager.getEnginesByMachineName(machineName);
            if (engines == null || engines.isEmpty()) {
                throw new NoSuchElementException("No machine found with name: " + machineName);
            }
            for (Engine engine : engines) {
                fillMapFromEngine(historyMap, engine);
            }
        }

        return historyMap;
    }

    private void fillMapFromEngine(Map<String, List<MessageLogDTO>> map, Engine engine) {
        List<CodeHistoryEntry> historyCodes = engine.GetCodeHistory();

        if (historyCodes == null || historyCodes.isEmpty()) {
            return;
        }

        for (CodeHistoryEntry codeEntry : historyCodes) {
            String configKey = codeEntry.getCodeData().toString();

            List<MessageLogDTO> messages = codeEntry.getProcessedStrings().stream()
                    .map(msg -> new MessageLogDTO(
                            msg.getInput(),
                            msg.getOutput(),
                            msg.getTimeNano()
                    ))
                    .collect(Collectors.toList());

            map.computeIfAbsent(configKey, k -> new ArrayList<>()).addAll(messages);
        }
    }
}