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
    static final movements = [ vec(1,0), vec(-1,0), vec(0,1), vec(0,-1) ]
    IntVec facing
    IntVec location
    int cost
    State prev
    
    @Override String toString() { "[location: ${location}, facing: ${facing}, cost: ${cost}]" }
    
    @Override int hashCode() { 31 * location.hashCode() + facing.hashCode() }
    
    @Override boolean equals(Object o) {
	if(o instanceof State) {
	    State rhs = (State) o
	    return location == rhs.location && facing == rhs.facing
	}
	else return false
    }

    @Override int compareTo(State s) { cost <=> s.cost }

    State getOpposite() {
	return new State(facing: facing * -1, location: location, cost: cost)
    }
    
    List<State> movements(def grid, def visited) {
	movements.findResults { IntVec m ->
	    final IntVec newLoc = location + m
	    if(!grid.containsKey(newLoc))
		return null
	    
	    if(m == facing) {
		State p = new State(facing: m, location: newLoc, cost: cost + 1, prev: this)
		return visited.contains(p) ? null : p
	    }
	    else {
		State p = new State(facing: m, location: location, cost: cost + 1000, prev: this)
		return visited.contains(p) || visited.contains(p.opposite) ? null : p
	    }
	}
    }
}

def solve(final start, final end, def grid) {
    def visited = new HashSet()
    def queue = new PriorityQueue([new State(facing: vec(0, 1), location: start, cost: 0, prev: null)])
    def cost = null
    def endStates = []
    
    while(queue) {
	def current = queue.poll()
	if(cost && cost < current.cost)
	    break
	
	visited.add current
	if(end == current.location) {
	    if(cost == null) {
		cost = current.cost
	    }

	    endStates.add current
	}

	queue.addAll(current.movements(grid, visited))
    }

    return [cost, endStates]
}

def allNodes(List<State> endStates) {
    def ret = new HashSet()
    endStates.each { State s ->
	State current = s
	while(current) {
	    ret.add(current.location)
	    current = current.prev
	}
    }

    return ret
}

def (cost, endStates) = solve(*parse('data/16'))
printAssert("Part 1:", cost, 98520,
	    "Part 2:", allNodes(endStates).size(), 609)
