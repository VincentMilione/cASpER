package it.unisa.casper.parser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import it.unisa.casper.storage.beans.*;
import javassist.compiler.ast.MethodDecl;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
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
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        TypeSolver typeSolver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        JavaParser jp = new JavaParser(parserConfiguration);
        ArrayList<CompilationUnit> classes = new ArrayList<CompilationUnit>();

        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(projectPackage.listFiles()));
        files.sort(Comparator.comparingInt(e -> e.getName().length()));

        for (File f : files) {
            if (!f.isDirectory()) {
                Optional<CompilationUnit> compilationUnit = jp.parse(f).getResult();
                compilationUnit.ifPresent(e -> classes.add(e));

            }

        }

        return classes;
    }

    private String getClassFileTextContent(CompilationUnit projectClass) {

        String textContent = "";

        for (Node n : projectClass.getChildNodes()) {
            textContent = textContent + n.toString();
        }

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
        Optional<String> type = projectClass.getPrimaryTypeName();
        String declaration = type.isPresent() ? type.get() : "";
        Optional<ClassOrInterfaceDeclaration> string = projectClass.getClassByName(declaration);

        if (string.isPresent() ? string.get().getExtendedTypes().isNonEmpty() : false) {
            JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));

            ResolvedType rt = JavaParserFacade.get(solver).convertToUsage(projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getExtendedTypes().get(0));
            return rt.describe();
            //return projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).get().getExtendedTypes().get(0).getNameWithScope();
        }
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

        JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));
        String temp;

        for (VariableDeclarator v : instanceVariables) {


            try {
                ResolvedType rt = JavaParserFacade.get(solver).convertToUsage(v.getType());
                temp=rt.describe();
            }

            catch(UnsolvedSymbolException | NullPointerException e){

                temp=v.getTypeAsString();
            }


            String name = v.getNameAsString();
            String type = temp;
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
        Optional<String> type = projectClass.getPrimaryTypeName();
        String typeName = type.isPresent() ? type.get() : "";
        Optional<ClassOrInterfaceDeclaration> classOption = projectClass.getClassByName(typeName);
        String classContent = classOption.isPresent() ? classOption.get().toString() : "";

        return classContent;
    }

    private String getMethodTextContent(MethodDeclaration method) {

        return method.toString();

    }

    private String getConstructorTextContent(ConstructorDeclaration method) {

        return method.toString();

    }

    private String getMethodBodyTextContent(MethodDeclaration method) {
        if(method.getBody().isPresent())
        return method.getBody().get().toString();
        else
            return "";
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
        try {

            JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));

            ResolvedType type = JavaParserFacade.get(solver).convertToUsage(method.getType());

            return type.describe();
        } catch (Exception e) {
            return method.getType().toString();
        }
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


        for(MethodCallExpr call : method.findAll(MethodCallExpr.class)) {

                result.add(call);
        }





        return result;
    }

    private List<MethodCallExpr> getInvokedMethodsByConstructor(ConstructorDeclaration method){


        List<MethodCallExpr> result = new ArrayList<MethodCallExpr>();





        for(MethodCallExpr call : method.findAll(MethodCallExpr.class)) {

                result.add(call);
        }





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
                Optional<Expression> optional = call.getScope();
                if (optional.isPresent()) {
                    ResolvedType resolvedType = JavaParserFacade.get(solver).getType(optional.get());
                    String className = resolvedType.describe();

                    bean = new MethodBean.Builder(className + "." + call.getNameAsString(), "").build();
                    if (!invocations.contains(bean))
                        invocations.add(bean);
                }
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



        if(method.findAll(ExplicitConstructorInvocationStmt.class).size()>=1){
            String superClassName = getSuperClassName(getConstructorContainingClass(method));
            bean = new MethodBean.Builder(superClassName+"."+superClassName.substring(superClassName.lastIndexOf(".")+1),"").build();
            invocations.add(bean);
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
        String returnTypeName;


             returnTypeName = getMethodReturnType(method);
             if(returnTypeName==null)
                 returnTypeName="void";


        ClassBean returnTypeBean = new ClassBean.Builder(returnTypeName,"").build();
        builder.setReturnType(returnTypeBean);
        builder.setInstanceVariableList(instanceVariableList);

        ClassBean belongingClass = new ClassBean.Builder(getClassQualifiedName(getMethodContainingClass(method)),textContent).build();
        builder.setBelongingClass(belongingClass);

        HashMap<String,ClassBean> map = new HashMap<>();
        for(com.github.javaparser.ast.body.Parameter p : getMethodParameters(method)){
            try {
                JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));
                ResolvedType type = JavaParserFacade.get(solver).convertToUsage(p.getType());
                map.put(p.getNameAsString(), new ClassBean.Builder(type.describe(), "").build());
            }catch (Exception e) {
                map.put(p.getNameAsString(), new ClassBean.Builder(p.getTypeAsString(), "").build());
            }
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

    private MethodBean parse(ConstructorDeclaration method, String textContent){

        MethodBean.Builder builder = new MethodBean.Builder(getClassQualifiedName(getConstructorContainingClass(method))+"."+getConstructorName(method),getConstructorTextContent(method));

        ArrayList<InstanceVariableBean> list = new ArrayList<>();
        List<FieldDeclaration> fields =getClassFields(getConstructorContainingClass(method));
        String methodBody=getConstructorBodyTextContent(method);

        for(FieldDeclaration fd :fields){
            List<InstanceVariableBean> temp = parse(fd);
            for(InstanceVariableBean var : temp)
                if(methodBody.contains(var.getFullQualifiedName()))
                    list.add(var);
        }

        InstanceVariableList instanceVariableList = new InstanceVariableList();
        instanceVariableList.setList(list);

        String returnTypeName = "void";
        ClassBean returnTypeBean = new ClassBean.Builder(returnTypeName,"").build();
        builder.setReturnType(returnTypeBean);


        builder.setInstanceVariableList(instanceVariableList);

        ClassBean belongingClass = new ClassBean.Builder(getClassQualifiedName(getConstructorContainingClass(method)),textContent).build();
        builder.setBelongingClass(belongingClass);

        HashMap<String,ClassBean> map = new HashMap<>();
        for(com.github.javaparser.ast.body.Parameter p : getConstructorParameters(method)){
            try {
                JavaParserTypeSolver solver = new JavaParserTypeSolver(project.getCompileSourceRoots().get(0));
                ResolvedType type = JavaParserFacade.get(solver).convertToUsage(p.getType());
                map.put(p.getNameAsString(), new ClassBean.Builder(type.describe(), "").build());
            }
            catch(NullPointerException | UnsolvedSymbolException e){
                map.put(p.getNameAsString(), new ClassBean.Builder(p.getTypeAsString(), "").build());

            }
        }

        builder.setParameters(map);
        builder.setVisibility(getConstructorVisibility(method));

        MethodBeanList methodCalls = new MethodList();
        List<MethodBean> methodCallList = new ArrayList<MethodBean>();

        for(MethodBean m : findMethodInvocationsByConstructor(method))
            methodCallList.add(m);


        ((MethodList) methodCalls).setList(methodCallList);
        builder.setMethodsCalls(methodCalls);
        return builder.build();

    }

    private ClassBean parse(CompilationUnit projectClass, String contentForPackage){
        File projectPackage = getClassPackage(projectClass);
        String packageName = getPackageQualifiedName(projectPackage);

        if(!projectClass.getClassByName(projectClass.getPrimaryTypeName().get()).isPresent())
            return null;

        PackageBean packageBean = new PackageBean.Builder(packageName,contentForPackage).build();

        String name = getClassQualifiedName(projectClass);
        String text = getClassFileTextContent(projectClass);

        ClassBean.Builder builder = new ClassBean.Builder(name, text);
        builder.setBelongingPackage(packageBean);

        String pathToFile = project.getCompileSourceRoots().get(0)+File.separator+(getClassQualifiedName(projectClass).replace('.',File.separatorChar))+".java";
        builder.setPathToFile(pathToFile);

        if(getSuperClassName(projectClass)!=null)
            builder.setSuperclass(getSuperClassName(projectClass));

        Pattern newLine = Pattern.compile("\n");
        String[] lines = newLine.split(getClassTextContent(projectClass));
        builder.setLOC(lines.length);

        ArrayList<InstanceVariableBean> listVariabili = new ArrayList<>();
        List<FieldDeclaration> fields = getClassFields(projectClass);
        for(FieldDeclaration field : fields){
            for(InstanceVariableBean var : parse(field))
                listVariabili.add(var);
        }

        InstanceVariableList instanceVariableList = new InstanceVariableList();
        instanceVariableList.setList(listVariabili);
        builder.setInstanceVariables(instanceVariableList);

        ArrayList<MethodBean> listaMetodi = new ArrayList<>();
        List<MethodDeclaration > methods = getClassMethods(projectClass);
        for(MethodDeclaration m :methods)
            listaMetodi.add(parse(m,text));

        List<ConstructorDeclaration> constructors = getClassConstructors(projectClass);
        for(ConstructorDeclaration cd : constructors)
            listaMetodi.add(parse(cd,text));

        MethodList methodList = new MethodList();
        methodList.setList(listaMetodi);
        builder.setMethods(methodList);

        List<String> imports = new ArrayList<String>();
        for(ImportDeclaration i : getClassImports(projectClass))
            imports.add(i.toString());
        builder.setImports(imports);
        return builder.build();
    }

    private List<ImportDeclaration> getClassImports(CompilationUnit projectClass){
        return projectClass.getImports();
    }

    private PackageBean parse(File projectPackage){

        StringBuilder textContent = new StringBuilder();
        String name;
        ArrayList<ClassBean> list = new ArrayList<ClassBean>();

        name = getPackageQualifiedName(projectPackage);
        List<CompilationUnit> classes=null;

        try {
            classes = getPackageClasses(projectPackage);
        }

        catch(FileNotFoundException e){

            e.printStackTrace();
        }


        for(CompilationUnit projectClass : classes)
            textContent.append(getClassFileTextContent(projectClass));

        PackageBean.Builder builder = new PackageBean.Builder(name,textContent.toString());

        ClassList classBeanList = new ClassList();
        ClassBean temp;

        for(CompilationUnit projectClass : classes) {
            temp=parse(projectClass, textContent.toString());
            if(temp!=null)
            list.add(temp);
        }

        classBeanList.setList(list);
        builder.setClassList(classBeanList);
        return builder.build();


    }

    private void getAllPackages(File start, ArrayList<File> list) {

        for(File child : start.listFiles())
            if(child.isDirectory()){
                for(int i =0; i<child.listFiles().length;i++)
                    if(!child.listFiles()[i].isDirectory()) {
                        list.add(child);
                        break;
                    }
                getAllPackages(child,list);
            }



    }

    public List<PackageBean> parse() throws ParsingException {

        PackageBean parsedPackageBean;
        List<String> temp = project.getCompileSourceRoots();
        String basePath = temp.get(0);
        ArrayList<File> packages = new ArrayList<File>();
        getAllPackages(new File(basePath),packages);
        for(File projectPackage : packages){
            parsedPackageBean = parse(projectPackage);
            projectPackages.add(parsedPackageBean);
        }



        return projectPackages;

    }


}

