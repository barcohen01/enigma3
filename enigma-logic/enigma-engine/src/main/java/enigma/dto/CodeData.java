package enigma.dto;

import java.util.Collections;
import java.util.List;

public class CodeData {
    public List<Integer> rotorsIDByPos;
    public List<Character> machineWindowLetters;
    public String reflectorRomanID;
    public List<Integer> stepsBetweenPeekWindowAndNotch;
    public List<PlugPairDTO> plugboardPairs;

    public CodeData(List<Integer> rotorsIDByPos,
                      List<Character> machineWindowLetters,
                      String romanValue,
                    List<PlugPairDTO> plugboardPairs) {

        this.rotorsIDByPos = rotorsIDByPos;
        this.machineWindowLetters = machineWindowLetters;
        this.reflectorRomanID = romanValue;
        this.plugboardPairs =
                (plugboardPairs != null) ? plugboardPairs : Collections.emptyList();

    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append('<');
        for (int i = 0; i < rotorsIDByPos.size(); i++) {
            sb.append(rotorsIDByPos.get(i));
            if (i < rotorsIDByPos.size() - 1) sb.append(',');
        }
        sb.append('>');

        sb.append('<');
        for (int i = 0; i < machineWindowLetters.size(); i++) {
            sb.append(machineWindowLetters.get(i));
            if (stepsBetweenPeekWindowAndNotch != null && i < stepsBetweenPeekWindowAndNotch.size()) {
                sb.append('(').append(stepsBetweenPeekWindowAndNotch.get(i)).append(')');
            }
            if (i < machineWindowLetters.size() - 1) sb.append(',');
        }
        sb.append('>');

        sb.append('<').append(reflectorRomanID).append('>');

        if (plugboardPairs != null && !plugboardPairs.isEmpty()) {
            sb.append('<');
            for (int i = 0; i < plugboardPairs.size(); i++) {
                sb.append(plugboardPairs.get(i).getLeft())
                        .append('|')
                        .append(plugboardPairs.get(i).getRight());
                if (i < plugboardPairs.size() - 1) sb.append(',');
            }
            sb.append('>');
        }

        return sb.toString();
    }
}