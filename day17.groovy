import static Aoc.*
import groovy.transform.InheritConstructors
import groovy.transform.CompileStatic
import groovy.transform.Field
import static Long.toHexString

@CompileStatic
class Computer {

    @CompileStatic
    abstract class Instruction {
	final long op
	final Computer c
	
	Instruction(final Computer c, final long op) {
	    this.c = c
	    this.op = op
	}

	@Override String toString() { return "${getClass().simpleName}(${opStr})" }

	String getOpStr() {
	    switch(op) {
		case 0: return '0'
		case 1: return '1'
		case 2: return '2'
		case 3: return '3'
		case 4: return 'A'
		case 5: return 'B'
		case 6: return 'C'
		case 7: throw new IllegalStateException()
	    }
	}
	
	long decode(long op) {
	    switch(op) {
		case 0:
		case 1:
		case 2:
		case 3: return op
		case 4: return c.A
		case 5: return c.B
		case 6: return c.C
		case 7: throw new IllegalStateException()
	    }
	}

	abstract void call()
    }

    @InheritConstructors class adv extends Instruction { void call() { c.A = (long) c.A.intdiv(2 ** decode(op)); c.IP++; } }
    @InheritConstructors class bxl extends Instruction { void call() { c.B = c.B ^ op; c.IP++; } }
    @InheritConstructors class bst extends Instruction { void call() { c.B = decode(op) % 8; c.IP++; } }
    @InheritConstructors class jnz extends Instruction { void call() { c.IP = (c.A == 0) ? c.IP + 1 : op.intdiv(2) } }
    @InheritConstructors class bxc extends Instruction { void call() { c.B = c.B ^ c.C; c.IP++; } }
    @InheritConstructors class out extends Instruction { void call() { c.output.add(decode(op) % 8); c.IP++; } }
    @InheritConstructors class bdv extends Instruction { void call() { c.B = (long) c.A.intdiv(2 ** decode(op)); c.IP++; } }
    @InheritConstructors class cdv extends Instruction { void call() { c.C = (long) c.A.intdiv(2 ** decode(op)); c.IP++; } }

    private Instruction factory(long id, long op) {
	switch(id) {
	    case 0L: return new adv(this, op)
	    case 1L: return new bxl(this, op)
	    case 2L: return new bst(this, op)
	    case 3L: return new jnz(this, op)
	    case 4L: return new bxc(this, op)
	    case 5L: return new out(this, op)
	    case 6L: return new bdv(this, op)
	    case 7L: return new cdv(this, op)
	}
    }

    private List<Instruction> parse(List<Long> vals) {
	List<Instruction> ret = []
	for(int i = 0; i < vals.size(); i += 2)
	    ret.add(factory(vals[i], vals[i+1]))
	return ret
    }
    
    long A, B, C, IP
    List<Instruction> instructions
    List<Long> output = []

    Computer(long A, long B, long C, List<Long> instructions) {
	this.A = A
	this.B = B
	this.C = C
	this.instructions = parse(instructions)
	this.IP = 0
    }

    void reset(long A) {
	this.A = A
	B = 0
	C = 0
	IP = 0
	output.clear()
    }

    private String num(long val) { String.format("%10o", val) }
    @Override String toString() { "Computer(A: ${num(A)}, B: ${num(B)}, C: ${num(C)}, IP: ${IP}, next: ${instructions[IP]}" }

    Computer exec() {
	while(step()) {}
	return this
    }

    boolean step() {
	instructions[IP].call()
	return IP < instructions.size()
    }
}

def parse(def path) {
    def A,B,C,instructions
    new File(path).readLines().eachWithIndex { line, idx ->
	if(idx == 0) A = line.split(' ')[2].trim().toLong()
	else if(idx == 1) B = line.split(' ')[2].trim().toLong()
	else if(idx == 2) C = line.split(' ')[2].trim().toLong()
	else if(idx == 4) instructions = line.split(' ')[1].trim().split(',').collect { it.trim().toLong() }
    }

    return new Computer(A, B, C, instructions)
}

//def cont
//println computer
//while((cont = computer.step()))
//    println computer
//println computer.output.join(',')

def myProgram(long val) {
    long A = val, B = 0, C = 0
    List output = [];

    while(A) {
	B = (A & 7)
	B = (B ^ 2)
	C = (A >>> B)
	B = (B ^ 3)
	B = (B ^ C)
	A = (A >>> 3)
	output.add(B & 7)
    }

    return output
}

def reduced(long val) {
    long A = val; B = 0; C = 0;
    List output = [];
    while(A) {
	B = ((((A & 7) ^ 2) ^ 3) ^ (A >>> ((A & 7) ^ 2)))
	A = (A >>> 3)
	output.add(B & 7)
    }

    return output
}

@Field final int bitsNeeded = 48
@Field final List<Integer> toMatch  = [2,4,1,2,7,5,1,3,4,3,5,5,0,3,3,0]

def nextStep(final long soFar, final int width) {
    long shifted = (soFar << 3L)
    for(long i = 0L; i < 8L; ++i) {
	long toTest = shifted | i
	List<Integer> output = reduced(toTest)
	if(output == toMatch[(toMatch.size() - width)..<toMatch.size()]) {
	    if((width * 3) == bitsNeeded) {
		return toTest
	    }
	    else {
		toTest = nextStep(toTest, width + 1)
		if(toTest) return toTest
	    }
	}
    }

    return 0L
}

def firstStep() {
    final int width = 5
    final long myMax = 2 ** (width * 3)

    for(long i = 0; i <= myMax; ++i) {
	List<Integer> output = reduced(i)
	if(output == toMatch[(toMatch.size() - width)..<toMatch.size()]) {
	    long result = nextStep(i, width + 1)
	    if(result) return result
	}
    }

    return 0L
}

def computer = parse('data/17').exec()
printAssert("Part 1:", computer.output.join(','), "3,7,1,7,2,1,0,6,3",
	    "Part 2:", reduced(37221334433268L).join(','), "2,4,1,2,7,5,1,3,4,3,5,5,0,3,3,0")

//need this: 2_412_751_343_550_330
//in octal: 0104_44611_62606_77572
