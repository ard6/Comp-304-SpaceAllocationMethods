

import java.util.Random;

public class Block { // This class is an object of the directory. The t-directory consists of 32768 blocks.
	
	int content;     //content of a block is a number integer which is greater than zero.
	int fileLength;  // file length is the byte amount of the file. The correct file length always kept in the first block of the file.
	public int getContent() {  // getter of the content.
		return content; 
	}
	public Block(int fileLength){      // constructor for the Block
		Random random = new Random();     // random number
		int rnd;
		rnd=random.nextInt(Integer.SIZE - 1);
		this.content=(rnd+1);
		this.fileLength=fileLength;
	}
	public int getFileLength() {   // getter of the file length.
		return fileLength; 
	}
	public void setFileLength(int fileLength) {     // setter of the file length.
		this.fileLength = fileLength;
	}
}
