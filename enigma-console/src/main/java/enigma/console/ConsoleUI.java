package enigma.console;

import enigma.dto.CodeData;
import enigma.engine.*;
import enigma.history.CodeHistoryEntry;
import enigma.history.ProcessedStringEntry;
import enigma.dto.ManualCodeResultDTO;
import  enigma.dto.PlugPairDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final Engine engine;
    private boolean isMachineLoaded = false;
    private boolean isCodeSet = false;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(Engine engine) {
        this.engine = engine;
    }

    void showMenu(){

        // Input validation
        while (true) {
            System.out.println("\n========== Enigma Machine - Main Menu ==========");
            System.out.println("1. Load machine XML file");
            System.out.println("2. Show machine specifications");
            System.out.println("3. Set manual code configuration");
            System.out.println("4. Set automatic code configuration");
            System.out.println("5. Process input");
            System.out.println("6. Reset current code");
            System.out.println("7. Show history & statistics");
            System.out.println("8. Exit");
            System.out.println("================================================");
            System.out.print("Please enter your choice (1-8): ");

            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);

                if (choice >= 1 && choice <= 8) {

                    if (choice == 8) {
                        break;
                    }
                    invokeUserChoice(choice);
                } else {
                    System.out.print("Invalid option. Please enter a number between 1 and 8: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number (1-8): ");
            }

        }
    }

    private void invokeUserChoice(int choice) {
        switch (choice) {
            case 1:
                loadXML();
                break;
            case 2:
                if (checkIfMachineIsLoaded()){
                    displayMachineSpecifications();
                }
                break;
            case 3:
                if (checkIfMachineIsLoaded()) {
                    getCodeManually();
                    isCodeSet=true;
                }
                break;
            case 4:
                if (checkIfMachineIsLoaded()) {
                    getCodeAuto();
                    isCodeSet=true;
                }
                break;
            case 5:
                if (checkIfMachineIsLoaded() && checkIfCodeIsSet()) {
                    processInput();
                }
                break;
            case 6:
                if (checkIfMachineIsLoaded() && checkIfCodeIsSet()) {
                    resetCode();
                }
                break;
            case 7:
                if (checkIfMachineIsLoaded()) {
                    printsHistory();
                }
                break;

        }
    }

    private Boolean checkIfCodeIsSet() {
        if (!isCodeSet) {
            System.out.println("You cannot perform this action because no code has been set");
        }

        return isCodeSet;
    }

    private Boolean checkIfMachineIsLoaded() {
        if (!isMachineLoaded) {
            System.out.println("You cannot perform this action because no XML file is loaded");
        }

        return isMachineLoaded;
    }

    private void printsHistory() {
        List<CodeHistoryEntry> historyCodes = engine.GetCodeHistory();

        int currCodeSerialNum = 1;

        if (historyCodes.isEmpty()) {
            System.out.println("No history found");
        }
        for  (CodeHistoryEntry codeHistoryEntry : historyCodes) {
            CodeData currCodeData = codeHistoryEntry.getCodeData();
            System.out.println("Code Number " + currCodeSerialNum + " configuration: " +
                    formatCodeConfiguration(currCodeData));
            List <ProcessedStringEntry> currCodeStrings = codeHistoryEntry.getProcessedStrings();

            int currStringSerialNum = 1;

            for   (ProcessedStringEntry processedStringEntry : currCodeStrings) {
                System.out.println(currStringSerialNum + ". <" + processedStringEntry.getInput() + "> --> <" + processedStringEntry.getOutput() + "> ("
                                + processedStringEntry.getTimeNano() +" nano-seconds)");
                currStringSerialNum++;
            }

            currCodeSerialNum++;
        }
    }

    private void getCodeAuto() {
        engine.codeAutomatic();
    }

    private void resetCode() {
        engine.ResetCodeToOriginalCode();
        System.out.println("Code reset to original configuration successfully.");
    }

    private void processInput() {

        System.out.println("Please enter string you want to encrypt/decrypt from current 'ABC' (" + engine.getCurrentAbc().trim() + "):");

        String input = scanner.nextLine().toUpperCase();

        try {
            String result = engine.process(input);
            System.out.println("The result is: " + result);
        }
        catch (MachineProcessException e) {
            if (e.getErrorType() == MachineProcessErrorType.INVALID_INPUT_CHAR) {
                System.out.println("Error: input contains characters not in the machine's ABC.");
            }
        }
    }

    private void getCodeManually() {

        System.out.println("Manual Code Configuration");
        System.out.println("-------------------------");

        // 1. read rotor IDs from user (syntax validation in helper)
        List<Integer> chosenRotorIds = readRotorIdsFromUser();
        if (chosenRotorIds == null) {
            // helper already printed error message
            return;
        }

        // 2. read window letters
        String windowLetters = readWindowLettersFromUser().toUpperCase();

        // 3. read reflector choice (syntax validation in helper)
        int reflectorChoice = readReflectorChoiceFromUser();
        if (reflectorChoice == -1) {
            // helper already printed error message
            return;
        }

        // 4. read plugboard pairs
        List<PlugPairDTO> plugboardPairs = readPlugboardPairsFromUser();
        if (plugboardPairs == null) {
            return;
        }

        // 5. logical validation in engine (via DTO)
        ManualCodeResultDTO result = engine.setManualCode(
                chosenRotorIds,
                windowLetters,
                reflectorChoice,
                plugboardPairs
        );

        // 5. handle result (success / error message)
        handleManualCodeResult(result);
    }

    // Reads rotor IDs from user input, validates SYNTAX (numbers & commas only).
