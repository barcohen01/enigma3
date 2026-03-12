package enigma.machine.component.plugboard;

import java.util.HashMap;
import java.util.Map;

public class Plugboard {

    private final Map<Character, Character> mapping = new HashMap<>();

    public void addPair(char a, char b) {
        mapping.put(a, b);
        mapping.put(b, a);
    }

    public char map(char c) {
        return mapping.getOrDefault(c, c);
    }

    public Map<Character, Character> getMapping() {
        return Map.copyOf(mapping);
    }

    public boolean isEmpty() {
        return mapping.isEmpty();
    }

    public Plugboard() {
    }

    public Plugboard(Plugboard other) {
        if (other != null) {
            this.mapping.putAll(other.mapping);
        }
    }
}