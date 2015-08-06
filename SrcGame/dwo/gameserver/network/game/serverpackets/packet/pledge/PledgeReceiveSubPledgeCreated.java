package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.SubPledge;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

/**
 * @author  -Wooden-
 */

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private SubPledge _subPledge;
	private L2Clan _clan;

	public PledgeReceiveSubPledgeCreated(SubPledge subPledge, L2Clan clan)
	{
		_subPledge = subPledge;
		_clan = clan;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x01);
		writeD(_subPledge.getTypeId());
		writeS(_subPledge.getName());
		writeS(getLeaderName());
	}

	private String getLeaderName()
	{
		int LeaderId = _subPledge.getLeaderId();
		if(_subPledge.getTypeId() == L2Clan.SUBUNIT_ACADEMY || LeaderId == 0)
		{
			return "";
		}
		else if(_clan.getClanMember(LeaderId) == null)
		{
			_subPledge.setLeaderId(0);
			_log.log(Level.WARN, "SubPledgeLeader: " + LeaderId + " is missing from clan: " + _clan.getName() + '[' + _clan.getClanId() + ']');
			return "";
		}
		else
		{
			return _clan.getClanMember(LeaderId).getName();
		}
	}
}
