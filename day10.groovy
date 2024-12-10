import static Aoc.*
import static IntVec.*

def graph = [:]
new File('data/10').readLines().eachWithIndex { line, row ->
    line.eachWithIndex { s, col -> graph[vec(row,col)] = s.toInteger() } }

def findPaths(def graph, Closure accumulator, List path) {
    if(path.size() == 10)
	accumulator(path)
    else {
	IntVec pos = path[-1]
	int height = graph[pos]
	for(IntVec n in pos.crossNeighbors) {
	    if(graph.containsKey(n) && graph[n] == height + 1) {
		findPaths(graph, accumulator, path + [n])
	    }
	}
    }
}

def execute(def graph, def closure) {
    graph.inject(0) { int total, IntVec pos, int height ->
	if(height == 0) {
	    Set accum = new HashSet()
	    findPaths(graph, closure.curry(accum), [pos])
	    total += accum.size()
	}

	total
    }
}

printAssert("Part 1:", execute(graph) { Set accum, List path -> accum.add(path[-1]) }, 794,
	    "Part 2:", execute(graph) { Set accum, List path -> accum.add(path as Set) }, 1706)
