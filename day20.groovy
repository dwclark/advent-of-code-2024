import groovy.transform.Field
import java.time.*
import java.util.concurrent.ConcurrentHashMap
import static Aoc.*
import static IntVec.vec
import static java.util.Map.entry
import static java.util.concurrent.CompletableFuture.supplyAsync

/*
 This is the "reading comprehension" day for Advent of Code
 */
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

    //there is only one path, read the directions!, just compute costs now
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

/*
 So I figured out that I failed to read that there is only a single path.
 This means that it's easy to figure out how much you save with a single
 wall skip (they all end up being single wall skips). Just go around the
 maze, find all the skips for a position, compute what you would save by
 doing the skip, then record the amount saved
 */
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

/*
 This one was even worse. I originally interpreted the instructed to mean
 that a cheat ended once I got back to a path. The instructions said:

 "Cheats don't need to use all 20 picoseconds; cheats can last any amount of
 time up to and including 20 picoseconds (but can still only end when the
 program is on normal track). Any cheat time not used is lost; it can't be
 saved for another cheat later."

 I interpreted that to mean that once you got back on track, any leftover
 time was forfeited and you had to compute the savings from that point.
 So I did an implementation where I used a bfs to walk up to 20 spaces
 along the walls and compute the savings once I re-entered. I think I
 actually did this right.

 However, what the instructions meant was that if you were travelling
 on the maze itself during the cheat, you were still using cheat time.
 This dawned on me when I went to reddit and saw that people were using
 manhattan distance to calculate cheats. The insight was that I read it
 wrong, not that manhattan distance is the only way to do it. I did have
 some trouble getting the correct bfs implementation, but I eventually
 did get it right in bfsCheats. cheats() using manhattan distance and
 bfsCheats() using bfs are almost exactly the same speed. See the
 commented out timing code at the bottom.
 */
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

boolean inBounds(final IntVec pos) {
    (pos[0] >= 0 && pos[0] < rows &&
     pos[1] >= 0 && pos[1] < columns)
}

Map bfsCheats(final IntVec pos, final int atLeast) {
    def exits = [:]
    def visited = new HashSet([pos])
    def queue = new LinkedList([entry(pos,0)])
    while(queue) {
	def e = queue.poll()
	def possible = e.key
	def walked = e.value

	if(costs.containsKey(possible)) {
	    int saved = costs[pos] - (costs[possible] + walked)
	    if(saved >= atLeast)
		exits[possible] = saved
	}

	if(walked < 20) {
	    possible.crossNeighbors.each { n ->
		if(inBounds(n) && !visited.contains(n)) {
		    visited.add(n)
		    queue.add(entry(n, walked+1))
		}
	    }
	}
    }

    return exits
}

Map part2(final int atLeast, Closure function) {
    Map saved = [:]
    List tasks = costs.keySet().collect { [it, supplyAsync(function.curry(it, atLeast))] }
    tasks.each { list ->
	def (pos, task) = list
	task.get().each { exit, num ->
	    saved[num] = saved.get(num, 0) + 1
	}
    }
    
    return saved
}

printAssert("Part 1:", part1(100).values().sum(), 1406,
	    "Part 2:", part2(100).values().sum(), 1006101)

/*Instant s1 = Instant.now()
assert part2(100, this.&cheats).values().sum() == 1006101
println "costs: ${Duration.between(s1, Instant.now())}"

Instant s2 = Instant.now()
assert part2(100, this.&bfsCheats).values().sum() == 1006101
println "bfsCosts: ${Duration.between(s2, Instant.now())}"
*/
