import static Aoc.*

def levels = new File('data/02').readLines().collect { line -> line.split(' ').collect { it.toInteger() } }

def safe = { level ->
    def sorted = level.toSorted()
    def reversed = sorted.reverse()
    if(sorted != level && reversed != level) {
	return false
    }
    
    for(int i = 0; i < sorted.size() - 1; ++i) {
	int diff = sorted[i+1] - sorted[i]
	if(diff < 1 || diff > 3) {
	    return false
	}
    }
    
    return true
}

def part1 = { all -> 
    all.count(safe)
}

def withRemoval = { level ->
    if(safe(level)) return true
    for(int i = 0; i < level.size(); ++i) {
	def copy = new ArrayList(level)
	copy.remove(i)
	if(safe(copy))
	    return true
    }

    return false
}
    

def part2 = { all -> all.count(withRemoval) }
    

println part2(levels)
