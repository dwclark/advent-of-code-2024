import static Aoc.*
import static IntVec.vec
import groovy.transform.Field
import java.time.Instant

def parseGrid(def location) {
    def ret = [:]
    new File(location).readLines().eachWithIndex { line, row ->
	line.eachWithIndex { s, col ->
	    ret[vec(row,col)] = s } }
    return Map.copyOf(ret)
}

@Field final grid = parseGrid('data/12')

def bfsRegion(def vec) {
    def region = new HashSet()
    def visited = new HashSet()
    def stack = new LinkedList([vec])
    while(stack) {
	def toTest = stack.pop()
	visited.add toTest
	if(grid[toTest] == grid[vec]) {
	    region.add toTest
	    toTest.crossNeighbors.each { n ->
		if(!visited.contains(n)) {
		    stack.push n
		}
	    }
	}
    }
    
    return region
}

def findRegions() {
    def regions = []
    def visited = new HashSet()

    grid.each { vec, s ->
	if(!visited.contains(vec)) {
	    def region = bfsRegion(vec)
	    visited.addAll(region)
	    regions.add(Set.copyOf(region))
	}
    }
    
    return regions
}

@Field final regions = findRegions()

def perimeter(Collection region) {
    int total
    region.each { e ->
	int tmp = 4
	e.crossNeighbors.each { n ->
	    if(region.contains(n))
		--tmp
	}
	
	total += tmp
    }

    return total
}

def fenceCosts1() {
    regions.sum {region -> (region.size() * perimeter(region)) }
}

def cornersAt(def counts, def region, def v) {
    def compute = { IntVec flank1, IntVec flank2, IntVec diag ->
	if(region.contains(flank1) && region.contains(flank2) && !region.contains(diag))
	    return 1
	
	if(!region.contains(flank1) && !region.contains(flank2) && !region.contains(diag))
	    return 1
	
	if(!region.contains(flank1) && !region.contains(flank2) && region.contains(diag))
	    return 2
	
	return 0
    }
    
    def corners
    if((corners = compute(v.west, v.north, v.northWest))) {
	counts[v] = corners
    }
    
    if((corners = compute(v.north, v.east, v.northEast))) {
	counts[v + vec(0,1)] = corners
    }
    
    if((corners = compute(v.west, v.south, v.southWest))) {
	counts[v + vec(1,0)] = corners
    }
    
    if((corners = compute(v.south, v.east, v.southEast))) {
	counts[v + vec(1,1)] = corners
    }
    
    return counts
}

def corners(def region) {
    def counts = [:]
    region.each { v -> cornersAt(counts, region, v) }
    return counts
}

def fenceCosts2() {
    regions.sum {region -> (region.size() * corners(region).values().sum()) }
}

printAssert("Part 1:", fenceCosts1(), 1473408,
	    "Part 2:", fenceCosts2(), 886364)
