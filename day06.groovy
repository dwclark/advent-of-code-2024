import static Aoc.*
import static IntVec.*
import static java.util.concurrent.CompletableFuture.supplyAsync

final DIRS = List.copyOf([vec(-1,0), vec(0,1), vec(1,0), vec(0,-1)])
final lines = new File('data/06').readLines()

def (grid,start) = { ->
    def grid = [:]
    def start
    lines.eachWithIndex { line, row ->
	line.eachWithIndex { s, col ->
	    def ivec = vec(row, col)
	    if(s == '^') {
		start = ivec
		grid[ivec] = '.'
	    }
	    else {
		grid[ivec] = s
	    }
	}
    }

    return [ Map.copyOf(grid), start ]
}()

def part1 = {
    def turn = 0
    def current = start
    def visited = new HashSet([start])
    
    while(grid[current]) {
	current += DIRS[turn]
	def at = grid[current]
	if(at == '.') {
	    visited.add(current)
	}
	else if(at == '#') {
	    current -= DIRS[turn]
	    turn = (turn + 1) % 4
	}
    }

    return visited.size()
}

def loopAt = { pos ->
    def newGrid = grid + [(pos): '#']
    def turn = 0
    def current = start
    def visits = [:]
    
    while(newGrid[current]) {
	current += DIRS[turn]
	if(newGrid[current] == '#') {
	    def soFar = visits.get(current, new HashSet())
	    if(soFar.contains(DIRS[turn])) {
		return pos
	    }
	    else {
		soFar.add(DIRS[turn])
	    }
	    
	    current -= DIRS[turn]
	    turn = (turn + 1) % 4
	}
    }

    return null
}

def part2 = {
    grid.inject([]) { results, pos, type ->
	(type != '#' && pos != start) ? results << supplyAsync(loopAt.curry(pos)) : results
    }.count { fut ->
	fut.get()
    }
}

printAssert("Part 1:", part1(), 4982,
	    "Part 2:", part2(), 1663)
