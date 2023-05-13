package utils;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Package;

import java.io.IOException;

public class SysmlUtils {
    public static int packageCount = 0;
    public static int blockCount = 0;
    public static int activityCount = 0;
    public static int actorCount = 0;

    public static void processPackage(Package p, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Package: " + p.GetName());

        Collection<Package> subPackages = p.GetPackages();
        for (Package subPackage : subPackages) {
            packageCount += 1;
            OntClass packageClass = OwlUtils.createClass(model, "package" + packageCount, subPackage.GetName());
            if (superClass != null) {
                OwlUtils.addSubClass(model, superClass, packageClass);
            }

            Collection<Element> elements = subPackage.GetElements();
            for (Element element : elements) {
                String type = element.GetType();
                switch (type) {
                    case "Class" -> processBlock(element, model, packageClass);
                    case "Actor" -> processActor(element);
                    case "Activity" -> processActivity(element);
                }
            }


            processPackage(subPackage, model, packageClass);
        }

    }

    public static void processBlock(Element block, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Block: " + block.GetName());
        blockCount += 1;
        OntClass blockClass = OwlUtils.createClass(model, "block" + blockCount, block.GetName());
        OwlUtils.addSubClass(model, superClass, blockClass);

    }

    public static void processActor(Element actor) {
        System.out.println("Processing Actor: " + actor.GetName());
        actorCount += 1;
    }

    public static void processActivity(Element activity) {
        System.out.println("Processing Activity: " + activity.GetName());
        activityCount += 1;
    }


    public static void main(String[] args) {
        org.sparx.Repository r = new org.sparx.Repository();
        r.OpenFile("C:\\Users\\xmy\\Desktop\\SysML2OWL\\src\\main\\resources\\sysml.eapx");
        System.out.println("Load EA file...");

        Package model = r.GetModels().GetAt((short) 0);
        Package iof = model.GetPackages().GetByName("IOF");
//        System.out.println("Processing Package: " + iof.GetName());
        Package p = iof.GetPackages().GetByName("实体").GetPackages().GetByName("发生体").GetPackages().GetByName("加工过程");
        Collection<Element> elements = p.GetElements();
        Element element = elements.GetByName("借用归还");
        System.out.println(element.GetType());
//        for (Element e : elements) {
//            System.out.println(e.GetName());
//        }
//        Element e = elements.GetByName("材料属性");
//        System.out.println(e.GetType());
//        Collection<Element> elements1 = e.GetElements();
//        for (Element element : elements1) {
//            System.out.println(element.GetName());
//        }
//        Collection<Connector> connectors = e.GetConnectors();
//        for (Connector connector : connectors) {
//            System.out.println(connector.GetType());
//        }

        r.CloseFile();
        r.Exit();
        System.out.println("Process Finish...");
    }
}
