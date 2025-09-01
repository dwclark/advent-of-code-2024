import groovy.transform.CompileStatic
import groovy.transform.Field
import static Aoc.*

@CompileStatic
long prune(final long num) {
    return num % 16777216
}

@CompileStatic
long mix(final long secret, final long num) {
    return secret ^ num
}

@CompileStatic
long newSecret(final long secret, Integer rounds = 1) {
    long res = secret
    rounds.times {
	res = prune(mix(res, res * 64L))
	res = prune(mix(res, (long) (res / 32)))
	res = prune(mix(res, res * 2048L))
    }

    return res
}

@Field final List<Long> all = new File('data/22').readLines().collect { it as Long }

printAssert("Part 1:", all.sum { newSecret(it, 2000) }, 17577894908L)
