package enigma.dto;

public class PlugPairDTO {

    private final char left;
    private final char right;

    public PlugPairDTO(char left, char right) {
        this.left = left;
        this.right = right;
    }

    public char getLeft() {
        return left;
    }

    public char getRight() {
        return right;
    }
}