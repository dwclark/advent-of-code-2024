import static Aoc.*
import groovy.transform.InheritConstructors
import groovy.transform.CompileStatic
import groovy.transform.Field

/*
 The computer class was straightforward enough, at least initially. I was able to
 get the right answer without any issues. However, I had no idea how to do part 2. I
 explored a couple of ideas. I tried to think of a way to run the computer in
 reverse. However, integer division operations made this impossible, information was
 lost between steps. This also doomed my second idea, making an inverse function
 of what the machine was doing. I also explored simpler ideas like looking for a
 pattern in inputs and outputs.

 I did make the Computer class more object-oriented so it was easier to print
 the state of what the computer was doing as it ran. While I did that to explore
 the above ideas, it wasn't a waste of time in the end as it was step 1 of a
 series of ideas I ended up doing.
 */
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

/*
 I used the following code to print out the complete state of the
 computer as it was doing a run. It shows all the registers, plus
 a human readable format of what was the next instruction to execute.

 This at least made clear a few insights. 1. The instructions always
 were executing a loop. 2. The jump instruction was always the last in
 the loop and jumped to the top of the loop as long as A wasn't zero
 3. The final instruction before the jump reduced the value in A by
 doing an integer division by 8 and then assigning it back to A, so
 the width of A was correlated with the number of iterations. 4. The
 final value was always an number that would be less than 8 since
 the output was B % 8, which is just the last 3 bits of B
 */

//def cont
//println computer
//while((cont = computer.step()))
//    println computer
//println computer.output.join(',')

/*
 I did look at reddit for clues, but not at any solutions. One clue was
 that someone simplified the loop the computer did to be simple operations
 on variables represented as simpler operations. They did this to feed the
 program into a SAT solver or constraint satisfaction program, I don't remember
 which. Most of those operations were bitwise, but a couple were not.

 I have no idea how to use those solvers, but it seemed like simplifying might
 give more clues. Being the completist I am, I reduced all operations to
 bitwise operations. The result was myProgram.
 */
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

/*
 Once I got myProgram, I remembered the substituion method of
 analyzing code in SICP. It seemed easy enough to do, so I
 went ahead at did that.

 At that point things started coming together. I remember on
 reddit people were claiming that the results were only ever
 based on a limited number of bits. Different numbers were
 given (obviously different inputs). However, from the reduced
 function, it's obvious that only 14 bits can ever be involved
 in computing the next result.

 I didn't know how to use a SAT solver, but I was pretty sure
 at that point I could write my own constraint satisfaction function.
 It took a lot of thinking to try and get all of the pieces correct.
 This function, satisfy, is shown below.
 */

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

/*
 Since the output is 16 digits of 3 bits each, and since
 A is reduced by 3 bits each iteration, we need 48 bits to solve this
 */
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

/*
 satisfy() tries all of the first 2**15 numbers to generate
 the last 5 numbers. There are many ways to do this, but at least
 one of them has to be involved in satisfying the constraint
 of matching all of the numbers. We need the first step to be 15 bits
 wide because a max of 14 bits will be involved in computing results.
 By doing this we can always be sure that we are constraining
 the last numbers to be correct.

 Once we have a match for the last five digits, we call into nextStep.
 nextStep() shifts the already satisfied 15 bits/last five numbers by
 3 bits left, then attempts to add one more 8 bit digit, making sure it can
 satisfy 18 bits/last six numbers. If it can satify, it recurses, increasing
 the width. Once the bit width in nextStep() == bitsNeeded, we have
 satisfied all of the constraints and we can return the number.

 Each phase will continue looping where it left off if it detects
 that downstream calls can't satisfy the remaining constraints.
 In that case, they return 0L and we keep looping. If the final
 call to nextStep() returns something other than 0L, we found the
 solution, return it (eventually) to satisfy() which returns it to the script
 */
def satisfy() {
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
	    "Matched Answer:", satisfy(), 37221334433268L,
	    "Part 2:", reduced(satisfy()).join(','), "2,4,1,2,7,5,1,3,4,3,5,5,0,3,3,0")
