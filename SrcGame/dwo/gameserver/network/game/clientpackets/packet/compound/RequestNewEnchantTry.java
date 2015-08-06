package dwo.gameserver.network.game.clientpackets.packet.compound;

import dwo.gameserver.model.actor.requests.CompoundRequest;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantFail;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantOneFail;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantSucess;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Rnd;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class RequestNewEnchantTry extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {

    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }
        else if (activeChar.isInStoreMode())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
            return;
        }
        else if (activeChar.isProcessingTransaction() || activeChar.isProcessingRequest())
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
            activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
            return;
        }

        final CompoundRequest request = activeChar.getRequest(CompoundRequest.class);
        if ((request == null) || request.isProcessing())
        {
            activeChar.sendPacket(ExEnchantFail.STATIC_PACKET);
            return;
        }

        request.setProcessing(true);

        final L2ItemInstance itemOne = request.getItemOne();
        final L2ItemInstance itemTwo = request.getItemTwo();
        if ((itemOne == null) || (itemTwo == null))
        {
            activeChar.sendPacket(ExEnchantFail.STATIC_PACKET);
            activeChar.removeRequest(request.getClass());
            return;
        }

        if (itemOne.getObjectId() == itemTwo.getObjectId())
        {
            activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
            activeChar.removeRequest(request.getClass());
            return;
        }

        if (itemOne.getItem().getId() != itemTwo.getItem().getId())
        {
            activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
            activeChar.removeRequest(request.getClass());
            return;
        }

        if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
        {
            activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
            activeChar.removeRequest(request.getClass());
            return;
        }

        final InventoryUpdate iu = new InventoryUpdate();
        final double random = Rnd.nextDouble() * 100;

        if (random < itemOne.getItem().getCompoundChance())
        {
            iu.addRemovedItem(itemOne);
            iu.addRemovedItem(itemTwo);

            if (activeChar.destroyItem(ProcessType.COMPOUND_ONE, itemOne, null, true) && activeChar.destroyItem(ProcessType.COMPOUND_TWO, itemTwo, null, true))
            {
                final L2ItemInstance item = activeChar.addItem(ProcessType.COMPOUND_RESULT, itemOne.getItem().getCompoundItem(), 1, null, true);
                activeChar.sendPacket(new ExEnchantSucess(item.getItem().getId()));
            }
        }
        else
        {
            iu.addRemovedItem(itemTwo);

            if (activeChar.destroyItem(ProcessType.COMPOUND_FAIL, itemTwo, null, true))
            {
                activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
            }
        }

        activeChar.sendPacket(iu);
        activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
        activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
        activeChar.removeRequest(request.getClass());
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
