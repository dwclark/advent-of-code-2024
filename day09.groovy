import static Aoc.*

class Block {
    int id, length, free
    int movedIn = 0, movedOut = 0

    int getRemaining() { length - movedOut }
    int getSpace() { free - movedIn }

    @Override
    boolean equals(Object rhs) {
	return id == ((Block) rhs).id
    }

    @Override
    String toString() { "(${id},${length},${free})" }
}

def readBlocks(def line) {
    def ret = []
    int id = 0, index = 0
    for(; index < line.length() - 1; ++id, index += 2) {
	ret << new Block(id: id, length: line[index].toInteger(), free: line[index+1].toInteger())
    }

    ret += new Block(id: id, length: line[index].toInteger(), free: 0)
}

def calculateIds(List<Block> blocks) {
    def total = 0G
    int index = 0
    int reverseIndex = -1
    
    def nextCopy = { Block current ->
	while(current != blocks[reverseIndex] && blocks[reverseIndex].remaining == 0)
	    --reverseIndex

	return (current == blocks[reverseIndex]) ? null : blocks[reverseIndex]
    }

    def copied = false
    
    for(Block block in blocks) {
	for(int i = 0; i < block.remaining; ++i) {
	    total += (block.id * index)
	    ++index
	}

	while(block.space) {
	    Block copyBlock = blocks[reverseIndex]
	    if(copyBlock == block)
		return total

	    if(copyBlock.remaining) {
		total += (copyBlock.id * index)
		++block.movedIn
		++copyBlock.movedOut
		++index
	    }
	    else {
		--reverseIndex
	    }
	}
    }

    return total
}

println calculateIds(readBlocks(new File('data/09').text))
