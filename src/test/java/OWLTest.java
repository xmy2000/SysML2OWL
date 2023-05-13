import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import utils.OwlUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class OWLTest {
    public static void main(String[] args) throws IOException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String filePath = "C:\\Users\\xmy\\Desktop\\SysML2OWL\\src\\main\\resources\\base.owl";
        try {
            model.read(new FileInputStream(filePath), OwlUtils.getSourceName(), "RDF/XML");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        ObjectProperty hasPart = OwlUtils.createObjectProperty(model, null, null, "hasPart");

        OntClass class1 = OwlUtils.createClass(model, "class1", "");
        OntClass class2 = OwlUtils.createClass(model, "class2", "");
        SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, hasPart, model.getOntClass(OwlUtils.getNameSpace() + "class2"));
        class1.addSuperClass(someValuesFromRestriction);
        OntClass class3 = OwlUtils.createClass(model, "class2", "3");
        OwlUtils.ontModel2Owl(model);
    }
}
