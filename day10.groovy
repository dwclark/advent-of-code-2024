import static IntVec.*

def graph = [:]
new File('data/10').readLines().eachWithIndex { line, row ->
    line.eachWithIndex { s, col -> graph[vec(row,col)] = s.toInteger() } }

def findTrails(def accum, def graph, def pos, final height) {
    if(height == 9)
	accum.add(pos)
    else {
	for(IntVec n in pos.crossNeighbors) {
	    if(graph.containsKey(n) && graph[n] == height + 1) {
		findTrails(accum, graph, n, graph[n])
	    }
	}
    }
}

def part1(def graph) {
    int trailheads
    graph.each { pos, height ->
	if(height == 0) {
	    def accum = new HashSet()
	    findTrails(accum, graph, pos, 0)
	    trailheads += accum.size()
	}
    }

    return trailheads
}

def findRating(def accum, def soFar, def graph, def pos, final height) {
    if(height == 9)
	accum.add(soFar)
    else {
	for(IntVec n in pos.crossNeighbors) {
	    if(graph.containsKey(n) && graph[n] == height + 1) {
		findRating(accum, soFar + [n], graph, n, graph[n])
	    }
	}
    }
}

def part2(def graph) {
    int ratings
    graph.each { pos, height ->
	if(height == 0) {
	    def accum = new HashSet()
	    findRating(accum, new HashSet([pos]), graph, pos, 0)
	    ratings += accum.size()
	}
    }

    return ratings
}


//println part1(graph)
println part2(graph)
