package cn.nukkit.network.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientDecodeAllocationLimitTest {

    // Unsigned VarInt 0x20000000: an ArrayList capacity that requests roughly 2 GiB
    // of references on a 64-bit JVM before the decoder reads any element bytes.
    private static final byte[] TWO_GIB_ITEM_COUNT = {
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x02
    };

    @Test
    void cameraSplineRejectsOversizedTopLevelCountBeforeAllocating() {
        CameraSplinePacket packet = new CameraSplinePacket();
        packet.setBuffer(TWO_GIB_ITEM_COUNT);

        assertThrows(IllegalArgumentException.class, packet::decode);
    }

    @Test
    void voxelShapesRejectsOversizedTopLevelCountBeforeAllocating() {
        VoxelShapesPacket packet = new VoxelShapesPacket();
        packet.setBuffer(TWO_GIB_ITEM_COUNT);

        assertThrows(IllegalArgumentException.class, packet::decode);
    }
}
