package rs;

import java.util.Random;

public class Read {
	private String ID = "";
	private String seq = "";
	private String pad = "";
	private String score = "";
	
	private int length = -1;
	
	private int amountOfMutations = 0;
	
	public void clean(){
		ID = "";
		seq = "";
		pad = "";
		score = "";		
		amountOfMutations = 0;
		length = -1;
	}
	
	public void setRead(String ID, String seq, String pad, String score){
		this.ID = ID;
		this.seq = seq;
		this.pad = pad;
		this.score = score;
		checkRead(seq, score);
	}	
	
	public void setSequence(String seq, int mutateOption, int scoreOffset, int phredScoreThreshold){
		String originalSeq = this.seq;
		this.seq = seq;		
		checkSeq(seq);	
		
		if(length != -1){
			length = seq.length();
		}
		
		mutate(mutateOption, scoreOffset, phredScoreThreshold);			
		
		/*check*//*
		System.out.println("Replaced: ");
		System.out.println(originalSeq);
		System.out.println("With: ");
		System.out.println(seq);
		*//*check*/		
	}
	
	public void setScore(String score){
		this.score = score;
		checkScore(score);
		if(length != -1){
			length = seq.length();
		}
	}
	
	public String getID(){
		return ID;
	}
	
	public String getPad(){
		return pad;		
	}
	
	public String getSequence(){
		return seq;
	}
	
	public String getScore(){
		return score;
	}
	
	public int getAmountOfMutations(){
		return amountOfMutations;
	}
	
	private void checkRead(String seq, String score){
		checkID();
		checkSeq(seq);
		checkPad();
		checkScore(score);
		compSeqScore(seq, score, -1);
	}
	
	private void checkID(){
		if(ID.equals("")|| ID == null){
			System.err.println("ID not set");
			printRead();
			System.exit(0);
		}else if(ID.charAt(0) != '@'){
			System.err.println("ID not starting with @");
			printRead();
			System.exit(0);			
		}
		//do nothing
		//ID is file specific
		//define if otherwise
	}
	
	public void checkID(String ID){
		if(ID.equals("")|| ID == null){
			System.err.println("ID not set");
			printRead();
			System.exit(0);
		}else if(ID.charAt(0) != '@'){
			System.err.println("ID not starting with @");
			printRead();
			System.exit(0);			
		}
		//do nothing
		//ID is file specific
		//define if otherwise
	}
	
	/*
	private void checkSeq(){
		for(int i = 0; i < this.seq.length(); i++){
			char base = this.seq.charAt(i);
			if(base != 'A' && base != 'C' && base != 'G' && base != 'T' && base != 'N'){
				System.err.println("Unrecognized base: ");
				System.err.println(base);			
				printRead();
				System.exit(0);
			}	
		}		
	}
	*/
	
	public void checkSeq(String seq){
		for(int i = 0; i < this.seq.length(); i++){
			char base = this.seq.charAt(i);
			if(base != 'A' && base != 'C' && base != 'G' && base != 'T' && base != 'N'){
				System.err.println("Unrecognized base: ");
				System.err.println(base);			
				printRead();
				System.exit(0);
			}	
		}	
		
		if(length != -1){
			if(!(length == seq.length())){
				System.err.println("Err: New Seq length does not correspond to the set length: " + seq.length());
				System.err.println("Current Read Stats:");
				System.err.println(ID);
				System.err.println(seq);
				System.err.println(pad);
				System.err.println(score);
			}
		}
	}
	
	private void checkNewSeq(String seq){
		if(!seq.equals("") ||  seq != null){
			if(seq.length() != this.seq.length()){
				System.err.println("New seq length is not original seq length");
				System.err.println("New: "  + seq.length());
				System.err.println("Original: " + this.seq.length());	
				printRead();
				System.exit(0);
			}
		}
		
		if(this.seq != "" && this.seq != null){
			for(int i = 0; i < this.seq.length(); i++){
				char base = this.seq.charAt(i);
				if(base != 'A' && base != 'C' && base != 'G' && base != 'T' && base != 'N'){
					System.err.println("Unrecognized base: ");
					System.err.println(base);			
					printRead();
					System.exit(0);
				}	
			}
		}else{
			/*check*//*
			System.err.println("Empty Sequence");				
			printRead();	
			*//*check*/	
		}	
		
		if(length != -1){
			if(!(length == seq.length())){
				System.err.println("Err: New Seq length does not correspond to the set length: " + seq.length());
				System.err.println("Current Read Stats:");
				System.err.println(ID);
				System.err.println(seq);
				System.err.println(pad);
				System.err.println(score);
			}
		}
	}
		
