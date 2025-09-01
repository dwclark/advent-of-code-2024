import groovy.transform.Field
import static Tuple.*
import static Aoc.*

@Field final List<String> LINES = new File('data/23').readLines()
@Field final Map<String,Set<String>> CONNECTIONS = LINES.inject([:]) { ret, line ->
    String[] tmp = line.split('-')
    ret.get(tmp[0], new HashSet()).add(tmp[1])
    ret.get(tmp[1], new HashSet()).add(tmp[0])
    ret
}

Set<Set<String>> threeConnections() {
    Set<Set<String>> ret = new HashSet()
    CONNECTIONS.each { one, oneConnected ->
	oneConnected.each { two ->
	    def twoConnected = CONNECTIONS[two]
	    twoConnected.each { three ->
		if(oneConnected.contains(three))
		    ret.add(new HashSet([one, two, three]))
	    }
	}
    }
    
    ret
}

int containsT(Set<Set<String>> connections) {
    connections.sum { connection -> connection.sum { it.startsWith('t') ? 1 : 0 } ? 1 : 0 }
}

printAssert("Part1:", containsT(threeConnections()), 1370)
