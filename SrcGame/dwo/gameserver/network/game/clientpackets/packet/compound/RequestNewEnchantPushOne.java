package dwo.gameserver.network.game.clientpackets.packet.compound;

import dwo.gameserver.model.actor.requests.CompoundRequest;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantOneFail;
import dwo.gameserver.network.game.serverpackets.packet.compound.ExEnchantOneOK;

/**
 * User: GenCloud
 * Date: 18.03.2015
 * Team: La2Era Team
 */
public class RequestNewEnchantPushOne extends L2GameClientPacket
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

        CompoundRequest request = new CompoundRequest(activeChar);
        if (!activeChar.addRequest(request))
        {
            activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
            return;
        }

        request.setItemOne(_objectId);
        L2ItemInstance itemOne = request.getItemOne();
        if (itemOne == null)
        {
            activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
            activeChar.removeRequest(request.getClass());
            return;
        }

        if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
        {
            activeChar.sendPacket(ExEnchantOneOK.STATIC_PACKET);
            activeChar.removeRequest(request.getClass());
            return;
        }

        activeChar.sendPacket(ExEnchantOneOK.STATIC_PACKET);
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
