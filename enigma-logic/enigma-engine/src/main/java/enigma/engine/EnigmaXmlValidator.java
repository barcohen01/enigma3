package enigma.engine;

import enigma.xml.data.*;

import java.util.*;

public class EnigmaXmlValidator {

    private int requiredNumOfRotors;

    public void validate(String path, BTEEnigma enigmaXML) {
        validateInternal(enigmaXML);
    }

    public void validateInternal(BTEEnigma enigmaXML) {
        String abc = enigmaXML.getABC().trim();
        requiredNumOfRotors = enigmaXML.getRotorsCount().intValue();

        validateAbc(abc);
        validateRotors(enigmaXML.getBTERotors().getBTERotor(), abc);
        validateReflectors(enigmaXML.getBTEReflectors().getBTEReflector(), abc);
    }
    /* ------------ File extension validation ------------ */

    public void ValidateExtension(String path) {
        if (path == null || !path.toLowerCase().endsWith(".xml")) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.INVALID_EXTENSION);
        }
    }

    /* ------------ ABC validation ------------ */

    private void validateAbc(String abc) {
        // ABC length must be even
        if (abc.length() % 2 != 0) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.INVALID_ABC_SIZE);
        }

        // No duplicate characters in ABC
        Set<Character> seen = new HashSet<>();
        for (char c : abc.toCharArray()) {
            if (!seen.add(c)) {
                throw new EnigmaConfigurationException(LoadXmlErrorType.DUPLICATE_ABC);
            }
        }
    }

    /* ------------ Rotors validation ------------ */

    private void validateRotors(List<BTERotor> rotors, String abc) {
        int abcSize = abc.length();

        // Ensure minimum required number of rotors
        if (rotors == null || rotors.size() < requiredNumOfRotors) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.NOT_ENOUGH_ROTORS);
        }

        if (requiredNumOfRotors == 0) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.ROTORS_COUNT_ZERO);
        }

        // Rotor IDs must be consecutive: 1..N
        List<Integer> ids = rotors.stream()
                .map(r -> r.getId())
                .sorted()
                .toList();

        if (ids.get(0) != 1) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_IDS_NOT_CONSECUTIVE);
        }
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i) != ids.get(i - 1) + 1) {
                throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_IDS_NOT_CONSECUTIVE);
            }
        }

        // Validate each rotor
        for (BTERotor rotor : rotors) {
            int notch = rotor.getNotch();
            if (notch < 1 || notch > abcSize) {
                throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_INVALID_NOTCH);
            }

            List<BTEPositioning> positions = rotor.getBTEPositioning();
            // Must contain exactly |ABC| mappings
            if (positions.size() != abcSize) {
                throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_MAPPING_NOT_FULL);
            }

            Set<Character> rightSeen = new HashSet<>();
            Set<Character> leftSeen = new HashSet<>();

            for (BTEPositioning pos : positions) {
                char right = pos.getRight().charAt(0);
                char left = pos.getLeft().charAt(0);

                // Character must exist in ABC
                if (abc.indexOf(right) == -1 || abc.indexOf(left) == -1) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_MAPPING_INVALID_LETTER);
                }

                // Duplicate mapping not allowed
                if (!rightSeen.add(right) || !leftSeen.add(left)) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.ROTOR_DUPLICATE_MAPPING);
                }
            }
        }
    }

    /* ------------ Reflectors validation ------------ */

    private void validateReflectors(List<BTEReflector> reflectors, String abc) {
        if (reflectors == null || reflectors.isEmpty()) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.NO_REFLECTORS);
        }

        int abcSize = abc.length();

        // Reflector IDs must be consecutive Roman numerals (I, II, III...)
        List<Integer> ids = reflectors.stream()
                .map(r -> romanToInt(r.getId()))
                .sorted()
                .toList();

        if (ids.get(0) != 1) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_IDS_NOT_CONSECUTIVE);
        }
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i) != ids.get(i - 1) + 1) {
                throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_IDS_NOT_CONSECUTIVE);
            }
        }

        // Validate each reflector
        for (BTEReflector reflector : reflectors) {

            // Index mapping: mapping[index] = pairedIndex
            int[] mapping = new int[abcSize + 1];
            Arrays.fill(mapping, -1);

            for (BTEReflect ref : reflector.getBTEReflect()) {

                // input/output are INT values
                int input = ref.getInput();
                int output = ref.getOutput();

                // Validate range 1..|ABC|
                if (input < 1 || input > abcSize || output < 1 || output > abcSize) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_INVALID_LETTER);
                }

                // No mapping of a position to itself
                if (input == output) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_SELF_MAPPING);
                }

                // A position cannot appear more than once
                if (mapping[input] != -1 || mapping[output] != -1) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_DUPLICATE_MAPPING);
                }

                // Symmetric mapping
                mapping[input] = output;
                mapping[output] = input;
            }

            // Ensure full coverage: every ABC index must have a partner
            for (int i = 1; i <= abcSize; i++) {
                if (mapping[i] == -1) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_NOT_FULL_MAPPING);
                }
            }

            // Validate symmetry: mapping[mapping[i]] == i
            for (int i = 1; i <= abcSize; i++) {
                int mapped = mapping[i];
                if (mapping[mapped] != i) {
                    throw new EnigmaConfigurationException(LoadXmlErrorType.REFLECTOR_NOT_SYMMETRIC);
                }
            }
        }
    }

    /* ------------ Utility: Roman numeral to integer ------------ */

    private int romanToInt(String roman) {
        return switch (roman) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> throw new EnigmaConfigurationException(LoadXmlErrorType.INVALID_REFLECTOR_ID);
        };
    }

}