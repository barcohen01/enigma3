package enigma.console;
import enigma.engine.Engine;
import enigma.engine.Engineimpl;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engineimpl();
        new ConsoleUI(engine).showMenu();
    }
    }