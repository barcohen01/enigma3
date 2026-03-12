package enigma.services;

import enigma.engine.Engine;
import enigma.engine.Engineimpl;
import enigma.manager.MachineManager;
import enigma.sessions.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private MachineManager machineManager;

    public String createSession(String machineName) {
        if (machineName == null || !machineManager.exists(machineName)) {
            throw new IllegalStateException("Unknown machine name: " + machineName);
        }

        Engine engineTemplate = machineManager.getMachine(machineName);
        Engine newEngine = new Engineimpl((Engineimpl) engineTemplate);

        return sessionManager.openSession(machineName, newEngine);
    }

    public void closeSession(String sessionID) {
        if (sessionManager.getSession(sessionID) == null) {
            throw new IllegalArgumentException("Unknown sessionID: " + sessionID);
        }
        sessionManager.removeSession(sessionID);
    }
}