import static Aoc.*
import static IntVec.vec
import static java.util.concurrent.ThreadLocalRandom.current
import groovy.transform.Field

class Robot {
    IntVec pos
    IntVec vel
}

@Field final int width = 101
@Field final int height = 103
@Field List<Robot> robots = parse('data/14')

def parse(String path) {
    final re = /p=(\d+),(\d+) v=(-?\d+),(-?\d+)/
    def text = new File(path).text
    def matcher = text =~ re
    robots = matcher.inject([]) { list, m -> list << new Robot(pos: vec(m[1] as int, m[2] as int),
							       vel: vec(m[3] as int, m[4] as int)) }
}

def normalize(int xraw, int yraw) {
    final int xoffset = xraw % width
    final int yoffset = yraw % height
    return vec(xoffset >= 0 ? xoffset : width + xoffset,
	       yoffset >= 0 ? yoffset : height + yoffset)
}

def move(def seconds) {
    robots.collect { r -> normalize(r.pos[0] + (r.vel[0] * seconds), r.pos[1] + (r.vel[1] * seconds)) } 
}

def quadrants(def positions) {
    int midX = width.intdiv(2)
    int midY = height.intdiv(2)
    
    positions.inject([:]) { map, pos ->
	int x = pos[0]
	int y = pos[1]
	if(x < midX && y < midY) map[1] = map.get(1, 0) + 1
	if(x > midX && y < midY) map[2] = map.get(2, 0) + 1
	if(x < midX && y > midY) map[3] = map.get(3, 0) + 1
	if(x > midX && y > midY) map[4] = map.get(4, 0) + 1
	map
    }
}

def part1() {
    quadrants(move(100)).inject(1) { n, q, c -> n *= c }
}

def clumpy(List<IntVec> positions, int trials, int threshold) {
    def toTry = []
    trials.times {
	int rand = current().nextInt(positions.size())
	toTry += rand
    }

    def set = positions as Set

    for(int index : toTry) {
	def visited = new HashSet()
	def found = new HashSet()
	def toVisit = new LinkedList([positions[index]])
	IntVec current

	while(toVisit && found.size() < threshold) {
	    current = toVisit.pop()
	    found.add current
	    visited.add current
	    
	    current.crossNeighbors.each { n ->
		if(set.contains(n) && !visited.contains(n))
		    toVisit.push(n)
	    }
	}

	if(found.size() >= threshold) return true
    }

    return false
}

def findClumpy() {
    for(index = 1; index < 100_000; ++index) {
	def positions = move(index)
	if(clumpy(positions, 50, 30))
	    return [index, positions]
    }

    return null
}

def part2() {
    def (index, found) = findClumpy()

    def list = []
    height.times { list.add new StringBuilder('.' * width) }
    found.each { v -> list[v[1]].setCharAt(v[0], '*' as char) }
    
    list.each { println it }
    println()

    return index
}

printAssert("Part 1:", part1(), 219512160,
	    "Part 2:", part2(), 6398)
