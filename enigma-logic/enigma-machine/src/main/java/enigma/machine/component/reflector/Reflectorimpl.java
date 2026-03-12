package enigma.machine.component.reflector;

import enigma.xml.data.*;


public class Reflectorimpl implements Reflector {
    BTEReflector reflector;

    public Reflectorimpl(BTEReflector reflector) {
        this.reflector = reflector;
    }

    @Override
    public int process(int input) {

        int res = 0;

        for (BTEReflect reflect:  reflector.getBTEReflect()){

            if (reflect.getInput() == input){
                res = reflect.getOutput();
                break;
            }
            else if(reflect.getOutput() == input){
                res = reflect.getInput();
                break;
            }
        }

        return res;

    }
}