package utils;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;

import java.io.IOException;
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

    public static OntClass getOrCreateOntClass(OntModel model, String className, String type) throws IOException {
        OntClass ontClass = model.getOntClass(OwlUtils.getNameSpace() + className);
        if (ontClass == null) {
            switch (type) {
                case "block" -> {
                    blockCount += 1;
                    ontClass = OwlUtils.createClass(model, className, "block: " + className);
                }
                case "action" -> {
                    activityCount += 1;
                    ontClass = OwlUtils.createClass(model, className, "action: " + className);
                }
                case "actor" -> {
                    actorCount += 1;
                    ontClass = OwlUtils.createClass(model, className, "actor: " + className);
                }
                default -> System.err.println("class创建失败");
            }
        }
        return ontClass;
    }

    public static DatatypeProperty getOrCreateDatatypeProperty(OntModel model, String propertyName, OntClass domainClass, String typeName) throws IOException {
        DatatypeProperty datatypeProperty = model.getDatatypeProperty(OwlUtils.getNameSpace() + propertyName);
        if (datatypeProperty == null) {
            if (typeName.equals("")) {
                typeName = "String";
            }
            datatypeProperty = OwlUtils.createDataProperty(model, domainClass, VALUE_TYPE.get(typeName), propertyName);
        }
        return datatypeProperty;
    }
}
