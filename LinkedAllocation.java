import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class LinkedAllocation {       // linked allocation class which uses a File Allocation Table and another table to keep the start and size informations. 
	
	
	/*
	// input files: you can copy the name of the file here. 
	
	//("input_8_600_5_5_0.txt")
	//("input_1024_200_5_9_9.txt")
	//("input_1024_200_9_0_0.txt")
	//("input_1024_200_9_0_9.txt")
	//("input_2048_600_5_5_0.txt")
	
	*/
		public static String FILE_NAME="input_8_600_5_5_0.txt";    // file name
		public static final int BLOCK_SIZE = 8;      //  the block size of the directory. You should change the BLOCK_SIZE with the block size amount in the file name.
		static Block[] directory = new Block[32768];    // The directory, which consists of 32768 blocks. It is an array for the fixed number of space.
		static HashMap<Integer,Integer> FAT = new HashMap<Integer,Integer>();  // File Allocation Table. It is a hashmap so that we can access in O(1) time.
		static HashMap<Integer,Pair<Integer,Integer>> DT = new HashMap<Integer,Pair<Integer,Integer>>();   // this DT is different from the original DT. It keeps id,start and size informations in a hashmap. 
		static int FileIDGenerator=-1;    // it is a global variable to keep the id of the next generated file.
		static public String line;   // this string is for reader.
		
		 
		 static int createRejects=0;    // a count for create rejects.
		 static int extendRejects=0;    // a count for extend rejects.
		 static int shrinkRejects=0;    // a count for shrink rejects. 
		 static int accessRejects=0;    // a count for access rejects

		public static void main(String[] args) {
		
			
			double timeStart = System.currentTimeMillis();     // to keep the run time.
		
			try {           // reader for the file.
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
			
			
			
			System.out.println("Linked Allocation");        // outputs of the run.
			System.out.println(FILE_NAME);
			System.out.println("run time: "+(System.currentTimeMillis() - timeStart));
			System.out.println();
			System.out.println("create Rejects: "+createRejects);
			System.out.println("extend Rejects: "+extendRejects);
			System.out.println("shrink Rejects: "+shrinkRejects);
			System.out.println("access Rejects: "+accessRejects);
		}
		
		private static void parse(String line) {     // parse method calls the corresponding method for the operations.
			if(line.charAt(0)=='c'){
				int fileLength=Integer.parseInt(line.substring(2));
				create_fileFAT(IDGenerator(),fileLength);      // if the line starts with 'c' it calls createFAT.
				
			}else if(line.charAt(0)=='a'){
				String delims = ":";        
				String[] tokens = line.split(delims);
				int fileID=Integer.parseInt(tokens[1]);
				int byteOffset=Integer.parseInt(tokens[2]);
				accessFAT(fileID,byteOffset);     // if the line starts with 'a' it calls accessFAT
			}else if(line.charAt(0)=='e'){
				String delims = ":";
				String[] tokens = line.split(delims);
				int fileID=Integer.parseInt(tokens[1]);
				int extension=Integer.parseInt(tokens[2]);
				extendFAT(fileID,extension);   // if the line starts with 'e' it calls extendFAT
			}else if(line.charAt(0)=='s'){
				String delims = ":";
				String[] tokens = line.split(delims);
				int fileID=Integer.parseInt(tokens[1]);
				int shrinking=Integer.parseInt(tokens[2]);
				shrinkFAT(fileID,shrinking);    // if the line starts with 'sh' it calls shrinkFAT
			}else{
				return;
			}
		}
		
		private static void shrinkFAT(int fileID, int shrinking) {     //shrinking method for FAT
			if(!DT.containsKey(fileID)){       // if the file id does not exist in the hashmap then reject to shrink.
				 
				//System.err.println("File not found to shrink "+fileID+":"+shrinking);    // you can uncomment this line if you want to see the rejection as an input(recommended for debugging).
				shrinkRejects++;     					
				return;
			}else{           // if file exists 
				int size=DT.get(fileID).getSize();    
				int fileblockSize=toBlocks(size);     
				if(shrinking>=fileblockSize){         // if the shrinking amount is greater than or equal to the file size it rejects.
					//System.err.println("Shrinking amount cannot be more than or equal to file size. "+ fileID+":"+shrinking);  // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
					shrinkRejects++;                 
					return;
				}else{								  // if the shrinking size is valid
					int start=DT.get(fileID).getStart();    
					int index=start; 						
					for(int i=0;i<fileblockSize-shrinking-1;i++){     // this for finds the last block before the block to be shrinked.
						index=FAT.get(index);     // index is set to the next blocks index in every iteration.
					}
					int tempindex=FAT.get(index);   
					FAT.replace(index, -1);			// updates the next of the previous block in FAT to -1 
					index=tempindex;				// index is updated to the its old value to continue. 
					
					for(int i=0;i<shrinking;i++){   // this for turns for shrinking amount
						tempindex=FAT.get(index);   // next index is kept in tempindex.
						FAT.remove(index);          // remove the block from the fat.
						directory[index]=null;		// remove the block from the directory.
						index=tempindex;			// update the index with the temp index
					}
					if(size%BLOCK_SIZE==0){			// if the file size is a multiple of block size then we can safely decrease the size by shrinking*blocksize
						size=size-(shrinking*BLOCK_SIZE);  // decrease the size by shrinking*blocksize
						directory[start].setFileLength(size); // writes the size of the file in the first block of that file in the directory.
						DT.get(fileID).setSize(size);        // update the size of the file in the DT
					}else{					// if the file size is not a multiple of block size
						size=size-(size%BLOCK_SIZE);     // first decrease the remaining part of the size 
						size=size-((shrinking-1)*BLOCK_SIZE);  // then decrease by (shrinking-1)*blocksize
						directory[start].setFileLength(size);   // writes the size of the file in the first block of that file in the directory.
						DT.get(fileID).setSize(size);			// update the size of the file in the DT
					}
				}
			}
		}

		private static void extendFAT(int fileID, int extension) {	//extension method for FAT
			if(extension>32768-FAT.size()){    // extension is greater than the remaining free space then rejects
				//System.err.println("There is no enough space for extension of the file: "+fileID); // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
				extendRejects++;   
				return;
			}else{
				if(!DT.containsKey(fileID)){    //if there is not such a an element in the DT where the key of the element is fileID
					//System.err.println("Cannot find the file to extend " + fileID);  // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
					extendRejects++;    
				}else{						// if the file exists.
					int start=DT.get(fileID).getStart();   
					int fileSize=DT.get(fileID).getSize();
					int fileBlockSize=toBlocks(DT.get(fileID).getSize());
					int index=start;
					int next;
					int prev=-1;
					int counter=0;
					boolean isFirst=true;     // this boolean is for checking if this is the first block of the extension or not.
					for(int i=0;i<fileBlockSize;i++){  // this for iterates until it finds the last block of the file before extension.s 
						next=FAT.get(index);
						if(next!=-1){
							index=next;
						}else{
							break;
						}	
					}
					fileSize=fileSize+(extension*BLOCK_SIZE);     // updates the file size.
					directory[start].setFileLength(fileSize);    // updates the file size of the first block of the file
					for(int i=0;i<directory.length;i++){		// this for iterates to find a free block.
						if(FAT.containsKey(i)){                // if the block is in the FAT then it is occupied.
							continue;
						}else{	                                     // if it is not in the FAT it is free.
							if(isFirst){              				// if it is the first extension block.
								
								FAT.put(i, -1);							
								FAT.replace(index, i);
								prev=i;
								directory[i]=new Block(fileSize);
								isFirst=false;
								counter++;
								if(counter==extension){
									break;
								}
							}else{
								FAT.put(i, -1);
								directory[i]=new Block(fileSize);
								FAT.replace(prev, i);
								prev=i;
								counter++;
								if(counter==extension){
									break;
								}
							}
						}
					}
					DT.get(fileID).setSize(fileSize);      // it updates the size in the DT.
				
				}
			}
		}

		private static void accessFAT(int fileID, int byteOffset) {    // access method for FAT
			if(byteOffset<0){   // if byte offset is less than zero, reject.
				//System.err.println("byte offset cannot be lower than 0." + fileID+":"+byteOffset);  // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
				accessRejects++;
				return;
			}
			int blockOffset= toBlocks(byteOffset+1)-1;
			
			if(!DT.containsKey(fileID)){    // if the file is not found, reject
				//System.err.println("File not found " +fileID);    // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
				accessRejects++;
			}else{       // if file exists
				int start=DT.get(fileID).getStart();
				int size=DT.get(fileID).getSize();
				if(byteOffset>size){
					//System.err.println("byte offset cannot be greater than the file size. "+fileID+":"+byteOffset);  // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
					accessRejects++;
					return;
				}
				
				int index=start;
				int next;
				for(int i=0;i<blockOffset;i++){     // this for iterates to find the block
					next=FAT.get(index);
					if(next!=-1){
						index=next;
					}else{
						break;
					}
				}
				//System.out.println("accessed: "+index + "  "+ fileID+":"+byteOffset);
			}
		}

		private static void create_fileFAT(int fileID, int fileLength) {  // create method for FAT
			int blockSize=toBlocks(fileLength);
			int start=-1;
			if(blockSize>32768-FAT.size()){       // if there is no enough space then reject
			//	System.err.println("There is no enough space to create file "+fileID);   // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
				FileIDGenerator--;   // the creation rejected so I don't want to lose this id.
				createRejects++;
				return;
			}
			if(DT.containsKey(fileID)){    // if the file already exists then reject.
				//System.err.println("file already exists.");    // you can uncomment this line if you want to see the rejection as an output(recommended for debugging).
				FileIDGenerator--;
				createRejects++;
			}else{                 // if this is a new file
				boolean isFirst=true;
				int prev=-1;
				int size=0;
				for(int i=0;i<directory.length;i++){     // this for iterates to find a free space.
					if(FAT.containsKey(i)){        // if it is in the FAT then it is occupied
						continue;
					}else{                
						if(isFirst){
							FAT.put(i, -1);
							directory[i]=new Block(fileLength);
							prev=i;
							start=i;
							isFirst=false;
							size++;
							if(size==blockSize){
								break;
							}
						}else{
							FAT.put(i, -1);
							directory[i]=new Block(fileLength);
							FAT.replace(prev, i);
							prev=i;
							size++;
							if(size==blockSize){
								break;
							}
						}
					}
				}
				DT.put(fileID, new Pair<Integer,Integer>(start,fileLength));   // updates the size in the DT

			}
		}
		
		public static int toBlocks(int fileLength){    // this method  takes file length as bytes and calculates the block amount 
			if(fileLength%BLOCK_SIZE==0){
				return fileLength/BLOCK_SIZE;
			}else{
				return fileLength/BLOCK_SIZE+1;
			}
		}
		public static int IDGenerator(){     // this method generates a new id when it is called.
			FileIDGenerator++;
			 return FileIDGenerator;
		}
	}

