package dwo.gameserver.network.game.clientpackets.packet.pcCafe;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExPCCafeRequestOpenWindowWithoutNPC extends L2GameClientPacket
{
    //ch
    @Override
    protected void readImpl()
    {
        // this just trigger
    }

    @Override
    protected void runImpl()
    {

        final L2PcInstance _activeChar = getClient().getActiveChar();
        if(_activeChar != null)
        {
            getHtmlPage(_activeChar);
        }
    }

    public void getHtmlPage(L2PcInstance player)
    {
        player.sendPacket(new NpcHtmlMessage(0, HtmCache.getInstance().getHtm(player.getLang(), "mods/pccafe.htm")));
    }

    @Override
    public String getType()
    {
        return getClass().getName();
    }
}
