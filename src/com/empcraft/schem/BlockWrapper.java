package com.empcraft.schem;

public class BlockWrapper {
    public short x;
    public short y;
    public short z;
    public short id;
    public byte data;

    public BlockWrapper(final short x, final short y, final short z, final short id, final byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}
