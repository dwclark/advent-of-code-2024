import groovy.transform.InheritConstructors
import groovy.transform.CompileStatic
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

    void run() {
	while(step()) {}
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

int A, B, C
List output

def myProgram = { int val ->
    A = val; B = 0; C = 0; output = [];
    return { ->
	B = A & 7
	B = B ^ 2
	C = A >>> B
	B = B ^ 3
	B = B ^ C
	output.add(B & 7)
	A = (A >>> 3)
    }
}

def toExec = myProgram(64_584_136)
9.times { toExec() }
println output.join(',')

/*def computer = parse('data/17')
(0..07777).each {
    computer.reset(it)
    computer.run()
    println "${it}: ${computer.output.join(',')}"
 }*/

//def cont
//println computer
//while((cont = computer.step()))
//    println computer
//println computer.output.join(',')

/*assert computer.output.join(',') == '3,7,1,7,2,1,0,6,3'

(5555..5555).each { num ->
    computer.reset(num)
    println "${Integer.toOctalString(num)} ********************************"
    println computer
    def cont
    while((cont = computer.step()))
	println computer
    println computer.output.join(',')
    println()
}
*/
