import static Aoc.*
import static IntVec.vec

def lines = new File('data/08').readLines()

def (graph, s2v) = { ->
    def graph = [:], s2v = [:]
    lines.eachWithIndex { line, row ->
	line.eachWithIndex { s, col ->
	    def v = vec(row,col)
	    graph[v] = s
	    if(s != '.')
		s2v.get(s, []) << v
	}
    }

    return [ Map.copyOf(graph), Map.copyOf(s2v) ]
}()

def p1 = { def all, def v, def diff ->
    def toTest = v + diff
    if(graph.containsKey(toTest)) all.add(toTest)
}

def p2 = { def all, def v, def diff ->
    for(int i = 0; i < Integer.MAX_VALUE; ++i) {
	def toTest = v + (diff * i)
	if(graph.containsKey(toTest))
	    all.add(toTest)
	else
	    break
    }
}

def antiNodes = { def closure ->
    def all = new HashSet()
    def testAndAdd = closure.curry(all)
    
    s2v.each { s, vecs ->
	[ vecs, vecs ].eachCombination { v1, v2 ->
	    if(v1 != v2) {
		testAndAdd(v2, (v2-v1))
		testAndAdd(v1, (v1-v2))
	    }
	}
    }
    
    return all.size()
}

println antiNodes(p1)
println antiNodes(p2)