// Returns List<Integer> on success, or null if syntax is invalid.
    private List<Integer> readRotorIdsFromUser() {
        System.out.println("Enter "+ engine.GetNumOfRotors() +" rotor IDs separated by commas from these options:");
        System.out.println("Note: First number = left in input, left-most rotor in machine.");

        int indexRotors = 0;
        List<Integer> rotorsIds = engine.GetRotorsIds();
        for (Integer rotorID : rotorsIds) {
            indexRotors++;
            if (indexRotors == rotorsIds.size()) {
                System.out.println(rotorID);
            } else {
                System.out.print(rotorID + ", ");
            }
        }

        String rotorsInput = scanner.nextLine().trim();

        List<Integer> chosenRotorIds = new ArrayList<>();
        try {
            String[] parts = rotorsInput.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    System.out.println("Invalid rotor list format. Please enter numbers separated by commas.");
                    return null;
                }
                int id = Integer.parseInt(trimmed);
                chosenRotorIds.add(id);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid rotor list format. Please enter numbers separated by commas.");
            return null;
        }

        return chosenRotorIds;
    }

    // Reads window letters string from user (no syntax validation beyond trimming).
    private String readWindowLettersFromUser() {
        System.out.println("\nEnter " +engine.GetNumOfRotors() +" initial rotor window letters (no spaces, no commas).");
        System.out.println("Note: First letter typed = left in input,");
        System.out.println("but matches the left-most rotor.");
        System.out.print("This is the current 'ABC': ");
        System.out.println(engine.getCurrentAbc());

        System.out.print("Window letters: ");
        return scanner.nextLine().trim();
    }

    // Reads reflector choice from user, validates SYNTAX (must be an integer).
