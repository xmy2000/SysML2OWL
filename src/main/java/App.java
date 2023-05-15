import controller.Sysml2owl;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.sparx.Package;
import org.sparx.Repository;
import utils.OwlUtils;
import utils.SysmlUtils;

public class App {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        // 初始化SysML仓库并加载文件
        Repository r = SysmlUtils.initRepository();
        //确定模型映射的顶层包
        Package modelPackage = r.GetModels().GetAt((short) 0);
        Package iof = modelPackage.GetPackages().GetByName("IOF");

        // 初始化OWL模型
        OntModel model = OwlUtils.initModel();
        ObjectProperty hasPart = OwlUtils.createObjectProperty(model, null, null, "hasPart");
        ObjectProperty hasOutput = OwlUtils.createObjectProperty(model, null, null, "hasOutput");
        ObjectProperty hasInput = OwlUtils.createObjectProperty(model, null, null, "hasInput");
        OwlUtils.ontModel2Owl(model);

        // 模型映射
        Sysml2owl.processPackage(r, iof, model, null);

        // 总结输出
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
