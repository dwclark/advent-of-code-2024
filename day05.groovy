import static Aoc.*

def parse(def loc) {
    def lines = new File(loc).readLines()
    def orderingRules = []
    def updateRules = []

    lines.each { line ->
	if(line.contains('|'))
	    orderingRules << line.split('\\|').collect { it as int }
	else if(line.contains(','))
	    updateRules << line.split(',').collect { it as int }
    }

    return [ orderingRules, updateRules ]
}

def (orderings, updates) = parse('data/05')

def isGood(def orderings, def update) {
    orderings.every { list ->
	int i1 = update.indexOf(list[0])
	int i2 = update.indexOf(list[1])
	return (i1 == -1 || i2 == -1 || i1 < i2)
    }
}

def reorder(def orderings, def update) {
    def subset = orderings.findAll { o -> update.indexOf(o[0]) != -1 && update.indexOf(o[1]) != -1 }
    //build up list by always looking for who is first
    def ret = []
    while(subset) {
	for(int i = 0; i < update.size(); ++i) {
	    int val = update[i]
	    if(subset.every { sub -> sub[0] == val || (sub[0] != val && sub[1] != val) }) {
		ret << val
		subset = subset.findAll { sub -> sub[0] != val }
		update.remove(i)
		break
	    }
	}

	if(subset.size() == 1) {
	    ret.addAll(subset[0])
	    subset.remove(0)
	}
    }

    return ret
}

def good = updates.findAll { update -> isGood(orderings, update) }
def bad = updates.findAll { update -> !isGood(orderings, update) }
def reordered = bad.collect { update -> reorder(orderings, update) }

printAssert("Part 1:", good.sum { list -> return list[list.size().intdiv(2)] }, 3608,
	    "Part 2:", reordered.sum { list -> return list[list.size().intdiv(2)] }, 4922)