// Returns reflector index (1-based), or -1 if syntax is invalid.
    private int readReflectorChoiceFromUser() {
        System.out.println("Type the number corresponding to the reflector you want to use: ");

        int index = 0;
        List<String> reflectorIds = engine.GetReflectorIds();
        for (String refID : reflectorIds) {
            index++;
            System.out.println(index + ". " + refID);
        }

        System.out.print("Your choice: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid reflector choice. Please enter a number.");
            return -1;
        }
    }

    // Handles the DTO returned from the engine for manual code configuration.
    private void handleManualCodeResult(ManualCodeResultDTO result) {
        if (result.isSuccess()) {
            System.out.println("Manual code configuration was set successfully.");

            CodeData current = engine.getCurrentCodeData();
            System.out.println("Current code configuration: " +
                    formatCodeConfiguration(current));
        } else {
            printManualCodeError(result.getErrorType());
        }
    }
    private void printManualCodeError(ManualCodeErrorType type) {
        switch (type) {
            case ROTORS_COUNT_MISMATCH ->
                    System.out.println("Error: number of rotors in the code does not match the machine's requirement. Please choose "
                            + engine.GetNumOfRotors() + " rotors");
            case ROTOR_ID_OUT_OF_RANGE ->
                    System.out.println("Error: one or more rotor IDs are out of range.");
            case ROTOR_ID_DUPLICATE ->
                    System.out.println("Error: a rotor ID appears more than once.");
            case WINDOW_LENGTH_MISMATCH ->
                    System.out.println("Error: number of window letters must match the number of rotors. Please choose "
                            + engine.GetNumOfRotors() + " letters");
            case WINDOW_LETTER_NOT_IN_ABC ->
                    System.out.println("Error: one or more window letters are not part of the current ABC.");
            case REFLECTOR_INDEX_OUT_OF_RANGE ->
                    System.out.println("Error: invalid reflector choice (index is out of range).");
            case PLUG_CHAR_NOT_IN_ABC ->
                    System.out.println("Error: one or more plugboard characters are not part of the current ABC.");
            case PLUG_SELF_MAPPING ->
                    System.out.println("Error: plugboard cannot map a character to itself.");
            case PLUG_CHAR_DUPLICATE ->
                    System.out.println("Error: a plugboard character is used in more than one pair.");
            case NONE ->
                    System.out.println("Error: failed to set manual code configuration.");
            default ->
                    System.out.println("Error: failed to set manual code configuration.");
        }
    }

    private void loadXML() {

        System.out.println("Please enter the full path to the XML file:");
        String filePath = scanner.nextLine().trim();

        try {
            engine.loadXml(filePath); // if no exception → configuration is valid

            System.out.println("XML file loaded successfully.");
            isMachineLoaded = true;
            isCodeSet = false; // reset code set flag on new load
        }
        catch (EnigmaConfigurationException e) {
            LoadXmlErrorType type = e.getErrorType();
            printLoadXmlError(type); // helper function
        }
        catch (Exception e) {
            System.out.println("Unexpected error while loading XML file.");
        }
    }

    private void displayMachineSpecifications(){
        System.out.println("Number of rotors in system: " + engine.getNumOfRotorsInSystem());
        System.out.println("Number of reflectors in system: " + engine.getNumOfReflectorsInSystem());
        System.out.println("Number of messages that sent to current machine:  " + engine.getCountOfMessagesThatWasSentToCurrentMachine());

        CodeData originalCode = engine.getOriginalCodeData();

        if (originalCode == null) {
            System.out.println("Original code configuration: N/A (no code selected yet)");
        } else {
            String formatted = formatCodeConfiguration(originalCode);
            System.out.println("Original code configuration: " + formatted);
        }

        CodeData current = engine.getCurrentCodeData();

        if (current == null) {
            System.out.println("Current code configuration: N/A (no code selected yet)");
        } else {
            System.out.println("Current code configuration: " +
                    formatCodeConfiguration(current));
        }
    }

    private static String formatCodeConfiguration(CodeData codeData) {
        if (codeData == null) {
            return "N/A";
        }

        StringBuilder sb = new StringBuilder();

        // <45,27,94>
        sb.append('<');
        for (int i = 0; i < codeData.rotorsIDByPos.size(); i++) {
            sb.append(codeData.rotorsIDByPos.get(i));
            if (i < codeData.rotorsIDByPos.size() - 1) {
                sb.append(','); }
        }

        sb.append('>');

        // <A(2),O(5),!(20)>
        sb.append('<');
        for (int i = 0; i < codeData.machineWindowLetters.size(); i++) {
            char letter = codeData.machineWindowLetters.get(i);
            int distance = codeData.stepsBetweenPeekWindowAndNotch.get(i);

            sb.append(letter)
                    .append('(')
                    .append(distance)
                    .append(')');

            if (i < codeData.machineWindowLetters.size() - 1) {
                sb.append(',');
            }
        }
        sb.append('>');

        // <III>
        sb.append('<')
                .append(codeData.reflectorRomanID)
                .append('>');


        if (codeData.plugboardPairs != null && !codeData.plugboardPairs.isEmpty()) {
            sb.append('<');
            for (int i = 0; i < codeData.plugboardPairs.size(); i++) {
                PlugPairDTO pair = codeData.plugboardPairs.get(i);
                sb.append(pair.getLeft())
                        .append('|')
                        .append(pair.getRight());
                if (i < codeData.plugboardPairs.size() - 1) {
                    sb.append(',');
                }
            }
            sb.append('>');
        }

        return sb.toString();
    }


    private void printLoadXmlError(LoadXmlErrorType type) {
        switch (type) {
            case INVALID_EXTENSION ->
                    System.out.println("Error: file must have a .xml extension.");
            case INVALID_ABC_SIZE ->
                    System.out.println("Error: the size of the ABC is odd; it is required to be even");
            case DUPLICATE_ABC ->
                    System.out.println("Error: there is a duplicate character in the ABC");
            case NOT_ENOUGH_ROTORS ->
                    System.out.println("Error: not enough rotors in the XML configuration.");
            case ROTOR_IDS_NOT_CONSECUTIVE ->
                    System.out.println("Error: rotor IDs must be consecutive (1..N) without gaps.");
            case ROTOR_INVALID_NOTCH ->
                    System.out.println("Error: a rotor has an invalid notch position.");
            case ROTOR_MAPPING_NOT_FULL ->
                    System.out.println("Error: a rotor does not contain a full mapping for all ABC letters.");
            case ROTOR_MAPPING_INVALID_LETTER ->
                    System.out.println("Error: a rotor mapping contains a letter that is not in the ABC.");
            case ROTOR_DUPLICATE_MAPPING ->
                    System.out.println("Error: a rotor contains duplicate mapping of letters.");
            case ROTORS_COUNT_ZERO ->
                    System.out.println("Error: The minimum number of rotors must be greater than 0");
            case NO_REFLECTORS ->
                    System.out.println("Error: no reflectors defined in XML.");
            case INVALID_REFLECTOR_ID ->
                    System.out.println("Error: invalid reflector ID (must be I, II, III, IV, V).");
            case REFLECTOR_IDS_NOT_CONSECUTIVE ->
                    System.out.println("Error: reflector IDs must be consecutive (I, II, III, IV, V).");
            case REFLECTOR_INVALID_LETTER ->
                    System.out.println("Error: reflector mapping uses an index that is out of ABC range.");
            case REFLECTOR_SELF_MAPPING ->
                    System.out.println("Error: reflector mapping cannot map a letter index to itself.");
            case REFLECTOR_DUPLICATE_MAPPING ->
                    System.out.println("Error: reflector contains duplicate mapping for a letter index.");
            case REFLECTOR_NOT_FULL_MAPPING ->
                    System.out.println("Error: reflector does not cover all letter indices (1..|ABC|).");
            case REFLECTOR_NOT_SYMMETRIC ->
                    System.out.println("Error: reflector mapping is not symmetric.");
            case TECHNICAL_ERROR ->
                    System.out.println("Error: file not found.");
            default ->
                    System.out.println("Error: failed to load XML configuration.");
        }
    }

    private List<PlugPairDTO> readPlugboardPairsFromUser() {
        System.out.println("\nEnter plugboard pairs as one continuous string (even length).");
        System.out.println("Example: dk49 !");
        System.out.println("Press ENTER for no plugs (no plugboard).");
        System.out.print("Plugboard input: ");

        String line = scanner.nextLine().toUpperCase();

        if (line.isEmpty()) {
            return new ArrayList<>(); // no plugs
        }

        if (line.length() % 2 != 0) {
            System.out.println("Invalid plugboard input: length must be even (each plug is 2 characters).");
            return null;
        }

        List<PlugPairDTO> pairs = new ArrayList<>();

        for (int i = 0; i < line.length(); i += 2) {
            char a = line.charAt(i);
            char b = line.charAt(i + 1);

            pairs.add(new PlugPairDTO(a, b));
        }

        return pairs;
    }
}
