package enigma.engine;

import enigma.dto.PlugPairDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManualCodeValidator {

    private final int requiredNumOfRotors;

    public ManualCodeValidator(int requiredNumOfRotors) {
        this.requiredNumOfRotors = requiredNumOfRotors;
    }

    public ManualCodeErrorType validate(List<Integer> rotorIds,
                                        String windowLetters,
                                        int reflectorMenuIndex,
                                        String abc,
                                        List<Integer> availableRotorIds,
                                        List<String> reflectorIds,
                                        List<PlugPairDTO> plugboardPairs) {

        // ---------- rotors ----------

        if (rotorIds.size() != requiredNumOfRotors) {
            return ManualCodeErrorType.ROTORS_COUNT_MISMATCH;
        }

        for (int id : rotorIds) {
            if (!availableRotorIds.contains(id)) {
                return ManualCodeErrorType.ROTOR_ID_OUT_OF_RANGE;
            }
        }

        Set<Integer> seen = new HashSet<>();
        for (int id : rotorIds) {
            if (!seen.add(id)) {
                return ManualCodeErrorType.ROTOR_ID_DUPLICATE;
            }
        }

        // ---------- window letters ----------

        if (windowLetters.length() != requiredNumOfRotors) {
            return ManualCodeErrorType.WINDOW_LENGTH_MISMATCH;
        }

        for (int i = 0; i < windowLetters.length(); i++) {
            char c = windowLetters.charAt(i);
            if (abc.indexOf(c) == -1) {
                return ManualCodeErrorType.WINDOW_LETTER_NOT_IN_ABC;
            }
        }

        // ---------- reflector ----------

        if (reflectorMenuIndex < 1 || reflectorMenuIndex > reflectorIds.size()) {
            return ManualCodeErrorType.REFLECTOR_INDEX_OUT_OF_RANGE;
        }

        // ---------- plugboard (new) ----------

        if (plugboardPairs != null) {
            Set<Character> usedChars = new HashSet<>();

            for (PlugPairDTO pair : plugboardPairs) {
                char a = pair.getLeft();
                char b = pair.getRight();

                if (abc.indexOf(a) == -1 || abc.indexOf(b) == -1) {
                    return ManualCodeErrorType.PLUG_CHAR_NOT_IN_ABC;
                }

                if (a == b) {
                    return ManualCodeErrorType.PLUG_SELF_MAPPING;
                }

                if (!usedChars.add(a) || !usedChars.add(b)) {
                    return ManualCodeErrorType.PLUG_CHAR_DUPLICATE;
                }
            }
        }

        return ManualCodeErrorType.NONE;
    }
}
