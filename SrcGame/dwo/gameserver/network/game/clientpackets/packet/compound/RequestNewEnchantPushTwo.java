package dwo.gameserver.network.game.clientpackets.packet.compound;

import dwo.gameserver.model.actor.requests.CompoundRequest;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantOneFail;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantTwoFail;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantTwoOK;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class RequestNewEnchantPushTwo extends L2GameClientPacket
{
    private int _objectId;

    @Override
    protected void readImpl()
    {
        _objectId = readD();
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

        CompoundRequest request = activeChar.getRequest(CompoundRequest.class);
        if ((request == null) || request.isProcessing())
        {
            activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
            return;
        }

        request.setItemTwo(_objectId);
        final L2ItemInstance itemOne = request.getItemOne();
        final L2ItemInstance itemTwo = request.getItemTwo();
        if ((itemOne == null) || (itemTwo == null))
        {
            activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
            return;
        }

        if (itemOne.getObjectId() == itemTwo.getObjectId())
        {
            activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
            return;
        }

        if (itemOne.getItem().getId() != itemTwo.getItem().getId())
        {
            activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
            return;
        }

        if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
        {
            activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
            return;
        }

        activeChar.sendPacket(ExEnchantTwoOK.STATIC_PACKET);
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
