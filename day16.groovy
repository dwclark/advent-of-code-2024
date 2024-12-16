import static Aoc.*
import static IntVec.*

def parse(String path) {
    def start, end
    def grid = [:]
    new File(path).readLines().eachWithIndex { line, row ->
	line.eachWithIndex { s, col ->
	    if(s == '.') grid[vec(row,col)] = s
	    else if(s == 'S') {
		start = vec(row,col)
		grid[start] = '.'
	    }
	    else if(s == 'E') {
		end = vec(row,col)
		grid[end] = '.'
	    }
	}
    }

    return [start, end, grid]
}

class State implements Comparable<State> {
    IntVec facing
    IntVec location
    int cost
    
    int compareTo(State s) { cost <=> s.cost }
}

def part1(final start, final end, def grid) {
    final movements = [ vec(1,0), vec(-1,0), vec(0,1), vec(0,-1) ]
    def visited = new HashSet()
    def queue = new PriorityQueue([new State(facing: vec(0, 1), location: start, cost: 0)])

    while(queue) {
	def current = queue.poll()
	visited.add current.location
	if(end == current.location)
	    return current.cost

	movements.each { move ->
	    def next = current.location + move
	    if(!visited.contains(next) && grid.containsKey(next)) {
		def cost = current.cost + (current.facing == move ? 1 : 1001)
		queue.add(new State(facing: move, location: next, cost: cost))
	    }
	}
    }
}

println part1(*parse('data/16'))
