import static Aoc.*
import static IntVec.vec

def parse(def path, def replacer = { s -> s }) {
    def warehouse = [:]
    def directions = []
    def start
    def lines = new File(path).readLines()
    def index = 0
    while(true) {
	def line = lines[index].trim().collect(replacer).flatten()
	line.eachWithIndex { s, col ->
	    warehouse[vec(index, col)] = s
	    if(s == '@') start = vec(index, col)
	}

	++index
	if(!lines[index].trim()) {
	    ++index
	    break
	}
    }

    while(index < lines.size()) {
	lines[index++].trim().each { s -> directions.add(s) }
    }

    return [warehouse, start, directions]
}

def move1(Map warehouse, IntVec motion, IntVec pos, String s) {
    if(warehouse[pos] == '.') {
	warehouse[pos] = s
	return true
    }
    else if(warehouse[pos] == '#') {
	return false
    }
    else if(warehouse[pos] == 'O') {
	//optimistically assume we can
	warehouse[pos] = s
	return move1(warehouse, motion, pos + motion, 'O')
    }
    else throw new IllegalStateException("warehouse[pos]: ${warehouse[pos]}")
}

def move2(Map warehouse, IntVec motion, IntVec pos, String s) {
    if(warehouse[pos] == '.') {
	warehouse[pos] = s
	return true
    }
    else if(warehouse[pos] == '#') {
	return false
    }
    else if(warehouse[pos] == ']') {
	def pair = pos + vec(0, -1)
	warehouse[pos] = s
	warehouse[pair] = '.'
	return (move2(warehouse, motion, pos + motion, ']') &&
		move2(warehouse, motion, pair + motion, '['))
    }
    else if(warehouse[pos] == '[') {
	def pair = pos + vec(0, 1)
	warehouse[pos] = s
	warehouse[pair] = '.'
	return (move2(warehouse, motion, pos + motion, '[') &&
		move2(warehouse, motion, pair + motion, ']'))
    }
    else throw new IllegalStateException("warehouse[pos]: ${warehouse[pos]}")
}

def moveAll(Map warehouse, IntVec start, List directions, Closure closure) {
    final MOTIONS = [ '>': vec(0, 1), '<': vec(0, -1), '^': vec(-1, 0), 'v': vec(1, 0)]
    def current = start
    def currentWarehouse = warehouse
    directions.each { dir ->
	final copy = new LinkedHashMap(currentWarehouse)
	final motion = MOTIONS[dir]
	if(closure(copy, motion, current + motion, '@')) {
	    copy[current] = '.'
	    currentWarehouse = copy
	    current += motion
	}
    }

    return currentWarehouse
}

def sumGps(Map warehouse, String lookFor) {
    warehouse.inject(0) { tot, pos, s ->
	tot += (s == lookFor) ? (pos[0] * 100) + pos[1] : 0
    }
}

def replaceDay2 = { s ->
    if(s == '.') return ['.','.']
    else if(s == '#') return ['#','#']
    else if(s == 'O') return ['[',']']
    else if(s == '@') return ['@','.']
}

printAssert("Part 1:", sumGps(moveAll(*parse('data/15'), this.&move1), 'O'), 1516281,
	    "Part 2:", sumGps(moveAll(*parse('data/15', replaceDay2), this.&move2), '['), 1527969)
