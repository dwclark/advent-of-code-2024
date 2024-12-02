import static Aoc.*

def safe = { level ->
    def s = level.toSorted()
    if(s != level && s.reverse() != level) return false
    (0..<s.size()-1).collect { idx -> s[idx+1] - s[idx] }.every { n -> n >=1 && n <= 3 }
}

def withRemoval = { level ->
    if(safe(level)) return true
    (0..<level.size()).any { i -> safe(level[(0..<i)] + level[i+1..<level.size()]) }
}

def levels = new File('data/02').readLines().collect { line -> line.split(' ').collect { it.toInteger() } }

printAssert("Part 1:", levels.count(safe), 483,
	    "Part 2:", levels.count(withRemoval), 528)
