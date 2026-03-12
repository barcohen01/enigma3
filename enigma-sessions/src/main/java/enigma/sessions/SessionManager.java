package enigma.sessions;

import enigma.engine.Engine;
import enigma.manager.MachineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SessionManager {

    @Autowired
    private MachineManager machineManager;

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public String openSession(String machineName, Engine engine) {
        UUID machineUuid = machineManager.getMachineUuid(machineName);
        SessionData sessionData = new SessionData(machineName, engine, machineUuid);
        sessions.put(sessionData.getSessionId(), sessionData);
        return sessionData.getSessionId();
    }

    public SessionData getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public List<Engine> getEnginesByMachineName(String machineName) {
        return sessions.values().stream()
                .filter(s -> s.getMachineName().equals(machineName))
                .map(SessionData::getEngine)
                .collect(Collectors.toList());
    }
}