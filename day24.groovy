import groovy.transform.Field

class Gate {
    final Map<String,Byte> wires
    final String in1
    final String op
    final String in2
    final String out
    
    Gate(Map<String,Byte> wires, String in1, String op, String in2, String out) {
	this.wires = wires
	this.in1 = in1
	this.op = op
	this.in2 = in2
	this.out = out
    }

    boolean isComputed() { return wires[out] != null }

    boolean isComputable() {
	return wires[in1] != null && wires[in2] != null
    }
    
    void compute() {
	if(computed || !computable)
	    throw new IllegalStateException()
	
	if(op == 'AND') wires[out] = wires[in1] & wires[in2]
	else if(op == 'OR') wires[out] = wires[in1] | wires[in2]
	else wires[out] = wires[in1] ^ wires[in2]
    }

    @Override
    String toString() {
	return "${in1} ${op} ${in2} -> ${out}"
    }
}

@Field final List<String> LINES = new File('data/24').readLines()

@Field final Map<String,Byte> WIRES = LINES.inject([:]) { ret, line ->
    if(line.contains(':')) {
	def tmp = line.split(':')
	ret[tmp[0].trim()] = tmp[1].trim() as byte
    }

    ret
}

@Field final List<Gate> GATES = LINES.inject([]) { gates, line ->
    if(line.contains('->')) {
	def tmp = line.split(' ')
	gates << new Gate(WIRES, tmp[0], tmp[1], tmp[2], tmp[4])
    }

    gates
}

long value() {
    long ret = 0L
    long idx = 0L

    def key = { -> String.format("z%02d", idx) }
    while(WIRES.containsKey(key())) {
	Byte b = WIRES[key()]
	ret |= (((long) b) << idx)
	++idx
    }

    return ret
}

while(GATES.any { gate -> !gate.computed }) {
    GATES.each { gate ->
	if(!gate.computed && gate.computable)
	    gate.compute()
    }
}

println new TreeMap(WIRES)
println value()
