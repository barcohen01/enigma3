package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.reflector.Reflectorimpl;
import enigma.machine.component.rotor.Rotor;
import enigma.machine.component.rotor.Rotorimpl;
import enigma.xml.data.*;

import java.util.ArrayList;
import java.util.List;

public class Codeimpl implements Code{
    private List<Integer> RotorsIdByPosition;
    private List<Integer> charsAtRotorsWindow;
    private String reflectorId;
    private BTEEnigma bteEnigma;
    private List<Rotor> rotors;
    private Reflector reflector;

    public Codeimpl(List<Integer> RotorsIdByPosition, List<Integer> charsAtRotorsWindow, String reflectorId, BTEEnigma enigmaXML) {
        this.RotorsIdByPosition = RotorsIdByPosition.reversed();
        this.reflectorId = reflectorId;
        this.charsAtRotorsWindow = charsAtRotorsWindow.reversed();
        this.bteEnigma = enigmaXML;

        this.rotors = generateRotors(enigmaXML.getBTERotors());
        this.reflector = createReflector(enigmaXML.getBTEReflectors());
    }

    private List<Rotor> generateRotors(BTERotors rotorsXML) {

        List<Rotor> rotors = new ArrayList<>(RotorsIdByPosition.size());
        List<Integer> leftPositions;
        List<Integer> rightPositions;
        int i = 0;
        for (int id : RotorsIdByPosition){
            for (BTERotor rotor : rotorsXML.getBTERotor()) {
                if (rotor.getId() == id) {
                    leftPositions = createRotorLeftPoistionList(rotor);
                    rightPositions = createRotorRightPoistionList(rotor);
                    rotors.add(new Rotorimpl(rightPositions.indexOf(charsAtRotorsWindow.get(i)), bteEnigma.getABC().trim().length(),
                            rotor.getNotch(), rightPositions, leftPositions));
                    i++;
                }
            }
        }

        return rotors;
    }

    @Override
    public List<Rotor> getRotors() {
        return rotors;
    }

    @Override
    public Reflector getReflector() {
        return reflector;
    }

    private Reflector createReflector(BTEReflectors bteReflectors) {
        Reflector reflector = null;

        for (BTEReflector bteReflector : bteReflectors.getBTEReflector()){
            if (bteReflector.getId() == reflectorId){
                reflector = new Reflectorimpl(bteReflector);
                break;
            }
        }

        return reflector;
    }

    private List<Integer> createRotorRightPoistionList(BTERotor specificRotor)
    {
        String abc = bteEnigma.getABC().trim();
        List<Integer> rightPositions = new ArrayList<>(specificRotor.getBTEPositioning().size());
        for (BTEPositioning pos : specificRotor.getBTEPositioning())
        {
            char letter = pos.getRight().charAt(0);
            rightPositions.add(abc.indexOf(letter) + 1);
        }

        return rightPositions;
    }

    private List<Integer> createRotorLeftPoistionList(BTERotor specificRotor)
    {
        String abc = bteEnigma.getABC().trim();
        List<Integer> leftPositions = new ArrayList<>(specificRotor.getBTEPositioning().size());
        for (BTEPositioning pos : specificRotor.getBTEPositioning())
        {
            char letter = pos.getLeft().charAt(0);
            leftPositions.add(abc.indexOf(letter) + 1);
        }

        return leftPositions;
    }

    public Codeimpl(Codeimpl other) {
        this.RotorsIdByPosition = new ArrayList<>(other.RotorsIdByPosition);
        this.charsAtRotorsWindow = new ArrayList<>(other.charsAtRotorsWindow);
        this.reflectorId = other.reflectorId;
        this.bteEnigma = other.bteEnigma;
        this.reflector = other.reflector;

        this.rotors = new ArrayList<>();
        for (Rotor r : other.rotors) {
            this.rotors.add(new Rotorimpl((Rotorimpl) r));
        }
    }
}
