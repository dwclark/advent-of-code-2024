import Aoc.*
import static IntVec.vec
import groovy.transform.Field
import java.util.concurrent.ConcurrentHashMap
import static java.util.concurrent.CompletableFuture.supplyAsync
import static java.util.Map.entry

List parse(String path) {
    Set grid = new HashSet()
    IntVec start, end
    def lines = new File(path).readLines()
    lines.eachWithIndex { line, row ->
	line.eachWithIndex { s, col ->
	    if(s in ['S','E','.']) {
		IntVec v = vec(row, col)
		grid.add(v)
		if(s == 'S') start = v
		if(s == 'E') end = v
	    }
	}
    }

    //there is only one path, read the directions!, just label everything
    int cost = 0
    IntVec current = end
    Set visited = new HashSet()
    Map costs = [:]
    while(current) {
	visited.add(current)
	costs[current] = cost++
	current = current.crossNeighbors.find { n -> grid.contains(n) && !visited.contains(n) }
    }

    return [ lines, costs, start, end ]
}

@Field final List init = parse('data/20')
@Field final List lines = init[0]
@Field final int rows = lines.size()
@Field final int columns = lines[0].length()
@Field final Map costs = Map.copyOf(init[1])
@Field final IntVec start = init[2]
@Field final IntVec end = init[3]

Map part1(int atLeast) {
    final List directions = [vec(0,1), vec(0,2), vec(0,-1), vec(0,-2),
			     vec(1,0), vec(2,0), vec(-1,0), vec(-2,0)]
    Map saved = [:]
    costs.each { pos, cost ->
	for(int i = 0; i < directions.size(); i += 2) {
	    IntVec toTest = pos + directions[i+1]
	    if(!costs.containsKey(pos + directions[i]) &&
	       costs.containsKey(toTest)) {
		int diff = (costs[pos] - costs[toTest]) - 2
		if(diff >= atLeast) {
		    saved[diff] = saved.get(diff, 0) + 1
		}
	    }
	}
    }

    return saved
}

boolean inBounds(final IntVec pos) {
    (pos[0] >= 0 && pos[0] < rows &&
     pos[1] >= 0 && pos[1] < columns)
}

Map cheats(final IntVec pos, final int atLeast) {
    def exits = [:]
    for(int row = -20; row <= 20; ++row) {
	for(int col = Math.abs(row) - 20; col <= 20 - Math.abs(row); ++col) {
	    IntVec possible = vec(row, col) + pos
	    int walked = Math.abs(row) + Math.abs(col)
	    if(costs.containsKey(possible)) {
		assert !exits.containsKey(possible)
		int saved = costs[pos] - (costs[possible] + walked)
		if(saved >= atLeast)
		    exits[possible] = saved
	    }
	}
    }

    return exits
}

Map part2() {
    Map saved = [:]
    List tasks = costs.keySet().collect { [it, supplyAsync(this.&cheats.curry(it, 100))] }
    tasks.each { list ->
	def (pos, task) = list
	task.get().each { exit, num ->
	    saved[num] = saved.get(num, 0) + 1
	}
	
	println "finished ${pos}"
    }
    
    return saved
}

def printCheats(Map exits) {
    println exits
    (0..<rows).each { row ->
	(0..<columns).each { col ->
	    IntVec v = vec(row, col)
	    if(!costs.containsKey(v)) print '#'
	    else if(exits.containsKey(v)) print 'C'
	    else if(v == start) print 'S'
	    else if(v == end) print 'E'
	    else print '.'
	}

	println()
    }
}

//println costs
//println part1(100).values().sum()
//printCheats(bfsCheats(vec(3,1), 50))
println part2().values().sum()

//int diff = costs[vec(3,1)] - (costs[vec(13,6)] + 19)
//println diff
