package it.unisa.casper.cli;

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


@Mojo(name = "hellogoal", defaultPhase = LifecyclePhase.COMPILE)
public class ExampleMojo extends AbstractMojo {

    @Parameter
    private HashMap<String, Double> cosine;
    @Parameter
    private HashMap<String, Integer> dependency;
    @Parameter
    private Properties textual;
    @Parameter
    private Properties structural;
    @Parameter(defaultValue = "false")
    private boolean textContent;
    @Parameter(defaultValue = "mfpb")
    private String display;
    @Inject
    private MavenProject project;
    @Parameter(property = "dump")
    private String dump;


    private List<PackageBean> projectPackages;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //check inputs

        try {
            parser = new ProjectParser(project);
            projectPackages = parser.parse();
            HashMap<String, HashMap> mapSmells = analysis();
            Output out = dump == null ? new Output(mapSmells) : new Output(mapSmells, new PrintStream(new File(dump)));
            out.write();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, HashMap> analysis() {
        HashMap<PackageBean, List<CodeSmell>> packageSmells = new HashMap<>();
        HashMap<ClassBean, List<CodeSmell>> classSmells = new HashMap<>();
        HashMap<MethodBean, List<CodeSmell>> methodSmells = new HashMap<>();
        HashMap<String, HashMap> projectSmells = new HashMap<>();
        List <CodeSmell> tmp;

        for (PackageBean packageBean : projectPackages) {
            AnalysisStartup.packageAnalysis(projectPackages, cosine, dependency, packageBean);
            tmp = packageBean.getAffectedSmell();

            if (!tmp.isEmpty())
                packageSmells.put(packageBean, tmp);

            for (ClassBean classBean : packageBean.getClassList()) {
                classAnalysis(projectPackages, cosine, dependency, classBean);
                tmp = classBean.getAffectedSmell();

                if (!tmp.isEmpty())
                    classSmells.put(classBean, classBean.getAffectedSmell());

                for (MethodBean methodBean : classBean.getMethodList()) {
                    methodAnalysis(projectPackages, cosine, dependency, methodBean);
                    tmp = methodBean.getAffectedSmell();
                    if (!tmp.isEmpty())
                        methodSmells.put(methodBean, methodBean.getAffectedSmell());
                }
            }
        }

        projectSmells.put("package", packageSmells);
        projectSmells.put("class", classSmells);
        projectSmells.put("method", methodSmells);

        return projectSmells;
    }

    private Parser parser;

    private class Output {

        private HashMap<String, HashMap> maps;
        private PrintStream stream;

        public Output(HashMap<String, HashMap> map, PrintStream stream) { maps = map; this.stream = stream; }

        public Output(HashMap<String, HashMap> map) { maps = map; stream = System.out;}

        public void write() {
            writeClassesAffected();
            writePackagesAffected();
            writeMethodsAffected();
        }

        private void writePackagesAffected () {
            HashMap<PackageBean, List<CodeSmell>> map = maps.get("package");

            for (Map.Entry<PackageBean, List<CodeSmell>> entry : map.entrySet()) {
                PackageBean bean = entry.getKey();
                List<CodeSmell> smells = entry.getValue();

                for (CodeSmell smell : smells)
                    stream.println(bean.getFullQualifiedName() +" " +smell.getSmellName()+" " +smell.getAlgoritmsUsed() +" " +smell.getIndex());
            }
        }

        private void writeClassesAffected () {
            HashMap<ClassBean, List<CodeSmell>> map = maps.get("class");

            for (Map.Entry<ClassBean, List<CodeSmell>> entry : map.entrySet()) {
                ClassBean bean = entry.getKey();
                List<CodeSmell> smells = entry.getValue();

                for (CodeSmell smell : smells)
                    stream.println(bean.getFullQualifiedName() +" " +smell.getSmellName()+" " +smell.getAlgoritmsUsed() +" " +smell.getIndex());
            }
        }

        private void writeMethodsAffected () {
            HashMap<MethodBean, List<CodeSmell>> map = maps.get("method");

            for (Map.Entry<MethodBean, List<CodeSmell>> entry : map.entrySet()) {
                MethodBean bean = entry.getKey();
                List<CodeSmell> smells = entry.getValue();

                for (CodeSmell smell : smells)
                    stream.println(bean.getFullQualifiedName() +" " +smell.getSmellName()+" " +smell.getAlgoritmsUsed() +" " +smell.getIndex());
            }
        }
    }
}

