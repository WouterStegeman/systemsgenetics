package check;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import rs.Read;

public class CheckFile {
	
	public static void checkFastq(String args[]){
		System.out.println("Starting Check on newly created file ...");
		Scanner fastqReader = null;
		int amountOfReads = 0;
		try {
			fastqReader = new Scanner(new File(args[0]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Read newRead = new Read();
		
		while(fastqReader.hasNext()){
			String ID = ""; 
			String seq = "";
			String pad = "";
			String score = "";
			
			if(fastqReader.hasNext()){
				ID = fastqReader.nextLine();
				newRead.checkID(ID);
			}
			
			if(fastqReader.hasNext()){
				seq  = fastqReader.nextLine();
				newRead.checkSeq(seq);
			}
			
			if(fastqReader.hasNext()){
				pad  = fastqReader.nextLine();
				newRead.checkPad(pad);
			} 
			
			if(fastqReader.hasNext()){
				score = fastqReader.nextLine();
				newRead.checkScore(score);
			}
			
			newRead.compSeqScore(seq, score, amountOfReads);
			amountOfReads += 1;
		}
		
		System.out.println("Check completed.");
		System.out.println("Acount Of Reads: " + amountOfReads);
	}
}
