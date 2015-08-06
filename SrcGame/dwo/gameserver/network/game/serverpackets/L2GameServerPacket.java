package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.ServerPacketOpCodeManager;
import dwo.gameserver.model.items.ItemInfo;
import dwo.gameserver.network.game.masktypes.IUpdateTypeComponent;
import dwo.gameserver.network.game.masktypes.ItemListType;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.model.items.base.L2WarehouseItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.L2TradeList.*;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.mmocore.SendablePacket;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author KenM
 */

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	public static final Logger _log = LogManager.getLogger(L2GameServerPacket.class);

	protected boolean _invisible;

    private static final int[] PAPERDOLL_ORDER = new int[]
            {
                    Inventory.PAPERDOLL_UNDER,
                    Inventory.PAPERDOLL_REAR,
                    Inventory.PAPERDOLL_LEAR,
                    Inventory.PAPERDOLL_NECK,
                    Inventory.PAPERDOLL_RFINGER,
                    Inventory.PAPERDOLL_LFINGER,
                    Inventory.PAPERDOLL_HEAD,
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_LHAND,
                    Inventory.PAPERDOLL_GLOVES,
                    Inventory.PAPERDOLL_CHEST,
                    Inventory.PAPERDOLL_LEGS,
                    Inventory.PAPERDOLL_FEET,
                    Inventory.PAPERDOLL_CLOAK,
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_HAIR,
                    Inventory.PAPERDOLL_HAIR2,
                    Inventory.PAPERDOLL_RBRACELET,
                    Inventory.PAPERDOLL_LBRACELET,
                    Inventory.PAPERDOLL_DECO1,
                    Inventory.PAPERDOLL_DECO2,
                    Inventory.PAPERDOLL_DECO3,
                    Inventory.PAPERDOLL_DECO4,
                    Inventory.PAPERDOLL_DECO5,
                    Inventory.PAPERDOLL_DECO6,
                    Inventory.PAPERDOLL_BELT,
                    Inventory.PAPERDOLL_BROACH,
                    Inventory.PAPERDOLL_STONE1,
                    Inventory.PAPERDOLL_STONE2,
                    Inventory.PAPERDOLL_STONE3,
                    Inventory.PAPERDOLL_STONE4,
                    Inventory.PAPERDOLL_STONE5,
                    Inventory.PAPERDOLL_STONE6

            };

    private static final int[] PAPERDOLL_ORDER_AUGMENT = new int[]
            {
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_LHAND,
                    Inventory.PAPERDOLL_RHAND
            };

    private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
            {
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_LHAND,
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_GLOVES,
                    Inventory.PAPERDOLL_CHEST,
                    Inventory.PAPERDOLL_LEGS,
                    Inventory.PAPERDOLL_FEET,
                    Inventory.PAPERDOLL_HAIR,
                    Inventory.PAPERDOLL_HAIR2
            };
	/**
	 * @return {@code true} if packet originated from invisible character.
	 */
	public boolean isInvisible()
	{
		return _invisible;
	}

	/**
	 * Set "invisible" boolean flag in the packet.
	 * Packets from invisible characters will not be broadcasted to players.
	 */
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}

	@Override
	protected void write()
	{
		try
		{
			if(isWriteOpCode())
			{
				writeOpCode();
			}
			writeImpl();
            if(Config.PACKET_HANDLER_DEBUG)
            {
                switch (getClass().getSimpleName())
                {
                    case "MTL":
                    case "SocialAction":
                    case "StatusUpdate":
                    case "ChangeMoveType":
                    case "NS":
                    case "MyTargetSelected":
                    case "TargetSelected":
                    case "ValidateLocation":
                    case "ActionFail":
                    case "ExAbnormalStatusUpdateFromTarget":
                    case "DeleteObject":
                    case "StopMove":
                        break;
                    default:
                        System.out.println("[S] " + getClass().getSimpleName());
                }
            }
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Client: " + getClient() + " - Failed writing: " + this + " ; " + e.getMessage(), e);
		}
	}

	public void runImpl()
	{

	}

	protected abstract void writeImpl();

	public boolean isWriteOpCode()
	{
		return true;
	}

	protected void writeOpCode()
	{
		try
		{
			int opCode = ServerPacketOpCodeManager.getInstance().getOpCodeForPacketHash(getClass().hashCode());
			if(opCode > 254)
			{
				writeC(0xFE);
				writeH(opCode - 255);

                /**if(Config.PACKET_HANDLER_DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Integer.toHexString(opCode - 255));
                    if (sb.length() < 2) {
                        sb.insert(0, '0');
                    }
                    String hex = sb.toString();
                    System.out.println(hex);
                }*/
			}
			else
			{
				writeC(opCode);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Client: " + getClient() + " - Failed writing: " + this + " ; " + e.getMessage(), e);
		}
	}

    /**
     * Служит для быстрой записи информации о предмете в пакете
     * @param item инстанс предмета
     */
    protected void writeItemInfo(L2ItemInstance item)
    {
        writeItemInfo(new ItemInfo(item));
    }

    protected void writeItemInfo(L2WarehouseItem item)
    {
        writeItemInfo(new ItemInfo(item));
    }

    protected void writeItemInfo(TradeItem item)
    {
        writeItemInfo(new ItemInfo(item));
    }

    protected void writeItemInfo(L2TradeItem item)
    {
        writeItemInfo(new ItemInfo(item));
    }

    protected void writeTradeItem(TradeItem item)
    {
        writeH(item.getItem().getType1());
        writeD(item.getObjectId());
        writeD(item.getItem().getItemId());
        writeQ(item.getCount());
        writeC(item.getItem().getType2());
        writeC(item.getCustomType1());
        writeQ(item.getItem().getBodyPart());
        writeH(item.getEnchantLevel());
        writeH(0x00); //всегда 0
        writeH(item.getCustomType2());
        writeItemElementalAndEnchant(new ItemInfo(item));
    }

    protected void writeItemInfo(ItemInfo item)
    {
        final int mask = calculateMask(item);
        writeC(mask);
        writeD(item.getObjectId());
        writeD(item.getItem().getItemId());
        writeC(item.getEquipped() == 0 ? (int) item.getLocation() : 0xFF);
        writeQ(item.getCount());
        writeC(item.getItem().getType2());
        writeC(item.getCustomType1());
        writeH(item.getEquipped());
        writeQ(item.getItem().getBodyPart());
        writeH(item.getEnchant());
        writeD(item.getMana());
        writeD(item.getTime());
        writeC(item.isBlock());
        if (containsMask(mask, ItemListType.AUGMENT_BONUS))
        {
            writeD(item.getAugmentationBonus());
        }
        if (containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
        {
            writeItemElemental(item);
        }
        if (containsMask(mask, ItemListType.ENCHANT_EFFECT))
        {
            writeItemEnchantEffect(item);
        }
        if (containsMask(mask, ItemListType.VISUAL_ID))
        {
            writeD(item.getSkin());
        }
    }

    protected static int calculateMask(ItemInfo item)
    {
        int mask = 0;
        if (item.getAugmentationBonus() > 0)
        {
            mask |= ItemListType.AUGMENT_BONUS.getMask();
        }

        if (item.getAttackElementType() >= 0)
        {
            mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
        }
        else
        {
            for (byte i = 0; i < 6; i++)
            {
                if (item.getElementDefAttr(i) >= 0)
                {
                    mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
                    break;
                }
            }
        }

        if (item.getEnchantEffect() != null)
        {
            for (int id : item.getEnchantEffect())
            {
                if (id > 0)
                {
                    mask |= ItemListType.ENCHANT_EFFECT.getMask();
                    break;
                }
            }
        }

        if (item.getSkin() > 0)
        {
            mask |= ItemListType.VISUAL_ID.getMask();
        }

        return mask;
    }

    protected void writeItemElementalAndEnchant(ItemInfo item)
    {
        writeItemElemental(item);
        writeItemEnchantEffect(item);
    }

    protected void writeItemElemental(ItemInfo item)
    {
        writeH(item.getAttackElementType());
        writeH(item.getAttackElementPower());
        for (byte i = 0; i < 6; i++)
        {
            writeH(item.getElementDefAttr(i));
        }
    }

    protected void writeItemEnchantEffect(ItemInfo item)
    {
        for (int op : item.getEnchantEffect())
        {
            writeH(op);
        }
    }

    protected void writeInventoryBlock(PcInventory inventory)
    {
        if (inventory.hasInventoryBlock())
        {
            writeH(inventory.getBlockItems().length);
            writeC(inventory.getBlockMode());
            for (int i : inventory.getBlockItems())
            {
                writeD(i);
            }
        }
        else
        {
            writeH(0x00);
        }
    }

    protected static boolean containsMask(int masks, IUpdateTypeComponent type)
    {
        return (masks & type.getMask()) == type.getMask();
    }

    protected int[] getPaperdollOrder()
    {
        return PAPERDOLL_ORDER;
    }

    protected int[] getPaperdollOrderAugument()
    {
        return PAPERDOLL_ORDER_AUGMENT;
    }

    protected int[] getPaperdollOrderVisualId()
    {
        return PAPERDOLL_ORDER_VISUAL_ID;
    }

    protected void writeString(String str)
    {
        if ((str == null) || str.isEmpty())
        {
            writeH(0x00);
            return;
        }
        final char[] chars = str.toCharArray();

        writeH(chars.length);
        for (char ch : chars)
        {
            _buf.putChar(ch);
        }
    }

	@Override
	public String toString()
	{
		return "[S] " + getClass().getSimpleName();
	}
}