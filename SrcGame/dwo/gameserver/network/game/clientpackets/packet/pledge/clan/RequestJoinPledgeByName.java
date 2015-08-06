package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.pledge.AskJoinPledge;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 03.08.12
 * Time: 10:31
 */
public class RequestJoinPledgeByName extends L2GameClientPacket
{

	private String _target;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		_target = readS();
		_pledgeType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}

		L2PcInstance target = WorldManager.getInstance().getPlayer(_target);
		if(target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		if(!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
		{
			return;
		}

		if(!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}

		String pledgeName = activeChar.getClan().getName();
		String subPledgeName = activeChar.getClan().getSubPledge(_pledgeType) != null ? activeChar.getClan().getSubPledge(_pledgeType).getName() : null;
		target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), subPledgeName, _pledgeType, pledgeName));
	}

	@Override
	public String getType()
	{
		return "[C] d0:c1 RequestClanAskJoinByName";
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

}
