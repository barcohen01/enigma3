package enigma.machine.component.rotor;

import java.util.List;

public class Rotorimpl implements Rotor{

    int notchIndex;
    int sizeOfABC;
    int windowIndex;
    List<Integer> rightPositions;
    List<Integer> leftPositions;

    public Rotorimpl(int windowIndex, int sizeOfABC, int notchIndex, List<Integer> rightPositions, List<Integer> leftPositions) {
        this.windowIndex = windowIndex;
        this.sizeOfABC = sizeOfABC;
        this.notchIndex = notchIndex - 1;
        this.rightPositions = rightPositions;
        this.leftPositions = leftPositions;

        // start from index 0
        for (int i = 0; i < this.sizeOfABC; i++) {
            rightPositions.set(i, rightPositions.get(i) - 1);
            leftPositions.set(i, leftPositions.get(i) - 1);
        }
    }

    @Override
    public int process(int input, Direction direction) {
    int DataAtInputIndexInList;
    int result;

    input -= 1; // start from index 0

        if (direction == Direction.FORWARD) {

            DataAtInputIndexInList = rightPositions.get((windowIndex + input) % sizeOfABC);

            result = leftPositions.indexOf(DataAtInputIndexInList);

            if (result >= windowIndex){
                result -= windowIndex;
            }
            else {
                result = result - windowIndex + sizeOfABC;
            }

        }
        else {
            DataAtInputIndexInList = leftPositions.get((windowIndex + input) % sizeOfABC);
            result = rightPositions.indexOf(DataAtInputIndexInList);

            if (result >= windowIndex){
                result -= windowIndex;
            }
            else {
                result = result - windowIndex + sizeOfABC;
            }
        }

        return result + 1;// real num
    }

    @Override
    public boolean advance() {
        windowIndex = (windowIndex + 1) % sizeOfABC;
        return windowIndex == notchIndex;
    }

    @Override
    public int getStepsBetweenPeekWindowAndNotch() {
        if (windowIndex <= notchIndex){
            return notchIndex -  windowIndex;
        }
        else {
            return notchIndex - windowIndex + sizeOfABC;
        }
    }

    @Override
    public int getIndexOfPeekWindowChar() {
        return rightPositions.get(windowIndex) + 1;
    }

    public Rotorimpl(Rotorimpl other) {
        this.windowIndex = other.windowIndex;
        this.sizeOfABC = other.sizeOfABC;
        this.notchIndex = other.notchIndex;

        this.rightPositions = other.rightPositions;
        this.leftPositions = other.leftPositions;
    }
}