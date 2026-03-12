package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.code.Codeimpl;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.plugboard.Plugboard;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

public class Machineimpl implements Machine {
    private Code code;
    private final Keyboard keyboard;
    private Plugboard plugboard = new Plugboard();

    public Machineimpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    public Machineimpl(Machineimpl other) {
        this.keyboard = other.keyboard;

        if (other.plugboard != null) {
            this.plugboard = new Plugboard(other.plugboard);
        }

        if (other.code != null) {
            this.code = new Codeimpl((Codeimpl) other.code);
        }
    }

    @Override
    public void setCode(Code code) {
        this.code = code;
    }

    @Override
    public void setPlugboard(Plugboard plugboard) {
        if (plugboard == null) {
            this.plugboard = new Plugboard();
        } else {
            this.plugboard = plugboard;
        }
    }

    @Override
    public char process(char input) {

        input = plugboard.map(input);

        int intermidiate = keyboard.processChar(input);

        List<Rotor> rotors = code.getRotors();

        // advance
        advance(rotors);

        intermidiate = forwardTransform(rotors, intermidiate);

        intermidiate = code.getReflector().process(intermidiate);

        intermidiate = backwardTransform(rotors, intermidiate);


        return plugboard.map(keyboard.lightALamp(intermidiate));
    }

    private int backwardTransform(List<Rotor> rotors, int intermidiate) {
        for (int i = rotors.size() - 1; i >= 0; i-- ) {
            intermidiate = rotors.get(i).process(intermidiate,  Direction.BACKWARD);
        }
        return intermidiate;
    }

    private  int forwardTransform(List<Rotor> rotors, int intermidiate) {
        for (int i = 0; i < rotors.size(); i++) {
            intermidiate = rotors.get(i).process(intermidiate,  Direction.FORWARD);
        }
        return intermidiate;
    }

    private void advance(List<Rotor> rotors) {
        int rotorIndex = 0;
        boolean shouldAdvance = false;
        do {
            shouldAdvance = rotors.get(rotorIndex).advance();
            rotorIndex++;
        } while (shouldAdvance && rotorIndex < rotors.size());
    }

}