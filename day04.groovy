import static Aoc.*

def input = lines('data/04')

def grid = [:]
input.eachWithIndex { line, row ->
    line.eachWithIndex { s, col ->
	grid[new Tuple(row,col)] = s
    }
}

def findXmas = { start ->
    def toAdd = [new Tuple(-1,-1), new Tuple(-1,0), new Tuple(-1, 1),
		 new Tuple(0,-1), new Tuple(0,1),
		 new Tuple(1,-1), new Tuple(1,0), new Tuple(1,1)]

    def found = 0
    toAdd.each { tup ->
	def mult = { val -> return new Tuple(start[0] + tup[0]*val, start[1] + tup[1]*val) }
	found += ((grid[mult(1)] == 'M' && grid[mult(2)] == 'A' && grid[mult(3)] == 'S') ? 1 : 0)
    }

    return found
}

def part1 = { ->
    def total = 0
    grid.each { tup, s ->
	if(s == 'X')
	    total += findXmas(tup)
    }

    return total
}

def findXmas2 = { start ->
    def first = [new Tuple(-1,-1), new Tuple(1,1)]
    def second = [new Tuple(-1,1), new Tuple(1,-1)]
    def add = { tup -> new Tuple(tup[0] + start[0], tup[1] + start[1]) }
    
    return (((grid[add(first[0])] == 'M' && grid[add(first[1])] == 'S') ||
	     (grid[add(first[0])] == 'S' && grid[add(first[1])] == 'M')) &&
	    ((grid[add(second[0])] == 'M' && grid[add(second[1])] == 'S') ||
	     (grid[add(second[0])] == 'S' && grid[add(second[1])] == 'M'))) ? 1 : 0
}

def part2 = { ->
    def total = 0
    grid.each { tup, s ->
	if(s == 'A') {
	    total += findXmas2(tup)
	}
    }

    return total
}

printAssert("Part 1:", part1(), 2644,
	    "Part 2:", part2(), 1952)
