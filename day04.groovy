import static Aoc.*
import static IntVec.*

def part1 = { grid ->
    final toAdd = [vec(-1,-1), vec(-1,0), vec(-1, 1),
		   vec(0,-1), vec(0,1),
		   vec(1,-1), vec(1,0), vec(1,1)]
    
    def findXmas = { v ->
	toAdd.sum { a ->
	    (grid[v + a] == 'M' && grid[v + (a*2)]  == 'A' && grid[v + (a*3)] == 'S') ? 1 : 0
	}
    }

    grid.inject(0) { total, v, s -> total += ((s == 'X') ? findXmas(v) : 0) }
}

def part2 = { grid ->
    final first = [vec(-1,-1), vec(1,1)]
    final second = [vec(-1,1), vec(1,-1)]

    def findXmas = { v ->
	(((grid[v + first[0]] == 'M' && grid[v + first[1]] == 'S') ||
	  (grid[v + first[0]] == 'S' && grid[v + first[1]] == 'M')) &&
	 ((grid[v + second[0]] == 'M' && grid[v + second[1]] == 'S') ||
	  (grid[v + second[0]] == 'S' && grid[v + second[1]] == 'M')))
    }
    
    grid.inject(0) { total, v, s -> total += ((s == 'A' && findXmas(v)) ? 1 : 0) }
}

def grid = [:]
lines('data/04').eachWithIndex { line, row ->
    line.eachWithIndex { s, col ->
	grid[vec(row,col)] = s
    }
}

printAssert("Part 1:", part1(grid), 2644,
	    "Part 2:", part2(grid), 1952)
