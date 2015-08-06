package dwo.gameserver.network.game.clientpackets.packet.beautyShop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class NotifyExitBeautyShop extends L2GameClientPacket
{
    private static final String _C__D0_E1_NOTIFYEXITBEAUTYSHOP = "[C] D0:E1 NotifyExitBeautyShop";

    @Override
    protected void readImpl()
    {

    }

    @Override
    protected void runImpl()
    {
        final L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }

        UI userInfo = new UI(activeChar, false);
        userInfo.addComponentType(UserInfoType.APPAREANCE);
        sendPacket(userInfo);
    }

    @Override
    public String getType()
    {
        return _C__D0_E1_NOTIFYEXITBEAUTYSHOP;
    }
}
