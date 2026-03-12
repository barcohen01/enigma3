package enigma.engine;

import enigma.dto.CodeData;
import enigma.dto.ManualCodeResultDTO;
import enigma.dto.PlugPairDTO;
import enigma.entities.MachineReflectorEntity;
import enigma.entities.MachineRotorEntity;
import enigma.history.CodeHistoryEntry;
import enigma.history.ProcessedStringEntry;
import enigma.machine.component.code.Code;
import enigma.machine.component.code.Codeimpl;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.keyboard.Keyboardimpl;
import enigma.machine.component.machine.Machine;
import enigma.machine.component.machine.Machineimpl;
import enigma.machine.component.plugboard.Plugboard;
import enigma.machine.component.rotor.Rotor;
import enigma.xml.data.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

@Service
public class Engineimpl implements Engine{

    private Machine machine;;
    private BTEEnigma enigmaXML;
    private Code code;
    private int countOfMessagesThatWasSentToCurrentMachine;
    private CodeData originalCodeData;
    private int NUM_OF_ROTORS;
    private final Random random = new Random();
    private List<CodeHistoryEntry> allCodeHistory;
    private CodeHistoryEntry currentCodeHistory;
    private final EnigmaXmlValidator xmlValidator = new EnigmaXmlValidator();
    private  ManualCodeValidator manualCodeValidator;

    public Engineimpl(){

    }

    public Engineimpl(Engineimpl other) {
        this.enigmaXML = other.enigmaXML;
        this.NUM_OF_ROTORS = other.NUM_OF_ROTORS;
        this.countOfMessagesThatWasSentToCurrentMachine = other.countOfMessagesThatWasSentToCurrentMachine;

        this.manualCodeValidator = other.manualCodeValidator;

        if (other.originalCodeData != null) {
            this.originalCodeData = new CodeData(
                    other.originalCodeData.rotorsIDByPos,
                    new ArrayList<>(other.originalCodeData.machineWindowLetters),
                    other.originalCodeData.reflectorRomanID,
                    new ArrayList<>(other.originalCodeData.plugboardPairs)
            );
        }

        if (other.machine != null) {
            this.machine = new Machineimpl((Machineimpl) other.machine);
        }

        if (other.code != null) {
            this.code = new Codeimpl((Codeimpl) other.code);
        }

        this.allCodeHistory = new ArrayList<>();
    }
    @Override
    public void loadXml(String path) {
        try {


            // FIRST: validate extension BEFORE reading file
            xmlValidator.ValidateExtension(path);

            // THEN: load the XML file
            BTEEnigma loadedEnigmaXML = XmlLoader.load(path);

            xmlToUpperCase(loadedEnigmaXML);
            // Validate full XML structure and content
            xmlValidator.validate(path, loadedEnigmaXML);
            NUM_OF_ROTORS = loadedEnigmaXML.getRotorsCount().intValue();
            manualCodeValidator = new ManualCodeValidator(NUM_OF_ROTORS);
            // If validation passed → initialize machine
            this.enigmaXML = loadedEnigmaXML;
            String abc = enigmaXML.getABC().trim();
            Keyboard keyboard = new Keyboardimpl(abc);
            this.machine = new Machineimpl(keyboard);
            this.allCodeHistory = new ArrayList<>();
            this.countOfMessagesThatWasSentToCurrentMachine = 0;
            originalCodeData = null;
            code = null;

        }
        catch (EnigmaConfigurationException e) {
            // Logical configuration error (invalid rotor, ABC wrong, etc.)
            throw e;
        }
        catch (Exception e) {
            // Technical error (IO issue, JAXB parse error, file not found)
            throw new EnigmaConfigurationException(LoadXmlErrorType.TECHNICAL_ERROR, e);
        }
    }


