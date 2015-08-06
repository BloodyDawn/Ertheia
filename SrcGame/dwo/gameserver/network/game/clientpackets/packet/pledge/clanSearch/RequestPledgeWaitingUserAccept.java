package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.pledge.JoinPledge;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeWaitingUserAccept extends L2GameClientPacket
{
    private boolean _acceptRequest;
    private int _playerId;
    private int _clanId;

    @Override
    protected void readImpl()
    {
        _acceptRequest = readD() == 1;
        _playerId = readD();
        _clanId = readD();
    }

	@Override
	protected void runImpl()
	{
        final L2PcInstance activeChar = getClient().getActiveChar();

        if ((activeChar == null) || (activeChar.getClan() == null))
        {
            return;
        }

		if(_acceptRequest)
        {
            L2PcInstance player = WorldManager.getInstance().getPlayer(_playerId);
            if (player != null)
            {
                final L2Clan clan = activeChar.getClan();
                clan.addClanMember(player);
                player.sendPacket(new JoinPledge(_clanId));
                final UI ui = new UI(player);
                ui.addComponentType(UserInfoType.CLAN);
                player.sendPacket(ui);
                player.broadcastUserInfo();

                ClanSearchManager.getInstance().removeApplicant(player.getClanId(), _playerId);
            }
        }
		else
		{
            ClanSearchManager.getInstance().removeApplicant(activeChar.getClanId(), _playerId);
        }
	}

	@Override
	public String getType()
	{
		return "[C] D0:E8 RequestPledgeWaitingUserAccept";
	}
}
