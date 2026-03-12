package enigma.dto;

import java.util.List;

public class ManualConfigDTO {
    public String sessionID;
    public List<RotorSelectionDTO> rotors;
    public String reflector;
    public List<PlugDTO> plugs;

    public static class RotorSelectionDTO {
        public int rotorNumber;
        public String rotorPosition;
    }

    public static class PlugDTO {
        public String plug1;
        public String plug2;
    }
}