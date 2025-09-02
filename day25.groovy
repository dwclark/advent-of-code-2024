import static Aoc.*
import groovy.transform.Field

@Field final Map<String, List<List<String>>> DATA = new File('data/25').readLines().inject([keys:[], locks:[], working:[]]) { accum, line ->
    if(line) {
	accum.working.add(line)
    }
    else {
	def finished = accum.working
	if(finished[0][0] == '#')
	    accum.locks.add(finished)
	else
	    accum.keys.add(finished)

	accum.working = []
    }

    accum
}

boolean fits(List<String> key, List<String> lock) {
    for(int i = 0; i < key.size(); ++i) {
	final String keyRow = key[i]
	final String lockRow = lock[i]
	for(int col = 0; col < keyRow.length(); ++col) {
	    if(keyRow[col] == '#' && lockRow[col] == '#')
		return false
	}
    }

    return true
}

int part1() {
    int total = 0
    DATA.keys.each { List<List<String>> key ->
	DATA.locks.each { List<List<String>> lock ->
	    if(fits(key, lock))
		++total
	}
    }

    return total
}

printAssert("Part 1:", part1(), 2933)
