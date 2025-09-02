import groovy.transform.Field
import groovy.transform.ToString
import static Aoc.*

class Gate {
    final Map<String,Byte> wires
    String in1
    final String op
    String in2
    String out
    
    Gate(Map<String,Byte> wires, List<String> inputs, String op, String out) {
	inputs = inputs.sort()
	this.wires = wires
	this.in1 = inputs[0]
	this.op = op
	this.in2 = inputs[1]
	this.out = out
    }

    boolean isComputed() { return wires[out] != null }

    boolean isComputable() {
	return wires[in1] != null && wires[in2] != null
    }

    boolean hasInputs(Collection<String> col) {
	return in1 in col && in2 in col
    }

    boolean hasSomeInputs(Collection<String> inputs) {
	return in1 in inputs || in2 in inputs
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
	gates << new Gate(WIRES, [tmp[0], tmp[2]], tmp[1], tmp[4])
    }

    gates
}

class GateSwap extends RuntimeException {
    GateSwap(String one, String two) {
	this.one = one
	this.two = two
    }

    final String one
    final String two
}

interface Adder {
    String getSumWire()
    String getCarryWire()

    default String xwire(int idx) { String.format("x%02d", idx) }
    default String ywire(int idx) { String.format("y%02d", idx) }
    default String zwire(int idx) { String.format("z%02d", idx) }
}

@ToString(includeNames=true)
class Full implements Adder {
    final Gate inputXor
    final Gate carryXor
    final Gate inputAnd
    final Gate carryAnd
    final Gate carryOr
    
    final String getCarryWire() { carryOr.out }
    final String getSumWire() { carryXor.out }

    private void launchSwap(List<Gate> gates, String one, String two) {
	def inputs = [ one, two ] as Set
	def partial = gates.find { g -> g.hasSomeInputs(inputs) }
	Set found = [partial.in1, partial.in2] as Set
	def counts = [:]
	inputs.each { s -> counts[s] = counts.get(s, 0) + 1 }
	found.each { s -> counts[s] = counts.get(s, 0) + 1}
	List toSwap = counts.findResults { k,v -> (v == 1) ? k : null } as List
	throw new GateSwap(toSwap[0], toSwap[1])
    }
    
    Full(int idx, List<Gate> gates, List<Adder> adders) {
	inputXor = gates.find { g -> g.op == 'XOR' && g.hasInputs([xwire(idx), ywire(idx)]) }
	carryXor = gates.find { g -> g.op == 'XOR' && g.hasInputs([inputXor.out, adders[idx-1].carryWire]) }
	inputAnd = gates.find { g -> g.op == 'AND' && g.hasInputs([xwire(idx), ywire(idx)]) }
	carryAnd = gates.find { g -> g.op == 'AND' && g.hasInputs([inputXor.out, adders[idx-1].carryWire]) }
	if(!carryAnd) {
	    launchSwap(gates, inputXor.out, adders[idx-1].carryWire)
	}
	
	
	carryOr = gates.find { g -> g.op == 'OR' && g.hasInputs([inputAnd.out, carryAnd.out]) }
	if(!carryOr) {
	    launchSwap(gates, inputAnd.out, carryAnd.out)
	}
    }
}

@ToString(includeNames=true)
class Half implements Adder {
    Half(int idx, List<Gate> gates) {
	Set<String> inputs = [ xwire(idx), ywire(idx) ]
	sumGate = gates.find { g -> g.hasInputs(inputs) && g.op == 'XOR' }
	carryGate = gates.find { g -> g.hasInputs(inputs) && g.op == 'AND' }
	assert sumGate && carryGate && carryWire && sumWire
    }
    
    final Gate sumGate
    final Gate carryGate
    final String getCarryWire() { carryGate.out }
    final String getSumWire() { sumGate.out }
}

long part1() {
    while(GATES.any { gate -> !gate.computed }) {
	GATES.each { gate ->
	    if(!gate.computed && gate.computable)
		gate.compute()
	}
    }

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

String part2() {
    def adders
    boolean didIt = false
    def swaps = []
    while(!didIt) {
	try {
	    adders = []
	    (0..44).each { idx ->
		if(idx == 0)
		    adders[idx] = new Half(idx, GATES)
		else if(idx < 45)
		    adders[idx] = new Full(idx, GATES, adders)
	    }
	    
	    didIt = true
	}
	catch(GateSwap gs) {
	    swaps.add gs.one
	    swaps.add gs.two
	    GATES.each { gate ->
		['in1', 'in2', 'out'].each { field ->
		    if(gate[field] == gs.one) gate[field] = gs.two
		    if(gate[field] == gs.two) gate[field] = gs.one
		}
	    }
	}
    }

    swaps.sort().join(',')
}

printAssert("Part 1:", part1(), 36035961805936L,
	    "Part 2:", part2(), 'jqf,mdd,skh,wpd,wts,z11,z19,z37')
