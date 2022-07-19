package it.unisa.casper.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import it.unisa.casper.storage.beans.PackageBean;
import org.apache.maven.project.MavenProject;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.*;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


/**
 * ParserTest verifica che il metodo parse() della classe ProjectParser.java
 * abbia lo stesso comportamento di PsiParser.java del progetto originale.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParserTest {

    @Mock
    MavenProject project;
    List<String> roots;
    List<String> oracles;
    Customization textContent = new Customization("**.textContent", (e1, e2) -> {
        String regex = "package[a-zA-Z\\.0-9]+;(import[a-zA-Z\\.0-9]+;)*";
        String[] packageContent1 = e1.toString().split(regex);
        String[] packageContent2 = e1.toString().split(regex);
        List<String> lst1 = Arrays.asList(packageContent1);
        List<String> lst2 = Arrays.asList(packageContent2);

        return lst2.containsAll(lst1) && lst1.containsAll(lst2);
    });

    Customization muteLOC = new Customization("**.classes.classes", new ArrayValueMatcher<>(
            new CustomComparator(JSONCompareMode.LENIENT, new Customization("**.LOC", (e1, e2) -> {
                return true;
            }))));

    Customization classes = new Customization("**.classes.classes", new ArrayValueMatcher<>(
            new CustomComparator(JSONCompareMode.LENIENT)));

    Customization methods = new Customization("**.classes.classes", new ArrayValueMatcher<>(
            new CustomComparator(JSONCompareMode.LENIENT, new Customization("**.methods", (e1, e2) -> {
                return true;
            }))
    ));

    CustomComparator comparator = new CustomComparator(JSONCompareMode.LENIENT, textContent, muteLOC,classes);

    public String purify (String s) {
        String withoutSpaces = s.replaceAll("\\s+","");
        String withoutEnters = withoutSpaces.replace("\\n", "");
        String withoutTabs = withoutEnters.replace("\\t", "");
        String withoutR = withoutTabs.replace("\\r", "");


        return withoutR;
    }

    @Before
    public void setOracles() {
        oracles = new ArrayList<>();

        for(int i = 1; i <= 11; i++) {
            String path = "./src/test/test_projects/parser/oracles/test";
            if (i <= 9) path += "0" +i+".txt";
            else path += i + ".txt";

            oracles.add(path);
        }
    }

    @Before
    public void setUpRoots () {
        roots = new ArrayList<>();

        roots.add("./src/test/test_projects/parser/projects/test01/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test02/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test03/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test04/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test05/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test06/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test07/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test08/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test09/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test10/src/main/java");
        roots.add("./src/test/test_projects/parser/projects/test11/src/main/java");
    }


    /**
     * TCP_01: caso in cui il progetto è vuoto
     * @input ./test/test_projects/parser/projects/test01
     * @oracle /test/test_projects/parser/oracles/test01.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_01 () throws ParsingException, IOException {
        String oraclePath = oracles.get(0);
        String oracleContent = new String(Files.readAllBytes(Paths.get(oraclePath))).replaceAll("\\s+","");
        boolean flag = false;

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(0)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        flag = oracleContent.equalsIgnoreCase(new GsonBuilder().create().toJson(lst).replaceAll("\\s+",""));

        assertTrue(flag);
    }

    /**
     * TCP_02: caso in cui il progetto è costituito da un package e una classe
     * @input ./test/test_projects/parser/projects/test02
     * @oracle /test/test_projects/parser/oracles/test02.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_02 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(1);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(1)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_03: caso in cui il progetto è costituito da un package e una classe con errori lessicali
     * @input ./test/test_projects/parser/projects/test03
     * @oracle /test/test_projects/parser/oracles/test03.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_03 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(2);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(2)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_04: caso in cui il progetto è costituito da un package e una classe con errori sintattici
     * @input ./test/test_projects/parser/projects/test04
     * @oracle /test/test_projects/parser/oracles/test04.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_04 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(3);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(3)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_05: caso in cui il progetto è costituito da un package e una classe con errori semantici
     * @input ./test/test_projects/parser/projects/test05
     * @oracle /test/test_projects/parser/oracles/test05.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_05 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(4);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(4)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_06: caso in cui il progetto è costituito da un package e più di una classe
     * @input ./test/test_projects/parser/projects/test06
     * @oracle /test/test_projects/parser/oracles/test06.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_06 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(5);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(5)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_07: caso in cui il progetto è costituito da un package contenente una gerarchia (e anche altre classi se si vuole)
     * @input ./test/test_projects/parser/projects/test07
     * @oracle /test/test_projects/parser/oracles/test07.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_07 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(6);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(6)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_08: caso in cui il progetto è costituito da un package contenente piu di una gerarchia (e anche altre classi se si vuole)
     * @input ./test/test_projects/parser/projects/test08
     * @oracle /test/test_projects/parser/oracles/test08.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_08 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(7);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(7)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_09: caso in cui il progetto è costituito da più di un package e classi
     * @input ./test/test_projects/parser/projects/test09
     * @oracle /test/test_projects/parser/oracles/test09.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_09 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(8);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(8)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_10: caso in cui il progetto è costituito da più di un package, classi ed una gerarchia
     * @input ./test/test_projects/parser/projects/test10
     * @oracle /test/test_projects/parser/oracles/test10.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_10 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(9);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(9)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);
    }

    /**
     * TCP_11: caso in cui il progetto è costituito da più di un package, classi e gerarchie
     * @input ./test/test_projects/parser/projects/test11
     * @oracle /test/test_projects/parser/oracles/test11.txt
     * @throws ParsingException
     * @throws IOException
     */
    @Test
    public void TCP_11 () throws ParsingException, IOException, JSONException {
        String oraclePath = oracles.get(10);
        String oracleContent = purify(new String(Files.readAllBytes(Paths.get(oraclePath))));

        when(project.getCompileSourceRoots()).thenReturn(Collections.singletonList(roots.get(10)));
        Parser parser = new ProjectParser(project);
        List<PackageBean> lst = parser.parse();
        String computedJSON = purify(new GsonBuilder().create().toJson(lst));

        JSONAssert.assertEquals(oracleContent, computedJSON, comparator);;
    }
}
