package rs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import check.CheckFile;

public class Start {
	
	// java rs/Start -ff ../../genomes/ -fq test/140516_SN163_0555_AC3VYAACXX_L7_ACTTGA_1_internal_id.bam.fastq -o test_out/140516_SN163_0555_AC3VYAACXX_L7ACTTGA_1_internal_id_rs.fastq -x 3 -e check

	
	//-f C:\Users\Chach\Desktop\Stage\A.fasta C:\Users\Chach\Desktop\Stage\C.fasta  -fq C:\Users\Chach\Desktop\Stage\test.fastq -o C:\Users\Chach\Desktop\Stage\check.fastq -x 4 -r 15
	/*---check remove*/		
	//System.out.println();
	/*---check remove*/		
	
	//use java -XX:ParallelGCThreads=1 -jar myJavaTool.jar	
	/*the reads are variable in length!*/
	
	Start(){
		sequences = new ArrayList<Sequence>();
	}
	
	ArrayList<Integer> readReplaceList =new ArrayList<Integer>();	
	
	ArrayList<Sequence> sequences;
	String fastqInput;
	String outpath;
	int mutateOption = 1; 	/* 1 No mutation
							 * 2 Mutate to N by PHRED score
						     * 3 Mutate to <Base> by PHRED score
							 * 4 Mutate to N by Threshold (0-93)
							*/
	
	int scoreOffset = 0;	//offset to lower the PHRED score (0-100)
	int phredScoreThreshold = 60; //0-93
	
	//phred score threshold to set base to N (option 2, 4)
	int amountOfReads = 100; //amount of reads to be inserted
	int amountOfGenomes = 0;
	int[] genomeReadsDone;	
	int totalReadsDone = 0;
	int readsInFile = 0;
	int totalAmountOfMutations = 0;
	
	Scanner fastaReader; 
	Scanner fastqReader;
	
	ArrayList<String> genomePath;
	String fastqPath;
	
