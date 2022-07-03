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







}

