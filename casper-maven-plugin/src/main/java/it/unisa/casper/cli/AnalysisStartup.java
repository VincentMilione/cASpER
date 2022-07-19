package it.unisa.casper.cli;

import it.unisa.casper.analysis.code_smell.*;
import it.unisa.casper.analysis.code_smell_detection.blob.StructuralBlobStrategy;
import it.unisa.casper.analysis.code_smell_detection.blob.TextualBlobStrategy;
import it.unisa.casper.analysis.code_smell_detection.feature_envy.StructuralFeatureEnvyStrategy;
import it.unisa.casper.analysis.code_smell_detection.feature_envy.TextualFeatureEnvyStrategy;
import it.unisa.casper.analysis.code_smell_detection.misplaced_class.StructuralMisplacedClassStrategy;
import it.unisa.casper.analysis.code_smell_detection.misplaced_class.TextualMisplacedClassStrategy;
import it.unisa.casper.analysis.code_smell_detection.promiscuous_package.StructuralPromiscuousPackageStrategy;
import it.unisa.casper.analysis.code_smell_detection.promiscuous_package.TextualPromiscuousPackageStrategy;
import it.unisa.casper.storage.beans.ClassBean;
import it.unisa.casper.storage.beans.MethodBean;
import it.unisa.casper.storage.beans.PackageBean;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AnalysisStartup {
    private static int maxS = 0;

    public static void methodAnalysis(List<PackageBean> projectPackages, HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, MethodBean methodBean) {
        TextualFeatureEnvyStrategy textualFeatureEnvyStrategy = new TextualFeatureEnvyStrategy(projectPackages, coseno.get("cosenoFeature"));
        FeatureEnvyCodeSmell tFeatureEnvyCodeSmell = new FeatureEnvyCodeSmell(textualFeatureEnvyStrategy, "Textual");
        methodBean.isAffected(tFeatureEnvyCodeSmell);

        StructuralFeatureEnvyStrategy structuralFeatureEnvyStrategy = new StructuralFeatureEnvyStrategy(projectPackages, dipendence.get("dipFeature"));
        FeatureEnvyCodeSmell sFeatureEnvyCodeSmell = new FeatureEnvyCodeSmell(structuralFeatureEnvyStrategy, "Structural");
        methodBean.isAffected(sFeatureEnvyCodeSmell);
    }

    public static void classAnalysis(List<PackageBean> projectPackages, HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, ClassBean classBean) {
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

    public static void packageAnalysis(List<PackageBean> projectPackages, HashMap<String, Double> coseno, HashMap<String, Integer> dipendence, PackageBean packageBean) {
        TextualPromiscuousPackageStrategy textualPromiscuousPackageStrategy = new TextualPromiscuousPackageStrategy(coseno.get("cosenoPromiscuous"));
        PromiscuousPackageCodeSmell tPromiscuousPackagecodeSmell = new PromiscuousPackageCodeSmell(textualPromiscuousPackageStrategy, "Textual");
        packageBean.isAffected(tPromiscuousPackagecodeSmell);
        packageBean.setSimilarity(0);

        StructuralPromiscuousPackageStrategy structuralPromiscuousPackageStrategy = new StructuralPromiscuousPackageStrategy(projectPackages, dipendence.get("dipPromiscuous") / 100, dipendence.get("dipPromiscuous2") / 100);
        PromiscuousPackageCodeSmell sPromiscuousPackagecodeSmell = new PromiscuousPackageCodeSmell(structuralPromiscuousPackageStrategy, "Structural");
        packageBean.isAffected(sPromiscuousPackagecodeSmell);
        packageBean.setSimilarity(0);
    }

    public static String prioritySmell(CodeSmell smell, HashMap<String, Integer> dependency, HashMap<String, Double> cosine) {
        if (Objects.isNull(smell) || Objects.isNull(cosine) || Objects.isNull(dependency)) return null; //null check

        String algoritmsUsed = smell.getAlgoritmsUsed();
        String smellName = smell.getSmellName();

        if(Objects.isNull(algoritmsUsed) || Objects.isNull(smellName)) return null;
        if (algoritmsUsed.startsWith("Textual")) {
            int index = smellName.indexOf(" ");
            String coseno = "coseno"+smellName.substring(0, index >= 0 ? index : smellName.length());
            double cosSoglia = cosine.get(coseno);
            HashMap<String, Double> cosEff = smell.getIndex();

            return priorityTextual(smellName, cosSoglia, cosEff.get("coseno"));
        } else
            return priorityStructural(smell, dependency);
    }

    private static String priorityTextual(String smellName, double soglia, double cosEff) {
        int complessita = 1, alto = 0;
        boolean basso = false;

        if (cosEff >= 0.75) alto++;
        if (cosEff >= soglia + (0.1 * soglia))
            complessita += 2;
        else basso = true;

        return prioritySmell(complessita, basso, alto);
    }

    private static String priorityStructural (CodeSmell smell, HashMap<String, Integer> soglie) {
        String smellName = smell.getSmellName();
        HashMap<String, Double> map = smell.getIndex();
        if (Objects.isNull(smellName) || Objects.isNull(map)) return null;

        int index = smellName.indexOf(" ");
        String dip = "dip"+smellName.substring(0, index >= 0 ? index : smellName.length());
        String priority = null;

        switch (smellName) {
            case "Blob":
                String dip2 = dip+"2";
                String dip3 = dip+"3";

                priority = blobPriority(soglie.get(dip), soglie.get(dip2), soglie.get(dip3),
                        map.get("LCOM"), map.get("featureSum"), map.get("ELOC"));
                break;
            case "Feature Envy":
            case "Misplaced Class":
                priority = generalPriority(soglie.get(dip), map.get("dipendenza"));
                break;
            case "Promiscuous Package":
                dip2 = dip +"2";

                priority = promiscuosPriority(soglie.get(dip), soglie.get(dip2),
                        map.get("InverseMIntraC"), map.get("MInterC"));
                break;
        }
        return priority;
    }

    private static String promiscuosPriority(int sogliaIntraC, int sogliaInter, double intraC, double inter) {
        int complessita = 0, alto = 0;
        boolean basso = false;
        double sogliaIntraCreale = sogliaIntraC / 100.0, sogliaInterreale = sogliaInter / 100.0;

        if (inter >= 0.75 || intraC >= 0.75) alto++;
        if (sogliaInterreale <= inter || sogliaIntraCreale <= intraC) {
            complessita += 2;
            alto++;
        }
        if (sogliaInterreale == inter || sogliaIntraCreale == intraC) basso = true;

        return prioritySmell(complessita, basso, alto);
    }

    public static String blobPriority(int sogliaLCOM, int sogliaFSUM, int sogliaELOC, double lcom, double fsum, double eloc) {
        int complessita = 0, alto = 0;
        boolean basso = false;

        if (sogliaLCOM <= lcom || sogliaFSUM <= fsum || sogliaELOC <= eloc) {
            complessita += 2;
            alto++;
            if (sogliaLCOM == lcom || sogliaFSUM == fsum || sogliaELOC == eloc) {
                basso = true;
            }
        }
        return prioritySmell(complessita, basso, alto);
    }

    public static String generalPriority(int sogliaDip, double dip) {
        int complessita = 2, alto = 0;
        boolean basso = false;

        if (dip <= sogliaDip)  basso = true;
        if (dip >= (maxS - (maxS * 0.25))) alto++;
        if (dip >= maxS) maxS = new Double(dip).intValue();

        return prioritySmell(complessita, basso, alto);
    }

    private static String prioritySmell(int complessita, boolean basso, int alto) {

        if (complessita <= 2 && alto < 1) {
            return "low";
        } else {
            if (!basso) {
                switch (alto) {
                    case 1:
                        return "high";
                    case 2:
                        return "urgent";
                    default:
                        return "medium";
                }
            }
            return "medium";
        }
    }

    public static String soglie(CodeSmell smell) {
        if (Objects.isNull(smell)) return null;
        HashMap<String, Double> map = smell.getIndex();
        String smellName = smell.getSmellName();
        String algorithm = smell.getAlgoritmsUsed();
        String soglia = null;

        if (algorithm.equals("Textual")) return map.get("coseno") + "";
        switch (smellName) {
            case "Blob":
                soglia = map.get("LCOM") +"-" +map.get("featureSum") +"-"+ map.get("ELOC");
                break;
            case "Feature Envy":
            case "Misplaced Class":
                soglia = map.get("dipendenza") +"";
                break;
            case "Promiscuous Package":
                soglia = map.get("InverseMIntraC")+"-" +map.get("MInterC");
                break;
        }
        return soglia;
    }
}
