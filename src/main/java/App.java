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
        long start = System.currentTimeMillis();

        org.sparx.Repository r = new org.sparx.Repository();
        r.OpenFile("D:\\code-project\\SysML2OWL\\src\\main\\resources\\sysml.eapx");
        System.out.println("Load EA file...");
        Package modelPackage = r.GetModels().GetAt((short) 0);
        Package iof = modelPackage.GetPackages().GetByName("IOF");
//        System.out.println("Processing Package: " + iof.GetName());

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String filePath = "D:\\code-project\\SysML2OWL\\src\\main\\resources\\base.owl";
        try {
            model.read(new FileInputStream(filePath), OwlUtils.getSourceName(), "RDF/XML");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        ObjectProperty hasPart = OwlUtils.createObjectProperty(model, null, null, "hasPart");
        ObjectProperty hasOutput = OwlUtils.createObjectProperty(model, null, null, "hasOutput");
        ObjectProperty hasInput = OwlUtils.createObjectProperty(model, null, null, "hasInput");

        SysmlUtils.processPackage(r, iof, model, null);

        System.out.println("Number of Package: " + SysmlUtils.packageCount);
        System.out.println("Number of Block: " + SysmlUtils.blockCount);
        System.out.println("Number of Activity: " + SysmlUtils.activityCount);
        r.CloseFile();
        r.Exit();
        System.out.println("Process Finish...");

        long end = System.currentTimeMillis();
        System.out.println("Using time: " + (end - start) + "ms");
    }
}
