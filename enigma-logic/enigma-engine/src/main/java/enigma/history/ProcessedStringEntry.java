package enigma.history;


public class ProcessedStringEntry {
    private final String input;
    private final String output;
    private final long timeNano;

    public ProcessedStringEntry(String input, String output, long timeNano) {
        this.input = input;
        this.output = output;
        this.timeNano = timeNano;
    }

    public String getInput() { return input; }
    public String getOutput() { return output; }
    public long getTimeNano() { return timeNano; }
}