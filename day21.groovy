import static Aoc.*
import static IntVec.vec
import groovy.transform.Field
/*
 After first perusal, it looks like we need a series of bfs path finders.
 First, find the movements necessary for the actual keybad using bfs, keeping
 track of motions needed (intvec motions). Translate intvec -> keypad motions

 Second, again using bfs on the second pad, use bfs to find the keypad motions,
 keeping track of intvec motions. Translate intvec -> keypad.

 Do this twice more and you should have the sequence of motions needed.

 OK, I was assuming that minimal in each case is independent of other steps.
 It looks like this isn't the case. However, since parts always start on A,
 it looks like each number is independent of other numbers. Assuming they are
 independent looks like it works somewhat, but not always
 */
def numbers = new File("data/21a").readLines()

abstract class Keypad {
    String press(IntVec pos)
    boolean valid(IntVec pos)
}

class Robot {
    final Map<IntVec,String> MOTIONS = ['^': vec(-1,0), 'v': vec(1,0), '<': vec(0,-1), '>': vec(0,1)]
    final List<String> POSSIBLE = ['^', 'v', '<', '>', 'A']
    
    Keypad keypad
    IntVec position
    
    void move(String dir) {
	if(LATERAL.containsKey(dir))
	    position += MOTIONS[dir]
	else
	    keypad.tryPress(position)
    }

    boolean legalMove(String dir) {
	IntVec possible = position + MOTIONS[dir]
	return keypad.valid(possible)
    }
}


class Directional extends Keypad {
    final Map<String,IntVec> PAD = [                 (vec(0,1)): '^', (vec(0,2)): 'A',
				    (vec(1,0)): '<', (vec(1,1)): 'v', (vec(1,2)): '>']

    Robot next

    String press(String s) { next.move(PAD[s]) }
    
    boolean valid(IntVec pos) { return PAD.contains(pos) && next.legalMove(PAD[s]) }
}

class Numeric extends Keypad {
    final Map<String,IntVec> PAD = [(vec(0,0)): '7', (vec(0,1)): '8', (vec(0,2)): '9',
				    (vec(1,0)): '4', (vec(1,1)): '5', (vec(1,2)): '6',
				    (vec(2,0)): '1', (vec(2,1)): '2', (vec(2,2)): '3',
				    (vec(3,1)): '0', (vec(3,2)): 'A']
    
    String tryPress(IntVec pos) {
	return PAD[pos]
    }
}

abstract class Keypad {
    
    IntVec hover
    abstract String push()
    abstract void move(String move)
    abstract boolean legal(String move)
}

class Numeric extends Keypad {
    final Map<String,IntVec> PAD = [(vec(0,0)): '7', (vec(0,1)): '8', (vec(0,2)): '9',
				    (vec(1,0)): '4', (vec(1,1)): '5', (vec(1,2)): '6',
				    (vec(2,0)): '1', (vec(2,1)): '2', (vec(2,2)): '3',
				    (vec(3,1)): '0', (vec(3,2)): 'A']

    Keypad() { hover = vec(3,2) }
    String push() { PAD[hover] }
    boolean legal(String move) { PAD.containsKey(hover + MOTIONS[move]) }
    void move(String move) { hover += MOTIONS[move] }
}

class Directional extends Keypad {
    final IntVec A = vec(1,2)
    final Map<String,IntVec> PAD = [                 (vec(0,1)): '^', (vec(0,2)): 'A',
				    (vec(1,0)): '<', (vec(1,1)): 'v', (vec(1,2)): '>']
    final Keypad next
    Directional(Keypad next) { hover = A; this.next = next }
    String push() {
	if(hover == A)
	    next.push()
	else
	    next.move(PAD[hover])
    }

    void move(String move) { hover += MOTIONS[move] }
	
}

Collection neighborMotions(List prev) {
    if(prev) {
	List<IntVec> ret = [prev[-1]]
	MOTIONS.keySet().each { IntVec vec ->
	    if(vec != prev[-1])
		ret.add(vec)
	}

	return ret
    }
    else MOTIONS.keySet() 
}

List bfs(Map grid, IntVec start, IntVec end) {
    final Set<IntVec> visited = new HashSet()
    final List frontier = new LinkedList()
    frontier.add([start,[]])
    visited.add(start)
    
    while(frontier) {
	def (current, seq) = frontier.poll()
	if(current == end)
	    return seq
	else {
	    neighborMotions(seq).each { motion ->
		IntVec next = current + motion
		if(grid.containsKey(next) && !visited.contains(next)) {
		    frontier.add([next, seq + motion])
		    visited.add(next)
		}
	    }
	}
    }

    throw new UnsupportedOperationException()
}

String translate(List<List<IntVec>> listMotions) {
    listMotions.inject(new StringBuilder()) { StringBuilder sb, List<IntVec> list ->
	list.each { IntVec vec -> sb.append(MOTIONS[vec]) }
	sb.append('A')
    }.toString()
}

String findMotions(Map grid, String sequence) {
    def coord = { String lookFor -> grid.findResult { vec, s -> s == lookFor ? vec : null } }
    List allMotions = []
    for(int i = 0; i < sequence.size() - 1; ++i) {
	IntVec start = coord(sequence[i])
	IntVec end = coord(sequence[i+1])
	List motions = bfs(grid, start, end)
	allMotions.add(motions)
    }

    translate(allMotions)
}

Map pushesNeeded(List<String> numbers) {
    Map<String,String> ret = [:]
    numbers.each { num ->
	def first = findMotions(NUMERIC, 'A' + num)
	def second = findMotions(DIRECTIONAL, 'A' + first)
	ret[num] = findMotions(DIRECTIONAL, 'A' + second)

	println first
	println second
	println ret[num]
    }

    return ret
}

void complexity(Map moves) {
    moves.each { num, seq ->
	println "${num[0..2].toInteger()}: ${seq.length()}"
    }
}
