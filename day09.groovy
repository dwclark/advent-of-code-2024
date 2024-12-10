import static Aoc.*

class Block {
    int id, length, free, copied
    List<Integer> addedIds = []
    
    int getRemaining() { length - copied }
    int getSpace() { free - addedIds.size() }

    @Override
    String toString() { "(${id},${length},${free})" }
}

List<Block> readBlocks(String line) {
    def ret = []
    int id = 0, index = 0
    for(; index < line.length() - 1; ++id, index += 2) {
	ret << new Block(id: id, length: line[index].toInteger(), free: line[index+1].toInteger())
    }

    ret += new Block(id: id, length: line[index].toInteger(), free: 0)
}

long checkSum(List<Block> blocks) {
    long total = 0L
    int index = 0
    for(Block block in blocks) {
	block.length.times { idx ->
	    total += (index++) * (idx < block.remaining ? block.id : 0)
	}
	
	block.free.times { idx ->
	    total += (index++) * (idx < block.addedIds.size() ? block.addedIds[idx] : 0)
	}
    }

    return total
}

List<Block> copy1(List<Block> blocks) {
    int forward = 0
    int reverse = blocks.size() - 1
    
    while(forward < reverse) {
	Block block = blocks[forward]

	while(block.space && forward < reverse) {
	    Block copyBlock = blocks[reverse]
	    if(copyBlock.remaining) {
		block.addedIds.add(copyBlock.id)
		++copyBlock.copied
	    }
	    else {
		--reverse
	    }
	}

	++forward
    }

    return blocks
}

List<Block> copy2(List<Block> blocks) {
    for(int reverse = blocks.size() - 1; reverse > 0; --reverse) {
	Block current = blocks[reverse]
	for(int forward = 0; forward < reverse; ++forward) {
	    Block target = blocks[forward]
	    if(current.length <= target.space) {
		current.copied += current.length
		current.length.times { target.addedIds.add(current.id) }
		break
	    }
	}
    }

    return blocks
}

final String line = new File('data/09').text
printAssert("Part 1:", checkSum(copy1(readBlocks(line))), 6607511583593L,
	    "Part 2:", checkSum(copy2(readBlocks(line))), 6636608781232L)
