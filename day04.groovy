import static Aoc.*
import static groovy.lang.Tuple.tuple


def part1 = { grid ->
    def toAdd = [tuple(-1,-1), tuple(-1,0), tuple(-1, 1),
		 tuple(0,-1), tuple(0,1),
		 tuple(1,-1), tuple(1,0), tuple(1,1)]
    
    def findXmas = { start ->
	toAdd.sum { tup ->
	    def letter = { val -> grid[tuple(start[0] + tup[0]*val, start[1] + tup[1]*val)] }
	    (letter(1) == 'M' && letter(2) == 'A' && letter(3) == 'S') ? 1 : 0
	}
    }

    grid.inject(0) { total, tup, s -> total += ((s == 'X') ? findXmas(tup) : 0) }
}

def part2 = { grid ->
    def findXmas = { start ->
	def first = [tuple(-1,-1), tuple(1,1)]
	def second = [tuple(-1,1), tuple(1,-1)]
	def letter = { tup -> grid[tuple(tup[0] + start[0], tup[1] + start[1])] }
	
	(((letter(first[0]) == 'M' && letter(first[1]) == 'S') ||
	  (letter(first[0]) == 'S' && letter(first[1]) == 'M')) &&
	 ((letter(second[0]) == 'M' && letter(second[1]) == 'S') ||
	  (letter(second[0]) == 'S' && letter(second[1]) == 'M')))
    }

    grid.inject(0) { total, tup, s -> total += ((s == 'A' && findXmas(tup)) ? 1 : 0) }
}

def grid = [:]
lines('data/04').eachWithIndex { line, row ->
    line.eachWithIndex { s, col ->
	grid[tuple(row,col)] = s
    }
}
    
printAssert("Part 1:", part1(grid), 2644,
	    "Part 2:", part2(grid), 1952)
