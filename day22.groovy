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
List<Map<List,Long>> priceChanges(List<List<Long>> all) {
    List<Map<List,Long>> ret = []
    all.each { List<Long> listSecrets ->
	Map<List,Long> changes = [:]
	List<Long> prices = listSecrets.collect { n -> n % 10 }
	for(int i = 4; i < prices.size(); ++i) {
	    List<Long> diffs = []
	    for(int j = i-3; j <=i; ++j) {
		diffs.add(prices[j] - prices[j-1])
	    }

	    Long diff = prices[i]
	    if(!changes.containsKey(diffs))
		changes[diffs] = diff
	}

	ret.add(changes)
    }

    return ret
}

@CompileStatic
Long bananas(List<Map<List,Long>> allChanges) {
    Map<List,Long> sums = [:]
    allChanges.each { Map<List,Long> changes ->
	changes.each { List key, Long change -> sums[key] = sums.get(key, 0L) + change } }

    sums.values().max()
}

@Field final List<Long> initialSecrets = new File('data/22').readLines().collect { it as Long }

List<List<Long>> allSecrets = initialSecrets.inject([]) { list, num -> list << secrets(num, 2000) }
List<Map<List,Long>> changes = priceChanges(allSecrets)
printAssert("Part 1:", allSecrets.sum { list -> list[-1] }, 17577894908L,
	    "Part 2:", bananas(changes), 1931L)
