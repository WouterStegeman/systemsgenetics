package rs;

public class Sequence {

	private String sequence;
	private int length;
	
	Sequence(String seq, int len){
		sequence = seq;
		length = len;
	}
	
	public String getSequence(){
		return sequence;
	}
	
	public int getLength(){
		return length;
	}
	
}

