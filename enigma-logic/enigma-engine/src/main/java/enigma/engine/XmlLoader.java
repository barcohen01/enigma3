package enigma.engine;

import enigma.xml.data.BTEEnigma;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.InputStream;

public class XmlLoader {

    /**
     * Loads the Enigma configuration XML and returns the generated BTEEnigma object.
     *
     * @param filePath Path to the XML file
     * @return BTEEnigma object representing the XML structure
     * @throws Exception if XML cannot be parsed
     */
    public static BTEEnigma load(String filePath) throws Exception {

        JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        return (BTEEnigma) unmarshaller.unmarshal(new File(filePath));

    }

    public static BTEEnigma loadFromStream(InputStream inputStream) throws Exception {
        JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (BTEEnigma) unmarshaller.unmarshal(inputStream);
    }
}