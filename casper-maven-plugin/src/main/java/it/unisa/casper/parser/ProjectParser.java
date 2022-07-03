package it.unisa.casper.parser;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import it.unisa.casper.analysis.code_smell.BlobCodeSmell;
import it.unisa.casper.analysis.code_smell.FeatureEnvyCodeSmell;
import it.unisa.casper.analysis.code_smell.MisplacedClassCodeSmell;
import it.unisa.casper.analysis.code_smell.PromiscuousPackageCodeSmell;
import it.unisa.casper.analysis.code_smell_detection.blob.StructuralBlobStrategy;
import it.unisa.casper.analysis.code_smell_detection.blob.TextualBlobStrategy;
import it.unisa.casper.analysis.code_smell_detection.feature_envy.StructuralFeatureEnvyStrategy;
import it.unisa.casper.analysis.code_smell_detection.feature_envy.TextualFeatureEnvyStrategy;
import it.unisa.casper.analysis.code_smell_detection.misplaced_class.StructuralMisplacedClassStrategy;
import it.unisa.casper.analysis.code_smell_detection.misplaced_class.TextualMisplacedClassStrategy;
import it.unisa.casper.analysis.code_smell_detection.promiscuous_package.StructuralPromiscuousPackageStrategy;
import it.unisa.casper.analysis.code_smell_detection.promiscuous_package.TextualPromiscuousPackageStrategy;
import it.unisa.casper.storage.beans.*;
import org.apache.maven.project.MavenProject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ProjectParser implements Parser{

    private MavenProject project;
    private final List<PackageBean> projectPackages;
    private List<Thread> threadList;

    public ProjectParser(MavenProject project){
        this.project=project;
        projectPackages= new ArrayList<PackageBean>();
        threadList = new ArrayList<Thread>();


    }







    private String getPackageQualifiedName(File projectPackage) {
        String name = projectPackage.getAbsolutePath();
        name = name.substring(name.lastIndexOf("java" + File.separator) + 5);
        name = name.replace(File.separatorChar, '.');
        return name;
    }

    private List<CompilationUnit> getPackageClasses(File projectPackage) throws FileNotFoundException {

        JavaParser jp = new JavaParser();
        ArrayList<CompilationUnit> classes = new ArrayList<CompilationUnit>();


        File[] files = projectPackage.listFiles();
        for (File f : files) {
            if (!f.isDirectory())
                classes.add(jp.parse(f).getResult().get());
        }

        return classes;
    }

    private String getClassFileTextContent(CompilationUnit projectClass) {

        String textContent = "";

        for (Node n : projectClass.getChildNodes())
            textContent = textContent + n.toString();

        return textContent;

    }

    private File getClassPackage(CompilationUnit projectClass) {

        String packageDeclaration = projectClass.getPackageDeclaration().get().toString();
        packageDeclaration = packageDeclaration.substring(packageDeclaration.indexOf(" ") + 1, packageDeclaration.indexOf(";"));
        return new File(project.getCompileSourceRoots().get(0) + File.separator + packageDeclaration.replace('.', File.separatorChar));


    }

    private String getClassQualifiedName(CompilationUnit projectClass) {

        String packageDeclaration = projectClass.getPackageDeclaration().get().toString();
        packageDeclaration = packageDeclaration.substring(packageDeclaration.indexOf(" ") + 1, packageDeclaration.indexOf(";"));
        return packageDeclaration + "." + projectClass.getPrimaryTypeName().get();
    }

    private String getSuperClassName(CompilationUnit projectClass) {

        if (projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getExtendedTypes().isNonEmpty())
            return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getExtendedTypes().get(0).getNameWithScope();
        else
            return null;


    }

    private List<FieldDeclaration> getClassFields(CompilationUnit projectClass) {

        return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getFields();
    }

    private List<InstanceVariableBean> parse(FieldDeclaration field) {

        List<VariableDeclarator> instanceVariables = new ArrayList<VariableDeclarator>();

        for (int i = 0; i < field.getVariables().size(); i++)
            instanceVariables.add(field.getVariables().get(i));

        boolean visibilityFound = false;
        String visibility = null;

        for (int i = 0; i < field.getModifiers().size() && !visibilityFound; i++) {


            if (field.getModifiers().get(i).getKeyword().toString().equalsIgnoreCase("public")) {

                visibility = "public";
                visibilityFound = true;
            } else if (field.getModifiers().get(i).getKeyword().toString().equalsIgnoreCase("private")) {

                visibility = "private";
                visibilityFound = true;
            } else if (field.getModifiers().get(i).getKeyword().toString().equalsIgnoreCase("protected")) {

                visibility = "protected";
                visibilityFound = true;
            }

        }

        List<InstanceVariableBean> result = new ArrayList<InstanceVariableBean>();


        for (VariableDeclarator v : instanceVariables) {

            String name = v.getNameAsString();
            String type = v.getTypeAsString();
            String init = "";
            if (v.getInitializer().isPresent()) {
                init = v.getInitializer().get().toString();
            }
            result.add(new InstanceVariableBean(name, type, init, visibility));
        }

        return result;
    }




    private void methosAnalysis(HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, MethodBean methodBean) {

        /*//ANALISI STORICA
        //feature envy

        HistoryFeatureEnvyStrategy historyFeatureEnvyStrategy = new HistoryFeatureEnvyStrategy(projectPackages);
        FeatureEnvyCodeSmell hFeatureEnvyCodeSmell = new FeatureEnvyCodeSmell(historyFeatureEnvyStrategy, "History");
        methodBean.isAffected(hFeatureEnvyCodeSmell);*/


        TextualFeatureEnvyStrategy textualFeatureEnvyStrategy = new TextualFeatureEnvyStrategy(projectPackages, coseno.get("cosenoFeature"));
        FeatureEnvyCodeSmell tFeatureEnvyCodeSmell = new FeatureEnvyCodeSmell(textualFeatureEnvyStrategy, "Textual");
        methodBean.isAffected(tFeatureEnvyCodeSmell);

        StructuralFeatureEnvyStrategy structuralFeatureEnvyStrategy = new StructuralFeatureEnvyStrategy(projectPackages, dipendence.get("dipFeature"));
        FeatureEnvyCodeSmell sFeatureEnvyCodeSmell = new FeatureEnvyCodeSmell(structuralFeatureEnvyStrategy, "Structural");
        methodBean.isAffected(sFeatureEnvyCodeSmell);

    }

    private void classAnalysis(HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, ClassBean classBean) {
        /*//ANALISI STORICA
        //blob
       HistoryBlobStrategy historyBlobStrategy = new HistoryBlobStrategy();
        BlobCodeSmell hBlobCodeSmell = new BlobCodeSmell(historyBlobStrategy, "History");
        Thread t = new Thread(new AnalyzerThread(classBean, hBlobCodeSmell));
        threadList.add(t);
        t.start();


        //Shotgun surgery
        HistoryShotgunSurgeryStrategy historyShotgunSurgeryStrategy = new HistoryShotgunSurgeryStrategy(projectPackages);
        ShotgunSurgeryCodeSmell shotgunSurgeryCodeSmell = new ShotgunSurgeryCodeSmell(historyShotgunSurgeryStrategy, "History");
        classBean.isAffected(shotgunSurgeryCodeSmell);



        //Divergent change
        HistoryDivergentChangeStrategy historyDivergentChangeStrategy = new HistoryDivergentChangeStrategy();
        DivergentChangeCodeSmell divergentChangeCodeSmell = new DivergentChangeCodeSmell(historyDivergentChangeStrategy, "History");
        classBean.isAffected(divergentChangeCodeSmell);



        //Parallel Inheritance
        HistoryParallelInheritanceStrategy historyParallelInheritanceStrategy = new HistoryParallelInheritanceStrategy(projectPackages);
        ParallelInheritanceCodeSmell parallelInheritanceCodeSmell = new ParallelInheritanceCodeSmell(historyParallelInheritanceStrategy, "History");
        classBean.isAffected(parallelInheritanceCodeSmell);*/


        TextualBlobStrategy textualBlobStrategy = new TextualBlobStrategy(coseno.get("cosenoBlob"));
        BlobCodeSmell tBlobCodeSmell = new BlobCodeSmell(textualBlobStrategy, "Textual");
        TextualMisplacedClassStrategy textualMisplacedClassStrategy = new TextualMisplacedClassStrategy(projectPackages, coseno.get("cosenoMisplaced"));
        MisplacedClassCodeSmell tMisplacedClassCodeSmell = new MisplacedClassCodeSmell(textualMisplacedClassStrategy, "Textual");
        classBean.isAffected(tBlobCodeSmell);
        classBean.isAffected(tMisplacedClassCodeSmell);
        classBean.setSimilarity(0);

        StructuralBlobStrategy structuralBlobStrategy = new StructuralBlobStrategy(dipendence.get("dipBlob"), dipendence.get("dipBlob2"), dipendence.get("dipBlob3"));
        BlobCodeSmell sBlobCodeSmell = new BlobCodeSmell(structuralBlobStrategy, "Structural");
        StructuralMisplacedClassStrategy structuralMisplacedClassStrategy = new StructuralMisplacedClassStrategy(projectPackages, dipendence.get("dipMisplaced"));
        MisplacedClassCodeSmell sMisplacedClassCodeSmell = new MisplacedClassCodeSmell(structuralMisplacedClassStrategy, "Structural");
        classBean.isAffected(sBlobCodeSmell);
        classBean.isAffected(sMisplacedClassCodeSmell);
        classBean.setSimilarity(0);

    }

    private void packageAnalysis(HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, PackageBean packageBean) {


        TextualPromiscuousPackageStrategy textualPromiscuousPackageStrategy = new TextualPromiscuousPackageStrategy(coseno.get("cosenoPromiscuous"));
        PromiscuousPackageCodeSmell tPromiscuousPackagecodeSmell = new PromiscuousPackageCodeSmell(textualPromiscuousPackageStrategy, "Textual");
        packageBean.isAffected(tPromiscuousPackagecodeSmell);
        packageBean.setSimilarity(0);

        StructuralPromiscuousPackageStrategy structuralPromiscuousPackageStrategy = new StructuralPromiscuousPackageStrategy(projectPackages, dipendence.get("dipPromiscuous") / 100, dipendence.get("dipPromiscuous2") / 100);
        PromiscuousPackageCodeSmell sPromiscuousPackagecodeSmell = new PromiscuousPackageCodeSmell(structuralPromiscuousPackageStrategy, "Structural");
        packageBean.isAffected(sPromiscuousPackagecodeSmell);
        packageBean.setSimilarity(0);

    }


}

