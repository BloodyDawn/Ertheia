package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;

public class Die extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private int _charObjId;
	private boolean _canTeleport;
	private boolean _sweepable;
	private L2Clan _clan;
	private boolean _isJailed;
	private int _fixedRes;

	/**
	 * @param cha
	 */
	public Die(L2Character cha)
	{
		_activeChar = cha;
		if(cha.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) cha;
			_clan = player.getClan();
			_fixedRes = player.getAccessLevel().allowFixedRes() || player.canUseFeatherOfBlessing() ? 0x01 : 0x00;
			_canTeleport = !cha.isPendingRevive();
			_isJailed = player.isInJail();
			if(EventManager.isStarted() && EventManager.isPlayerParticipant(player))
			{
				_canTeleport = false;
			}
		}
		_charObjId = cha.getObjectId();

		if(cha.isL2Attackable())
		{
			_sweepable = ((L2Attackable) cha).isSweepActive();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		// NOTE:
		// 6d 00 00 00 00 - to nearest village
		// 6d 01 00 00 00 - to hide away
		// 6d 02 00 00 00 - to castle
		// 6d 03 00 00 00 - to siege HQ
		// sweepable
		// 6d 04 00 00 00 - FIXED

		writeD(_canTeleport ? 0x01 : 0); // 6d 00 00 00 00 - to nearest village
		if(_canTeleport && _clan != null && !_isJailed)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;

			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getNearbyClanHall(_activeChar);
			if(castle != null && castle.getSiege().isInProgress())
			{
				// Идет осада замка
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if(siegeClan == null && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if(fort != null && fort.getSiege().isInProgress())
			{
				// Идет осада форта
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if(siegeClan == null && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}

			writeD(_clan.getClanhallId() > 0 ? 0x01 : 0x00);            // 6d 01 00 00 00 - to hide away
			writeD(_clan.getCastleId() > 0 || isInCastleDefense ? 0x01 : 0x00);  // 6d 02 00 00 00 - to castle
			writeD(siegeClan != null && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty() || hall != null && hall.getSiege().checkIsAttacker(_clan) ? 0x01 : 0x00);       // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00);                               // sweepable  (blue glow)
			writeD(_fixedRes);                  // 6d 04 00 00 00 - to FIXED
			writeD(_clan.getFortId() > 0 || isInFortDefense ? 0x01 : 0x00);    // 6d 05 00 00 00 - to fortress
			writeD(0x00); //?
			writeD(0x00); //?
			writeC(0x00); //?
			writeD(0x00); //?
			writeD(0x00); //?
			//writeD(0x00); // шлется циклом  for ( i = 0; i < v10; ++i )
		}
		else
		{
			writeD(0x00);                                               // 6d 01 00 00 00 - to hide away
			writeD(0x00);                                               // 6d 02 00 00 00 - to castle
			writeD(0x00);                                               // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00);                               // sweepable  (blue glow)
			writeD(_fixedRes);                  // 6d 04 00 00 00 - to FIXED
			writeD(0x00);    // 6d 05 00 00 00 - to fortress
			writeD(0x00); //?
			writeD(0x00); //?
			writeC(0x00); //?
			writeD(0x00); //?
			writeD(0x00); //?
			//writeD(0x00); // шлется циклом  for ( i = 0; i < v10; ++i )
		}
	}
}
