package it.unisa.casper.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dnl.utils.text.table.TextTable;
import it.unisa.casper.analysis.code_smell.*;
import static it.unisa.casper.cli.AnalysisStartup.*;
import it.unisa.casper.parser.Parser;
import it.unisa.casper.parser.ProjectParser;
import it.unisa.casper.storage.beans.ClassBean;
import it.unisa.casper.storage.beans.MethodBean;
import it.unisa.casper.storage.beans.PackageBean;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import javax.inject.Inject;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;


@Mojo(name = "goaldetection", defaultPhase = LifecyclePhase.COMPILE)
public class ExampleMojo extends AbstractMojo {

    @Parameter
    private HashMap<String, Double> cosine;
    @Parameter
    private HashMap<String, Integer> dependency;
    @Parameter(defaultValue = "false")
    private boolean textContent;
    @Inject
    private MavenProject project;
    @Parameter(property = "dump")
    private String dump;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //check inputs
        Boolean isDump = Objects.isNull(dump) ? false : dump.length() > 0;

        try (PrintStream stream = isDump ? new PrintStream(new File(dump)) : System.out) {

            parser = new ProjectParser(project);
            List<PackageBean> projectPackages = parser.parse();
            List<HashMap<String, String>> mapSmells = analysis(projectPackages);
            String [] columnames = {"Component Name","Smell Name","Algorithm", "Priority", "soglia"};

            TextTable table = new TextTable(columnames, prepareData(mapSmells));
            table.printTable(stream, 2);
            //print results of analysis
            if (textContent) {
                stream.println();
                stream.println("----------------------------------------------------");
                for (HashMap<String, String> object : mapSmells) {
                    stream.printf("%s\t\t\t%s\t\t%s\n%s", object.get("componentName"), object.get("smellName"), object.get("algorithm"), object.get("textContent"));
                    stream.println();
                    stream.println("----------------------------------------------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object[][] prepareData (List<HashMap<String, String>> mapSmells) {
        Object[][] objects = new Object[mapSmells.size()][];
        int i = 0;
        for (HashMap<String, String> el : mapSmells) {
            String componentName = el.get("componentName");
            String smellName = el.get("smellName");
            String algorithm = el.get("algorithm");
            String soglia = el.get("soglie");
            String priority = el.get("priority");

            objects[i] = new Object[5];
            objects[i][0] = componentName;
            objects[i][1] = smellName;
            objects[i][2] = algorithm;
            objects[i][3] = priority;
            objects[i][4] = soglia;
            i++;
        }
        return objects;
    }

    private List <HashMap<String, String>> extractInfo (String fullQualifiedName, List<CodeSmell> smells, String text) {
        List<HashMap<String, String>> lst = new ArrayList<>();

        for (CodeSmell smell : smells) {
                HashMap<String, String> map = new HashMap<>();


                map.put("componentName", fullQualifiedName);
                map.put("textContent", text);
                map.put("smellName", smell.getSmellName());
                map.put("algorithm", smell.getAlgoritmsUsed());
                map.put("soglie", AnalysisStartup.soglie(smell));
                map.put("priority", AnalysisStartup.prioritySmell(smell, dependency, cosine));
                lst.add(map);
        }

        return lst;
    }


    private List<HashMap<String, String>> analysis(List<PackageBean> projectPackages) {
        if (Objects.isNull(projectPackages)) return new ArrayList<>();

        List<HashMap<String, String>> projectSmells = new ArrayList<>();

        for (PackageBean packageBean : projectPackages) {
            AnalysisStartup.packageAnalysis(projectPackages, cosine, dependency, packageBean);
            String fullqualifiedPackage = packageBean.getFullQualifiedName(), packageText = packageBean.getTextContent();
            List<CodeSmell> packageSmells = packageBean.getAffectedSmell(); //smell name, algorithm e soglie;
            List<ClassBean> classes = !Objects.isNull(packageBean.getClassList()) ? packageBean.getClassList() : new ArrayList<>();

            for (ClassBean classBean : classes) {
                classAnalysis(projectPackages, cosine, dependency, classBean);
                String fullqualifiedClass = classBean.getFullQualifiedName(), classText = classBean.getTextContent();
                List<CodeSmell> classSmells = classBean.getAffectedSmell(); //smell name, algorithm e soglie;
                List<MethodBean> methods = !Objects.isNull(classBean.getMethodList()) ? classBean.getMethodList() : new ArrayList<>();

                for (MethodBean methodBean : methods) {
                    methodAnalysis(projectPackages, cosine, dependency, methodBean);
                    String fullqualifiedMethod = methodBean.getFullQualifiedName(), methodText = classBean.getTextContent();
                    List<CodeSmell> methodSmells = methodBean.getAffectedSmell(); //smell name, algorithm e soglie;

                    projectSmells.addAll(extractInfo(fullqualifiedMethod, methodSmells, methodText));
                }
                projectSmells.addAll(extractInfo(fullqualifiedClass, classSmells, classText));
            }
            projectSmells.addAll(extractInfo(fullqualifiedPackage, packageSmells, packageText));
        }

        return projectSmells;
    }

    private Parser parser;
}

