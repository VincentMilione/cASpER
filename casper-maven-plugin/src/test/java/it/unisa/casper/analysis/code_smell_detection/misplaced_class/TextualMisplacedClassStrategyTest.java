package it.unisa.casper.analysis.code_smell_detection.misplaced_class;

import it.unisa.casper.analysis.code_smell.MisplacedClassCodeSmell;
import it.unisa.casper.analysis.code_smell_detection.Helper.CosineSimilarityStub;
import it.unisa.casper.storage.beans.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TextualMisplacedClassStrategyTest {

    private List<PackageBean> systemPackage = new ArrayList<PackageBean>();
    private MethodBeanList methods;
    private ClassBean classe, smelly, noSmelly;
    private ClassBeanList classes;
    private PackageBean pack, packE;
    private String path = "./src/test/input/textual/misplaced";

    @Before
    public void setUp() throws IOException {
        String filename = System.getProperty("user.home") + File.separator + ".casper" + File.separator + "stopwordlist.txt";
        File stopwordlist = new File(filename);
        stopwordlist.delete();
        InstanceVariableBeanList instances = new InstanceVariableList();

        String packageContent1 = new String (Files.readAllBytes(Paths.get(path+"/package1.txt")));
        String packageContent2 = new String (Files.readAllBytes(Paths.get(path+"/package2.txt")));
        String classMain = new String (Files.readAllBytes(Paths.get(path+"/Main.txt")));
        String classPhone = new String (Files.readAllBytes(Paths.get(path+"/Phone.txt")));
        String classRistorante = new String (Files.readAllBytes(Paths.get(path+"/Ristorante.txt")));
        String classCliente = new String (Files.readAllBytes(Paths.get(path+"/Cliente.txt")));
        String classGestione = new String (Files.readAllBytes(Paths.get(path+"/Gestione.txt")));

        classes = new ClassList();
        packE = new PackageBean.Builder("misplaced_class.package", packageContent1).setClassList(classes).build();

        instances.getList().add(new InstanceVariableBean("name", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("eta", "int", "", "private "));
        methods = new MethodList();
        List<String> imports = new ArrayList<String>();

        methods = new MethodList();
        classe = new ClassBean.Builder("misplaced_class.package.Cliente", classCliente)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(imports)
                .setLOC(22)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("misplaced_class.package", packageContent1).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(3)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\misplaced_class\\package")
                .setAffectedSmell()
                .build();
        packE.addClassList(classe);

        noSmelly = new ClassBean.Builder("misplaced_class.package.Gestione", classGestione)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<>())
                .setLOC(18)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("misplaced_class.package", packageContent1).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(0)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\misplaced_class\\package")
                .setAffectedSmell()
                .build();

        packE.addClassList(noSmelly);
        systemPackage.add(packE);

        methods = new MethodList();
        classes = new ClassList();
        pack = new PackageBean.Builder("misplaced_class.package2", packageContent2).setClassList(classes).build();

        smelly = new ClassBean.Builder("misplaced_class.package2.Main", classMain)
                .setInstanceVariables(new InstanceVariableList())
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(8)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("misplaced_class.package2", packageContent2).build())
                .setEnviedPackage(packE)
                .setEntityClassUsage(1)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\misplaced_class\\package2")
                .setAffectedSmell()
                .build();
        pack.addClassList(smelly);

        instances = new InstanceVariableList();
        methods = new MethodList();

        classe = new ClassBean.Builder("misplaced_class.package2.Phone", classPhone)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(11)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("misplaced_class.package2", packageContent2).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(3)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\misplaced_class\\package2")
                .setAffectedSmell()
                .build();
        pack.addClassList(classe);

        classe = new ClassBean.Builder("misplaced_class.package2.Ristorante", classRistorante)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(12)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("misplaced_class.package2", packageContent2).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(2)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\misplaced_class\\package2")
                .setAffectedSmell()
                .build();
        pack.addClassList(classe);

        systemPackage.add(pack);

    }

    @Test
    public void isSmellyTrue() {
        TextualMisplacedClassStrategy analisi = new TextualMisplacedClassStrategy(systemPackage, 0); //soglia default
        MisplacedClassCodeSmell smell = new MisplacedClassCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertTrue(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertTrue(risultato);
    }

    @Test
    public void isSmellyNearThreshold() {
        String[] document1 = new String[2];
        document1[0] = "class";
        document1[1] = smelly.getTextContent();
        String[] document2 = new String[2];
        document2[0] = "package";
        document2[1] = smelly.getEnviedPackage().getTextContent();

        TextualMisplacedClassStrategy analisi = new TextualMisplacedClassStrategy(systemPackage, CosineSimilarityStub.computeSimilarity(document1, document2) - 0.01);
        MisplacedClassCodeSmell smell = new MisplacedClassCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertTrue(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertTrue(risultato);
    }

    @Test
    public void isSmellyMinThreshold() {
        String[] document1 = new String[2];
        document1[0] = "class";
        document1[1] = smelly.getTextContent();
        String[] document2 = new String[2];
        document2[0] = "package";
        document2[1] = smelly.getEnviedPackage().getTextContent();

        TextualMisplacedClassStrategy analisi = new TextualMisplacedClassStrategy(systemPackage, CosineSimilarityStub.computeSimilarity(document1, document2));
        MisplacedClassCodeSmell smell = new MisplacedClassCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertFalse(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertFalse(risultato);
    }

    @Test
    public void isSmellyFalse() {
        TextualMisplacedClassStrategy analisi = new TextualMisplacedClassStrategy(systemPackage, 0); //soglia default
        MisplacedClassCodeSmell smell = new MisplacedClassCodeSmell(analisi, "Textual");
        boolean risultato = noSmelly.isAffected(smell);
        assertFalse(noSmelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertFalse(risultato);
    }

}