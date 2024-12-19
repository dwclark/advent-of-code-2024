import static Aoc.*
import groovy.transform.Field
import groovy.transform.Memoized

@Field List available
@Field List required

void parse(String path) {
    def lines = new File(path).readLines()
    available = lines[0].split(',').collect { it.trim() }
    required = lines[2..<(lines.size())].collect { it.trim() }
}

parse('data/19')

@Memoized
boolean canForm(String req) {
    if(req == '') return true
    
    available.find { a ->
	req.startsWith(a) && canForm(req.substring(a.length())) }
}

@Memoized
long allForms(String req) {
    return available.sum(0L) { a ->
	if(req.startsWith(a)) {
	    String remaining = req.substring(a.length())
	    if(!remaining) return 1
	    else return allForms(remaining)
	}
	else return 0
    }
}

final List possible = required.findAll { canForm(it) }
printAssert("Part 1:", possible.size(), 324,
	    "Part 2:", possible.sum { allForms(it) }, 575227823167869L)
