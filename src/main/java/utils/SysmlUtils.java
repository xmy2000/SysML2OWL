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

    public static void processPackage(Repository r, Package p, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Package: " + p.GetName());

        Collection<Package> subPackages = p.GetPackages();
        for (Package subPackage : subPackages) {
            packageCount += 1;
            OntClass packageClass = OwlUtils.createClass(model, subPackage.GetName(), "package: " + subPackage.GetName());
            if (superClass != null) {
                OwlUtils.addSubClass(model, superClass, packageClass);
            }

            Collection<Element> elements = subPackage.GetElements();
            for (Element element : elements) {
                String type = element.GetType();
                switch (type) {
                    case "Class" -> processBlock(r, element, model, packageClass);
                    case "Actor" -> processActor(r, element, model, packageClass);
                    case "Activity" -> processActivity(r, element, model, packageClass);
                }
            }

            processPackage(r, subPackage, model, packageClass);
        }

    }

    public static void processBlock(Repository r, Element block, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Block: " + block.GetName());
        OntClass blockClass = model.getOntClass(OwlUtils.getNameSpace() + block.GetName());
        if (blockClass == null) {
            blockCount += 1;
            blockClass = OwlUtils.createClass(model, block.GetName(), "block: " + block.GetName());
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
                    partClass = OwlUtils.createClass(model, typeName, "block: " + typeName);
                }
                SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, hasPart, partClass);
                blockClass.addSuperClass(someValuesFromRestriction);
                OwlUtils.ontModel2Owl(model);
            }
        }

        //补全关联关系
        Collection<Connector> connectors = block.GetConnectors();
        for (Connector c : connectors) {
            if (c.GetType().equals("Association")) {
                String name = c.GetName();
                if (name.equals("")) {
                    name = "hasRelation";
                }
                ObjectProperty objectProperty = model.getObjectProperty(OwlUtils.getNameSpace() + name);
                if (objectProperty == null) {
                    objectProperty = OwlUtils.createObjectProperty(model, null, null, name);
                }
                int id = c.GetSupplierID();
                String className = r.GetElementByID(id).GetName();
                if (!className.equals(block.GetName())) {
                    OntClass ontClass = model.getOntClass(OwlUtils.getNameSpace() + className);
                    if (ontClass == null) {
                        blockCount += 1;
                        ontClass = OwlUtils.createClass(model, className, "block: " + className);
                    }
                    SomeValuesFromRestriction relation = model.createSomeValuesFromRestriction(null, objectProperty, ontClass);
                    blockClass.addSuperClass(relation);
                    OwlUtils.ontModel2Owl(model);
                }
            } else if (c.GetType().equals("Generalization")) {
                int id = c.GetSupplierID();
                String className = r.GetElementByID(id).GetName();
                if (!className.equals(block.GetName())) {
                    OntClass ontClass = model.getOntClass(OwlUtils.getNameSpace() + className);
                    if (ontClass == null) {
                        blockCount += 1;
                        ontClass = OwlUtils.createClass(model, className, "block: " + className);
                    }
                    blockClass.addSuperClass(ontClass);
                }
            }
        }
    }

    public static void processActor(Repository r, Element actor, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Actor: " + actor.GetName());
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");
        actorCount += 1;
        OntClass actorClass = OwlUtils.createClass(model, actor.GetName(), "actor: " + actor.GetName());
        OwlUtils.addSubClass(model, superClass, actorClass);

        Collection<Connector> connectors = actor.GetConnectors();
        for (Connector c : connectors) {
            if (c.GetType().equals("Aggregation")) {
                int id = c.GetSupplierID();
                Element supplier = r.GetElementByID(id);
                OntClass supplierClass = model.getOntClass(OwlUtils.getNameSpace() + supplier.GetName());
                if (supplierClass == null) {
                    blockCount += 1;
                    supplierClass = OwlUtils.createClass(model, supplier.GetName(), "block: " + supplier.GetName());
                }
                SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, hasPart, actorClass);
                supplierClass.addSuperClass(someValuesFromRestriction);
                OwlUtils.ontModel2Owl(model);
            }
        }
    }

    public static void processActivity(Repository r, Element activity, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Activity: " + activity.GetName());
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");
        ObjectProperty hasOutput = model.getObjectProperty(OwlUtils.getNameSpace() + "hasOutput");
        ObjectProperty hasInput = model.getObjectProperty(OwlUtils.getNameSpace() + "hasInput");
        activityCount += 1;
        OntClass activityClass = OwlUtils.createClass(model, activity.GetName(), "action: " + activity.GetName());
        OwlUtils.addSubClass(model, superClass, activityClass);

        Collection<Element> actions = activity.GetElements();
        for (Element action : actions) {
            String type = action.GetType();
            if (type.equals("Action")) {

                if (action.GetName().equals("")) {
                    // 调用已有activity
                    int classifierID = action.GetClassifierID();
                    String name = r.GetElementByID(classifierID).GetName();
                    OntClass ontClass = model.getOntClass(OwlUtils.getNameSpace() + name);
                    if (ontClass == null) {
                        activityCount += 1;
                        ontClass = OwlUtils.createClass(model, name, "action: " + name);
                    }
                    SomeValuesFromRestriction partRestriction = model.createSomeValuesFromRestriction(null, hasPart, ontClass);
                    activityClass.addSuperClass(partRestriction);
                    OwlUtils.ontModel2Owl(model);

                    Collection<Element> pins = action.GetElements();
                    for (Element pin : pins) {
                        String pinName = pin.GetName();
                        Collection<Connector> dataFlow = pin.GetConnectors();
                        if (pinName.equals("output")) {
                            for (Connector flow : dataFlow) {
                                int id = flow.GetSupplierID();
                                Element data = r.GetElementByID(id);
                                int getClassifierID = data.GetClassifierID();
                                if (getClassifierID > 0) {
                                    String dataType = r.GetElementByID(getClassifierID).GetName();
                                    OntClass dataClass = model.getOntClass(OwlUtils.getNameSpace() + dataType);
                                    if (dataClass == null) {
                                        blockCount += 1;
                                        dataClass = OwlUtils.createClass(model, dataType, "block: " + dataType);
                                    }
                                    SomeValuesFromRestriction outputRestriction = model.createSomeValuesFromRestriction(null, hasOutput, dataClass);
                                    ontClass.addSuperClass(outputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                } else {
                                    DatatypeProperty dataProperty = OwlUtils.createDataProperty(model, ontClass, XSD.xstring, "output-" + data.GetName());
                                    SomeValuesFromRestriction outputRestriction = model.createSomeValuesFromRestriction(null, dataProperty, XSD.xstring);
                                    ontClass.addSuperClass(outputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                }
                            }
                        } else if (pinName.equals("input")) {
                            for (Connector flow : dataFlow) {
                                int id = flow.GetClientID();
                                Element data = r.GetElementByID(id);
                                int getClassifierID = data.GetClassifierID();
                                if (getClassifierID > 0) {
                                    String dataType = r.GetElementByID(getClassifierID).GetName();
                                    OntClass dataClass = model.getOntClass(OwlUtils.getNameSpace() + dataType);
                                    if (dataClass == null) {
                                        blockCount += 1;
                                        dataClass = OwlUtils.createClass(model, dataType, "block: " + dataType);
                                    }
                                    SomeValuesFromRestriction inputRestriction = model.createSomeValuesFromRestriction(null, hasInput, dataClass);
                                    ontClass.addSuperClass(inputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                } else {
                                    DatatypeProperty dataProperty = OwlUtils.createDataProperty(model, ontClass, XSD.xstring, "input-" + data.GetName());
                                    SomeValuesFromRestriction inputRestriction = model.createSomeValuesFromRestriction(null, dataProperty, XSD.xstring);
                                    ontClass.addSuperClass(inputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                }
                            }
                        }
                    }

                } else {
                    // 普通action
                    activityCount += 1;
                    OntClass actionClass = OwlUtils.createClass(model, action.GetName(), "action: " + action.GetName());
                    OwlUtils.addSubClass(model, superClass, actionClass);
                    SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, hasPart, actionClass);
                    activityClass.addSuperClass(someValuesFromRestriction);
                    OwlUtils.ontModel2Owl(model);

                    Collection<Element> pins = action.GetElements();
                    for (Element pin : pins) {
                        String name = pin.GetName();
                        Collection<Connector> dataFlow = pin.GetConnectors();
                        if (name.equals("output")) {
                            for (Connector flow : dataFlow) {
                                int id = flow.GetSupplierID();
                                Element data = r.GetElementByID(id);
                                int classifierID = data.GetClassifierID();
                                if (classifierID > 0) {
                                    String dataType = r.GetElementByID(classifierID).GetName();
                                    OntClass dataClass = model.getOntClass(OwlUtils.getNameSpace() + dataType);
                                    if (dataClass == null) {
                                        blockCount += 1;
                                        dataClass = OwlUtils.createClass(model, dataType, "block: " + dataType);
                                    }
                                    SomeValuesFromRestriction outputRestriction = model.createSomeValuesFromRestriction(null, hasOutput, dataClass);
                                    actionClass.addSuperClass(outputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                } else {
                                    DatatypeProperty dataProperty = OwlUtils.createDataProperty(model, actionClass, XSD.xstring, "output-" + data.GetName());
                                    SomeValuesFromRestriction outputRestriction = model.createSomeValuesFromRestriction(null, dataProperty, XSD.xstring);
                                    actionClass.addSuperClass(outputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                }
                            }
                        } else if (name.equals("input")) {
                            for (Connector flow : dataFlow) {
                                int id = flow.GetClientID();
                                Element data = r.GetElementByID(id);
                                int classifierID = data.GetClassifierID();
                                if (classifierID > 0) {
                                    String dataType = r.GetElementByID(classifierID).GetName();
                                    OntClass dataClass = model.getOntClass(OwlUtils.getNameSpace() + dataType);
                                    if (dataClass == null) {
                                        blockCount += 1;
                                        dataClass = OwlUtils.createClass(model, dataType, "block: " + dataType);
                                    }
                                    SomeValuesFromRestriction inputRestriction = model.createSomeValuesFromRestriction(null, hasInput, dataClass);
                                    actionClass.addSuperClass(inputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                } else {
                                    DatatypeProperty dataProperty = OwlUtils.createDataProperty(model, actionClass, XSD.xstring, "input-" + data.GetName());
                                    SomeValuesFromRestriction inputRestriction = model.createSomeValuesFromRestriction(null, dataProperty, XSD.xstring);
                                    actionClass.addSuperClass(inputRestriction);
                                    OwlUtils.ontModel2Owl(model);
                                }
                            }
                        }
                    }
                }

            }
        }
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