	private void checkPad(){	
		if(pad.equals("")|| pad == null){
			System.err.println("Padding not set");
			printRead();
			System.exit(0);
		}else if(pad.charAt(0) != '+'){
			System.err.println("Padding not starting with +");
			printRead();
			System.exit(0);			
		}	
	}
	
	public void checkPad(String pad){
		if(pad.equals("")|| pad == null){
			System.err.println("Padding not set");
			printRead();
			System.exit(0);
		}else if(pad.charAt(0) != '+'){
			System.err.println("Padding not starting with +");
			printRead();
			System.exit(0);			
		}	
	}
	
	/*
	private void checkScore(){
		for(int i = 0; i < score.length(); i++){
			int score = (int) this.score.charAt(i);
			if(score < 0 || score > 147){
				System.err.println("Unrecognized Score: ");
				System.err.println(score);
				printRead();
				System.exit(0);
			}
		}
	}
	*/
	
	public void checkScore(String scoreString){
		for(int i = 0; i < score.length(); i++){
			int score = (int) scoreString.charAt(i);
			if(score < 0 || score > 147){
				System.err.println("Unrecognized Score: ");
				System.err.println(score);
				printRead();
				System.exit(0);
			}
		}
		
		if(length != -1){
			if(!(length == score.length())){
				System.err.println("Err: New Score length does not correspond to the set length: " + score.length());
				System.err.println("Current Read Stats:");
				System.err.println(ID);
				System.err.println(seq);
				System.err.println(pad);
				System.err.println(score);
			}
		}
	}
	
	public void compSeqScore(String seq, String score, int readNumber){
		if(seq.length() != score.length()){
			System.err.println("Sequence and Score are not of equal length");
			System.err.println("Current Read Stats:");
			System.err.println("Read number: " + readNumber);
			System.err.println("ID: " + ID);
			System.err.println("seq: " + seq);
			System.err.println("pad: " + pad);
			System.err.println("score: " + score);			
		}
	}
	
	public void printRead(){
		System.out.println("----------------------");
		System.out.println(ID);
		System.out.println(seq);
		System.out.println(pad);
		System.out.println(score);
		System.out.println("----------------------");
	}
	
	private void mutate(int mutateOption, int scoreOffset, int phredScoreThreshold){				
		int score = 0;
		char base = '\0';
		Random random = new Random();
		String newSeq = "";
		for(int i = 0; i < seq.length(); i++){
			score = (int) this.score.charAt(i) - 33;			
			
			double doubleScore = Math.pow(10, (-score/ 10)) - scoreOffset;
			if(doubleScore < 0){
				doubleScore = 0;
			}
			base = seq.charAt(i);
			
			/*mutate the sequence accordingly to the PHRED score*/				
			if(mutateOption == 2 || mutateOption == 3){
				if(Math.random() < doubleScore){
					amountOfMutations += 1;
					//System.out.print("Log: changing base: " + base);
					//System.out.print(" at: " + i);
					int newRandom = random.nextInt(3);
					if(mutateOption == 2){
						base = 'N';
					}else if(mutateOption == 3){
						switch (base){
							case 'A':
							if(newRandom == 0){
									base = 'T';
							}else if(newRandom == 1){
									base = 'G';
							}else if(newRandom == 2){
									base = 'C';
							}else{
								System.err.println("Random Number Error");
								System.exit(1);
							}
							break;
							case 'T':
							if(newRandom == 0){
								base = 'A';
							}else if(newRandom == 1){
									base = 'G';
							}else if(newRandom == 2){
									base = 'C';
							}else{
								System.err.println("Random Number Error");
								System.exit(1);
							}
							break;						
							case 'C':
							if(newRandom == 0){
								base = 'T';
							}else if(newRandom == 1){
									base = 'G';
							}else if(newRandom == 2){
									base = 'A';
							}else{
								System.err.println("Random Number Error");
								System.exit(1);
							}
							break;
							case 'G':
							if(newRandom == 0){
									base = 'T';
							}else if(newRandom == 1){
									base = 'A';
							}else if(newRandom == 2){
									base = 'C';
							}else{
								System.err.println("Random Number Error");
								System.exit(1);
							}
							break;
							default:
							System.err.println("Unrecognized Base: ");
							System.err.println(base);
							System.exit(1);
							break;			
						}									
					}
					//System.out.print(" for: " + base);
					//System.out.println();
				}			
			}else if(mutateOption == 4){
				/*set N accordingly to the PRHED score*/
				if(doubleScore < phredScoreThreshold){					
					//System.out.print("Log: changing base " + base);
					//System.out.print(" at: " + i);
					base = 'N';
					//System.out.print(" -- to: " + 'N');
					//System.out.println();					
				}
			}					
		newSeq += base;
		}	
		
	checkNewSeq(newSeq);
	this.seq = newSeq;			
	} // end of function mutate
	
}


