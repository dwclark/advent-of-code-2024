import static Aoc.*
import static IntVec.*

final DIRS = [vec(-1,0), vec(0,1), vec(1,0), vec(0,-1)]

def lines = new File('data/06').readLines()
final numRows = lines.size()
final numCols = lines[0].length()

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

grid = Map.copyOf(grid)

def part1 = {
    def turn = 0
    def current = start
    def visited = new HashSet([start])
    
    while(grid[current]) {
	current = current + DIRS[turn % 4]
	def at = grid[current]
	if(at == '.') {
	    visited.add(current)
	}
	else if(at == '#') {
	    current = current - DIRS[turn % 4]
	    ++turn
	}
	else {
	    break
	}
    }

    return visited.size()
}

def part2 = {
    def positions = new HashSet()

    for(int row = 0; row < numRows; ++row) {
	for(int col = 0; col < numCols; ++col) {
	    def pos = vec(row, col)
	    def type = grid[pos]
	    println pos
	    if(type == '#' || pos == start)
		continue
	    
	    def newGrid = new LinkedHashMap(grid)
	    newGrid[pos] = '#'

	    def turn = 0
	    def current = start
	    def newVisits = [:]
	    
	    while(newGrid[current]) {
		if(pos == vec(19,7))
		    println "At 19,7: ${current}"
		
		current = current + DIRS[turn % 4]
		def at = newGrid[current]
		if(at == '.') {
		    //do nothing
		}
		else if(at == '#') {
		    def soFar = newVisits.get(current, new HashSet())
		    if(soFar.contains(DIRS[turn % 4])) {
			positions.add(pos)
			break
		    }
		    else {
			soFar.add(DIRS[turn % 4])
		    }
		    
		    current = current - DIRS[turn % 4]
		    ++turn
		}
		else {
		    break
		}
	    }
	}
    }

    return positions.size()
}

println part2()
