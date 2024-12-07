import static Aoc.*
import static java.util.concurrent.CompletableFuture.supplyAsync

final lines = new File('data/07').readLines().collect { it.replace(':', '') }
final eqs = lines.collect { it.split(/\s+/).collect { it.toBigInteger() } }
final p1Ops = [ { one, two -> one * two }, { one, two -> one + two } ]
final p2Ops = p1Ops + [ { one, two -> "${one}${two}".toBigInteger() } ]

def possible(List accum, List ops, List vals) {
    List results = ops.collect { op -> op.call(vals[0], vals[1]) }
    List rest = vals[(2..<vals.size())]
    
    if(!rest) {
	accum.addAll(results)
    }
    else {
	for(result in results)
	    possible(accum, ops, [result] + rest)
    }

    return accum
}

def canWork(List vals, List ops) {
    def accum = possible([], ops, vals[(1..<vals.size())])
    return (accum.any { it == vals[0] }) ? vals[0] : 0
}

def schedule = { List ops ->
    def futures = eqs.collect { eq -> supplyAsync({ -> canWork(eq, ops) }) }
    futures.inject(0G) { tot, fut -> tot += fut.get() }
}

printAssert("Part 1:", schedule(p1Ops), 8401132154762G,
	    "Part 2:", schedule(p2Ops), 95297119227552G)

