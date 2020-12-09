

public class Pair<Start,Size> {    // this pair class keeps the start of a file and size of a file in pairs.
    private Start start;    //  start of a file.
    private Size size; // size of a file.

    public Pair(Start start, Size size) {   // constructor of the pair.
        this.start = start;
        this.size = size;
    }

    public void setStart(Start start) {       // setter of the start.
        this.start = start;
    }

    public void setSize(Size size) {      // setter of the size
        this.size = size;
    }

    public Start getStart() {     //  getter of the start 
        return start;
    }

    public Size getSize() {    // getter of the size
        return size;
    }
}