    private void setCode(CodeData codeData){
        currentCodeHistory = new CodeHistoryEntry(codeData);
        allCodeHistory.add(currentCodeHistory);
        this.originalCodeData = codeData;
        ResetCodeToOriginalCode();

        // init steps between peek window to notch for each rotor
        List <Integer> stepsBetweenPeekWindowToNotch = new ArrayList<>();

        for (Rotor rotor : this.code.getRotors()){
            stepsBetweenPeekWindowToNotch.add(rotor.getStepsBetweenPeekWindowAndNotch());
        }

        this.originalCodeData.stepsBetweenPeekWindowAndNotch = stepsBetweenPeekWindowToNotch.reversed();
    }

    @Override
    public void ResetCodeToOriginalCode() {
        List<Integer> machinePeekWindowIndexes = convertListOfCharacterToInteger(originalCodeData.machineWindowLetters);
        this.code = new Codeimpl(originalCodeData.rotorsIDByPos, machinePeekWindowIndexes, originalCodeData.reflectorRomanID, enigmaXML);
        machine.setCode(this.code);

        Plugboard plugboard = buildPlugboardFromCodeData(originalCodeData);
        machine.setPlugboard(plugboard);
    }

    @Override
    public void codeAutomatic() {
        List<Integer> chosenRotorIds = chooseRandomRotorIds();
        List<Character> peekWindowsStartingPositions = chooseRandomStartingPositions();
        String chosenReflectorId = chooseRandomReflectorId();
        List<PlugPairDTO> randomPlugboardPairs = chooseRandomPlugboardPairs();

        CodeData codeData = new CodeData(
                chosenRotorIds,
                peekWindowsStartingPositions,
                chosenReflectorId,
                randomPlugboardPairs
        );

        setCode(codeData);
    }

    private List<Integer> chooseRandomRotorIds() {
        List<BTERotor> allRotors = enigmaXML.getBTERotors().getBTERotor();
        List<BTERotor> allRotorsShuffled = new ArrayList<>(allRotors);
        Collections.shuffle(allRotorsShuffled, random);

        List<BTERotor> chosenRotors =
                new ArrayList<>(allRotorsShuffled.subList(0, NUM_OF_ROTORS));

        return chosenRotors.stream()
                .map(BTERotor::getId)
                .toList();
    }

    private List<Character> chooseRandomStartingPositions() {
        String abc = enigmaXML.getABC().trim();
        List<Character> peekWindowsStartingPositions = new ArrayList<>();

        for (int i = 0; i < NUM_OF_ROTORS; i++) {
            int idx = random.nextInt(abc.length());
            char startPos = abc.charAt(idx);
            peekWindowsStartingPositions.add(startPos);
        }

        return peekWindowsStartingPositions;
    }

    private String chooseRandomReflectorId() {
        List<BTEReflector> allReflectors =
                enigmaXML.getBTEReflectors().getBTEReflector();

        int reflectorIdx = random.nextInt(allReflectors.size());
        BTEReflector chosenReflector = allReflectors.get(reflectorIdx);

        return chosenReflector.getId();
    }

    @Override
    public String process(String input) {

        for (char c : input.toCharArray()) {
            if (!isCharInABC(c)) {
                throw new MachineProcessException(MachineProcessErrorType.INVALID_INPUT_CHAR);            }
        }

        long start = System.nanoTime();
        char[] result = new char[input.length()];

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            result[i] = machine.process(c);
        }

        long end = System.nanoTime();
        long duration = end - start;

        String output = new String(result);
        ProcessedStringEntry entry =
                new ProcessedStringEntry(input, output, duration);

        currentCodeHistory.addProcessedString(entry);
        countOfMessagesThatWasSentToCurrentMachine++;

