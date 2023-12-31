package edu.tarleton.drduplex.index.plain.persistent;

import edu.tarleton.drduplex.index.MappedFile;
import java.io.IOException;

/**
 * The fourth-level indirect block of positions in the plain (not compressed)
 * persistent TRIE.
 *
 * @author Zdenek Tronicek
 */
public class PPosBlockIndirect4 extends PPosBlock {

    public static final int BLOCK_SIZE = (PPosBlockDirect.LENGTH - 8 - 2 - 2) / 8;
    private int posBlockCount;
    private final PPosBlock[] posBlock = new PPosBlock[BLOCK_SIZE];

    public PPosBlockIndirect4() {
        super(PBlockType.INDIRECT4);
    }

    public PPosBlockIndirect4(long id) {
        super(id, PBlockType.INDIRECT4);
    }

    public PPosBlockIndirect4(PPosBlock pb) {
        super(PBlockType.INDIRECT4);
        posBlock[0] = pb;
        posBlockCount++;
    }

    @Override
    public boolean addPos(PPos pos) {
        if (posBlockCount == 0) {
            posBlock[0] = new PPosBlockIndirect3();
            posBlock[0].addPos(pos);
            posBlockCount++;
            return true;
        }
        if (posBlockCount == posBlock.length) {
            return posBlock[posBlockCount - 1].addPos(pos);
        }
        if (posBlock[posBlockCount - 1].addPos(pos)) {
            return true;
        }
        posBlock[posBlockCount] = new PPosBlockIndirect3();
        posBlock[posBlockCount].addPos(pos);
        posBlockCount++;
        return true;
    }

    @Override
    public int countPositions() {
        int posCount = 0;
        for (int i = 0; i < posBlockCount; i++) {
            posCount += posBlock[i].countPositions();
        }
        return posCount;
    }

    @Override
    public PPos[] getPositions() {
        PPos[] pp = new PPos[0];
        for (int i = 0; i < posBlockCount; i++) {
            PPos[] pp2 = posBlock[i].getPositions();
            pp = merge(pp, pp2);
        }
        return pp;
    }

    @Override
    public PPos[] getPositions(int minSize, int maxSize) {
        PPos[] pp = new PPos[0];
        for (int i = 0; i < posBlockCount; i++) {
            PPos[] pp2 = posBlock[i].getPositions(minSize, maxSize);
            pp = merge(pp, pp2);
        }
        return pp;
    }

    private PPos[] merge(PPos[] pp1, PPos[] pp2) {
        PPos[] pp = new PPos[pp1.length + pp2.length];
        System.arraycopy(pp1, 0, pp, 0, pp1.length);
        System.arraycopy(pp2, 0, pp, pp1.length, pp2.length);
        return pp;
    }

    @Override
    public void readFrom(Storage storage) throws IOException {
        MappedFile posFile = storage.getPosFile();
        readHeaderFrom(posFile);
        posBlockCount = posFile.readShort();
        for (int i = 0; i < posBlockCount; i++) {
            long pbId = posFile.readLong();
            posBlock[i] = new PPosBlockIndirect3(pbId);
        }
        for (int i = 0; i < posBlockCount; i++) {
            posBlock[i].readFrom(storage);
        }
    }

    @Override
    public void writeTo(Storage storage) throws IOException {
        MappedFile posFile = storage.getPosFile();
        super.writeHeaderTo(posFile);
        posFile.writeShort((short) posBlockCount);
        for (int i = 0; i < posBlockCount; i++) {
            long pbId = posBlock[i].getId();
            posFile.writeLong(pbId);
        }
        for (int i = 0; i < posBlockCount; i++) {
            posBlock[i].writeTo(storage);
        }
    }

    @Override
    public PPosBlock toUpperLevelPosBlock(PPosBlock pblock) {
        throw new RuntimeException("too many positions");
    }

    @Override
    public void print() {
        System.out.printf("    posBlockId: %d (indirect4)%n", id);
        for (int i = 0; i < posBlockCount; i++) {
            posBlock[i].print();
        }
    }
}
