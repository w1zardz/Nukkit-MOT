package cn.nukkit.network.protocol;

import cn.nukkit.GameVersion;
import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.Test;
import cn.nukkit.network.protocol.netease.SyncSkinPacket;

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

    @Test
    void netEaseSkinRejectsOversizedEntryCountBeforeAllocating() {
        SyncSkinPacket packet = new SyncSkinPacket();
        packet.setBuffer(TWO_GIB_ITEM_COUNT);

        assertThrows(IllegalArgumentException.class, packet::decode);
    }

    @Test
    void legacySubChunkRequestRejectsTooManyOffsetsBeforeBuildingObjects() {
        BinaryStream stream = new BinaryStream();
        stream.putVarInt(0);
        stream.putSignedBlockPosition(new BlockVector3(0, 0, 0));
        stream.putLInt(8193);

        SubChunkRequestPacket packet = new SubChunkRequestPacket();
        packet.protocol = ProtocolInfo.v1_20_0;
        packet.gameVersion = GameVersion.byProtocol(packet.protocol, false);
        packet.setBuffer(stream.getBuffer());

        assertThrows(IllegalArgumentException.class, packet::decode);
    }

    @Test
    void modernSubChunkRequestRejectsUnsignedOverflowBeforeBuildingObjects() {
        BinaryStream stream = new BinaryStream();
        stream.putVarInt(0);
        stream.putUnsignedVarInt(0xFFFFFFFFL);

        SubChunkRequestPacket packet = new SubChunkRequestPacket();
        packet.protocol = ProtocolInfo.v1_26_30;
        packet.gameVersion = GameVersion.byProtocol(packet.protocol, false);
        packet.setBuffer(stream.getBuffer());

        assertThrows(IllegalArgumentException.class, packet::decode);
    }

    @Test
    void mapInfoRejectsMorePixelsThanOneVanillaMap() {
        BinaryStream stream = new BinaryStream();
        stream.putVarLong(0);
        stream.putLInt(128 * 128 + 1);

        MapInfoRequestPacket packet = new MapInfoRequestPacket();
        packet.protocol = ProtocolInfo.v1_19_20;
        packet.gameVersion = GameVersion.byProtocol(packet.protocol, false);
        packet.setBuffer(stream.getBuffer());

        assertThrows(IllegalArgumentException.class, packet::decode);
    }
}
