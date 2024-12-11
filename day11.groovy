import static Aoc.*
import groovy.transform.Immutable
import groovy.transform.Memoized
import groovy.transform.Field

@Field final int max = 75
@Field final str = "3279 998884 1832781 517 8 18864 28 0"
@Field final vals = str.split(' ')

def blink(def list) {
    def ret = []
    list.each { val ->
	if(val == '0') ret.add('1')
	else if(val.length() % 2 == 0) {
	    ret.add(val.substring(0, val.length().intdiv(2)))
	    def second = val.substring(val.length().intdiv(2)).toBigInteger()
	    ret.add(second.toString())
	}
	else ret.add((2024G * val.toBigInteger()).toString())
    }

    return ret
}

@Memoized
def calculate(String str, int height) {
    if(height == max)
	return 1G

    if(str == '0')
	calculate('1', height + 1)
    else if(str.length() % 2 == 0) {
	int mid = str.length().intdiv(2)
	String first = str.substring(0, mid)
	String second = str.substring(mid).toBigInteger().toString()
	return calculate(first, height+1) + calculate(second, height+1)
    }
    else {
	return calculate((2024G * str.toBigInteger()).toString(), height+1)
    }
}

def part1() { (0..<25).toList().inject(vals) { newVals, i -> blink(newVals) }.size() }
def part2() { vals.inject(0G) { tot, s -> tot += calculate(s, 0) } }

printAssert("Part 1:", part1(), 218956G,
	    "Part 2:", part2(), 259593838049805G)
