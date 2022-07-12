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

public class AnalysisStartup {

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

    private static String getFeatureEnvyPriority(CodeSmell codeSmell){
        HashMap<String, Double> threshold = codeSmell.getIndex();
        Double value = threshold.get("threshold");

        if(value > 0 && value <= 5){
            return "low";
        }else{
            if(value > 5 && value <= 10 ){
                return "medium";
            }else{
                if(value > 15 && value <= 20){
                    return "high";
                }else{
                    return "urgent";
                }
            }
        }
    }

    private static String getParallelInheritancePriority(CodeSmell codeSmell){
        HashMap<String, Double> threshold = codeSmell.getIndex();
        Double value = threshold.get("threshold");

        if(value > 0 && value <= 20){
            return "low";
        }else{
            if(value > 20 && value <= 30 ){
                return "medium";
            }else{
                if(value > 30 && value <= 40){
                    return "high";
                }else{
                    return "urgent";
                }
            }
        }
    }

    private static String shotgunSurgeryPriority(CodeSmell codeSmell){
        HashMap<String, Double> threshold = codeSmell.getIndex();
        Double value = threshold.get("threshold");

        if(value > 0 && value <= 2){
            return "low";
        }else{
            if(value > 2 && value <= 4 ){
                return "medium";
            }else{
                if(value > 4 && value <= 6){
                    return "high";
                }else{
                    return "urgent";
                }
            }
        }
    }

    private static String divergentChangePriority(CodeSmell codeSmell){
        HashMap<String, Double> threshold = codeSmell.getIndex();
        Double value = threshold.get("threshold");

        if(value > 0 && value <= 2){
            return "low";
        }else{
            if(value > 2 && value <= 4 ){
                return "medium";
            }else{
                if(value > 4 && value <= 6){
                    return "high";
                }else{
                    return "urgent";
                }
            }
        }
    }

    private static String blobPriority(CodeSmell codeSmell){
        HashMap<String, Double> threshold = codeSmell.getIndex();
        Double value = threshold.get("threshold") - 8;

        if(value >= 0 && value <= 2){
            return "low";
        }else{
            if(value > 2 && value <= 10 ){
                return "medium";
            }else{
                if(value > 10 && value <= 15){
                    return "high";
                }else{
                    return "urgent";
                }
            }
        }
    }
}
