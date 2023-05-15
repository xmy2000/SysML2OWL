package utils;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.Repository;

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

    public static Repository initRepository() throws IOException {
        Repository r = new Repository();
        r.OpenFile(PropertiesReader.getProperty("SysML_path"));
        System.out.println("Load EA file...");
        return r;
    }

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

    public static ObjectProperty getOrCreateObjectProperty(OntModel model, String propertyName, OntClass domainClass, OntClass rangeClass) throws IOException {
        ObjectProperty objectProperty = model.getObjectProperty(OwlUtils.getNameSpace() + propertyName);
        if (objectProperty == null) {
            objectProperty = OwlUtils.createObjectProperty(model, domainClass, rangeClass, propertyName);
        }
        return objectProperty;
    }

    /**
     * 寻找活动的输出数据
     *
     * @param dataFlow    与活动相连的物质流
     * @param r           SysML仓库
     * @param model       OWL模型
     * @param actionClass 活动类
     * @throws IOException
     */
    public static void findActionOutput(Collection<Connector> dataFlow, Repository r, OntModel model, OntClass actionClass) throws IOException {
        ObjectProperty hasOutput = model.getObjectProperty(OwlUtils.getNameSpace() + "hasOutput");
        for (Connector flow : dataFlow) {
            int id = flow.GetSupplierID();
            Element data = r.GetElementByID(id);
            int getClassifierID = data.GetClassifierID();
            if (getClassifierID > 0) {  // 关联数据为已有模块
                String dataType = r.GetElementByID(getClassifierID).GetName();
                OntClass dataClass = SysmlUtils.getOrCreateOntClass(model, dataType, "block");
                OwlUtils.addRestriction(model, hasOutput, actionClass, dataClass);
            } else {  // 关联数据为数据项
                DatatypeProperty dataProperty = SysmlUtils.getOrCreateDatatypeProperty(model, "output-" + data.GetName(), actionClass, "");
                OwlUtils.addRestriction(model, dataProperty, actionClass, XSD.xstring);
            }
        }
    }

    /**
     * 寻找活动的输入
     *
     * @param dataFlow    与活动相连的物质流
     * @param r           SysML仓库
     * @param model       OWL模型
     * @param actionClass 活动类
     * @throws IOException
     */
    public static void findActionInput(Collection<Connector> dataFlow, Repository r, OntModel model, OntClass actionClass) throws IOException {
        ObjectProperty hasInput = model.getObjectProperty(OwlUtils.getNameSpace() + "hasInput");
        for (Connector flow : dataFlow) {
            int id = flow.GetClientID();
            Element data = r.GetElementByID(id);
            int getClassifierID = data.GetClassifierID();
            if (getClassifierID > 0) {
                String dataType = r.GetElementByID(getClassifierID).GetName();
                OntClass dataClass = SysmlUtils.getOrCreateOntClass(model, dataType, "block");
                OwlUtils.addRestriction(model, hasInput, actionClass, dataClass);
            } else {
                DatatypeProperty dataProperty = SysmlUtils.getOrCreateDatatypeProperty(model, "input-" + data.GetName(), actionClass, "");
                OwlUtils.addRestriction(model, dataProperty, actionClass, XSD.xstring);
            }
        }
    }
}
