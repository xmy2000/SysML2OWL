package controller;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.sparx.Package;
import org.sparx.*;
import utils.OwlUtils;
import utils.SysmlUtils;

import java.io.IOException;

public class Sysml2owl {

    /**
     * 处理SysML的包
     *
     * @param r          SysML仓库
     * @param p          处理的包
     * @param model      OWL模型
     * @param superClass 当前包的父类
     * @throws IOException
     */
    public static void processPackage(Repository r, Package p, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Package: " + p.GetName());

        // 创建子包对应的类
        Collection<Package> subPackages = p.GetPackages();
        for (Package subPackage : subPackages) {
            SysmlUtils.packageCount += 1;
            OntClass packageClass = OwlUtils.createClass(model, subPackage.GetName(), "package: " + subPackage.GetName());
            if (superClass != null) {
                OwlUtils.addSubClass(model, superClass, packageClass);
            }

            // 创建包中元素对应的类
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

    /**
     * 处理SysML模块
     *
     * @param r          SysML仓库
     * @param block      处理的模块
     * @param model      OWL模型
     * @param superClass 当前模块父类
     * @throws IOException
     */
    public static void processBlock(Repository r, Element block, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Block: " + block.GetName());

        // 创建block对应的class
        OntClass blockClass = SysmlUtils.getOrCreateOntClass(model, block.GetName(), "block");
        // 添加package对应的父类
        OwlUtils.addSubClass(model, superClass, blockClass);
        // 添加block的values/parts/references分区中的关联关系
        Collection<Element> parts = block.GetElements();
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");
        for (Element part : parts) {
            String typeName = (String) part.GetPropertyTypeName();
            if (typeName.equals("")) {
                continue;
            }
            boolean flag = SysmlUtils.VALUE_TYPE.containsKey(typeName);
            if (flag) {  // 属性类型为值类型，添加为DataProperty
                DatatypeProperty datatypeProperty = SysmlUtils.getOrCreateDatatypeProperty(model, part.GetName(), blockClass, typeName);
                OwlUtils.addRestriction(model, datatypeProperty, blockClass, SysmlUtils.VALUE_TYPE.get(typeName));
            } else {  // 属性类型为block，添加为ObjectProperty
                OntClass partClass = SysmlUtils.getOrCreateOntClass(model, typeName, "block");
                OwlUtils.addRestriction(model, hasPart, blockClass, partClass);
            }
        }

        //补全block其他关联关系
        Collection<Connector> connectors = block.GetConnectors();
        for (Connector c : connectors) {
            if (c.GetType().equals("Association")) {  // 自定义的关联关系
                String name = c.GetName();
                if (name.equals("")) {  // 若关联关系未命名则统一映射成hasRelation
                    name = "hasRelation";
                }
                // 创建关联关系为ObjectProperty
                ObjectProperty objectProperty = SysmlUtils.getOrCreateObjectProperty(model, name, null, null);
                // 得到关系连接的另一端的模块
                int id = c.GetSupplierID();
                String className = r.GetElementByID(id).GetName();
                if (!className.equals(block.GetName())) {
                    OntClass ontClass = SysmlUtils.getOrCreateOntClass(model, className, "block");
                    OwlUtils.addRestriction(model, objectProperty, blockClass, ontClass);
                }
            } else if (c.GetType().equals("Generalization")) {  // 继承泛化关系
                // 得到关系连接的另一端的模块
                int id = c.GetSupplierID();
                String className = r.GetElementByID(id).GetName();
                if (!className.equals(block.GetName())) {
                    OntClass ontClass = SysmlUtils.getOrCreateOntClass(model, className, "block");
                    blockClass.addSuperClass(ontClass);
                }
            }
        }
    }

    /**
     * 处理SysML的角色
     *
     * @param r          SysML仓库
     * @param actor      处理的角色
     * @param model      OWL仓库
     * @param superClass 当前角色的父类
     * @throws IOException
     */
    public static void processActor(Repository r, Element actor, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Actor: " + actor.GetName());
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");

        OntClass actorClass = SysmlUtils.getOrCreateOntClass(model, actor.GetName(), "actor");
        OwlUtils.addSubClass(model, superClass, actorClass);
        // 创建actor与block之间的从属关系
        Collection<Connector> connectors = actor.GetConnectors();
        for (Connector c : connectors) {
            if (c.GetType().equals("Aggregation")) {
                int id = c.GetSupplierID();
                Element supplier = r.GetElementByID(id);
                OntClass supplierClass = SysmlUtils.getOrCreateOntClass(model, supplier.GetName(), "block");
                OwlUtils.addRestriction(model, hasPart, supplierClass, actorClass);
            }
        }
    }

    /**
     * 处理SysML活动
     *
     * @param r          SysML仓库
     * @param activity   处理的活动
     * @param model      OWL模型
     * @param superClass 当前活动的父类
     * @throws IOException
     */
    public static void processActivity(Repository r, Element activity, OntModel model, OntClass superClass) throws IOException {
        System.out.println("Processing Activity: " + activity.GetName());

        OntClass activityClass = SysmlUtils.getOrCreateOntClass(model, activity.GetName(), "action");
        OwlUtils.addSubClass(model, superClass, activityClass);
        // 创建activity包含的action
        Collection<Element> actions = activity.GetElements();
        for (Element action : actions) {
            String type = action.GetType();
            if (type.equals("Action")) {
                if (action.GetName().equals("")) {
                    // 调用action
                    int classifierID = action.GetClassifierID();
                    String name = r.GetElementByID(classifierID).GetName();
                    addAction(r, model, action, superClass, activityClass, name);

                } else {
                    // 普通action
                    addAction(r, model, action, superClass, activityClass, "");
                }
            }
        }
    }

    /**
     * 添加当前活动的子活动
     *
     * @param r             SysML仓库
     * @param model         OWL模型
     * @param action        添加的子活动
     * @param superClass    父活动的父类
     * @param activityClass 父活动（hasPart）
     * @param actionName    子活动名称
     * @throws IOException
     */
    public static void addAction(Repository r, OntModel model, Element action, OntClass superClass, OntClass activityClass, String actionName) throws IOException {
        ObjectProperty hasPart = model.getObjectProperty(OwlUtils.getNameSpace() + "hasPart");

        if (actionName.equals("")) {
            actionName = action.GetName();
        }
        OntClass actionClass = SysmlUtils.getOrCreateOntClass(model, actionName, "action");
        OwlUtils.addSubClass(model, superClass, actionClass);
        OwlUtils.addRestriction(model, hasPart, activityClass, actionClass);

        Collection<Element> pins = action.GetElements();
        for (Element pin : pins) {
            String name = pin.GetName();
            Collection<Connector> dataFlow = pin.GetConnectors();
            if (name.equals("output")) {
                SysmlUtils.findActionOutput(dataFlow, r, model, actionClass);
            } else if (name.equals("input")) {
                SysmlUtils.findActionInput(dataFlow, r, model, actionClass);
            }
        }
    }
}

