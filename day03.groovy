import static Aoc.*

static part1(List lines) {
    def regex = ~/mul\((\d+),(\d+)\)/
    def matchers = lines.collect { line -> line =~ regex }
    return matchers.collect { matcher ->  matcher.collect { m -> m[1].toInteger() * m[2].toInteger() }.sum() }.sum()
}

static part2(List lines) {
    def regex = ~/do\(\)|don't\(\)|mul\((\d+),(\d+)\)/
    def matchers = lines.collect { line -> line =~ regex }
    def enabled = true
    def process = { m ->
	if(m[0] == 'do()') enabled = true
	else if(m[0] == "don't()") enabled = false
	
	return (enabled && m[0].startsWith('mul')) ? m[1].toInteger() * m[2].toInteger() : 0
    }

    matchers.collect { matcher -> matcher.collect { m -> process(m) }.sum() }.sum()
}

def lines = new File('data/03').readLines()
printAssert("Part 1:", part1(lines), 173517243,
	    "Part 2:", part2(lines), 100450138)
