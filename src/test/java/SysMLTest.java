import org.sparx.*;
import org.sparx.Package;

public class SysMLTest {
    public static void main(String[] args) {
        org.sparx.Repository r = new org.sparx.Repository();
        r.OpenFile("D:\\code-project\\SysML2OWL\\src\\main\\resources\\sysml.eapx");
        System.out.println("Load EA file...");

        Package model = r.GetModels().GetAt((short) 0);
        Package iof = model.GetPackages().GetByName("IOF");
//        Package p = iof.GetPackages().GetByName("实体").GetPackages().GetByName("独立连续体").GetPackages().GetByName("工人");
//        Collection<Element> elements = p.GetElements();
//        Element element = elements.GetByName("工艺员");
//        Collection<Connector> connectors = element.GetConnectors();
//        for (Connector connector : connectors) {
//            System.out.println(connector.GetName());
//            System.out.println(connector.GetType());
//            int id = connector.GetSupplierID();
//            Element supplier = r.GetElementByID(id);
//            System.out.println(supplier.GetName());
//        }


//        Package p = iof.GetPackages().GetByName("实体").GetPackages().GetByName("发生体").GetPackages().GetByName("加工过程");
//        Collection<Element> elements = p.GetElements();
//        Element activity = elements.GetByName("借用归还");
//        System.out.println(activity.GetName() + activity.GetType());

//        Collection<Element> son = activity.GetElements();
//        for (Element s : son) {
//            System.out.println(s.GetName() + " " + s.GetType());
//            int id = s.GetClassifierID();
//
//        }

//        Element sonactivity = son.GetAt((short) 0);
//        System.out.println(sonactivity.GetType());
//        int id = sonactivity.GetClassifierID();
//        System.out.println(r.GetElementByID(id).GetName());


//        Element element = son.GetByName("磨损测量");
//        Element pin = element.GetElements().GetByName("output");
//        Connector output = pin.GetConnectors().GetAt((short) 0);
//        int id = output.GetSupplierID();
//        Element data = r.GetElementByID(id);
//        System.out.println(data.GetName());
//        System.out.println(data.GetClassfierID());
//        System.out.println(r.GetElementByID(0).GetName());


////        System.out.println(element.GetName() + " " + element.GetType() + " " + element.GetClassifierType());
//        int id = element.GetClassifierID();
////        Element classifier = r.GetElementByID(id);
////        System.out.println(classifier.GetName());
//
//        Collection<Element> elements1 = element.GetElements();
//        for (Element element1 : elements1) {
//            System.out.println(element1.GetName());
//            Collection<Connector> connectors = element1.GetConnectors();
//            if (element1.GetName().equals("output")) {
//                for (Connector connector : connectors) {
//                    int id1 = connector.GetSupplierID();
//                    Element data = r.GetElementByID(id1);
//                    System.out.println(data.GetName());
//                    System.out.println(r.GetElementByID(data.GetClassifierID()).GetName());
//                }
//            } else if (element1.GetName().equals("input")) {
//                for (Connector connector : connectors) {
//                    int id1 = connector.GetClientID();
//                    System.out.println(r.GetElementByID(id1).GetName()+" "+r.GetElementByID(id1).GetClassifierID());
//                }
//            }

//        }

        Package p = iof.GetPackages().GetByName("实体").GetPackages().GetByName("普遍依赖连续体").GetPackages().GetByName("工艺数据");
        Collection<Element> elements = p.GetElements();
        Element data = elements.GetByName("刀具清单");
        Collection<Connector> connectors = data.GetConnectors();
        for (Connector connector : connectors) {
            System.out.println(connector.GetType() + " " + r.GetElementByID(connector.GetSupplierID()).GetName());
        }
        //Association关联->指向
        //Generalization->父类
        r.CloseFile();
        r.Exit();
        System.out.println("Process Finish...");
    }
}
