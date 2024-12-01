import static Aoc.*

static parse(def file) {
    def list = new File(file).readLines().collect { line -> line.split(/\s+/) }
    return [list.collect { it[0].toInteger() }.sort(),
	    list.collect { it[1].toInteger() }.sort() ]
}

static part1(def one, def two) {
    return (0..<one.size()).sum { i -> Math.abs(one[i] - two[i]) }
}

static part2(def one, def two) {
    def grouped = two.countBy { it }
    one.sum { num -> grouped.containsKey(num) ? (num * grouped[num]) : 0 }
}

def vals = parse('data/01')
printAssert("Part 1: ", part1(*vals), 765748,
	    "Part 2: ", part2(*vals), 27732508)