        return output;
    }

    private List<Integer> convertListOfCharacterToInteger(List<Character> machinePeekWindowLetters)
    {

        List<Integer> machineWindowLettersIndexes = new ArrayList<Integer>(machinePeekWindowLetters.size());
        String abc = enigmaXML.getABC().trim();
        for(int i = 0; i < machinePeekWindowLetters.size(); i++){
            machineWindowLettersIndexes.add(abc.indexOf(machinePeekWindowLetters.get(i)) + 1);
        }
        return machineWindowLettersIndexes;
    }

    @Override
    public List<String> GetReflectorIds() {
        List<String> reflectorIds = new ArrayList<>();

        for (BTEReflector reflector: enigmaXML.getBTEReflectors().getBTEReflector()) {
            reflectorIds.add(reflector.getId());
        }

        return reflectorIds;
    }

    @Override
    public int getNumOfRotorsInSystem()
    {
        return enigmaXML.getBTERotors().getBTERotor().size();
    }

    @Override
    public int getCountOfMessagesThatWasSentToCurrentMachine()
    {
        return countOfMessagesThatWasSentToCurrentMachine;
    }

    @Override
    public int getNumOfReflectorsInSystem()
    {
        return enigmaXML.getBTEReflectors().getBTEReflector().size();
    }

    @Override
     public CodeData getOriginalCodeData() {
        return originalCodeData;
     }

     @Override
    public CodeData getCurrentCodeData(){

        if (originalCodeData == null) return null;

        List<Integer> rotorsIDByPos = originalCodeData.rotorsIDByPos;
        String reflectorRomanID = originalCodeData.reflectorRomanID;

         List <Integer> stepsBetweenPeekWindowToNotch = new ArrayList<>();
         List <Character> machinePeekWindowLetters = new ArrayList<>();

         for (Rotor rotor : this.code.getRotors()){
             stepsBetweenPeekWindowToNotch.add(rotor.getStepsBetweenPeekWindowAndNotch());
             machinePeekWindowLetters.add(enigmaXML.getABC().trim().charAt(rotor.getIndexOfPeekWindowChar() - 1));
         }

         stepsBetweenPeekWindowToNotch = stepsBetweenPeekWindowToNotch.reversed();
         machinePeekWindowLetters = machinePeekWindowLetters.reversed();

         CodeData currCodeData = new CodeData(rotorsIDByPos, machinePeekWindowLetters,reflectorRomanID, originalCodeData.plugboardPairs);
         currCodeData.stepsBetweenPeekWindowAndNotch = stepsBetweenPeekWindowToNotch;

         return currCodeData;
    }

    @Override
    public List<Integer> GetRotorsIds() {
        List<Integer> rotorsIds = new ArrayList<>();

        for (BTERotor rotor: enigmaXML.getBTERotors().getBTERotor()) {
            rotorsIds.add(rotor.getId());
        }

        return rotorsIds;
    }

    @Override
    public String getCurrentAbc(){
        return enigmaXML.getABC().trim();
    }

    @Override
    public List<CodeHistoryEntry> GetCodeHistory() {
        return allCodeHistory;
    }

    @Override
    public ManualCodeResultDTO setManualCode(List<Integer> rotorIds,
                                             String windowLetters,
                                             int reflectorMenuIndex,
                                             List<PlugPairDTO> plugboardPairs) {

        String abc = enigmaXML.getABC().trim();
        List<Integer> availableRotorIds = GetRotorsIds();
        List<String> reflectorIds = GetReflectorIds();

        ManualCodeErrorType errorType = manualCodeValidator.validate(
                rotorIds,
                windowLetters,
                reflectorMenuIndex,
                abc,
                availableRotorIds,
                reflectorIds,
                plugboardPairs
        );

        if (errorType != ManualCodeErrorType.NONE) {
            return ManualCodeResultDTO.failure(errorType);
        }

        String chosenReflectorId = reflectorIds.get(reflectorMenuIndex - 1);

        List<Character> windowLettersList = new ArrayList<>();
        for (int i = 0; i < windowLetters.length(); i++) {
            windowLettersList.add(windowLetters.charAt(i));
        }

        CodeData codeData = new CodeData(rotorIds, windowLettersList, chosenReflectorId, plugboardPairs);

        setCode(codeData);

        return ManualCodeResultDTO.success();
    }

    @Override
   public int GetNumOfRotors(){
        return NUM_OF_ROTORS;
   }

    private boolean isCharInABC(char c) {
        String abc = enigmaXML.getABC().trim();
        return abc.indexOf(c) != -1;
    }

    private void xmlToUpperCase(BTEEnigma enigma) {

        if (enigma.getABC() != null) {
            enigma.setABC(enigma.getABC().toUpperCase());
        }

        if (enigma.getBTERotors() != null && enigma.getBTERotors().getBTERotor() != null) {
            for (BTERotor rotor : enigma.getBTERotors().getBTERotor()) {
                if (rotor.getBTEPositioning() != null) {
                    for (BTEPositioning pos : rotor.getBTEPositioning()) {
                        if (pos.getRight() != null) {
                            pos.setRight(pos.getRight().toUpperCase());
                        }
                        if (pos.getLeft() != null) {
                            pos.setLeft(pos.getLeft().toUpperCase());
                        }
                    }
                }
            }
        }
    }

    private Plugboard buildPlugboardFromCodeData(CodeData codeData) {
        Plugboard plugboard = new Plugboard();

        if (codeData.plugboardPairs == null || codeData.plugboardPairs.isEmpty()) {
            return plugboard;
        }

        for (PlugPairDTO pair : codeData.plugboardPairs) {
            plugboard.addPair(pair.getLeft(), pair.getRight());
        }

        return plugboard;
    }

    private List<PlugPairDTO> chooseRandomPlugboardPairs() {
        String abc = enigmaXML.getABC().trim();

        List<Character> availableChars = new ArrayList<>();
        for (int i = 0; i < abc.length(); i++) {
            availableChars.add(abc.charAt(i));
        }

        Collections.shuffle(availableChars, random);

        int maxPairs = availableChars.size() / 2;

        int numPairs = random.nextInt(maxPairs + 1);

        List<PlugPairDTO> result = new ArrayList<>();
        int index = 0;

        for (int i = 0; i < numPairs; i++) {
            char a = availableChars.get(index++);
            char b = availableChars.get(index++);

            result.add(new PlugPairDTO(a, b));
        }

        return result;
    }

    public String getMachineName()
    {
        return enigmaXML.getName();
    }

    @Override
    public void loadXmlFromStream(InputStream inputStream) {
        try {
            BTEEnigma loadedEnigmaXML = XmlLoader.loadFromStream(inputStream);

            xmlToUpperCase(loadedEnigmaXML);

            xmlValidator.validateInternal(loadedEnigmaXML);

            NUM_OF_ROTORS = loadedEnigmaXML.getRotorsCount().intValue();
            manualCodeValidator = new ManualCodeValidator(NUM_OF_ROTORS);
            this.enigmaXML = loadedEnigmaXML;

            String abc = enigmaXML.getABC().trim();
            Keyboard keyboard = new Keyboardimpl(abc);
            this.machine = new Machineimpl(keyboard);

            this.allCodeHistory = new ArrayList<>();
            this.countOfMessagesThatWasSentToCurrentMachine = 0;
            this.originalCodeData = null;
            this.code = null;

        } catch (EnigmaConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new EnigmaConfigurationException(LoadXmlErrorType.TECHNICAL_ERROR, e);
        }
    }

    @Override
    public String getAbc() {
        return enigmaXML.getABC().trim();
    }

    @Override
    public int getRotorsCount() {
        return NUM_OF_ROTORS;
    }

    @Override
    public List<BTERotor> getAllRotors() {
        return enigmaXML.getBTERotors().getBTERotor();
    }

    @Override
    public List<BTEReflector> getAllReflectors() {
        return enigmaXML.getBTEReflectors().getBTEReflector();
    }
}