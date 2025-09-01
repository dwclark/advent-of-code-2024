import groovy.transform.CompileStatic
import groovy.transform.Field
import static Aoc.*
import java.util.concurrent.CompletableFuture

@CompileStatic
long prune(final long num) {
    return num % 16777216
}

@CompileStatic
long mix(final long secret, final long num) {
    return secret ^ num
}

@CompileStatic
List<Long> secrets(final long start, int rounds = 1) {
    (0..<rounds).inject([start]) { list, ignore ->
	long secret = list[-1]
	secret = prune(mix(secret, secret * 64L))
	secret = prune(mix(secret, (long) (secret / 32)))
	list << prune(mix(secret, secret * 2048L))
    }
}

@CompileStatic
List<List<Map.Entry<List,Long>>> priceChanges(List<List<Long>> all) {
    List<List<Map.Entry<List,Long>>> ret = []
    all.each { List<Long> listSecrets ->
	List<Long> prices = listSecrets.collect { n -> n % 10 }
	List<Map.Entry<List,Long>> changes = []
	for(int i = 4; i < prices.size(); ++i) {
	    List<Long> diffs = []
	    for(int j = i-3; j <=i; ++j) {
		diffs.add(prices[j] - prices[j-1])
	    }

	    long diff = prices[i]
	    Map.Entry<List,Long> toAdd = Map.entry(diffs, prices[i])
	    changes.add(toAdd)
	}

	ret.add(changes)
    }

    return ret
}

@CompileStatic
Long bananas(List<List<Map.Entry<List,Long>>> allChanges) {
    Set<List> keys = new HashSet<>()
    allChanges.each { List<Map.Entry<List,Long>> changes ->
	changes.each { Map.Entry<List,Long> change ->
	    keys.add(change.key)
	}
    }

    List<CompletableFuture<Long>> futures = keys.collect { List key ->
	CompletableFuture.supplyAsync {
	    long total = 0L
	    
	    allChanges.each { List<Map.Entry<List,Long>> changes ->
		for(Map.Entry<List,Long> change : changes) {
		    List diffs = change.key
		    Long price = change.value
		    if(diffs == key) {
			total += price
			break
		    }
		}
	    }
	    
	    return total
	}
    }

    futures.collect { f -> f.get() }.max()
}

@Field final List<Long> initialSecrets = new File('data/22').readLines().collect { it as Long }

List<List<Long>> allSecrets = initialSecrets.inject([]) { list, num -> list << secrets(num, 2000) }
List<List<Map.Entry<List,Long>>> changes = priceChanges(allSecrets)
printAssert("Part 1:", allSecrets.sum { list -> list[-1] }, 17577894908L,
	    "Part 2:", bananas(changes), 1931L)


