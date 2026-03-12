package enigma.services;

import enigma.dto.*;
import enigma.engine.Engine;
import enigma.engine.ManualCodeErrorType;
import enigma.sessions.SessionData;
import enigma.sessions.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigService {

    @Autowired
    private SessionManager sessionManager;

    public MachineConfigDTO getFullConfig(String sessionID, boolean verbose) {
        SessionData session = sessionManager.getSession(sessionID);
        if (session == null) return null;

        Engine engine = session.getEngine();
        MachineConfigDTO config = new MachineConfigDTO();

        config.totalRotors = engine.getNumOfRotorsInSystem();
        config.totalReflectors = engine.getNumOfReflectorsInSystem();
        config.totalProcessedMessages = engine.getCountOfMessagesThatWasSentToCurrentMachine();

        config.originalCodeCompact = formatCodeConfiguration(engine.getOriginalCodeData());
        config.currentRotorsPositionCompact = formatOnlyRotors(engine.getCurrentCodeData());

        if (verbose) {
            config.originalCode = mapToFullCodeDTO(engine.getOriginalCodeData());
            config.currentRotorsPosition = mapToFullCodeDTO(engine.getCurrentCodeData());
        }
        return config;
    }

    public ManualCodeResultDTO setManualConfig(ManualConfigDTO request) {
        SessionData session = sessionManager.getSession(request.sessionID);
        if (session == null) return null;

        List<Integer> rotorIds = new ArrayList<>();
        StringBuilder windowLetters = new StringBuilder();

        for (ManualConfigDTO.RotorSelectionDTO r : request.rotors) {
            rotorIds.add(r.rotorNumber);
            windowLetters.append(r.rotorPosition.toUpperCase());
        }

        List<PlugPairDTO> enginePlugs = new ArrayList<>();
        if (request.plugs != null) {
            for (ManualConfigDTO.PlugDTO p : request.plugs) {
                enginePlugs.add(new PlugPairDTO(p.plug1.toUpperCase().charAt(0), p.plug2.toUpperCase().charAt(0)));
            }
        }

        int reflectorIndex = romanToInt(request.reflector);

        return session.getEngine().setManualCode(
                rotorIds,
                windowLetters.toString(),
                reflectorIndex,
                enginePlugs
        );
    }

    public String getManualCodeErrorMessage(ManualCodeErrorType type, Engine engine) {
        return switch (type) {
            case ROTORS_COUNT_MISMATCH -> "number of rotors does not match. Please choose " + engine.GetNumOfRotors() + " rotors";
            case ROTOR_ID_OUT_OF_RANGE -> "one or more rotor IDs are out of range.";
            case ROTOR_ID_DUPLICATE -> "a rotor ID appears more than once.";
            case WINDOW_LENGTH_MISMATCH -> "number of window letters must match the number of rotors.";
            case WINDOW_LETTER_NOT_IN_ABC -> "one or more window letters are not part of the current ABC.";
            case REFLECTOR_INDEX_OUT_OF_RANGE -> "invalid reflector choice.";
            case PLUG_CHAR_NOT_IN_ABC -> "one or more plugboard characters are not part of the current ABC.";
            case PLUG_SELF_MAPPING -> "plugboard cannot map a character to itself.";
            case PLUG_CHAR_DUPLICATE -> "a plugboard character is used in more than one pair.";
            default -> "failed to set manual code configuration.";
        };
    }

    private int romanToInt(String reflectorStr) {
        return switch (reflectorStr.toUpperCase()) {
            case "A", "I" -> 1;
            case "B", "II" -> 2;
            case "C", "III" -> 3;
            case "D", "IV" -> 4;
            case "E", "V" -> 5;
            default -> throw new IllegalArgumentException("Invalid reflector ID: " + reflectorStr);
        };
    }

    private String formatOnlyRotors(CodeData codeData) {
        if (codeData == null) return "N/A";
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < codeData.machineWindowLetters.size(); i++) {
            parts.add(codeData.machineWindowLetters.get(i) + "(" + codeData.stepsBetweenPeekWindowAndNotch.get(i) + ")");
        }
        return String.join(",", parts);
    }

    private String formatCodeConfiguration(CodeData codeData) {
        if (codeData == null) return "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < codeData.rotorsIDByPos.size(); i++) {
            sb.append(codeData.rotorsIDByPos.get(i));
            if (i < codeData.rotorsIDByPos.size() - 1) sb.append(',');
        }
        sb.append('>');

        sb.append('<');
        for (int i = 0; i < codeData.machineWindowLetters.size(); i++) {
            sb.append(codeData.machineWindowLetters.get(i))
                    .append('(').append(codeData.stepsBetweenPeekWindowAndNotch.get(i)).append(')');
            if (i < codeData.machineWindowLetters.size() - 1) sb.append(',');
        }
        sb.append('>');

        sb.append('<').append(codeData.reflectorRomanID).append('>');

        if (codeData.plugboardPairs != null && !codeData.plugboardPairs.isEmpty()) {
            sb.append('<');
            for (int i = 0; i < codeData.plugboardPairs.size(); i++) {
                PlugPairDTO pair = codeData.plugboardPairs.get(i);
                sb.append(pair.getLeft()).append('|').append(pair.getRight());
                if (i < codeData.plugboardPairs.size() - 1) sb.append(',');
            }
            sb.append('>');
        }
        return sb.toString();
    }

    private MachineConfigDTO.FullCodeDTO mapToFullCodeDTO(CodeData codeData) {
        if (codeData == null) return null;

        MachineConfigDTO.FullCodeDTO fullCode = new MachineConfigDTO.FullCodeDTO();
        fullCode.reflector = codeData.reflectorRomanID;

        fullCode.rotors = new ArrayList<>();
        for (int i = 0; i < codeData.rotorsIDByPos.size(); i++) {
            MachineConfigDTO.RotorDTO r = new MachineConfigDTO.RotorDTO();
            r.rotorNumber = codeData.rotorsIDByPos.get(i);
            r.rotorPosition = String.valueOf(codeData.machineWindowLetters.get(i));
            r.notchDistance = codeData.stepsBetweenPeekWindowAndNotch.get(i);
            fullCode.rotors.add(r);
        }

        fullCode.plugs = new ArrayList<>();
        if (codeData.plugboardPairs != null) {
            for (PlugPairDTO pair : codeData.plugboardPairs) {
                MachineConfigDTO.PlugDTO p = new MachineConfigDTO.PlugDTO();
                p.plug1 = String.valueOf(pair.getLeft());
                p.plug2 = String.valueOf(pair.getRight());
                fullCode.plugs.add(p);
            }
        }
        return fullCode;
    }
}