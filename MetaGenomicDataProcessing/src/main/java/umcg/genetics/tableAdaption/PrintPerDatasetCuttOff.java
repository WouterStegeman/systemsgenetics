/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package umcg.genetics.tableAdaption;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import umcg.genetica.io.text.TextFile;
import umcg.genetica.math.matrix2.DoubleMatrixDataset;
import umcg.genetica.math.matrix2.MatrixHandling;

/**
 *
 * @author MarcJan
 */
public class PrintPerDatasetCuttOff {

    public static void main(String[] args) {
        
        String inputTable = "D:\\UMCG\\Projects\\MGS_MicrobiomeQTLs\\GFD_IBS_IBD_LLD_GoNL_500Fg_metaphlan_2.2_results_AsinNorm2.txt";
        String gtmFolder = "D:\\UMCG\\Projects\\MGS_MicrobiomeQTLs\\GTMs\\";
        String outputFolder = "D:\\UMCG\\Projects\\MGS_MicrobiomeQTLs\\DataPerCohort\\";
        int cutOffNumber = 50;
        int minPercentage = 20;
        boolean percentage = true;
        boolean takeCuttOffAndPercentage = true;
        
        //Read information from gtm
        HashMap<String, HashSet<String>> InformationPerCohort = readGTMInformation(gtmFolder);
        
        DoubleMatrixDataset<String, String> bugMatrix = null;
        try {
            bugMatrix = DoubleMatrixDataset.loadDoubleData(inputTable);
        } catch (IOException ex) {
            Logger.getLogger(PrintPerDatasetCuttOff.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Write matrix with direct filtering on X number of bugs in the individual cohort.
        writeTablesToFile(InformationPerCohort, bugMatrix, cutOffNumber, minPercentage, percentage, takeCuttOffAndPercentage, outputFolder);
        
    }

    private static HashMap<String, HashSet<String>> readGTMInformation(String gtmFolder) {
        HashMap<String, HashSet<String>> informationPerDataset = new HashMap<String, HashSet<String>>();
        
        File folder = new File(gtmFolder);
        HashSet<File> fileList = new HashSet<File>();
        listFilesForFolder(folder, fileList);
        for (File F : fileList) {
            System.out.println(F.getName().split("_")[0]);
            
            try {
                informationPerDataset.put(F.getName().split("_")[0], readFileSample(F));
            } catch (IOException ex) {
                Logger.getLogger(PrintPerDatasetCuttOff.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
        
        return informationPerDataset;
    }

    private static HashSet<String> readFileSample(File F) throws IOException {
        HashSet<String> relevantSamples = new HashSet<String>();
        TextFile in = new TextFile(F.getAbsolutePath(), TextFile.R);
        
        String row;
        while( (row=in.readLine())!=null){
            relevantSamples.add(row.split("\t")[1]);
//            System.out.println(row.split("\t")[1]);
        }
        in.close();
        
        return relevantSamples;
    }
    
    private static void writeTablesToFile(HashMap<String, HashSet<String>> InformationPerCohort, DoubleMatrixDataset<String, String> bugMatrix, int cutOffNumber, int minPercentage, boolean percentage, boolean takeCuttOffAndPercentage, String outputFolder) {
        
        for(Entry<String, HashSet<String>> dataset : InformationPerCohort.entrySet()){
            // select samples of interest
            DoubleMatrixDataset<String, String> relevantBugMatrix = MatrixHandling.CreatSubsetBasedOnColumns(bugMatrix, dataset.getValue(), false);

            //Select bugs present in at least cutOffNumer
            relevantBugMatrix =  selectToppresentBugs(relevantBugMatrix,cutOffNumber, minPercentage, percentage, takeCuttOffAndPercentage);

            try {
                relevantBugMatrix.save(outputFolder+"matrix_"+dataset.getKey()+"_CuttOfNumber_"+cutOffNumber+"_incPercentage20.tsv");
            } catch (IOException ex) {
                Logger.getLogger(PrintPerDatasetCuttOff.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static DoubleMatrixDataset<String, String> selectToppresentBugs(DoubleMatrixDataset<String, String> relevantBugMatrix, int cutOffNumber, int minPercentage, boolean percentage, boolean takeCuttOffAndPercentage) {
        HashSet<String> interestingColumns = new HashSet<String>();
        double nrSamples = relevantBugMatrix.columns();
        for(int r = 0; r < relevantBugMatrix.rows(); r++){
            int nonZeroCols = 0;
            for(int c = 0; c < relevantBugMatrix.columns(); c++){
                if(relevantBugMatrix.getMatrix().getQuick(r, c)>0){
                    nonZeroCols++;
                }
            }
            if(!percentage && nonZeroCols>=cutOffNumber){
                interestingColumns.add(relevantBugMatrix.getRowObjects().get(r));
            } else if(takeCuttOffAndPercentage && ((nonZeroCols/nrSamples)*100)>=minPercentage && nonZeroCols>=cutOffNumber){
                interestingColumns.add(relevantBugMatrix.getRowObjects().get(r));
            } else if(percentage && ((nonZeroCols/nrSamples)*100)>=minPercentage && nonZeroCols>=cutOffNumber){
                interestingColumns.add(relevantBugMatrix.getRowObjects().get(r));
            }
        }
        return MatrixHandling.CreatSubsetBasedOnRows(relevantBugMatrix, interestingColumns, false);
        
    }
    
    public static void listFilesForFolder(File folder, HashSet<File> fileList) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, fileList);
            } else {
                fileList.add(fileEntry);
            }
        }
    }
}
