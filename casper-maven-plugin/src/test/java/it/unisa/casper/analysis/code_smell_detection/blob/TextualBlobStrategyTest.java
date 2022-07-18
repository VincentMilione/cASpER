package it.unisa.casper.analysis.code_smell_detection.blob;

import it.unisa.casper.analysis.code_smell.BlobCodeSmell;
import it.unisa.casper.analysis.code_smell_detection.Helper.SmellynessMetricStub;
import it.unisa.casper.storage.beans.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TextualBlobStrategyTest {

    private MethodBeanList methods, called1, called2, called3, called4;
    private MethodBean metodo;
    private ClassBean classe, noSmelly, smelly;
    private ClassBeanList classes;
    private PackageBean pack;
    private String path = "./src/test/input/textual/blob";

    @Before
    public void setUp() throws IOException {
        String filename = System.getProperty("user.home") + File.separator + ".casper" + File.separator + "stopwordlist.txt";
        File stopwordlist = new File(filename);
        stopwordlist.delete();

        MethodBeanList vuota = new MethodList();
        HashMap<String, ClassBean> nulla = new HashMap<String, ClassBean>();
        String packageContent = new String (Files.readAllBytes(Paths.get(path+"/package.txt")));
        String classBankAccount = new String (Files.readAllBytes(Paths.get(path+"/BankAccount.txt")));
        String classCliente = new String (Files.readAllBytes(Paths.get(path+"/Cliente.txt")));
        String classControl = new String (Files.readAllBytes(Paths.get(path+"/controlProdotto.txt")));
        String classPhone = new String (Files.readAllBytes(Paths.get(path+"/Phone.txt")));
        String classRistorante = new String (Files.readAllBytes(Paths.get(path+"/Ristorante.txt")));
        String classProdotto = new String (Files.readAllBytes(Paths.get(path+"/Prodotto.txt")));

        classes = new ClassList();
        pack = new PackageBean.Builder("blob.package", packageContent).setClassList(classes).build();

        InstanceVariableBeanList instances = new InstanceVariableList();
        List<String> imports = new ArrayList<String>();
        imports.add("import java.util.Scanner;");
        imports.add("import java.util.ArrayList;");

        called1 = new MethodList();
        called2 = new MethodList();
        called3 = new MethodList();
        called4 = new MethodList();

        methods = new MethodList();
        instances.getList().add(new InstanceVariableBean("balance", "double", "", "private "));
        noSmelly = new ClassBean.Builder("blob.package.BankAccount", classBankAccount)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(10)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", packageContent).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(2)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        HashMap<String, ClassBean> hash = new HashMap<String, ClassBean>();
        hash.put("balance", new ClassBean.Builder("Double", "").build());
        metodo = new MethodBean.Builder("blob.package.BankAccount.BankAccount", "this.balance = balance;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.BankAccount ", classBankAccount).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        noSmelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.BankAccount.getBalance", "return balance;")
                .setReturnType(new ClassBean.Builder("Double", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.BankAccount", classBankAccount).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        noSmelly.addMethodBeanList(metodo);
        called4.getList().add(metodo);
        pack.addClassList(noSmelly);

        methods = new MethodList();
        instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("unformattedNumber", "String", "", "private final "));
        classe = new ClassBean.Builder("blob.package.Phone", classPhone)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(11)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", packageContent).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(4)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("unformattedNumber", new ClassBean.Builder("String", "").build());

        metodo = new MethodBean.Builder("blob.package.Phone.Phone", "this.unformattedNumber = unformattedNumber;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone ", classPhone).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getAreaCode", "return unformattedNumber.substring(0,3);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", classPhone).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getPrefix", "return unformattedNumber.substring(3,6);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", classPhone).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getNumber", "return unformattedNumber.substring(6,10);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", classPhone).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);
        pack.addClassList(classe);

        methods = new MethodList();
        classe = new ClassBean.Builder("blob.package.Cliente", classCliente)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(12)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("package", packageContent).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(8)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package\\")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("name", new ClassBean.Builder("String", "").build());
        hash.put("età", new ClassBean.Builder("int", "").build());
        metodo = new MethodBean.Builder("blob.package.Cliente.Cliente", "this.name = name;\n" +
                "\t\tthis.età = età;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", classCliente).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called1.getList().add(metodo);

        instances.getList().remove(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.getName", "return name;")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", classCliente).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);

        instances.getList().remove(new InstanceVariableBean("name", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.getEtà", "return età;")
                .setReturnType(new ClassBean.Builder("int", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", classCliente).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called1.getList().add(metodo);
        pack.addClassList(classe);

        instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("nome_Ristorante", "String", "", "private "));
        methods = new MethodList();
        classe = new ClassBean.Builder("blob.package.Ristorante", classRistorante)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(12)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", packageContent).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(2)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package\\")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("nome_Ristorante", new ClassBean.Builder("String", "").build());
        metodo = new MethodBean.Builder("blob.package.Ristorante.Ristorante", "this.nome_Ristorante = nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Ristorante", classRistorante).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called2.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Ristorante.getNome_Ristorante", "return nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("Ristorante", classRistorante).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called2.getList().add(metodo);

        instances.getList().remove(new InstanceVariableBean("name", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.setNome_Ristorante", "this.nome_Ristorante = nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Ristorante", classRistorante).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();

        classe.addMethodBeanList(metodo);
        pack.addClassList(classe);

        methods = new MethodList();
        smelly = new ClassBean.Builder("blob.package.Prodotto", classProdotto)
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(imports)
                .setLOC(42)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", packageContent).build())
                .setEnviedPackage(null)
                .setEntityClassUsage(0)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("b", new ClassBean.Builder("String", "").build());
        metodo = new MethodBean.Builder("blob.package.Prodotto.withdraw", "public double withdraw(String b) {\n" +
                "            BankAccount new= BankAccount(b);\n" +
                "            b.getBalance() - 1000;\n" +
                "            return new;\n" +
                "        }")
                .setReturnType(new ClassBean.Builder("BankAccount", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called4)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", classProdotto).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Prodotto.listaClienti", "Scanner in= new Scanner(System.in);\n" +
                "\t\tString ristorante=in.nextLine();\n" +
                "\t\tRistorante r= new Ristorante(ristorante);\n" +
                "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                "\t")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called2)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", classProdotto).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("mobilePhone", "Phone", "", "private"));
        metodo = new MethodBean.Builder("blob.package.Prodotto.getMobilePhoneNumber", "return \"(\" +\n" +
                "         mobilePhone.getAreaCode() + \") \" +\n" +
                "         mobilePhone.getPrefix() + \"-\" +\n" +
                "         mobilePhone.getNumber();\n" +
                "   }")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(called3)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto ", classProdotto).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Prodotto.scorriListaClienti", "ArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Ugo\",51);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Maria\",16);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Lucia\",20);\n" +
                "\t\tclienti.add(c);\n" +
                "\n" +
                "\t\tint contatore=0;\n" +
                "\n" +
                "\t\tfor(int i=0;i<4;i++) {\n" +
                "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                "\t\t}\t\n" +
                "\t\treturn clienti.get(contatore);")
                .setReturnType(new ClassBean.Builder("Cliente", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called1)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", classProdotto).build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);
        pack.addClassList(smelly);


    }

    @Test
    public void isSmellyTrue() {
        TextualBlobStrategy analisi = new TextualBlobStrategy(0.5); //soglia default
        BlobCodeSmell smell = new BlobCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertTrue(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertTrue(risultato);
    }

    @Test
    public void isSmellyNearThreshold() {
        TextualBlobStrategy analisi = new TextualBlobStrategy(SmellynessMetricStub.computeSmellynessClass(smelly.getTextContent()) - 0.1);
        BlobCodeSmell smell = new BlobCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertTrue(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertTrue(risultato);
    }

    @Test
    public void isSmellyMinThreshold() {
        TextualBlobStrategy analisi = new TextualBlobStrategy(SmellynessMetricStub.computeSmellynessClass(smelly.getTextContent()));
        BlobCodeSmell smell = new BlobCodeSmell(analisi, "Textual");
        boolean risultato = smelly.isAffected(smell);
        assertFalse(smelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertFalse(risultato);
    }

    @Test
    public void isSmellyFalse() {
        TextualBlobStrategy analisi = new TextualBlobStrategy(0.5); //soglia default
        BlobCodeSmell smell = new BlobCodeSmell(analisi, "Textual");
        boolean risultato = noSmelly.isAffected(smell);
        assertFalse(noSmelly.getAffectedSmell().contains(smell));
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + risultato);
        assertFalse(risultato);
    }

}