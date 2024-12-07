import static Aoc.*
import static java.util.concurrent.CompletableFuture.supplyAsync

def lines = new File('data/07').readLines().collect { it.replace(':', '') }
def eqs = lines.collect { it.split(/\s+/).collect { new BigInteger(it) } }
final p1Ops = [ { one, two -> one * two }, { one, two -> one + two } ]
final p2Ops = p1Ops + [ { one, two -> new BigInteger("${one}${two}") } ]

def possible(List accum, List ops, List vals) {
    def results = ops.collect { op -> op.call(vals[0], vals[1]) }
    
    if(vals.size() == 2) {
	accum.addAll(results)
    }
    else {
	List rest = vals[(2..<vals.size())]
	for(result in results)
	    possible(accum, ops, [result] + rest)
    }
}

def canWork(List vals, List ops) {
    def need = vals[0]
    def accum = []
    possible(accum, ops, vals[(1..<vals.size())])
    return (accum.any { it == need }) ? need : 0
}

def schedule = { List ops ->
    def futures = eqs.collect { eq -> supplyAsync({ -> canWork(eq, ops) }) }
    futures.inject(0G) { tot, fut -> tot += fut.get() }
}

printAssert("Part 1:", schedule(p1Ops), 8401132154762G,
	    "Part 2:", schedule(p2Ops), 95297119227552G)

