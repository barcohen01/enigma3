package enigma.engine;

import enigma.dto.CodeData;
import enigma.dto.ManualCodeResultDTO;
import enigma.dto.PlugPairDTO;
import enigma.entities.MachineReflectorEntity;
import enigma.entities.MachineRotorEntity;
import enigma.history.CodeHistoryEntry;
import enigma.xml.data.BTEReflector;
import enigma.xml.data.BTERotor;

import java.io.InputStream;
import java.util.List;

public interface Engine {
    void loadXml(String path);
    void codeAutomatic();
    String process(String input);
    int getNumOfRotorsInSystem();
    int getCountOfMessagesThatWasSentToCurrentMachine();
    int getNumOfReflectorsInSystem();
    CodeData getOriginalCodeData();
    CodeData getCurrentCodeData();
    List<String> GetReflectorIds();
    List<Integer> GetRotorsIds();
    String getCurrentAbc();
    void ResetCodeToOriginalCode();
    List<CodeHistoryEntry> GetCodeHistory();
    ManualCodeResultDTO setManualCode(List<Integer> rotorIds,
                                      String windowLetters,
                                      int reflectorChoice,
                                      List<PlugPairDTO> plugboardPairs);
    int GetNumOfRotors();

    String getMachineName();

    void loadXmlFromStream(InputStream inputStream);
    String getAbc();
    int getRotorsCount();
    List<BTERotor> getAllRotors();
    List<BTEReflector> getAllReflectors();}