	@SuppressWarnings("resource")
	void run(String[] args){
		
		/* argument handling*/
		/* 0 fasta file
		 * 1 fastq file
		 * 2 outpath
		 * 3 option
		 * 4 optional addition to option
		 * 5 amount of reads
		*/
		
		parseArguments(args);	
	
		/*count the amount of reads*/		
		try {
			fastqReader = new Scanner(new File(fastqPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int linesInFile = 0;		
		while(fastqReader.hasNext()){
			fastqReader.nextLine();
			linesInFile += 1;
		}		
		
		linesInFile -= linesInFile % 4; //correction due to header
		readsInFile = linesInFile / 4;		
		/*end*/
		
		parseGenome();		

		/*get the reads to replace*/
		/*this method circumvents the problem of sorting the whole fastq file in memory*/
		/*the reads are variable in length!*/
		for(int i =0; i < amountOfReads * sequences.size(); i++){
			int readToReplace = (int)(Math.random() * ((readsInFile))); 
			int tries = 100;
			int counter = 0;
			while(readReplaceList.contains(readToReplace)){
				readToReplace = (int)(Math.random() * ((readsInFile))); 
				counter += 1;
				if(counter > tries){
					System.err.println("Run error");
					System.err.println("Could not find a read to replace after 100 tries");
					System.err.println("Probably too many reads requested for a too small FASTQ file");
					System.err.println("Reads requested (read req*fasta files):");
					System.err.println(amountOfReads * sequences.size());
					System.err.println("Reads in file: ");
					System.err.println(readsInFile);
					System.exit(0);					
				}
			}			
			readReplaceList.add(readToReplace);			
		}
		Collections.sort(readReplaceList);	
		/*end*/			
		
		replace(new File(fastqPath));
		
		System.out.println("Succesful run");
		System.out.println("Amount of reads replaced: ");
		System.out.println(totalReadsDone);		
		
		System.out.println("Amount of Reads per Genome");
		for(int i = 0; i < genomePath.size(); i++){
			System.out.println(genomePath.get(i));
			System.out.println(genomeReadsDone[i]);
		}
		
		System.out.println("Amount Of Mutations: ");
		System.out.println(totalAmountOfMutations);
		
		/*integrity check on fastq*/
		String[] newArgs = {fastqPath};	
		CheckFile.checkFastq(newArgs);
	}
	
	void replace(File fastq){	
		int amountReplaced = 0;
		int readLength = 0;
		genomeReadsDone = new int[sequences.size()];
				
		Scanner fastqScanner = null;
		try {
			fastqScanner = new Scanner(fastq);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int counter = 0;
		int nextToReplace = readReplaceList.get(0);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outpath, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		Read read = new Read();
		while(fastqScanner.hasNext()){
			//get the new read
			read.setRead(fastqScanner.nextLine(), fastqScanner.nextLine(), fastqScanner.nextLine(), fastqScanner.nextLine());
			
			//write the new read: if --original else -- new read
			if(counter != nextToReplace){
				writer.write(read.getID() + "\n");
				writer.write(read.getSequence() + "\n");
				writer.write(read.getPad() + "\n");
				writer.write(read.getScore() + "\n");
			}else{
				
				/*check*//*
				System.out.println("counter: " + counter);
				System.out.println("nextToReplace: " + nextToReplace);
				*//*check*/
				
				readLength = read.getSequence().length();				
				int whichGenomeTemp = (int)(Math.random() * ((amountOfGenomes)));
				int whichGenome = whichGenomeTemp;
				
				/*pick genome to make read from and check if avaiable*/
				while(genomeReadsDone[whichGenomeTemp] >= amountOfReads){					
					whichGenomeTemp += 1; //if full take the next
					if(whichGenomeTemp >= sequences.size()){
						whichGenomeTemp = 0;						
					}
					if(whichGenomeTemp == whichGenome){
						System.err.println("Error in Start().replace()");
						System.err.println("All genomes exceed their reads");
						System.err.println("Exiting ... ");
						System.exit(1);
					}
					whichGenome = whichGenomeTemp;
				}
				
				read.setSequence(newSequence(readLength, whichGenome), mutateOption, scoreOffset, phredScoreThreshold);
				genomeReadsDone[whichGenome] += 1;
				totalReadsDone += 1;
				
				writer.write(read.getID()  + "\n");
				writer.write(read.getSequence()  + "\n");
				writer.write(read.getPad()  + "\n");
				writer.write(read.getScore()  + "\n");
				
				amountReplaced += 1;		
				
				if(!(amountReplaced >= readReplaceList.size())){
					nextToReplace = readReplaceList.get(amountReplaced);
				}
			}
			counter += 1;
			totalAmountOfMutations += read.getAmountOfMutations();
			read.clean();
		}		
		writer.flush();
		writer.close();
	}
	
	String newSequence(int readLength, int whichGenome){
		/*method get a new read from a genome*/
		
		String readSeq = ""; //storage of the new sequence
		Sequence sequence = sequences.get(whichGenome);
		int startPosRead = (int)(Math.random() * (sequence.getLength())); 
		int next = 1;
		for(int j = 0; j < readLength; j++){
			if(startPosRead + next == sequence.getLength()){ //if end of sequence start from the beginning	
				startPosRead = 0;
				next = 1;
				readSeq += sequence.getSequence().charAt(0);
			}else{ 
				readSeq += sequence.getSequence().charAt(startPosRead + next);				
				next += 1;		
			}				
		}				
		return readSeq;		
	}
	
	void parseArguments(String[] args){
		Hashtable<String, ArrayList<String>> arguments = new Hashtable<String, ArrayList<String>>();
		ArrayList<String> options = new ArrayList<String>(){{add("-f"); add("-fq");	add("-x");add("-o"); add("-r");}};
		
		if(args.length == 0){
			System.out.println ("Usage -- arguments: \n -f fasta file \n -fq fastq file \n -o outpath \n -x option "
					+ " \n -x #<amount of reads> --t #threshold --l #<lower PHRED score>");		
			System.exit(1);
		}else if(args[0].equals("--help")){
			System.out.println ("Usage -- arguments: \n -f fasta file \n -fq fastq file \n -o outpath \n -x option "
					+ " \n -x #<amount of reads> --t #threshold --l #<lower PHRED score>");		
			System.exit(1);
		}
		
		if(args[0].equals("--check")){
			if(args.length < 2){
				System.out.println ("Usage --check <file>");
				System.exit(1);
			}else{
				String[] filePath = {args[1]};
				CheckFile.checkFastq(filePath);
				System.exit(0);
			}
		}else{
			
			int counter = 0; //counts the amount of arguments for each option e.g. -f ... ... ...		
			for(int i = 0; i < args.length; i+=2){
				if(!options.contains(args[i])){		
					//system exit on unrecognized argument
					System.err.println("Unrecognized argument: ");
					System.err.println(args[i]);
					System.err.println("Usable options: ");
					for(int j = 0; j < options.size(); j++){
						System.err.print(options.get(j) + " ");				
					}
					System.err.println();
					System.err.println("try --help");
					System.err.println("Exiting");
					System.exit(1);				
				}else if(options.contains(args[i]) && !options.contains(args[i+1])){
					//if -option + corresponding argument
					ArrayList<String> arg = new ArrayList<String>();
					arg.add(args[i+1]);
					arguments.put(args[i], arg);
					
					counter = 1;
					//next option/argument parsing
					boolean hasNext = true;
					if(i+1+counter >= args.length){
						hasNext = false;
					}
					
					while(hasNext){						
						if(!options.contains(args[i+counter + 1])){						
							arg.add(args[i+1+counter]);
							counter += 1;											
							if(i+1+counter >= args.length){
								hasNext = false;
							}
						}else{
							hasNext = false;
						}
					}
					//reset
					i += counter - 1;
					counter = 0;
				}						
			}		
			
			/*0 = fasta file*/
			if(arguments.containsKey("-f")){			
				System.out.println("Genomes provided: ");
				genomePath = new ArrayList<String>();
				amountOfGenomes = arguments.get("-f").size();
				for(int i = 0; i < arguments.get("-f").size(); i++){
					System.out.println("Using fasta file: " + arguments.get("-f").get(i));			
				
					genomePath.add(arguments.get("-f").get(i)); 	
					Scanner filePathScanner = new Scanner(genomePath.get(i));
					filePathScanner.useDelimiter("\\.");
					String extension = "";
					
					while(filePathScanner.hasNext()){		
						extension = filePathScanner.next();			
					}
					
					if(!(extension.equals("fasta"))){
						System.err.println("Parse arg error");
						System.err.println("Provide a fasta file");
						System.err.println(genomePath.get(i));
						System.err.print("Exiting ...");
						System.exit(0);
					}
				}
			}else{		
				System.err.println("Provide a fasta file: -f <absPath>.fasta");
				System.exit(0);			
			}
			
			/*end*/
			
			/*1 = fastq file */	
			if(arguments.containsKey("-fq")){
				fastqPath = arguments.get("-fq").get(0); 	
				Scanner filePathScanner = new Scanner(fastqPath);
				filePathScanner.useDelimiter("\\.");
				String extension = "";
			
				while(filePathScanner.hasNext()){		
					extension = filePathScanner.next();			
				}
				
				if(!(extension.equals("fastq"))){
					System.err.println("Provide a fastq file");
					System.out.println(extension);
					System.exit(0);
				}
			}else{
				System.err.println("Provide a fastq file: -fq <absPath.fastq");		
				System.exit(0);
			}
			/*end*/
			
			/*2 = outpath*/
			if(arguments.containsKey("-o")){
				outpath = arguments.get("-o").get(0);
			}else{
				System.out.println("No outpath provided: -o <absPath>");
				System.exit(1);
			}
			
			/*3&4 = options*/
			
			/* 1 No mutation
			 * 2 Mutate to N by PHRED score
		     * 3 Mutate to <Base> by PHRED score
			 * 4 Mutate to N by Threshold (0-93)
			*/
			if(arguments.containsKey("-x")){			
			    try {
			    	mutateOption = Integer.parseInt(arguments.get("-x").get(0));
			    	if(mutateOption < 1 || mutateOption > 4){
			    		System.err.println("1 No mutation \n2 Mutate to PHRED score \n3 Mutate to <Base> by PHRED score"
			    				+ "\n4 Mutate to N by Threshold (0-93) ");
			    		System.exit(1);
			    	}
			    		
			    	
			    }catch (NumberFormatException e) {
			        System.err.println("Argument" + arguments.get("-x") + " must be an integer.");
			        System.exit(1);
			    }	
			}else{
				System.out.println("No option provided. Running without mutations");
				mutateOption = 1;			
			}
			
			if(arguments.containsKey("--t")){
				try {
					phredScoreThreshold = Integer.parseInt(arguments.get("-t").get(0));
					if(!(phredScoreThreshold >= 0 &&  phredScoreThreshold <= 93)){	    				
						System.err.println("Set score -t between 0 and 93");
						System.exit(1);
					}
				 }catch (NumberFormatException e) {
					 System.err.println(arguments.get("-t") + " must be an integer.");
				     System.exit(1);
	   		 	}
			}
			
			if(arguments.containsKey("--l")){	
				try {
					scoreOffset = Integer.parseInt(arguments.get("-t").get(0));
					if(!(scoreOffset >= 0 &&  scoreOffset <= 93)){	    
						System.err.println("Set score -l between 0 and 93");
	    				System.exit(1);
	    			}
			    }catch (NumberFormatException e) {
			        System.err.println("Argument for " + arguments.get("-t") + " must be an integer.");
			        System.exit(1);
			    }
			}
			/*5 = amount of reads */
			if(arguments.containsKey("-r")){	
			    try {
			    	amountOfReads = Integer.parseInt(arguments.get("-r").get(0));
			    }catch (NumberFormatException e) {
			        System.err.println("Argument" + arguments.get("-r") + " must be an integer.");
			        System.exit(1);
			    }
			}else{
				System.out.println("Set amount of reads: -r <amount>");
				System.exit(1);
			}
			
			if(arguments.containsKey("-t")){
				try {
			    	amountOfReads = Integer.parseInt(arguments.get("-t").get(0));
			    } catch (NumberFormatException e) {
			        System.err.println("Argument" + arguments.get("-r") + " must be an integer.");
			        System.exit(1);
			    }
			}
			
			/* end of argument handling*/		
			
			/* echo options */
			System.out.println("Running rs with: ");
			switch(mutateOption){
				case 1: System.out.println("Option 0; No Mutation");
					break;
				case 2: System.out.println("Option 2; Mutate N to PHRED score");
						System.out.println("Cut off: " + phredScoreThreshold);
					break;
				case 3: System.out.println("Option 3; Mutate <Base> to PHRED score");					
					break;
				case 4: System.out.println("Option 4; Change score and mutate N ");
						System.out.println("Cut off: " + phredScoreThreshold);
					break;
				case 5: System.out.println("Option 5; Change score and mutate <Base>");
					break;
				default: System.out.println("Undefined option");
					System.out.println("Exiting");
					System.exit(1);
				break;	
			}
		}
				
	}
	
	void parseGenome(){
		/*store the genome sequence*/		
		String tempGenome = null;
		int tempSeqlen = 0;
		
		for(int i = 0; i < genomePath.size(); i++){				
			
			try {
				fastaReader = new Scanner(new File(genomePath.get(i)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fastaReader.nextLine(); //skip header		
			fastaReader.useDelimiter(" ");
			while(fastaReader.hasNext()){
				tempGenome = fastaReader.next();		
			}			
			tempGenome = tempGenome.replace("\n" , "");
			tempSeqlen = tempGenome.length();	
			
			sequences.add(new Sequence(tempGenome, tempSeqlen));
			
			//clear
			tempGenome = null;
			tempSeqlen = 0;
			/*end*/
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Start().run(args);
	}
}

