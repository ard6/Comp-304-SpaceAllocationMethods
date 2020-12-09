

public class DTContent {      // Since my DT is a linked list, DT consists of DTContent objects.

	
	int fileIdentifier;   // the file ID
	int start;    // start of the file
	int size;     // size of the file
	DTContent next;    // next DTContent of the file. This is different from FAT because it is guaranteed that the next is always contiguous.
	DTContent prev;    // previous DTContent of the file.
	
	public DTContent (int fileIdentifier, int start,int size){    // constructor of the DTContent.
		this.fileIdentifier=fileIdentifier;
		this.start=start;
		this.size=size;
	}

	public DTContent getNext() {    // getter of the next. 
		return next;
	}

	public void setNext(DTContent next) {    // setter of the next. 
		this.next = next;
	}

	public DTContent getPrev() {     // getter of the previous.
		return prev;
	}

	public void setPrev(DTContent prev) {    // setter of the previous.
		this.prev = prev;
	}

	public int getStart() {    // getter of the start.
		return start;
	}

	public void setStart(int start) {    // setter of the class.
		this.start = start;
	}

	public int getSize() {      // getter of the size.
		return size;
	}

	public void setSize(int size) {   // setter of the size.
		this.size = size;
	}
	
	
	
}
