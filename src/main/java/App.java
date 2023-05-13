import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.sparx.Package;
import utils.OwlUtils;
import utils.SysmlUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        org.sparx.Repository r = new org.sparx.Repository();
        r.OpenFile("C:\\Users\\xmy\\Desktop\\SysML2OWL\\src\\main\\resources\\sysml.eapx");
        System.out.println("Load EA file...");
        Package modelPackage = r.GetModels().GetAt((short) 0);
        Package iof = modelPackage.GetPackages().GetByName("IOF");
//        System.out.println("Processing Package: " + iof.GetName());

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String filePath = "C:\\Users\\xmy\\Desktop\\SysML2OWL\\src\\main\\resources\\base.owl";
        try {
            model.read(new FileInputStream(filePath), OwlUtils.getSourceName(), "RDF/XML");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        ObjectProperty hasPart = OwlUtils.createObjectProperty(model, null, null, "hasPart");

        SysmlUtils.processPackage(iof, model, null);

//        OwlUtils.removeClass(model, "");
        System.out.println("Number of Package: " + SysmlUtils.packageCount);
        System.out.println("Number of Block: " + SysmlUtils.blockCount);
        System.out.println("Number of Activity: " + SysmlUtils.activityCount);
        r.CloseFile();
        r.Exit();
        System.out.println("Process Finish...");
    }
}
