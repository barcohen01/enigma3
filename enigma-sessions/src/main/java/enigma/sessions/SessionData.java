package enigma.sessions;
import enigma.engine.Engine;
import java.util.UUID;

public class SessionData {
    private final String sessionId;
    private final String machineName;
    private final Engine engine;
    private final UUID machineUuid;

    public SessionData(String machineName, Engine engine, UUID machineUuid) {
        this.sessionId = UUID.randomUUID().toString();
        this.machineName = machineName;
        this.engine = engine;
        this.machineUuid = machineUuid;
    }

    public UUID getMachineUuid() { return machineUuid; }

    // Getters
    public String getSessionId() { return sessionId; }
    public Engine getEngine() { return engine; }
    public String getMachineName() { return machineName; }
}