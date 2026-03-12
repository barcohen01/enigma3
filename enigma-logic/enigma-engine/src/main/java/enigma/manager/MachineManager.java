package enigma.manager;

import enigma.engine.Engine;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MachineManager {

    private static class MachineEntry {
        final Engine engine;
        final UUID uuid;

        MachineEntry(Engine engine, UUID uuid) {
            this.engine = engine;
            this.uuid = uuid;
        }
    }

    private final Map<String, MachineEntry> machines = new ConcurrentHashMap<>();

    public void addMachine(String name, UUID uuid, Engine engine) throws Exception {
        if (machines.containsKey(name)) {
            throw new Exception("Machine with name '" + name + "' already exists.");
        }
        machines.put(name, new MachineEntry(engine, uuid));
    }

    public Engine getMachine(String name) {
        MachineEntry entry = machines.get(name);
        return entry != null ? entry.engine : null;
    }

    public UUID getMachineUuid(String name) {
        MachineEntry entry = machines.get(name);
        return entry != null ? entry.uuid : null;
    }

    public boolean exists(String name) {
        return machines.containsKey(name);
    }
}