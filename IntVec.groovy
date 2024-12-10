import groovy.transform.CompileStatic

class IntVec {
    private final int[] ary
    
    IntVec(int[] ary) {
	this.ary = ary
    }

    static IntVec vec(int[] ary) {
	return new IntVec(ary)
    }

    private int[] allocate(IntVec rhs) {
	assert ary.length == rhs.ary.length
	return new int[ary.length]
    }

    @Override
    String toString() { return "(${ary.join(',')})" }

    @Override
    boolean equals(Object rhs) {
	if(!(rhs instanceof IntVec))
	    return false

	return Arrays.equals(ary, ((IntVec) rhs).ary)
    }

    @Override
    int hashCode() {
	return Arrays.hashCode(ary)
    }

    IntVec plus(IntVec rhs) {
	int[] tmp = allocate(rhs)
	for(int i = 0; i < ary.length; ++i)
	    tmp[i] = ary[i] + rhs.ary[i]
	return new IntVec(tmp)
    }

    IntVec minus(IntVec rhs) {
	final int[] tmp = allocate(rhs)
	for(int i = 0; i < ary.length; ++i)
	    tmp[i] = ary[i] - rhs.ary[i]
	return new IntVec(tmp)
    }

    IntVec multiply(int rhs) {
	final int[] tmp = new int[ary.length]
	for(int i = 0; i < ary.length; ++i)
	    tmp[i] = ary[i] * rhs
	return new IntVec(tmp)
    }

    int getAt(int idx) { return ary[i] }

    int manhattan(IntVec rhs) {
	assert ary.length == rhs.ary.length
	int total = 0
	for(int i = 0; i < ary.length; ++i)
	    total += Math.abs(ary[i] - rhs.ary[i])
	return total
    }

    double rectangular(IntVec rhs) {
	assert ary.length == rhs.ary.length
	int total = 0
	for(int i = 0; i < ary.length; ++i) {
	    int diff = ary[i] - rhs.ary[i]
	    total += diff * diff
	}
	
	return Math.sqrt(total)
    }

    private final static List<IntVec> N_TO_ADD =
	List.copyOf([vec(-1,-1), vec(-1,0), vec(-1, 1),
		     vec(0,-1), vec(0,1),
		     vec(1,-1), vec(1,0), vec(1,1)])
    
    List<IntVec> getNeighbors() {
	assert ary.length == 2
	return N_TO_ADD.collect { toAdd -> toAdd + this }
    }

    List<IntVec> getCrossNeighbors() {
	[vec(0,1), vec(0,-1), vec(-1,0), vec(1,0)].collect { toAdd -> toAdd + this }
    }
}
