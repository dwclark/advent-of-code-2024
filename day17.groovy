class Computer {
    def A, B, C, IP
    List<Long> instructions
    List<Long> output = []

    Map table = [0L: this.&adv, 1L: this.&bxl, 2L: this.&bst,
		 3L: this.&jnz, 4L: this.&bxc, 5L: this.&out,
		 6L: this.&bdv, 7L: this.&cdv].asImmutable()
    
    Computer(def A, def B, def C, List<Long> instructions) {
	this.A = A
	this.B = B
	this.C = C
	this.instructions = instructions
	this.IP = 0
    }

    @Override String toString() { "Computer(A: ${A}, B: ${B}, C: ${C}, instructions: ${instructions}" }

    def run() {
	while(IP < instructions.size()) {
	    def ins = instructions[IP]
	    def op = instructions[IP+1]
	    table[ins].call(op)
	}
    }

    def decodeCombo(def op) {
	switch(op) {
	    case 0:
	    case 1:
	    case 2:
	    case 3: return op
	    case 4: return A
	    case 5: return B
	    case 6: return C
	    case 7: throw new IllegalStateException()
	}
    }

    void adv(def op) {
	A = (int) (A / (2 ** decodeCombo(op)))
	IP += 2
    }

    void bxl(def op) {
	B = B ^ op
	IP += 2
    }

    void bst(def op) {
	B = decodeCombo(op) % 8
	IP += 2
    }

    void jnz(def op) {
	if(A == 0)
	    IP += 2
	else
	    IP = op
    }

    void bxc(def op) {
	B = B ^ C
	IP += 2
    }

    void out(def op) {
	output.add(decodeCombo(op) % 8)
	IP += 2
    }

    void bdv(def op) {
	B = (int) (A / (2 ** decodeCombo(op)))
	IP += 2
    }

    void cdv(def op) {
	C = (int) (A / (2 ** decodeCombo(op)))
	IP += 2
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

def computer = parse('data/17')
computer.run()
println computer.output.join(',')
