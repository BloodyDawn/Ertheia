package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private L2Clan _clan;

	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_clan.getClanId());           // ClanID
		writeD(_clan.getCrestId());          // CrestID
		writeD(_clan.getLevel());             // SkillLevel
		writeD(_clan.getCastleId());         // CastleID
		writeD(0x00); // GOD                 // AgitType         0 обычный   1 временный ( инст )
		writeD(_clan.getClanhallId());        // AgitID                если временный то ид инста
		writeD(_clan.getFortId());           // FortressID
		writeD(_clan.getRank());             // ClanRank
		writeD(_clan.getReputationScore());  // ClanNameValue
		writeD(0x00); // GOD                 // Status
		writeD(0x00); // GOD                 // Guilty
		writeD(_clan.getAllyId());           // AllianceID
		writeS(_clan.getAllyName());         // AllianceName
		writeD(_clan.getAllyCrestId());      // AllianceCrestID
		writeD(_clan.isAtWar() ? 1 : 0);     // InWar
		writeD(0x00); // GOD                 // LargeCrestID
		writeD(0x00); // GOD
	}
}
