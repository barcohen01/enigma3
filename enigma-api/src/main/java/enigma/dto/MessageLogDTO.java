package enigma.dto;

public class MessageLogDTO {
    public String input;
    public String output;
    public long duration; // תואם ל-Swagger

    public MessageLogDTO(String input, String output, long duration) {
        this.input = input;
        this.output = output;
        this.duration = duration;
    }
}