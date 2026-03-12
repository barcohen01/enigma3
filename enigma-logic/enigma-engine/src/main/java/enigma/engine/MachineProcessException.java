package enigma.engine;

public class MachineProcessException extends RuntimeException {

    private final MachineProcessErrorType errorType;

    public MachineProcessException(MachineProcessErrorType errorType) {
        this.errorType = errorType;
    }

    public MachineProcessErrorType getErrorType() {
        return errorType;
    }
}