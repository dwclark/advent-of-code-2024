import static Aoc.*
import groovy.transform.Field

@Field def re = /p=(\d+),(\d+) v=(-?\d+),(-?\d+)/
@Field final int width = 101
@Field final int height = 103
@Field def text = new File('data/14').text
@Field def matcher = text =~ re
@Field def posVels = matcher.collect { m -> [ x: m[1] as int, y: m[2] as int, vx: m[3] as int, vy: m[4] as int] }

def move(def seconds) {
    def normalize = { val, span ->
	int offset = val % span
	return (offset >= 0) ? offset : span + offset
    }
    
    posVels.collect { p ->
	int rawX = p.x + (seconds * p.vx)
	int rawY = p.y + (seconds * p.vy)
	[x: normalize(rawX, width),
	 y: normalize(rawY, height)]
    }
}

def quadrants(def positions) {
    int midX = width.intdiv(2)
    int midY = height.intdiv(2)
    
    positions.inject([:]) { map, pos ->
	if(pos.x < midX && pos.y < midY) map[1] = map.get(1, 0) + 1
	if(pos.x > midX && pos.y < midY) map[2] = map.get(2, 0) + 1
	if(pos.x < midX && pos.y > midY) map[3] = map.get(3, 0) + 1
	if(pos.x > midX && pos.y > midY) map[4] = map.get(4, 0) + 1
	map
    }
}

def part1() {
    quadrants(move(100)).inject(1) { n, k, v -> n *= v }
}

def part2() {
    def newMoves
    def index
    for(index = 1; index < 100_000; ++index) {
	newMoves = move(index)
	def set = newMoves as Set
	if(newMoves.size() == set.size())
	    break;
    }

    def list = []
    height.times {
	list.add new StringBuilder('.' * width)
    }
	
    newMoves.each { v ->
	list[v.y].setCharAt(v.x, '*' as char)
    }
    
    list.each { println it }
    println()

    return index
}

printAssert("Part 1:", part1(), 219512160,
	    "Part 2:", part2(), 6398)


