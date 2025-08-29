import static Aoc.*
import static IntVec.vec
import groovy.transform.CompileStatic
import static Tuple.tuple
import groovy.transform.Field

int totalComplexity(Map<String,String> vals) {
    vals.inject(0) { tot, code, motions -> tot += (code.replace('A','') as int) * motions.length() }
}

assert totalComplexity('029A': '<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A',
		       '980A': '<v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A',
		       '179A': '<v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A',
		       '456A': '<v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A',
		       '379A': '<v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A') == 126384

@CompileStatic
enum Motion {
    U('^', vec(-1,0)), D('v', vec(1,0)), L('<', vec(0,-1)), R('>', vec(0,1)), P('A', vec(0,0))

    private Motion(String str, IntVec vec) {
	this.vec = vec
	this.str = str
    }
    
    final IntVec vec
    final String str
    final static List<Motion> all = List.of(Motion.values())

    boolean opposite(Motion that) {
	return ((this.is(U) && that.is(D)) ||
		(this.is(D) && that.is(U)) ||
		(this.is(R) && that.is(L)) ||
		(this.is(L) && that.is(R)))
    }
}

@CompileStatic
class Numeric {
    static final Map<IntVec,String> PAD = [(vec(0,0)): '7', (vec(0,1)): '8', (vec(0,2)): '9',
					   (vec(1,0)): '4', (vec(1,1)): '5', (vec(1,2)): '6',
					   (vec(2,0)): '1', (vec(2,1)): '2', (vec(2,2)): '3',
				           (vec(3,1)): '0', (vec(3,2)): 'A'].asImmutable()
    
    static final Map<String,IntVec> LOOKUP = PAD.collectEntries { key, value -> new MapEntry(value, key) }.asImmutable()
    
    final IntVec position
    final String soFar
    
    Numeric(String start = 'A', String soFar = '') {
	this(LOOKUP[start], soFar)
    }
    
    Numeric(IntVec position, String soFar) {
	this.position = position
	this.soFar = soFar
    }

    IntVec legalMotion(Motion proposed, String goal) {
	final IntVec newPos = position + proposed.vec

	//if it puts us on an illegal square, not allowed
	if(!PAD.containsKey(newPos)) return null

	//Keypress, if on correct square it's legal,
	//otherwise it's illegal
	if(proposed.is(Motion.P)) {
	    if(PAD[position] == goal)
		return newPos
	    else
		return null
	}

	//It's a motion command
	//Does it get us closer to the goal or farther away?
	final IntVec goalPosition = LOOKUP[goal]
	final int currentDistance = position.manhattan(goalPosition)
	final int newDistance = newPos.manhattan(goalPosition)
	if(newDistance < currentDistance)
	    return newPos
	else
	    return null
    }
    
    Tuple2<Numeric,String> move(Motion proposed, String goal) {
	final IntVec newPos = legalMotion(proposed, goal)
	if(!newPos) return null

	if(proposed == Motion.P)
	    return tuple(new Numeric(position, soFar + PAD[position]), PAD[position])
	else
	    return tuple(new Numeric(newPos, soFar), null)
    }

    @Override String toString() {
	return "Over ${PAD[position]}, position: ${position}"
    }
}

@CompileStatic
class Direction {
    static final Map<IntVec,Motion> PAD = [                      (vec(0,1)): Motion.U, (vec(0,2)): Motion.P,
					   (vec(1,0)): Motion.L, (vec(1,1)): Motion.D, (vec(1,2)): Motion.R]
    static final Map<Motion,IntVec> LOOKUP = PAD.collectEntries { key, value -> new MapEntry(value, key) }.asImmutable()

    final IntVec position
    final List<Motion> soFar
    
    Direction() { this(LOOKUP[Motion.P], List.of()) }

    Direction(IntVec position, List<Motion> soFar) {
	this.position = position
	this.soFar = soFar
    }

    IntVec legalMotion(Motion proposed) {
	final IntVec newPos = position + proposed.vec

	//proposed motion puts on an illegal square
	if(!PAD.containsKey(newPos)) return null

	//just started, every move to a legal square is legal
	if(soFar.empty) return newPos

	//if we go the opposition direction from another motion
	//since the last press, we are backtracking needlessly
	for(int i = soFar.size() - 1; i >= 0; --i) {
	    if(soFar[i].is(Motion.P))
		return newPos
	    else if(soFar[i].opposite(proposed))
		return null
	}

	return newPos
    }

