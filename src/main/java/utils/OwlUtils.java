package utils;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdf.model.Resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OwlUtils {
    public static String getSourceName() throws IOException {
        return PropertiesReader.getProperty("owl_source_url");
    }

    public static String getNameSpace() throws IOException {
        return getSourceName() + "#";
    }

    public static OntModel initModel() throws IOException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String filePath = PropertiesReader.getProperty("base_owl_path");
        model.read(new FileInputStream(filePath), OwlUtils.getSourceName(), "RDF/XML");
        return model;
    }

    public static void ontModel2Owl(OntModel ontModel) throws IOException {
        //输出owl文件到文件系统
        String filepath = PropertiesReader.getProperty("owl_out_path");
        FileOutputStream fileOS = new FileOutputStream(filepath);
        RDFWriterI writer = ontModel.getWriter("RDF/XML");
        writer.setProperty("showXMLDeclaration", "true");
        writer.setProperty("showDoctypeDeclaration", "true");
        writer.write(ontModel, fileOS, null);
        fileOS.close();
    }

    public static OntModel owl2OntModel(String path) throws IOException {
        //设置本体的命名空间
        String SOURCE = getSourceName();
        OntDocumentManager ontDocMgr = new OntDocumentManager();
        //设置本体owl文件的位置
        ontDocMgr.addAltEntry(SOURCE, path); // "file:src/main/resources/owl/core/CoreOntology.owl"
        OntModelSpec ontModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
        ontModelSpec.setDocumentManager(ontDocMgr);
        // asserted ontology
        OntModel baseOnt = ModelFactory.createOntologyModel(ontModelSpec);
        baseOnt.read(SOURCE, "RDF/XML");
        return baseOnt;
    }

    public static OntClass createClass(OntModel ontModel, String className, String label) throws IOException {
        String nameSpace = getNameSpace();
        OntClass newClass = ontModel.createClass(nameSpace + className);
        newClass.addLabel(label, "");
        ontModel2Owl(ontModel);
        return newClass;
    }

    public static void addSubClass(OntModel ontModel, OntClass fatherClass, OntClass sonClass) throws IOException {
        fatherClass.addSubClass(sonClass);
        ontModel2Owl(ontModel);
    }

    public static ObjectProperty createObjectProperty(OntModel ontModel, OntClass sourceClass, OntClass targetClass, String relationName) throws IOException {
        String nameSpace = getNameSpace();
        ObjectProperty newRelation = ontModel.createObjectProperty(nameSpace + relationName);
        if (sourceClass != null) {
            newRelation.addDomain(sourceClass);
        }
        if (targetClass != null) {
            newRelation.addRange(targetClass);
        }
        ontModel2Owl(ontModel);
        return newRelation;
    }

    public static DatatypeProperty createDataProperty(OntModel ontModel, OntClass ontClass, Resource resource, String propertyName) throws IOException {
        String nameSpace = getNameSpace();
        DatatypeProperty newProperty = ontModel.createDatatypeProperty(nameSpace + propertyName);
        newProperty.addDomain(ontClass);
        newProperty.addRange(resource);
        ontModel2Owl(ontModel);
        return newProperty;
    }

    public static void removeClass(OntModel ontModel, String classname) throws Exception {
        OntClass ontClass = ontModel.getOntClass(OwlUtils.getNameSpace() + classname);
        ontClass.remove();
        ontModel2Owl(ontModel);
    }

    public static void addRestriction(OntModel model, Property property, OntClass source, Resource target) throws IOException {
        SomeValuesFromRestriction someValuesFromRestriction = model.createSomeValuesFromRestriction(null, property, target);
        source.addSuperClass(someValuesFromRestriction);
        OwlUtils.ontModel2Owl(model);
    }
}
