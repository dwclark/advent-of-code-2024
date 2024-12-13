@Grab(group='org.apache.commons', module='commons-math3', version='3.6.1')
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import static Aoc.*

def re = /Button A: X\+(\d+), Y\+(\d+)\R/ +
    /Button B: X\+(\d+), Y\+(\d+)\R/ +
    /Prize: X=(\d+), Y=(\d+)/

def solve(List listMatrix, List listVec) {
    def matrix = new Array2DRowRealMatrix(listMatrix as double[][])
    def vec = new ArrayRealVector(listVec as double[])
    
    def lu = new LUDecomposition(matrix)
    lu.solver.solve(vec).toArray() as List
}

def text = new File("data/13").text
def matcher = text =~ re
def equations = matcher.collect { match ->
    [[[match[1], match[3]], [match[2], match[4]]],
     [match[5], match[6]]]
}

def toInts(List<Double> list) {
    def r0 = Math.round(list[0])
    def r1 = Math.round(list[1])
    if(Math.abs(list[0] - r0) < 0.0001d &&
       Math.abs(list[1] - r1) < 0.0001d)
	return [r0,r1]
    else
	return [0,0]
}

def part1(def equations) {
    equations.sum { system ->
	def solution = toInts(solve(system[0], system[1]))
	3 * solution[0] + solution[1]
    }
}

def part2(def equations) {
    def augment = 10000000000000d
    equations.sum { system ->
	def sys1 = [ augment + system[1][0].toDouble(), augment + system[1][1].toDouble() ]
	def solution = toInts(solve(system[0], sys1))
	3 * solution[0] + solution[1]
    }
}

printAssert("Part 1:", part1(equations), 28138,
	    "Part 2:", part2(equations), 108394825772874L)


