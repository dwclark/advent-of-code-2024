import static Aoc.*
import static IntVec.vec
import groovy.transform.Field

@Field final int xMax = 70
@Field final int yMax = 70
@Field final int maxBytes = 1_024
@Field final List lines = new File('data/18').readLines()
@Field final Set grid = populateGrid()
@Field final IntVec start = vec(0,0)
@Field final IntVec end = vec(xMax, yMax)

IntVec parseLine(int i) {
    def ary = lines[i].split(',')
    return vec(ary[0].toInteger(), ary[1].toInteger())
}

Set populateGrid() {
    def grid = new HashSet()
    (0..xMax).each { x ->
	(0..yMax).each { y -> grid.add(vec(x, y)) } }
    maxBytes.times { i -> assert grid.remove(parseLine(i)) }
    return grid
}

int solve() {
    def frontier = new PriorityQueue({ one, two -> one[1] <=> two[1]})
    def costs = grid.collectEntries { vec -> new MapEntry(vec, Integer.MAX_VALUE) }
    frontier.add([start, 0])
    costs[start] = 0
    
    while(frontier) {
	def (current, heuristic) = frontier.poll()
	if(current == end)
	    return costs[current]

	current.crossNeighbors.each { next ->
	    def cost = costs[current] + 1
	    if(cost < costs[next]) {
		costs[next] = cost
		frontier.add([next, cost + next.manhattan(end)])
	    }
	}
    }

    return -1
}

IntVec part2() {
    for(int i = maxBytes; i < (71 * 71); ++i) {
	IntVec possible = parseLine(i)
	assert grid.remove(possible)
	int solution = solve()
	if(solution == -1)
	    return possible
    }
}

printAssert("Part 1:", solve(), 276,
	    "Part 2:", part2(), vec(60,37))
    
