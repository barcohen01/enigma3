package enigma.dto;
import java.util.List;

public class SessionHistoryDTO {
    public List<CodeHistoryDTO> history;

    public SessionHistoryDTO(List<CodeHistoryDTO> history) {
        this.history = history;
    }

    public static class CodeHistoryDTO {
        public String configuration;
        public List<MessageDetailsDTO> processedMessages;

        public CodeHistoryDTO(String configuration, List<MessageDetailsDTO> messages) {
            this.configuration = configuration;
            this.processedMessages = messages;
        }
    }

    public static class MessageDetailsDTO {
        public String input;
        public String output;
        public long duration;

        public MessageDetailsDTO(String input, String output, long durationNs) {
            this.input = input;
            this.output = output;
            this.duration = durationNs;
        }
    }
}