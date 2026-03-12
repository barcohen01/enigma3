package enigma.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MachineConfigDTO {
    public int totalRotors;
    public int totalReflectors;
    public int totalProcessedMessages;

    public FullCodeDTO originalCode;
    public FullCodeDTO currentRotorsPosition;

    public String originalCodeCompact;
    public String currentRotorsPositionCompact;

    public static class FullCodeDTO {
        public List<RotorDTO> rotors;
        public String reflector;
        public List<PlugDTO> plugs;
    }

    public static class RotorDTO {
        public int rotorNumber;
        public String rotorPosition;
        public int notchDistance;
    }

    public static class PlugDTO {
        public String plug1;
        public String plug2;
    }
}