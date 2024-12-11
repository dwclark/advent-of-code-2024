import static Aoc.*
import groovy.transform.Immutable
//def str = "125 17"
def str = "3279 998884 1832781 517 8 18864 28 0"
//def str = "3279" //partitioning won't help, looks like num unique values grows slowly
def vals = str.split(' ') as List

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

def part1(def newVals) {
    35.times { i ->
	newVals = blink(newVals)
	println("${i}: ${(newVals as Set).size()}")
    }

    return newVals
}

//def ret = part1(vals)

@Immutable
class LevelPair {
    String str
    int level
}

def cache = [:]
def max = 75
def calculate

calculate = { LevelPair pair ->
    def found = cache[pair]

    if(found != null) return found

    if(pair.level == max) {
	cache[pair] = 1G
	return 1G
    }
    else {
	if(pair.str == '0') {
	    LevelPair newPair = new LevelPair('1', pair.level+1)
	    cache[newPair] = calculate(newPair)
	    return cache[newPair]
	}
	else if(pair.str.length() % 2 == 0) {
	    def v = pair.str
	    def mid = v.length().intdiv(2)
	    def first = v.substring(0, mid)
	    LevelPair pair1 = new LevelPair(first, pair.level+1)
	    cache[pair1] = calculate(pair1)
	    def second = v.substring(mid).toBigInteger().toString()
	    LevelPair pair2 = new LevelPair(second, pair.level+1)
	    cache[pair2] = calculate(pair2)
	    return cache[pair1] + cache[pair2]
	}
	else {
	    LevelPair newPair = new LevelPair((2024G * pair.str.toBigInteger()).toString(),
					      pair.level+1)
	    cache[newPair] = calculate(newPair)
	    return cache[newPair]
	}
    }
}

def pairs = str.split(' ').collect { new LevelPair(it, 0) }
println pairs.inject(0G) { tot, pair -> tot += calculate(pair) }
//println calculate(new LevelPair('0', 0))
