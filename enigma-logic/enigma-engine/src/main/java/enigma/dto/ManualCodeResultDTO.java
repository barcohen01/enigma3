package enigma.dto;

import enigma.engine.ManualCodeErrorType;

public class ManualCodeResultDTO {

    private final boolean success;
    private final ManualCodeErrorType errorType;

    public ManualCodeResultDTO(boolean success, ManualCodeErrorType errorType) {
        this.success = success;
        this.errorType = errorType;
    }

    public boolean isSuccess() {
        return success;
    }

    public ManualCodeErrorType getErrorType() {
        return errorType;
    }

    // factory for success
    public static ManualCodeResultDTO success() {
        return new ManualCodeResultDTO(true, ManualCodeErrorType.NONE);
    }

    // factory for failure
    public static ManualCodeResultDTO failure(ManualCodeErrorType type) {
        return new ManualCodeResultDTO(false, type);
    }
}