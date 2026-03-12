package enigma.services;

import enigma.engine.Engine;
import enigma.engine.Engineimpl;
import enigma.engine.LoadXmlErrorType;
import enigma.entities.ReflectorId;
import enigma.manager.MachineManager;
import enigma.repositories.MachineRepository;
import enigma.repositories.ReflectorRepository;
import enigma.repositories.RotorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import enigma.entities.MachineEntity;
import enigma.entities.MachineRotorEntity;
import enigma.entities.MachineReflectorEntity;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.UUID;

@Service
public class LoaderService {

    @Autowired
    private MachineManager machineManager;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private RotorRepository rotorRepository;

    @Autowired
    private ReflectorRepository reflectorRepository;


    @Transactional
    public String loadMachine(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File not provided");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
            throw new IllegalArgumentException("file must have a .xml extension.");
        }

        Engine newEngine = new Engineimpl();
        try (InputStream inputStream = file.getInputStream()) {
            newEngine.loadXmlFromStream(inputStream);
        }

        String machineName = newEngine.getMachineName();

        if (machineManager.exists(machineName)) {
            throw new IllegalStateException("A machine with the name '" + machineName + "' already exists.");
        }

        UUID machineUuid = UUID.randomUUID();

        MachineEntity machineEntity = new MachineEntity();
        machineEntity.setId(machineUuid);
        machineEntity.setName(machineName);
        machineEntity.setRotorsCount(newEngine.getRotorsCount());
        machineEntity.setAbc(newEngine.getAbc());
        machineRepository.save(machineEntity);

        newEngine.getAllRotors().forEach(rotor -> {
            MachineRotorEntity rotorEntity = new MachineRotorEntity();
            rotorEntity.setId(UUID.randomUUID());
            rotorEntity.setMachine_id(machineUuid);
            rotorEntity.setRotor_id(rotor.getId());
            rotorEntity.setNotch(rotor.getNotch());

            StringBuilder right = new StringBuilder();
            StringBuilder left = new StringBuilder();
            rotor.getBTEPositioning().forEach(pos -> {
                right.append(pos.getRight());
                left.append(pos.getLeft());
            });

            rotorEntity.setWiring_right(right.toString());
            rotorEntity.setWiring_left(left.toString());
            rotorRepository.save(rotorEntity);
        });

        newEngine.getAllReflectors().forEach(reflector -> {
            MachineReflectorEntity reflectorEntity = new MachineReflectorEntity();
            reflectorEntity.setId(UUID.randomUUID());
            reflectorEntity.setMachine_id(machineUuid);
            reflectorEntity.setReflector_id(mapStringToEnum(reflector.getId()));

            StringBuilder input = new StringBuilder();
            StringBuilder output = new StringBuilder();
            reflector.getBTEReflect().forEach(reflect -> {
                input.append(reflect.getInput());
                output.append(reflect.getOutput());
            });

            reflectorEntity.setInput(input.toString());
            reflectorEntity.setOutput(output.toString());
            reflectorRepository.save(reflectorEntity);
        });

        machineManager.addMachine(machineName, machineUuid, newEngine);

        return machineName;
    }

    public String getErrorMessage(LoadXmlErrorType type) {
        return switch (type) {
            case INVALID_EXTENSION -> "file must have a .xml extension.";
            case INVALID_ABC_SIZE -> "the size of the ABC is odd; it is required to be even";
            case DUPLICATE_ABC -> "there is a duplicate character in the ABC";
            case NOT_ENOUGH_ROTORS -> "not enough rotors in the XML configuration.";
            case ROTOR_IDS_NOT_CONSECUTIVE -> "rotor IDs must be consecutive (1..N) without gaps.";
            case ROTOR_INVALID_NOTCH -> "a rotor has an invalid notch position.";
            case ROTOR_MAPPING_NOT_FULL -> "a rotor does not contain a full mapping for all ABC letters.";
            case ROTOR_MAPPING_INVALID_LETTER -> "a rotor mapping contains a letter that is not in the ABC.";
            case ROTOR_DUPLICATE_MAPPING -> "a rotor contains duplicate mapping of letters.";
            case ROTORS_COUNT_ZERO -> "The minimum number of rotors must be greater than 0";
            case NO_REFLECTORS -> "no reflectors defined in XML.";
            case INVALID_REFLECTOR_ID -> "invalid reflector ID (must be I, II, III, IV, V).";
            case REFLECTOR_IDS_NOT_CONSECUTIVE -> "reflector IDs must be consecutive (I, II, III, IV, V).";
            case REFLECTOR_INVALID_LETTER -> "reflector mapping uses an index that is out of ABC range.";
            case REFLECTOR_SELF_MAPPING -> "reflector mapping cannot map a letter index to itself.";
            case REFLECTOR_DUPLICATE_MAPPING -> "reflector contains duplicate mapping for a letter index.";
            case REFLECTOR_NOT_FULL_MAPPING -> "reflector does not cover all letter indices (1..|ABC|).";
            case REFLECTOR_NOT_SYMMETRIC -> "reflector mapping is not symmetric.";
            case TECHNICAL_ERROR -> "file not found or could not be read.";
            default -> "failed to load XML configuration.";
        };
    }

    private ReflectorId mapStringToEnum(String xmlId) {
        return switch (xmlId.toUpperCase()) {
            case "A", "1", "I" -> ReflectorId.I;
            case "B", "2", "II" -> ReflectorId.II;
            case "C", "3", "III" -> ReflectorId.III;
            case "D", "4", "IV" -> ReflectorId.IV;
            case "E", "5", "V" -> ReflectorId.V;
            default -> throw new IllegalArgumentException("Unknown reflector ID: " + xmlId);
        };
    }
}