package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.InventorySlot;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.network.game.serverpackets.AbstractMaskPacket;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
    private final L2PcInstance _activeChar;

    private final byte[] _masks = new byte[]
            {
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00
            };

    public ExUserInfoEquipSlot(L2PcInstance cha)
    {
        this(cha, true);
    }

    public ExUserInfoEquipSlot(L2PcInstance cha, boolean addAll)
    {
        _activeChar = cha;

        if (addAll)
        {
            addComponentType(InventorySlot.values());
        }
    }

    @Override
    protected byte[] getMasks()
    {
        return _masks;
    }

    @Override
    protected void onNewMaskAdded(InventorySlot component)
    {
    }

    @Override
    protected void writeImpl()
    {
        writeD(_activeChar.getObjectId());
        writeH(InventorySlot.values().length);
        writeB(_masks);

        final PcInventory inventory = _activeChar.getInventory();
        for (InventorySlot slot : InventorySlot.values())
        {
            if (containsMask(slot))
            {
                writeH(18);
                writeD(inventory.getPaperdollObjectId((int) slot.getSlot()));
                writeD(inventory.getPaperdollItemId((int) slot.getSlot()));
                writeD(inventory.getPaperdollAugmentationId((int) slot.getSlot()));
                writeD(inventory.getPaperdollItemSkinByItemId((int) slot.getSlot()));
            }
        }
    }
}
