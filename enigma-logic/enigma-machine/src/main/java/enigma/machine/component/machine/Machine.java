package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.plugboard.Plugboard;

public interface Machine {
    char process(char input);
    void setCode(Code code);

    void setPlugboard(Plugboard plugboard);
}
