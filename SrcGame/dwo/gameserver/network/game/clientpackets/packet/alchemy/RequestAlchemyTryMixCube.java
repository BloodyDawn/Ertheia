package dwo.gameserver.network.game.clientpackets.packet.alchemy;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.alchemy.ExTryMixCube;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

/**
 * User: GenCloud
 * Date: 21.01.2015
 * Team: La2Era Team
 */
public class RequestAlchemyTryMixCube extends L2GameClientPacket
{
    private TIntLongMap _items;

    public RequestAlchemyTryMixCube() {
        _items = null;
    }
    
    @Override
    protected void readImpl()
    {
        int count = readD();
        _items = new TIntLongHashMap(count);
        for (int i = 0; i < count; ++i) 
        {
            int itemObjectId = readD();
            long itemCount = readQ();
            _items.put(itemObjectId, itemCount);
        }
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();

        if (activeChar == null || activeChar.getRace() != Race.Ertheia )
        {
            return;
        }
        if (_items == null || _items.isEmpty())
        {
            activeChar.sendPacket(ExTryMixCube.FAIL);
            return;
        }
        if (activeChar.isInCombat())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_DURING_BATTLE);
            activeChar.sendPacket(ExTryMixCube.FAIL);
            return;
        }
        if (activeChar.isInStoreMode() || activeChar.isProcessingTransaction())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_TRADING_OR_USING_A_PRIVATE_STORE_OR_SHOP);
            activeChar.sendPacket(ExTryMixCube.FAIL);
            return;
        }
        if (activeChar.isDead())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_DEAD);
            activeChar.sendPacket(ExTryMixCube.FAIL);
            return;
        }
        if (activeChar.isMovementDisabled())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_IMMOBILE);
            activeChar.sendPacket(ExTryMixCube.FAIL);
            return;
        }

        long totalPrice = 0;
        long elcyumCrystalCount = 0;
        
        Inventory inventory = activeChar.getInventory();
        TIntLongIterator iterator = _items.iterator();

        while (iterator.hasNext())
        {
            iterator.advance();

            int itemObjectId = iterator.key();
            long itemCount = iterator.value();

            L2ItemInstance item = inventory.getItemByObjectId(itemObjectId);

            if (item != null)
            {
                if (item.getCount() < itemCount) 
                {
                    continue;
                }
                if (!item.isDestroyable())
                {
                    continue;
                }
                if (item.getEnchantLevel() > 0) 
                {
                    continue;
                }
                if (item.isAugmented())
                {
                    continue;
                }
                if (item.isShadowItem()) 
                {
                    continue;
                }
                if (item.getItemId() == 36514)
                {
                    if (_items.size() <= 3)
                    {
                        continue;
                    }
                    elcyumCrystalCount = itemCount;
                }
                else
                {
                    long price = item.getItemId() == 57 ? itemCount : item.getReferencePrice();
                    if (price <= 0)
                    {
                        continue;
                    }
                    totalPrice += price;
                }
                inventory.destroyItem(ProcessType.ALCHEMY, itemObjectId, itemCount, activeChar, null);
                activeChar.sendPacket(SystemMessage.removeItems(item.getItemId(), itemCount));
            }
        }

        long stoneCount = 0;

        if (totalPrice > 0)
        {
            if (_items.size() >= 3)
            {
                stoneCount = totalPrice / 10000;
                stoneCount += elcyumCrystalCount * 1000;
            }
            else if (totalPrice >= 20000 && totalPrice < 35000)
            {
                stoneCount = 1;
            }
            else if (totalPrice >= 35000 && totalPrice < 50000)
            {
                stoneCount = 2;
            }
            else if (totalPrice >= 50000)
            {
                stoneCount = (long) Math.floor(totalPrice / 16666.666666666668);
            }
        }

        if (stoneCount > 0)
        {
            activeChar.getInventory().addItem(ProcessType.ALCHEMY, 39461, stoneCount, activeChar, true);
        }

        activeChar.sendPacket(new ExTryMixCube(stoneCount, 39461));
        activeChar.sendPacket(new ItemList(activeChar, false));
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}