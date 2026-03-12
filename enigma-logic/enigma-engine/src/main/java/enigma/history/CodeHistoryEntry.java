package enigma.history;

import enigma.dto.CodeData;

import java.util.ArrayList;
import java.util.List;

public class CodeHistoryEntry {
    private final CodeData codeData;
    private final List<ProcessedStringEntry> processedStrings = new ArrayList<>();

    public CodeHistoryEntry(CodeData codeData) {
        this.codeData = codeData;
    }

    public CodeData getCodeData() { return codeData; }
    public List<ProcessedStringEntry> getProcessedStrings() { return processedStrings; }

    public void addProcessedString(ProcessedStringEntry entry) {
        processedStrings.add(entry);
    }
}