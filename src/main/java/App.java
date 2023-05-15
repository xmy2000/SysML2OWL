import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.sparx.Package;
import utils.OwlUtils;
import utils.PropertiesReader;
import utils.Sysml2owl;
import utils.SysmlUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        org.sparx.Repository r = new org.sparx.Repository();
        r.OpenFile(PropertiesReader.getProperty("SysML_path"));
        System.out.println("Load EA file...");
        Package modelPackage = r.GetModels().GetAt((short) 0);
        Package iof = modelPackage.GetPackages().GetByName("IOF");
//        System.out.println("Processing Package: " + iof.GetName());

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String filePath = PropertiesReader.getProperty("base_owl_path");
        try {
            model.read(new FileInputStream(filePath), OwlUtils.getSourceName(), "RDF/XML");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        ObjectProperty hasPart = OwlUtils.createObjectProperty(model, null, null, "hasPart");
        ObjectProperty hasOutput = OwlUtils.createObjectProperty(model, null, null, "hasOutput");
        ObjectProperty hasInput = OwlUtils.createObjectProperty(model, null, null, "hasInput");

        Sysml2owl.processPackage(r, iof, model, null);

        System.out.println("Number of Package: " + SysmlUtils.packageCount);
        System.out.println("Number of Block: " + SysmlUtils.blockCount);
        System.out.println("Number of Actor: " + SysmlUtils.actorCount);
        System.out.println("Number of Activity: " + SysmlUtils.activityCount);
        r.CloseFile();
        r.Exit();
        System.out.println("Process Finish...");

        long end = System.currentTimeMillis();
        System.out.println("Using time: " + (end - start) + "ms");
    }
}
