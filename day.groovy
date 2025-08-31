import groovy.transform.CompileStatic
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.transform.Immutable
import static IntVec.vec
import static Tuple.tuple

@CompileStatic
enum Mapping {
    NUMERIC('7': vec(0,0), '8': vec(0,1), '9': vec(0,2),
	    '4': vec(1,0), '5': vec(1,1), '6': vec(1,2),
	    '1': vec(2,0), '2': vec(2,1), '3': vec(2,2),
	    '0': vec(3,1), 'A': vec(3,2)),
	DIRECTION('^': vec(0,1), 'A': vec(0,2),
		  '<': vec(1,0), 'v': vec(1,1), '>': vec(1,2)),
	MOTION('^': vec(-1,0), 'v': vec(1,0),
	       '<': vec(0,-1), '>': vec(0,1))
    
    Mapping(Map<String,IntVec> info) {
	this.info = info.asImmutable()
	this.lookup = info.collectEntries { k, v -> new MapEntry(v, k) }.asImmutable()
    }

    final Map<String,IntVec> info
    final Map<IntVec,String> lookup
}

//based on what previous robot has to spend to actually arrive at it starting from A
//I managed to fill in numbers that get the correct result for the examples, but not for my data. :(
@Field Map<String,Integer> COSTS = [ '^': 4, 'A': 1, '<': 6, 'v': 5, '>': 3 ]
@Field IntVec NO_MOTION = vec(0,0)

//cost model is most important
//stay on square (emit same key cost: 0, move cost: manhattan distance to move)
//but move must still be legal!
@Immutable(knownImmutableClasses=[IntVec])
@CompileStatic
class State implements Comparable<State> {
    int cost
    IntVec at
    List<IntVec> motions
    int compareTo(State rhs) { return cost <=> rhs.cost }
}

@CompileStatic @Memoized
String solve(String strStart, String strEnd, Mapping mapping) {
    final IntVec PUSH = vec(0,0)
    final IntVec GOAL = mapping.info[strEnd]
    Queue<State> queue = new PriorityQueue<>()
    queue.add(new State(cost: 0, at: mapping.info[strStart], motions: List.of()))
    
    while(queue) {
	final State current = queue.poll()
	if(current.at == GOAL) {
	    return current.motions.collect { v -> Mapping.MOTION.lookup[v] }.join('') + 'A'
	}
	
	Mapping.MOTION.info.each { String str, IntVec motion ->
	    final IntVec previousMotion = current.motions ? current.motions[-1] : NO_MOTION
	    final IntVec newAt = current.at + motion
	    final int cost = (motion == previousMotion) ? 0 : COSTS[str]
	    if(mapping.lookup[newAt] && newAt.manhattan(GOAL) < current.at.manhattan(GOAL)) {
		queue.add(new State(cost: current.cost + cost, at: newAt, motions: current.motions + motion))
	    }
	}
    }
}

StringBuilder solveString(StringBuilder str, int levels, int level) {
    final Mapping mapping = (levels == level) ? Mapping.NUMERIC : Mapping.DIRECTION
    
    final StringBuilder sb = new StringBuilder()
    for(int i = 0; i < str.length(); ++i) {
	String first = (i == 0) ? 'A' : str[i-1]
	String second = str[i]
	sb.append(solve(first, second, mapping))
    }

    if(level == 0)
	return sb
    else
	return solveString(sb, levels, level - 1)
}

assert solveString(new StringBuilder('029A'), 2, 2).length() == 68
assert solveString(new StringBuilder('980A'), 2, 2).length() == 60
assert solveString(new StringBuilder('179A'), 2, 2).length() == 68
assert solveString(new StringBuilder('456A'), 2, 2).length() == 64
assert solveString(new StringBuilder('379A'), 2, 2).length() == 64

//println solveString(new StringBuilder('0'), 20, 20).length()

@Field int levels

@Memoized
String solveLevel(String s1, String s2, int level) {
    final Mapping mapping = (level == levels) ? Mapping.NUMERIC : Mapping.DIRECTION
    
    if(level > 0) {
	String accum = ''
	final String solution = solve(s1, s2, mapping)
	for(int j = 0; j < solution.length(); ++j) {
	    accum += solveLevel(solution[j-1], solution[j], level-1)
	}

	return accum
    }
    else {
	return solve(s1, s2, mapping)
    }
}

String solveLevels(String code, int totalLevels) {
    levels = totalLevels
    long total = 0L
    String str = 'A' + code
    String accum = ''
    for(int i = 1; i < str.length(); ++i) {
	accum += solveLevel(str[i-1], str[i], levels)
    }
    
    return accum
}

/*assert solveLevels('029A', 2) == 68L
assert solveLevels('980A', 2) == 60L
assert solveLevels('179A', 2) == 68L
assert solveLevels('456A', 2) == 64L
assert solveLevels('379A', 2) == 64L
 */

@Field final List<String> GOALS = /*['029A', '980A', '179A', '456A', '379A']*/ new File('data/21').readLines()

void solveGoals(int levels) {
    GOALS.each { code ->
	println "${code}: ${solveString(new StringBuilder(code), 2, 2)}"
	//tot += (solveLevels(code, levels) * (code.replace('A', '') as long))
    }
}

solveGoals(2)
