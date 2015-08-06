package dwo.gameserver.network.game.serverpackets.packet.primeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.primeshop.PrimeShopGroup;
import dwo.gameserver.model.items.primeshop.PrimeShopItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;


public class ExBR_ProductList extends L2GameServerPacket
{
    private final L2PcInstance _activeChar;
    private final int _type;
    private final Collection<PrimeShopGroup> _primeList;

    public ExBR_ProductList(L2PcInstance activeChar, int type, Collection<PrimeShopGroup> items)
    {
        _activeChar = activeChar;
        _type = type;
        _primeList = items;
    }

    @Override
    protected final void writeImpl()
    {
        writeQ(_activeChar.getInventory().getAdenaCount()); // Adena
        writeQ(0x00); // Hero coins
        writeC(_type); // Type 0 - Home, 1 - History, 2 - Favorites
        writeD(_primeList.size());
        for (PrimeShopGroup brItem : _primeList)
        {
            writeD(brItem.getBrId());
            
            writeC(brItem.getCat());
            writeC(brItem.getPaymentType()); // Payment Type: 0 - Prime Points, 1 - Adena, 2 - Hero Coins
            
            writeD(brItem.getPrice());
            
            writeC(brItem.getPanelType()); // Item Panel Type: 0 - None, 1 - Event, 2 - Sale, 3 - New, 4 - Best
            
            writeD(brItem.getRecommended()); // Recommended: (bit flags) 1 - Top, 2 - Left, 4 - Right
            writeD(brItem.getStartSale());
            writeD(brItem.getEndSale());
            
            writeC(brItem.getDaysOfWeek());
            writeC(brItem.getStartHour());
            writeC(brItem.getStartMinute());
            writeC(brItem.getStopHour());
            writeC(brItem.getStopMinute());
            
            writeD(brItem.getStock());
            writeD(brItem.getTotal());
            
            writeC(brItem.getSalePercent());
            writeC(brItem.getMinLevel());
            writeC(brItem.getMaxLevel());
            
            writeD(brItem.getMinBirthday());
            writeD(brItem.getMaxBirthday());
            writeD(brItem.getRestrictionDay());
            writeD(brItem.getAvailableCount());
            
            writeC(brItem.getItems().size());
            for (PrimeShopItem item : brItem.getItems())
            {
                writeD(item.getId());
                writeD((int) item.getCount());
                writeD(item.getWeight());
                writeD(item.isTradable());
            }
        }
    }
}
