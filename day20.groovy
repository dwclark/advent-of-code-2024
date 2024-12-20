import Aoc.*
import static IntVec.vec
import groovy.transform.Field
import java.util.concurrent.ConcurrentHashMap
import static java.util.concurrent.CompletableFuture.supplyAsync

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

    return [ lines, Set.copyOf(grid), start, end ]
}

@Field final List init = parse('data/20')
@Field final int atLeast = 100
@Field final List lines = init[0]
@Field final int rows = lines.size()
@Field final int columns = lines[0].length()
@Field final Set grid = init[1]
@Field final IntVec start = init[2]
@Field final IntVec end = init[3]

int solve(Set grid) {
    def frontier = new PriorityQueue({ one, two -> one[1] <=> two[1]})
    def costs = grid.collectEntries { vec -> new MapEntry(vec, Integer.MAX_VALUE) }
    frontier.add([start, 0])
    costs[start] = 0
    
    while(frontier) {
	def (current, heuristic) = frontier.poll()
	if(current == end)
	    return costs[current]

	current.crossNeighbors.each { next ->
	    if(grid.contains(next)) {
		def cost = costs[current] + 1
		if(cost < costs[next]) {
		    costs[next] = cost
		    frontier.add([next, cost + next.manhattan(end)])
		}
	    }
	}
    }

    return -1
}

def canRemove(IntVec possible) {
    (!grid.contains(possible) &&
     ((grid.contains(possible.east) && (grid.contains(possible.west))) ||
      (grid.contains(possible.north) && grid.contains(possible.south))))
}

def cheater() {
    int row = 0, col = 0
    return { ->
	for(; row < rows; ++row) {
	    for(; col < columns; ++col) {
		IntVec possible = vec(row, col)
		if(canRemove(possible)) {
		    println "Added ${possible}"
		    Set newGrid = new HashSet(grid)
		    newGrid.add(possible)
		    ++col
		    return [possible, newGrid]
		}
	    }

	    col = 0
	}

	return [null, null]
    }
}

final int baseLine = solve(grid)
println baseLine
def part1() {
    def cheat = cheater()
    def solutions = new ConcurrentHashMap()
    def tasks = []
    while(true) {
	def (possible, newGrid) = cheat()
	if(!possible)
	    break
	else {
	    def task = { vec, set ->
		solutions[vec] = solve(set)
		return vec
	    }

	    tasks.add(task.curry(possible, newGrid))
	}
    }

    tasks.collect { supplyAsync(it) }.each { println "finished ${it.get()}" }
    return solutions
}

def p1Solutions = part1()

def groupedSavings(def solutions, int baseLine) {
    Map grouped = [:]
    solutions.each { vec, time ->
	int saved = baseLine - time
	if(saved >= atLeast)
	    grouped[saved] = grouped.get(saved, 0) + 1
    }

    return grouped
}

def grouped = groupedSavings(p1Solutions, baseLine)
println grouped
println grouped.values().sum()

//Thoughts on part 2:
/*
 It seems like enumerating the new paths will be a pain and take forever
 Not to mention that then trying to solve them will be even worse
 Pre-compute all paths with something like Floyd-Warshall?
 Terminate early?
 Prune areas that I don't need to bother will because there's no way to save enough time to matter?
 */
