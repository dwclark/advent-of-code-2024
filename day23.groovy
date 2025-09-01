import groovy.transform.Field
import static Aoc.*
import static Tuple.*

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

boolean allConnected(List<String> strings) {
    for(int i = 0; i < strings.size(); ++i) {
	for(int j = i+1; j < strings.size(); ++j) {
	    if(!CONNECTIONS[strings[i]].contains(strings[j])) {
		return false
	    }
	}
    }
    
    return true
}

Set<String> largest() {
    List<String> soFar = List.of()
    CONNECTIONS.each { key, set ->
	List<String> list = set as List
	list.subsequences().each { subSeq ->
	    if(soFar.size() < (subSeq.size() + 1) && allConnected(subSeq))
		soFar = (subSeq + key).sort()
	}
    }

    return soFar
}

printAssert("Part 1:", containsT(threeConnections()), 1370,
	    "Part 2:", largest().join(','), 'am,au,be,cm,fo,ha,hh,im,nt,os,qz,rr,so')
