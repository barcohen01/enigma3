package enigma.engine;

public class EnigmaConfigurationException extends RuntimeException {
    private final LoadXmlErrorType errorType;

    public EnigmaConfigurationException(LoadXmlErrorType errorType) {
        this.errorType = errorType;
    }

    public EnigmaConfigurationException(LoadXmlErrorType errorType, Throwable cause) {
        super(cause);
        this.errorType = errorType;
    }

    public LoadXmlErrorType getErrorType() {
        return errorType;
    }
}