package dwo.gameserver.network.game.clientpackets.packet.alchemy;

import dwo.gameserver.datatables.xml.AlchemyDataTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.alchemy.AlchemyDataTemplate;
import dwo.gameserver.model.items.alchemy.AlchemyDataTemplate.AlchemyItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.alchemy.ExAlchemyConversion;
import dwo.gameserver.util.Rnd;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

/**
 * User: GenCloud
 * Date: 21.01.2015
 * Team: La2Era Team
 * TODO
 */
public class RequestAlchemyConversion extends L2GameClientPacket {

    private int _skillId;
    private int _skillLevel;
    private int _count;

    @Override
    protected void readImpl()
    {
        _count = readD();
        readH();
        _skillId = readD();
        _skillLevel = readD();
    }

    @Override
    protected void runImpl() {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }
        if (_count <= 0) 
        {
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        if (activeChar.isInCombat()) 
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_DURING_BATTLE);
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        if (activeChar.isInStoreMode() || activeChar.isProcessingTransaction())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_TRADING_OR_USING_A_PRIVATE_STORE_OR_SHOP);
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        if (activeChar.isDead())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_DEAD);
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        if (activeChar.isMovementDisabled()) 
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_IMMOBILE);
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        
        L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
        if (skill == null)
        {
            _log.warn(getClass().getSimpleName() + ": Error while alchemy: Cannot find alchemy skill[" + _skillId + "-" + _skillLevel + "]!");
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        
        AlchemyDataTemplate data = AlchemyDataTable.getInstance().getData(skill);
        if (data == null) 
        {
            _log.warn(getClass().getSimpleName() + ": Error while alchemy: Cannot find alchemy data[" + _skillId + "-" + _skillLevel + "]!");
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
            return;
        }
        
        AlchemyItem[] ingridients = data.getIngridients();
        AlchemyItem[] onSuccessProducts = data.getOnSuccessProducts();
        AlchemyItem[] onFailProducts = data.getOnFailProducts();
        
        TIntLongMap deletedItems = new TIntLongHashMap();
        TIntLongMap addedItems = new TIntLongHashMap();
        
        int convensionCount = _count;
        
        Inventory inventory = activeChar.getInventory();
        
        for (AlchemyItem ingridient : ingridients)
        {
            L2ItemInstance item = inventory.getItemByItemId(ingridient.getId());
            if (item == null || item.getCount() < ingridient.getCount()) 
            {
                activeChar.sendPacket(ExAlchemyConversion.FAIL);
                return;
            }
            convensionCount = Math.min(convensionCount, (int) Math.floor(item.getCount() / ingridient.getCount()));
        }
        
        for (AlchemyItem ingridient : ingridients) 
        {
            long count = ingridient.getCount() * convensionCount;
            if (inventory.destroyItemByItemId(ProcessType.ALCHEMY, ingridient.getId(), count, activeChar, "alchemy") == null) 
            {
                long deleted = deletedItems.get(ingridient.getId());
                deletedItems.put(ingridient.getId(), deleted + count);
            }
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < convensionCount; ++i) 
        {
            if (Rnd.getChance(data.getSuccessRate()))
            {
                ++successCount;
            }
            else 
            {
                ++failCount;
            }
        }
        
        if (successCount > 0) 
        {
            for (AlchemyItem product : onSuccessProducts)
            {
                long count2 = product.getCount() * successCount;
                long deleted2 = deletedItems.get(product.getId());
                
                if (deleted2 > 0) 
                {
                    deletedItems.put(product.getId(), Math.max(0, deleted2 - count2));
                    long added = count2 - deleted2;
                    
                    if (added > 0) 
                    {
                        addedItems.put(product.getId(), addedItems.get(product.getId()) + added);
                    }
                }
                else 
                {
                    addedItems.put(product.getId(), addedItems.get(product.getId()) + count2);
                }
            }
        }
        if (failCount > 0) 
        {
            for (AlchemyItem product : onFailProducts) 
            {
                long count2 = product.getCount() * failCount;
                long deleted2 = deletedItems.get(product.getId());
                
                if (deleted2 > 0) 
                {
                    deletedItems.put(product.getId(), Math.max(0, deleted2 - count2));
                    long added = count2 - deleted2;
                    
                    if (added > 0) 
                    {
                        addedItems.put(product.getId(), addedItems.get(product.getId()) + added);
                    }
                }
                else
                {
                    addedItems.put(product.getId(), addedItems.get(product.getId()) + count2);
                }
            }
        }
        
        TIntLongIterator iterator = deletedItems.iterator();
        
        while (iterator.hasNext()) 
        {
            iterator.advance();
            long count3 = iterator.value();
            
            if (count3 > 0) 
            {
                activeChar.sendPacket(SystemMessage.removeItems(iterator.key(), count3));
            }
        }
        
        iterator = addedItems.iterator();
        
        while (iterator.hasNext()) 
        {
            iterator.advance();
            long count3 = iterator.value();
            
            if (count3 > 0)
            {
                activeChar.getInventory().addItem(ProcessType.ALCHEMY, iterator.key(), count3, activeChar, true);
            }
        }
        if (successCount == 0 && failCount == 0) 
        {
            activeChar.sendPacket(ExAlchemyConversion.FAIL);
        }
        else 
        {
            activeChar.sendPacket(new ExAlchemyConversion(successCount, failCount));
        }
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}