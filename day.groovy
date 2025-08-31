import groovy.transform.CompileStatic
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.transform.Immutable
import static IntVec.vec
import static Tuple.tuple

/*
 OK, I looked at the advent of code reddit, but I think the advice that I read there
 was pretty bad. The key to this will likely be using Dijkstra's on the numeric
 keypad, but the cost function is going to be based on all of the directional keypads
 stacked above the number keypad. In part 1, this means recursion through 3 keypads
 while in part 2, recursion through 26 keypads. However, since all keypads above the first
 will always start at the 'A', meaning a lot of what happens can be cached.

 The cost of a move will be the number of presses at the top layer. So the complexity
 of a given set of motions will be total cost * code.

 May also have to do some DFS path enumeration to cache paths instead of always
 figuring them out at each stage...or maybe not, just solve cheapest each time.
 Caching should smooth over slow runtimes.
 */

@CompileStatic
enum Mapping {
    NUMERIC('7': vec(0,0), '8': vec(0,1), '9': vec(0,2),
	    '4': vec(1,0), '5': vec(1,1), '6': vec(1,2),
	    '1': vec(2,0), '2': vec(2,1), '3': vec(2,2),
	    '0': vec(3,1), 'A': vec(3,2)),
	DIRECTION('^': vec(0,1), 'A': vec(0,2),
		  '<': vec(1,0), 'v': vec(1,1), '>': vec(1,2)),
	MOTION('^': vec(-1,0), 'v': vec(1,0),
	       '<': vec(0,-1), '>': vec(0,1), 'A': vec(0,0))
    
    Mapping(Map<String,IntVec> info) {
	this.info = info.asImmutable()
	this.lookup = info.collectEntries { k, v -> new MapEntry(v, k) }.asImmutable()
    }

    final Map<String,IntVec> info
    final Map<IntVec,String> lookup

    boolean legal(IntVec vec) { lookup.containsKey(vec) }
}

@Field int LEVELS = 2

@Immutable(knownImmutableClasses=[IntVec])
@CompileStatic
class State implements Comparable<State> {
    long cost 
    IntVec at
    String directions
    List<IntVec> goals
    
    int compareTo(State rhs) { return cost <=> rhs.cost }

    static State create(String code) {
	return new State(cost: 0L, at: Mapping.NUMERIC.info['A'],
			 directions: 'A',
			 goals: code.collect { s -> Mapping.NUMERIC.info[s] })
    }
}

@Memoized @CompileStatic
List<String> activationPaths(final String from, final String to) {
    final IntVec goal = Mapping.DIRECTION.info[to]
    final LinkedList<Tuple2<IntVec,String>> stack = new LinkedList<>([tuple(Mapping.DIRECTION.info[from], '')])
    final Set<String> discovered = new HashSet<>()
    final List<String> ret = []
    
    while(stack) {
	Tuple2<IntVec,String> step = stack.pop()
	if(step.v1 == goal) {
	    ret.add(step.v2 + 'A')
	}
	else {
	    Mapping.MOTION.info.each { String s, IntVec move ->
		final IntVec newAt = step.v1 + move
		if(Mapping.DIRECTION.legal(newAt) &&
		   newAt.manhattan(goal) < step.v1.manhattan(goal)) {
		    stack.push(tuple(newAt, step.v2 + s))
		}
	    }
	}
    }

    return ret
}

@CompileStatic @Memoized
long computeCost(String start, String goal, int level) {
    List<String> paths = activationPaths(start, goal)
    List<Long> possible
    
    if(level+1 == LEVELS) {
	possible = paths.collect { String path -> (long) path.length() }
    }
    else {
	possible = []
	for(String path : paths) {
	    long pathTotal = 0L
	    for(int i = 0; i < path.length(); ++i) {
		pathTotal += computeCost(i==0 ? 'A' : path[i-1], path[i], level + 1)
	    }

	    possible.add(pathTotal)
	}
    }

    return possible.min()
}

@CompileStatic @Memoized
long solve(String code) {
    final IntVec PUSH = vec(0,0)
    final Queue<State> queue = new PriorityQueue<>()
    queue.add(State.create(code))
    
    while(queue) {
	final State current = queue.poll()
	if(!current.goals) {
	    println "${current.directions} cost: ${current.cost}"
	    return current.cost * (code.replace('A','') as long)
	}
	
	Mapping.MOTION.info.each { String str, IntVec motion ->
	    if(current.at == current.goals[0] && motion == PUSH) {
		queue.add(new State(cost: current.cost + computeCost(current.directions[-1], str, 0),
				    at: current.at,
				    directions: current.directions + str,
				    goals: current.goals.tail()))
	    }

	    if(current.at != current.goals[0] && motion != PUSH) {
		final IntVec newAt = current.at + motion
		if(Mapping.NUMERIC.legal(newAt) &&
		   newAt.manhattan(current.goals[0]) < current.at.manhattan(current.goals[0])) {
		    queue.add(new State(cost: current.cost + computeCost(current.directions[-1], str, 0),
					at: newAt,
					directions: current.directions + str,
					goals: current.goals))
		}
	    }
	}
    }
}

/*println activationPaths('A', '>')
println activationPaths('A', 'v')
println activationPaths('<', 'A')
println activationPaths('v', '^')
println activationPaths('^', 'v')
println activationPaths('<', '<')
println activationPaths('v', 'A')*/


println solve('029A')
