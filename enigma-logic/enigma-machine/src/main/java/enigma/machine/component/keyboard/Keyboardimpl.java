package enigma.machine.component.keyboard;

public class Keyboardimpl implements Keyboard{

    String abc;

    public Keyboardimpl(String abc) {
        this.abc = abc;
    }

    @Override
    public int processChar(char input) {
        return abc.indexOf(input) + 1;
    }

    @Override
    public char lightALamp(int input) {
        return abc.charAt(input - 1);
    }
}
