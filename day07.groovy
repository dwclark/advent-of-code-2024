import static Aoc.*

def lines = new File('data/07').readLines().collect { it.replace(':', '') }
def equations = lines.collect { it.split(/\s+/).collect { new BigInteger(it) } }
def possible1, possible2

possible1 = { List accum, List vals ->
    def added = vals[0] + vals[1]
    def multiplied = vals[0] * vals[1]
    
    if(vals.size() == 2) {
	accum.add(added)
	accum.add(multiplied)
    }
    else {
	List rest = vals[(2..<vals.size())]
	possible1(accum, [added] + rest)
	possible1(accum, [multiplied] + rest)
    }
}

possible2 = { List accum, List vals ->
    def added = vals[0] + vals[1]
    def multiplied = vals[0] * vals[1]
    def concat = new BigInteger("${vals[0]}${vals[1]}")
    
    if(vals.size() == 2) {
	accum.add(added)
	accum.add(multiplied)
	accum.add(concat)
    }
    else {
	List rest = vals[(2..<vals.size())]
	possible2(accum, [added] + rest)
	possible2(accum, [multiplied] + rest)
	possible2(accum, [concat] + rest)
    }
}

def canWork = { List vals, Closure closure ->
    def need = vals[0]
    def accum = []
    closure.call(accum, vals[(1..<vals.size())])
    if(accum.any { it == need })
	return need
    else
	return 0
}

def part1 = {
    equations.inject(0G) { total, equation -> total += canWork(equation, possible1) }
}

def part2 = {
    equations.inject(0G) { total, equation -> total += canWork(equation, possible2) }
}

//println part1()
println part2()
