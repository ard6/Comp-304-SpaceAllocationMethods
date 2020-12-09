

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ContiguousAllocation {  // contiguous allocation class which uses a Directory Table.
	/*
	// input files: you can copy the name of the file here. 
	
	//("input_8_600_5_5_0.txt")
	//("input_1024_200_5_9_9.txt")
	//("input_1024_200_9_0_0.txt")
	//("input_1024_200_9_0_9.txt")
	//("input_2048_600_5_5_0.txt")
	
	*/
	
	public static String FILE_NAME="input_2048_600_5_5_0.txt";  // file name
	public static final int BLOCK_SIZE = 2048;    //  the block size of the directory. You should change the BLOCK_SIZE with the block size amount in the file name.
	public static int Total=0;      // this variable is for keeping the current size of the directory
	static Block[] directory = new Block[32768];     // The directory, which consists of 32768 blocks. It is an array for the fixed number of space.
	static int FileIDGenerator=-1;  // it is a global variable to keep the id of the next generated file.
	static public String line;     // this string is for reader.
	
	 static int DTsize=0;       // this is for keeping the size of the DT
	 static DTContent first=null;		// this is a pointer to the first element of the DT.
	 static DTContent last= null;		// this is a pointer to the last element of the DT.
	 
	 static int createRejects=0;     // a count for create rejects.
	 static int extendRejects=0; 	// a count for extend rejects.
	 static int shrinkRejects=0;    // a count for shrink rejects. 
	 static int accessRejects=0;    // a count for access rejects
	

	public static void main(String[] args) {
	
		double timeStart = System.currentTimeMillis();    // to keep the run time.
		
		DTContent space = new DTContent(-1,0,32768);     //initially there is only a space with size 32768 blocks and DT keeps also the spaces as DTContents. 
														// the IDs of the spaces are -1
		first=space;
		last=space;
		DTsize++;
		try {                // reader for the file.
		 File file = new File(FILE_NAME);  
	     Scanner Reader = new Scanner(file);
	     line = Reader.nextLine();
	    
	     while (line != null) {
	    	 parse(line);     // the methods for the create, access, extend and shrink are called in the parse method.
	    	if( Reader.hasNextLine()){
	    		line = Reader.nextLine();
	    	}else{
	    		break;
	    	}
		}
	     Reader.close();
		}catch(FileNotFoundException e) {
		      System.out.println("Cannot read file");
		      e.printStackTrace();
		}
		
		System.out.println("Contiguous Allocation");      // outputs of the run.
		System.out.println(FILE_NAME);
		System.out.println("run time: "+(System.currentTimeMillis() - timeStart));
		System.out.println();
		System.out.println("create Rejects: "+createRejects);
		System.out.println("extend Rejects: "+extendRejects);
		System.out.println("shrink Rejects: "+shrinkRejects);
		System.out.println("access Rejects: "+accessRejects);
	}
	
	private static void parse(String line) {    // parse method calls the corresponding method for the operations.
		if(line.charAt(0)=='c'){                 
			int fileLength=Integer.parseInt(line.substring(2));
			create_fileDT(IDGenerator(),fileLength);         // if the line starts with 'c' it calls createFAT.
		}else if(line.charAt(0)=='a'){
			String delims = ":";
			String[] tokens = line.split(delims);
			int fileID=Integer.parseInt(tokens[1]);
			int byteOffset=Integer.parseInt(tokens[2]);
			accessDT(fileID,byteOffset);                   // if the line starts with 'a' it calls accessFAT
		}else if(line.charAt(0)=='e'){
			String delims = ":";
			String[] tokens = line.split(delims);
			int fileID=Integer.parseInt(tokens[1]);
			int extension=Integer.parseInt(tokens[2]);
			extendDT(fileID,extension);                        // if the line starts with 'e' it calls extendFAT
		}else if(line.charAt(0)=='s'){
			String delims = ":";
			String[] tokens = line.split(delims);
			int fileID=Integer.parseInt(tokens[1]);
			int shrinking=Integer.parseInt(tokens[2]);
			shrinkDT(fileID,shrinking);                      // if the line starts with 'sh' it calls shrinkFAT
		}else{
			return;
		}	
	}


	private static void shrinkDT(int fileID, int shrinking) {        //shrinking method for DT
		DTContent currentFile = first;                             
		for(int i =0;i<DTsize;i++){                         // this for iterates over DT
			if(currentFile.fileIdentifier==fileID){            // if there is such a file
				if(currentFile.size<=shrinking){             // if there is no enough space
					//System.err.println("Shrinking amount cannot be equal to or greater than file size "+ fileID+":"+shrinking+" filesize: "+ currentFile.size);   // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
					shrinkRejects++;
					break;
				}
				if(currentFile.next.fileIdentifier!=-1){      // if the current.next file is not a space then create a new node for the dt indicating that there is a space. 
					DTContent space= new DTContent(-1,currentFile.start+currentFile.size-1,shrinking);  
					currentFile.getNext().setPrev(space);		
					space.setNext(currentFile.getNext());
					currentFile.setNext(space);
					space.setPrev(currentFile);
					currentFile.setSize(currentFile.getSize()-shrinking); 
					currentFile.next.setStart(currentFile.next.getStart()-shrinking+1);
					DTsize++;		
				}else{    // if the next node is a space than update the size of this space
					currentFile.setSize(currentFile.getSize()-shrinking); 
					currentFile.getNext().setSize(currentFile.getNext().getSize()+shrinking);
					currentFile.getNext().setStart(currentFile.getNext().getStart()-shrinking);	
				}
				if(directory[currentFile.start].fileLength%BLOCK_SIZE==0){          //if file length is a multiple of the BLOCK_SIZE then we can safely decrease the size of the file in the directory. 
					for(int j=0;j<shrinking;j++){	       // this for iterates for the shrinking amount and sets the last "shrinking" blocks to null 
						directory[currentFile.start].setFileLength(directory[currentFile.start].fileLength-BLOCK_SIZE); 
						directory[currentFile.start+currentFile.size+j]=null;
						Total--;     // decrease the total size
					}	
				}else{      // if file size is not a multiple of the block size then first we should decrease the size by the remainder part.
					directory[currentFile.start].setFileLength(directory[currentFile.start].fileLength-(directory[currentFile.start].fileLength%BLOCK_SIZE)); 
					directory[currentFile.start+currentFile.size+shrinking-1]=null;
					Total--;
					for(int j=0;j<shrinking-1;j++){	   // this for iterates for shrinking-1 times because before the loop we shrink the remainder part.
						directory[currentFile.start].setFileLength(directory[currentFile.start].fileLength-BLOCK_SIZE); 
						directory[currentFile.start+currentFile.size+j]=null;
						Total--;
					}
					
				}
				
				break;
			}else{       // if the current is not the file that we want to shrink
				if(currentFile.getNext()!=null){		  // if there is a next node set the current to the next
					currentFile=currentFile.getNext();		
				}else{      // if there is not a next node and we cannot find the file then reject.
					last=currentFile;
					//System.err.println("File is not found to shrink."+ fileID);       // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
					shrinkRejects++;
					break;
				}
			}
		}
	}

	private static void extendDT(int fileID, int extension) {   // extend method for DT
	
		DTContent currentFile = first;
		if (32768 - Total >= extension) {      // if there is enough space
			for (int i = 0; i < DTsize; i++) {     // this for iterates over the DT to find the file to be extended
				if (currentFile.fileIdentifier == fileID) {     // if the current is the file to be extended.
					if (currentFile.next.fileIdentifier == -1 && currentFile.next.size >= extension) {    //if next node is a space and this space is enough for the extension
						for (int j = 0; j < extension; j++) {     // this for iterates for extension times to extend the file in the directory
							directory[currentFile.start + currentFile.getSize() + j] = new Block(directory[currentFile.getStart()].fileLength);   // create a new block
							Total++;    // increase the total size
						}
						directory[currentFile.start]
								.setFileLength(directory[currentFile.start].getFileLength() + (extension * BLOCK_SIZE)); //  update the file length of the first block of the file
						currentFile.setSize(currentFile.getSize() + extension);    // in dt, update the size of the current file 

						currentFile.getNext().setSize(currentFile.getNext().getSize() - extension);     // in dt, update the size of the next space 
						
						if (currentFile.getNext().getSize() == 0) {       // after extension if the size of the next space becomes 0, then we should delete this node. 
							if (currentFile.getNext().getNext() == null) {   // if it is the last node
								currentFile.getNext().setPrev(null);
								currentFile.setNext(null);
								DTsize--;
								last=currentFile;
							} else {
								currentFile.setNext(currentFile.getNext().getNext());
								currentFile.getNext().getPrev().setNext(null);
								currentFile.getNext().getPrev().setPrev(null);
								currentFile.getNext().setPrev(currentFile);
								DTsize--;
							}
						}else{      // if the size of the space is not 0 after the extension, then set the start of the space
							currentFile.getNext().setStart(currentFile.getNext().getStart() + extension);
						}
						break;
					} else {     // if there is no space after the file to be extended, then defragment
						defragmentExtend(fileID,extension);
						break;
					}
				} else {   // if the current file is not the file to be extended
					if (currentFile.getNext() != null) {    
						currentFile = currentFile.getNext();
					} else {
						last=currentFile;
						//System.err.println("File is not found to extend."+ fileID);
						extendRejects++;
						break;
					}
				}
			}
		}else{      // if there is no enough space then reject. 
		//	System.err.println("There is no enough space for extend operation. "+ fileID+":"+extension);  // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
			extendRejects++;
		}
	}

	private static void accessDT(int fileID, int byteOffset) {     // access method for DT
		
		int location=-1;
		DTContent current=first;
		for(int i=0;i<DTsize;i++){     // this loop iterates to find the file to be accessed
			
			if(current.fileIdentifier==fileID){    // if current is the file to be accessed
				
				if(byteOffset<0){     // if byte give byte offset is less than zero then reject
					//System.err.println("byte offset cannot be less than 0. "+fileID+":"+byteOffset);  
					accessRejects++;
					break;
				}
				if(byteOffset>directory[current.getStart()].fileLength){
					//System.err.println("byte offset cannot be greater than the file size."+fileID+":"+byteOffset);  // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
					accessRejects++;
					break;
				}
				location=current.getStart()+toBlocks(byteOffset+1)-1;    // it calculates the location of the block

			//	System.out.println("location of the file "+fileID+": "+location);   // you can uncomment this if you want to see the location of the block
				
				return;
			}else{       // if current is not the file to be accessed
				if(current.getNext()!=null){
					current=current.getNext();
				}else{    // if there is no such a file then reject
				//	System.err.println("File not found cannot access. "+ fileID);  // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
					accessRejects++;
					break;
				}
			}
		}
		return;
	}

	public static void create_fileDT(int fileID, int fileLength) {     // create method for DT
		int lengthBlocks = toBlocks(fileLength);     

		DTContent current = first;
		if (32768 - Total >= lengthBlocks) {   // if there is enough space
			DTContent file = new DTContent(fileID, current.start, lengthBlocks);

			boolean found = false;
			for (int i = 0; i < DTsize; i++) {      // this loop iterates to find the file
				if (current.fileIdentifier == fileID) {        // if found, then reject
					found = true;
				//	System.err.println("File already exists."+fileID);   // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
					createRejects++;
					break;
				} else {    
					if (current.next != null) {
						current = current.next;
					}
				}
			}
			
			if (!found) {    // if file is not found
				current=first;
				for (int i = 0; i < DTsize; i++) {   // this loop iterates to find a space to create the file
					if (fileLength <= 0) {    // if file length is less than or equal to 0 then reject
				//		System.err.println("Create:Size cannot be less then or equal to 0"+fileID+":"+fileLength);    // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
						FileIDGenerator--;    // the creation is rejected so I dont want to lose the id
						createRejects++;
						break;
					}

					if (current.fileIdentifier == -1 && current.size >= lengthBlocks) {     // if there is a space and that space is enough for the creation
						file.setStart(current.getStart());
						current.setStart(file.start + file.size);
						current.setSize(current.getSize() - lengthBlocks);

						if (current.getPrev() == null) {  // if space is the first element of the dt
							first = file;
							file.setPrev(null);
						} else {
							current.getPrev().setNext(file);
							file.setPrev(current.getPrev());
						}

						file.setNext(current);
						current.setPrev(file);
						DTsize++;

						if (current.getSize() == 0) {     // after creation if the size of the space becomes 0 then delete it
							if (current.getPrev() == null) {
								current.getNext().setPrev(null);
								current.setNext(null);
								DTsize--;
							} else if (current.getNext() == null) {
								current.getPrev().setNext(null);
								current.setPrev(null);
								last=current;
								DTsize--;
							} else {
								current.getPrev().setNext(current.getNext());
								current.getNext().setPrev(current.getPrev());
								current.setNext(null);
								current.setPrev(null);
								DTsize--;
							}
						}
				  
								directory[file.getStart()] = new Block(fileLength);    // update the file length in the first block of the file
								Total=Total + file.getSize();
						break;
					} else {     // if current is not an enough space 
						if (current.getNext() != null) {
							current = current.getNext();
						} else {     // if we cannot find a suitable space then defragment
							last=current;
							defragment(file, fileLength);
							break;
						}
					}
				}
			}
		}else{   // if there is no enough space then reject
			//System.err.println("There is no enough space for create operation."+fileID+":"+fileLength); // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
			FileIDGenerator--;
			createRejects++;
		}
	}
	
	
	private static void defragmentExtend(int fileID, int extension) {  // defragment method for extend operation
		DTContent current=first;
		for(int i=0;i<DTsize;i++){      //  this loop iterates over dt and defragments the dt until it reaches the file to be extended
				if(current.fileIdentifier==fileID){     
					current=current.next;
					break;
				}
				if(current.fileIdentifier==-1){      // if current is a space than swap it with the next node in the DT
					if(current.getNext()!=null){
						moveFile(current.getNext(),current);      // moves the file back in the directory
						swapNodes(current.getNext(),current);	// swaps the current with the next 
						
						if(current.getNext().fileIdentifier==-1){      // after swap, if the next node is also a space then add them together
							current.setSize(current.getNext().getSize()+current.getSize());
							if (current.getNext().getNext() == null) {
								current.getNext().setPrev(null);
								current.setNext(null);
								DTsize--;
							} else {
								current.setNext(current.getNext().getNext());
								current.getNext().getPrev().setPrev(null);
								current.getNext().getPrev().setNext(null);
								current.getNext().setPrev(current);
								DTsize--;
							}
						}
						if(current.getPrev().fileIdentifier==fileID){    // when we found the file, exit the loop
							break;
						}
					}else{
						break;
					}
				}else{
					if(current.getNext()!=null){
						current=current.next;
					}else{
						break;
					}
				}
		}
		if(current.fileIdentifier==-1 &&current.getSize()>=extension){    // now look at the size after our file, if it is enough after the half defragment, then place it
			if(current.getPrev().fileIdentifier==fileID){
				directory[current.getPrev().start].setFileLength(directory[current.getPrev().start].fileLength+(extension*BLOCK_SIZE));
				for(int i=0;i<extension;i++){
					directory[current.getPrev().getStart()+current.getPrev().size+i]=new Block(directory[current.getPrev().start].fileLength);
					Total++;
				}
				current.getPrev().setSize(current.getPrev().getSize()+extension);
				current.setSize(current.getSize()-extension);
				current.setStart(current.getPrev().getStart()+current.getPrev().getSize());
			}
		}else{    // if the space is not enough continue to defragment
			while(current.fileIdentifier!=-1){
				current=current.getNext();
			}			
			for(int i=0;i<DTsize;i++){
				if(current.fileIdentifier==-1){
					if(current.getNext()!=null){
						moveFile(current.getNext(),current);
						swapNodes(current.getNext(),current);
						if(current.getNext()==null){
							break;
						}
						if(current.getNext().fileIdentifier==-1){
							current.setSize(current.getNext().getSize()+current.getSize());
							if (current.getNext().getNext() == null) {
								current.getNext().setPrev(null);
								current.setNext(null);
								DTsize--;
							} else {
								current.setNext(current.getNext().getNext());
								current.getNext().getPrev().setPrev(null);
								current.getNext().getPrev().setNext(null);
								current.getNext().setPrev(current);
								DTsize--;
							}
						}
					}else{
						break;
					}
				}
		}
			for(int i=0;i<DTsize;i++){     // after defragmentation finished this loop iterates for at most DTsize
				if(current.fileIdentifier==-1&&current.getNext()==null){   // if the current is the last node
					current.setSize(current.getSize()-extension);        //  then set the size
					if(current.getSize()==0){                          // if the size becomes 0 after extension
						current.getPrev().setNext(null);
						current.setNext(null);
					}else{     // after extension if the size of the space is not zero, then set its start
						current.setStart(current.getStart()+extension);
					}
					current=current.getPrev();   // move one step previous
				} else {
					if (current.fileIdentifier == fileID) {  // if we find the file to be extended then extend
						
						directory[current.getStart()].setFileLength(
								directory[current.getStart()].getFileLength() + (extension * BLOCK_SIZE));
						for (int j = 0; j < extension; j++) {     // creates new blocks in the directory
							directory[current.getStart() + current.getSize() + j] = new Block(
									directory[current.getStart()].fileLength);
							Total++;
						}
						current.setSize(current.getSize() + extension);
						break;
					} else {    // if the current is not the file to be extended
						
						for (int j = 0; j < current.getSize(); j++) {     // then shift the file to the right
							int k=current.getStart() + current.getSize() + extension-j-1;
							int l=current.getStart()+ current.getSize()-j-1;
							
							directory[k] = directory[l];     // shift it block by block
							
						} 
						for(int j=0;j<extension;j++){      
							directory[current.getStart()
										+j]=null;
						}
						current.setStart(current.getStart() + extension);
						current = current.getPrev();
					}
				}
			}
		}
	}
	
	private static void defragment(DTContent file,int fileLength) {      // defragment method for create
		
		int lengthBlocks=toBlocks(fileLength);   
		DTContent current=first;
		for(int i=0;i<DTsize;i++){      // this loop iterates for at most DTsize
			if(current.fileIdentifier==-1){          // if current is a space
				if(current.getNext()!=null){      		// if it is not the last element then swap the space with a file
					moveFile(current.getNext(),current);     
					swapNodes(current.getNext(),current);
					if(current.getNext()==null){
						break;
					}
					if(current.getNext().fileIdentifier==-1){    // after swap if the next is also a space than concatenate them
						current.setSize(current.getNext().getSize()+current.getSize());
						if (current.getNext().getNext() == null) {
							current.getNext().setPrev(null);
							current.setNext(null);
							DTsize--;
						} else {
							current.setNext(current.getNext().getNext());
							current.getNext().getPrev().setPrev(null);
							current.getNext().getPrev().setNext(null);
							current.getNext().setPrev(current);
							DTsize--;
						}
					}
				}else{     // if it is the last element
					if(current.getSize()>=file.getSize()){     // if the space is greater than the file size
						file.setStart(current.getStart());
						current.setStart(file.start + file.size);
						current.setSize(current.getSize() - lengthBlocks);
						if (current.getPrev() == null) {
							first = file;
							file.setPrev(null);
						} else {
							current.getPrev().setNext(file);
							file.setPrev(current.getPrev());
						}
						file.setNext(current);
						current.setPrev(file);
						DTsize++;
						if (current.getSize() == 0) {    // if current size becomes 0 after creation remove the node
							if (current.getPrev() == null) {
								current.getNext().setPrev(null);
								current.setNext(null);
								DTsize--;
							} else if (current.getNext() == null) {
								current.getPrev().setNext(null);
								current.setPrev(null);
								DTsize--;
							} else {
								current.getPrev().setNext(current.getNext());
								current.getNext().setPrev(current.getPrev());
								current.setNext(null);
								current.setPrev(null);
								DTsize--;
							}
						}
							
								directory[file.getStart()] = new Block(fileLength);
								Total= Total+file.getSize();
						
						    
					}else{    // if there is no enough space
			//			System.err.println("There is no enough space for create operation" +file.fileIdentifier);  // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
						FileIDGenerator--;
						createRejects++;
					}
					break;
				}
			}else{
				if(current.next!=null){
					current=current.getNext();
				}else{
					
					break;
				}
			}
		}
	}
	private static void swapNodes(DTContent next, DTContent current) {     // this method swaps two nodes in dt
		if(next.getNext()==null){
			current.getPrev().setNext(next);
			next.setPrev(current.getPrev());
			next.setNext(current);
			current.setPrev(next);
			current.setNext(null);
			next.setStart(current.getStart());
			current.setStart(next.getStart()+next.getSize());	
		}else{
			current.setNext(next.getNext());
			current.getNext().setPrev(current);
			current.getPrev().setNext(next);
			next.setPrev(current.getPrev());
			next.setNext(current);
			current.setPrev(next);
			next.setStart(current.getStart());
			current.setStart(next.getStart()+next.getSize());
		}
	}
	private static void moveFile(DTContent next, DTContent current) {     // this method moves a file in directory block by block
		for(int i=0;i<next.size;i++){
			directory[current.start+i]=directory[next.start+i];
			directory[next.start+i]=null;
		}
	}
	public static int toBlocks(int fileLength){    // this method  takes file length as bytes and calculates the block amount
		if(fileLength%BLOCK_SIZE==0){
			return fileLength/BLOCK_SIZE;
		}else{
			return fileLength/BLOCK_SIZE+1;
		}
	}
	public static int IDGenerator(){   // this method generates a new id when it is called.
		FileIDGenerator++;
		 return FileIDGenerator;
	}
}