    Tuple2<Direction,Motion> move(Motion proposed) {
	final IntVec newPos = legalMotion(proposed)
	if(!newPos) return null

	final Direction newDir = new Direction(newPos, soFar + proposed)
	return tuple(newDir, (proposed == Motion.P) ? PAD[position] : null)
    }

    @Override String toString() {
	return "Over ${PAD[position]}, position: ${position}"
    }
}

@CompileStatic
class Mechanism {
    final List<Motion> moves
    final List<Direction> directions
    final Numeric n
    
    Mechanism(int directionPads, String keypad = 'A', List<Motion> moves = List.of()) {
	this((0..<directionPads).collect { new Direction() }, new Numeric(keypad), moves)
    }
    
    Mechanism(List<Direction> directions, Numeric n, List<Motion> moves) {
	this.directions = directions
	this.n = n
	this.moves = moves
    }

    Tuple2<Mechanism,String> move(Motion m, String goal) {
	List<Direction> newDirections = new ArrayList(directions)
	Motion currentMotion = m
	for(int i = 0; i < newDirections.size(); ++i) {
	    Direction d = newDirections[i]
	    final Tuple2<Direction,Motion> r = d.move(currentMotion)
	    if(r == null) return null
	    newDirections[i] = r.v1
	    if(r.v2) currentMotion = r.v2
	    else return tuple(new Mechanism(newDirections, n, moves + m), null)
	}

	final Tuple2<Numeric,String> r = n.move(currentMotion, goal)
	if(r == null) return null
	else if(!r.v2) return tuple(new Mechanism(newDirections, r.v1, moves + m), null)
	else return tuple(new Mechanism(newDirections, r.v1, moves + m), r.v2)
    }

    Mechanism doMoves(String strMoves, String goal) {
	int index = 0
	Mechanism mechanism = this
	for(String s : strMoves) {
	    Motion motion = Motion.all.find { m -> m.str == s }
	    Tuple2<Mechanism,String> ret = mechanism.move(motion, goal[index])
	    if(ret == null)
		throw new IllegalStateException()
	    
	    mechanism = ret.v1
	    if(ret.v2 == goal[index]) ++index
	}

	return mechanism
    }

    @Override String toString() {
	return moves.collect { it.str }.join('')
    }

    int getComplexity() {
	return (n.soFar.replace('A','') as int) * moves.size()
    }
}

static Mechanism solve(int directionPads, String goal) {
    int index = 0
    LinkedList queue = new LinkedList()
    queue.add(new Mechanism(directionPads))
    while(queue) {
	final Mechanism mechanism = queue.pop()
	for(Motion m : Motion.all) {
	    Tuple2<Mechanism,String> ret = mechanism.move(m, goal[index])
	    if(ret) {
		Mechanism toTest = ret.v1
		String pushed = ret.v2
		if(pushed) {
		    assert pushed == goal[index]
		    if(toTest.n.soFar == goal)
			return toTest
		    else {
			queue.clear()
			++index
			queue.add(toTest)
		    }
		}
		else {
		    queue.add(ret.v1)
		}
	    }
	}
    }
}

static void test() {
    List tests = [[goal: '029A', complexity: 68 * 29,
		   moves: '<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A'],
		  [goal: '980A', complexity: 60 * 980,
		   moves: '<v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A'],
		  [goal: '179A', complexity: 68 * 179,
		   moves: '<v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A'],
		  [goal: '456A', complexity: 64 * 456,
		   moves: '<v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A'],
		  [goal: '379A', complexity: 64 * 379,
		   moves: '<v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A']]

    tests.each { test ->
	Mechanism m = new Mechanism(2).doMoves(test.moves, test.goal)
	assert m.n.soFar == test.goal
	assert m.complexity == test.complexity
    }

    List<Mechanism> solutions = []
    
    tests.each { test ->
	Mechanism solution = solve(test.goal)
	assert solution.complexity == test.complexity
	println "${solution.n.soFar}: ${solution} ${solution.complexity}"
	solutions.add(solution)
    }

    assert solutions.inject(0) { tot, solution -> tot += solution.complexity }
}

@Field final List<String> GOALS = new File('data/21').readLines()

int totalComplexity(int directionPads) {
    GOALS.collect { goal -> solve(directionPads, goal) }.inject(0) { tot, solution -> tot += solution.complexity }
}

printAssert("Part 1:", totalComplexity(2), 188384)

