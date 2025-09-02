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

/*while(GATES.any { gate -> !gate.computed }) {
    GATES.each { gate ->
	if(!gate.computed && gate.computable)
	    gate.compute()
    }
}

printAssert("Part 1:", value(), 36035961805936L)*/

//part 2, looks like this is supposed to represent a series of half adders
//diagram or somehow figure out how the half adders are misconfigured

//OK, looks like x,y all on inputs, z all on outputs, as is to be expected for a half adder
//println GATES.findAll { g -> g.out.startsWith('x') || g.out.startsWith('x') }
//println GATES.findAll { g -> g.out.startsWith('y') || g.out.startsWith('y') }
//println GATES.findAll { g -> g.in1.startsWith('z') || g.in2.startsWith('z') }

//GATES.findAll { g -> g.out.startsWith('z') }.sort { g1, g2 -> g1.out <=> g2.out }.each { println it }
//jgw OR rhh -> z37 is probably incorrect, all other z terminals are XOR, except for z45, which is the final carry op

//yeah, this needs to be beefed up to display the two half-adders for each output/carry
/*def complexes = new TreeMap()
GATES.findAll { g -> g.out.startsWith('z') }.each { g ->
    inputs = []
    complexes[g.out] = [g]
    def upstream = [g.in1, g.in2]
    def upstreamGates = GATES.findAll { upGate -> upGate.out in upstream || upGate.out in upstream }
    complexes[g.out].addAll(upstreamGates)
    def moreUpstream = upstreamGates.collect { up -> [up.in1, up.in2] }.flatten()
    def moreUpstreamGates = GATES.findAll { upGate -> upGate.out in moreUpstream || upGate.out in moreUpstream }
    complexes[g.out].addAll(moreUpstreamGates)
}*/

//complexes.each { k, v -> println "${k}: ${v}" }

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

class Carry {
}

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
	println "did it!"
    }
    catch(GateSwap gs) {
	println "swapping ${gs.one} and ${gs.two}"
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

adders.each { println it }
println swaps.sort().join(',')

void fullAdders() {
    (0..45).each { idx ->
	def zwire = String.format("z%02d", idx)
	def xwire = String.format("x%02d", idx)
	def ywire = String.format("y%02d", idx)

	println("Finding info for ${zwire}")
	def xyXORGate = GATES.find { g -> g.in1 == xwire && g.op == 'XOR' && g.in2 == ywire }
	if(xyXORGate)
	    println "Found xy xor gate: ${xyXORGate}"
	else
	    println "ERROR: no xy xor gate"

	def carryInWire = null
	def sumXORGate = GATES.find { g -> (g.in1 == xyXORGate?.out || g.in2 == xyXORGate?.out) && g.op == 'XOR' && g.out == zwire }
	if(sumXORGate) {
	    carryInWire = (sumXORGate.in1 == xyXORGate.out) ? sumXORGate.in2 : sumXORGate.in1
	    println "Found sum xor gate ${sumXORGate}, carry in wire: ${carryInWire}"
	}
	else {
	    println "ERROR: no sum xor gate"
	    println "Similar gates: ${GATES.findAll { g -> g.out == zwire }}"
	}
	
	
	def xyANDGate = GATES.find { g -> g.in1 == xwire && g.op == 'AND' && g.in2 == ywire }
	if(xyANDGate)
	    println "Found xy and gate: ${xyANDGate}"
	else
	    println "ERROR: no xy and gate"

	def carryInWires = [ xyXORGate?.out, carryInWire ]
	def carryANDGate = GATES.find { g -> g.in1 in carryInWires && g.in2 in carryInWires && g.op == 'AND' }
	if(carryANDGate)
	    println "Found carry and gate: ${carryANDGate}"
	else
	    println "ERROR: no carry and gate"

	def carryOutWires = [carryANDGate?.out, xyANDGate?.out]
	def carryORGate = GATES.find { g -> g.in1 in carryOutWires && g.in2 in carryOutWires && g.op == 'OR' }
	if(carryORGate)
	    println "Found carry or gate ${carryORGate}"
	else
	    println "ERROR: no carry or gate"

	println()
    }
}

//fullAdders()

