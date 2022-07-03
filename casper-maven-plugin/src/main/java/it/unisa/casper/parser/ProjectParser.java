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

    private List<MethodDeclaration> getClassMethods(CompilationUnit projectClass) {

        return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getMethods();

    }

    private List<ConstructorDeclaration> getClassConstructors(CompilationUnit projectClass) {

        return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getConstructors();

    }

    private String getMethodName(MethodDeclaration method) {
        return method.getNameAsString();
    }

    private String getConstructorName(ConstructorDeclaration method) {
        return method.getNameAsString();
    }

    private CompilationUnit getMethodContainingClass(MethodDeclaration method) {
        return method.findCompilationUnit().get();
    }

    private CompilationUnit getConstructorContainingClass(ConstructorDeclaration method) {
        return method.findCompilationUnit().get();
    }

    private String getClassTextContent(CompilationUnit projectClass) {
        return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().toString();
    }

    private String getMethodTextContent(MethodDeclaration method) {

        return method.toString();

    }

    private String getConstructorTextContent(ConstructorDeclaration method) {

        return method.toString();

    }

    private String getMethodBodyTextContent(MethodDeclaration method) {
        return method.getBody().get().toString();
    }

    private String getConstructorBodyTextContent(ConstructorDeclaration method) {
        return method.getBody().toString();
    }

    private boolean isStaticMethod(MethodDeclaration method) {

        NodeList<Modifier> modifiers = method.getModifiers();
        boolean result = false;

        for (int i = 0; i < modifiers.size() && !result; i++)
            if (modifiers.get(i).getKeyword().toString().equalsIgnoreCase("static"))
                result = true;

        return result;

    }

    private String getMethodReturnType(MethodDeclaration method) {

        return method.getType().toString();


    }

    private NodeList<com.github.javaparser.ast.body.Parameter> getMethodParameters(MethodDeclaration method) {
        return method.getParameters();

    }

    private NodeList<com.github.javaparser.ast.body.Parameter> getConstructorParameters(ConstructorDeclaration method) {
        return method.getParameters();

    }



    private String getMethodVisibility(MethodDeclaration method) {
        NodeList<Modifier> modifiers = method.getModifiers();
        boolean found = false;
        String visibility = null;

        for (int i = 0; i < modifiers.size() && !found; i++){
            if (modifiers.get(i).getKeyword().toString().equalsIgnoreCase("public")) {
                found = true;
                visibility = "public" ;

            } else
            if(modifiers.get(i).getKeyword().toString().equalsIgnoreCase("private")){
                found=true;
                visibility="private";
            }
            else
            if(modifiers.get(i).getKeyword().toString().equalsIgnoreCase("protected")){
                found=true;
                visibility="protected";
            }
        }

        return visibility;
    }

    private String getConstructorVisibility(ConstructorDeclaration method) {
        NodeList<Modifier> modifiers = method.getModifiers();
        boolean found = false;
        String visibility = null;

        for (int i = 0; i < modifiers.size() && !found; i++){
            if (modifiers.get(i).getKeyword().toString().equalsIgnoreCase("public")) {
                found = true;
                visibility = "public" ;

            } else
            if(modifiers.get(i).getKeyword().toString().equalsIgnoreCase("private")){
                found=true;
                visibility="private";
            }
            else
            if(modifiers.get(i).getKeyword().toString().equalsIgnoreCase("protected")){
                found=true;
                visibility="protected";
            }
        }

        return visibility;
    }

    private List<MethodCallExpr> getInvokedMethods(MethodDeclaration method){


        List<MethodCallExpr> result = new ArrayList<MethodCallExpr>();


        for(MethodCallExpr call : method.findAll(MethodCallExpr.class))
            result.add(call);





        return result;
    }

    private List<MethodCallExpr> getInvokedMethodsByConstructor(ConstructorDeclaration method){


        List<MethodCallExpr> result = new ArrayList<MethodCallExpr>();


        for(MethodCallExpr call : method.findAll(MethodCallExpr.class))
            result.add(call);





        return result;
    }

    private List<ObjectCreationExpr> getInvokedConstructors(MethodDeclaration method){

        List<ObjectCreationExpr> result = new ArrayList<ObjectCreationExpr>();




        for(ObjectCreationExpr call : method.findAll(ObjectCreationExpr.class))
            result.add(call);


        return result;

    }

    private List<ObjectCreationExpr> getInvokedConstructorsByConstructor(ConstructorDeclaration method){

        List<ObjectCreationExpr> result = new ArrayList<ObjectCreationExpr>();




        for(ObjectCreationExpr call : method.findAll(ObjectCreationExpr.class))
            result.add(call);


        return result;

    }

    private List<MethodBean> findMethodInvocations(MethodDeclaration method){

        List<MethodBean> invocations = new ArrayList<MethodBean>();

        String qualifiedName = "";

        List<MethodCallExpr> methodCallExpressions = getInvokedMethods(method);
        List<ObjectCreationExpr> constructorExpressions = getInvokedConstructors(method);

        MethodBean bean;
        JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));

        for(MethodCallExpr call : methodCallExpressions){
            try{
                String className=JavaParserFacade.get(solver).getType(call.getScope().get()).toString();
                className = className.substring(className.indexOf("{")+1,className.indexOf(","));
                bean = new MethodBean.Builder(className+"."+call.getNameAsString(),"").build();
                if(!invocations.contains(bean))
                    invocations.add(bean);
            }

            catch(Exception e){
                //Il metodo è un metodo di libreria
            }
        }



        for(ObjectCreationExpr call : constructorExpressions){
            try{

                String className=JavaParserFacade.get(solver).getType(call).toString();
                className = className.substring(className.indexOf("{")+1,className.indexOf(","));
                bean = new MethodBean.Builder(className+"."+call.getType().getNameAsString(),"").build();
                if(!invocations.contains(bean))
                    invocations.add(bean);


            }
            catch(Exception e){

                //Il costruttore è un costruttore di libreria

            }
        }

        return invocations;




    }

    private List<MethodBean> findMethodInvocationsByConstructor(ConstructorDeclaration method){

        List<MethodBean> invocations = new ArrayList<MethodBean>();

        String qualifiedName = "";

        List<MethodCallExpr> methodCallExpressions = getInvokedMethodsByConstructor(method);
        List<ObjectCreationExpr> constructorExpressions = getInvokedConstructorsByConstructor(method);

        MethodBean bean;
        JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));

        for(MethodCallExpr call : methodCallExpressions){
            try{
                String className=JavaParserFacade.get(solver).getType(call).toString();
                className = className.substring(className.indexOf("{")+1,className.indexOf(","));
                bean = new MethodBean.Builder(className+"."+call.getNameAsString(),"").build();
                if(!invocations.contains(bean))
                    invocations.add(bean);
            }

            catch(Exception e){
                //Il metodo è un metodo di libreria
            }
        }




        for(ObjectCreationExpr call : constructorExpressions){
            try{

                String className=JavaParserFacade.get(solver).getType(call).toString();
                className = className.substring(className.indexOf("{")+1,className.indexOf(","));
                bean = new MethodBean.Builder(className+"."+call.getType().getNameAsString(),"").build();
                if(!invocations.contains(bean))
                    invocations.add(bean);


            }
            catch(Exception e){

                //Il costruttore è un costruttore di libreria

            }
        }

        return invocations;




    }

    private MethodBean parse(MethodDeclaration method, String textContent){

        MethodBean.Builder builder = new MethodBean.Builder(getClassQualifiedName(getMethodContainingClass(method))+"."+getMethodName(method),getMethodTextContent(method));
        builder.setStaticMethod(isStaticMethod(method));

        ArrayList<InstanceVariableBean> list = new ArrayList<>();
        List<FieldDeclaration> fields =getClassFields(getMethodContainingClass(method));
        String methodBody=getMethodBodyTextContent(method);

        for(FieldDeclaration fd :fields){
            List<InstanceVariableBean> temp = parse(fd);
            for(InstanceVariableBean var : temp)
                if(methodBody.contains(var.getFullQualifiedName()))
                    list.add(var);
        }

        InstanceVariableList instanceVariableList = new InstanceVariableList();
        instanceVariableList.setList(list);

        String returnTypeName = getMethodReturnType(method);
        ClassBean returnTypeBean = new ClassBean.Builder(returnTypeName,"").build();
        builder.setReturnType(returnTypeBean);
        builder.setInstanceVariableList(instanceVariableList);

        ClassBean belongingClass = new ClassBean.Builder(getClassQualifiedName(getMethodContainingClass(method)),textContent).build();
        builder.setBelongingClass(belongingClass);

        HashMap<String,ClassBean> map = new HashMap<>();
        for(com.github.javaparser.ast.body.Parameter p : getMethodParameters(method)){
            map.put(p.getNameAsString(), new ClassBean.Builder(p.getTypeAsString(),"").build());
        }

        builder.setParameters(map);
        builder.setVisibility(getMethodVisibility(method));

        MethodBeanList methodCalls = new MethodList();
        List<MethodBean> methodCallList = new ArrayList<MethodBean>();

        for(MethodBean m : findMethodInvocations(method))
            methodCallList.add(m);


        ((MethodList) methodCalls).setList(methodCallList);
        builder.setMethodsCalls(methodCalls);
        return builder.build();

    }







}

