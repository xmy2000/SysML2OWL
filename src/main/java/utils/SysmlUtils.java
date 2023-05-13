package utils;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;
import org.sparx.*;
import org.sparx.Package;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SysmlUtils {
    public static int packageCount = 0;
    public static int blockCount = 0;
    public static int activityCount = 0;
    public static int actorCount = 0;

    public static Map<String, Resource> VALUE_TYPE = new HashMap<>() {{
        put("Integer", XSD.xint);
        put("String", XSD.xstring);
        put("Boolean", XSD.xboolean);
        put("Date", XSD.dateTimeStamp);
        put("File", XSD.xstring);
        put("Signal", XSD.xstring);
        put("Real", XSD.xfloat);
    }};

    public static void processPackage(Package p, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Package: " + p.GetName());

        Collection<Package> subPackages = p.GetPackages();
        for (Package subPackage : subPackages) {
            packageCount += 1;
            OntClass packageClass = OwlUtils.createClass(model, subPackage.GetName(), subPackage.GetName());
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
        OntClass blockClass = model.getOntClass(OwlUtils.getNameSpace() + block.GetName());
        if (blockClass == null) {
            blockCount += 1;
            blockClass = OwlUtils.createClass(model, block.GetName(), block.GetName());
        }
        OwlUtils.addSubClass(model, superClass, blockClass);
        Collection<Element> parts = block.GetElements();
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");
        for (Element part : parts) {
            String typeName = (String) part.GetPropertyTypeName();
            if (typeName.equals("")) {
                continue;
            }
            boolean flag = VALUE_TYPE.containsKey(typeName);
            if (flag) {
                DatatypeProperty datatypeProperty = model.getDatatypeProperty(OwlUtils.getNameSpace() + part.GetName());
                if (datatypeProperty == null) {
                    datatypeProperty = OwlUtils.createDataProperty(model, blockClass, VALUE_TYPE.get(typeName), part.GetName());
                }
                SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, datatypeProperty, VALUE_TYPE.get(typeName));
                blockClass.addSuperClass(someValuesFromRestriction);
                OwlUtils.ontModel2Owl(model);
            } else {
                OntClass partClass = model.getOntClass(OwlUtils.getNameSpace() + typeName);
                if (partClass == null) {
                    blockCount += 1;
                    partClass = OwlUtils.createClass(model, typeName, typeName);
                }
                SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, hasPart, partClass);
                blockClass.addSuperClass(someValuesFromRestriction);
                OwlUtils.ontModel2Owl(model);
            }
        }

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
        Package p = iof.GetPackages().GetByName("实体").GetPackages().GetByName("特定依赖连续体").GetPackages().GetByName("刀具数据");
        Collection<Element> elements = p.GetElements();
        Element element = elements.GetByName("刀具动态属性数据");
        Collection<Element> elements1 = element.GetElements();
        for (Element element1 : elements1) {
            System.out.println(element1.GetName() + element1.GetPropertyTypeName());
            Properties properties = element1.GetProperties();
//            for (Object property : properties) {
//                Property property1 = (Property) property;
//                System.out.println(property1.GetName() + property1.GetType() + property1.GetValue() + property1.GetObjectType());
//            }
        }


//        System.out.println(element.GetType());
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
