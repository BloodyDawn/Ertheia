/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.player.duel;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ActionFail;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDuelEnd;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDuelReady;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDuelStart;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDuelUpdateUserInfo;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.stream.Collectors;

public class Duel
{
	protected static final Logger _log = LogManager.getLogger(Duel.class);

	private int _duelId;
	private L2PcInstance _playerA;
	private L2PcInstance _playerB;
	private boolean _partyDuel;
	private Calendar _duelEndTime;
	private int _surrenderRequest;
	private int _countdown = 4;
	private boolean _finished;

	private FastList<PlayerCondition> _playerConditions;

	public Duel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel, int duelId)
	{
		_duelId = duelId;
		_playerA = playerA;
		_playerB = playerB;
		_partyDuel = partyDuel == 1;

		_duelEndTime = Calendar.getInstance();
		if(_partyDuel)
		{
			_duelEndTime.add(Calendar.SECOND, 300);
		}
		else
		{
			_duelEndTime.add(Calendar.SECOND, 120);
		}

		_playerConditions = new FastList<PlayerCondition>().shared();

		_finished = false;

		if(_partyDuel)
		{
			// increase countdown so that start task can teleport players
			_countdown++;
			// inform players that they will be portet shortly
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
		}
		// Schedule duel start
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartDuelTask(this), 3000);
	}

	/**
	 * Stops all players from attacking.
	 * Used for duel timeout / interrupt.
	 */
	private void stopFighting()
	{
		ActionFail af = ActionFail.STATIC_PACKET;
		if(_partyDuel)
		{
			for(L2PcInstance temp : _playerA.getParty().getMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
			for(L2PcInstance temp : _playerB.getParty().getMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
		}
		else
		{
			_playerA.abortCast();
			_playerB.abortCast();
			_playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			_playerA.setTarget(null);
			_playerB.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			_playerB.setTarget(null);
			_playerA.sendPacket(af);
			_playerB.sendPacket(af);
		}
	}

	/**
	 * Check if a player engaged in pvp combat (only for 1on1 duels)
	 *
	 * @return returns true if a duelist is engaged in Pvp combat
	 */
	public boolean isDuelistInPvp(boolean sendMessage)
	{
		if(_partyDuel)
		{
			// Party duels take place in arenas - should be no other players there
			return false;
		}
		if(_playerA.getPvPFlagController().isFlagged() || _playerB.getPvPFlagController().isFlagged())
		{
			if(sendMessage)
			{
				String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
				_playerA.sendMessage(engagedInPvP);
				_playerB.sendMessage(engagedInPvP);
			}
			return true;
		}
		return false;
	}

	// ===============================================================
	// Schedule task

	/**
	 * Starts the duel
	 */
	public void startDuel()
	{
		if(_playerA == null || _playerB == null || _playerA.isInDuel() || _playerB.isInDuel())
		{
			// clean up
			_playerConditions.clear();
			_playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}

		if(_partyDuel)
		{
			// set isInDuel() state
			// cancel all active trades, just in case? xD
			for(L2PcInstance temp : _playerA.getParty().getMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(_duelId);
				temp.setTeam(1);
				temp.broadcastUserInfo();
				broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
			}
			for(L2PcInstance temp : _playerB.getParty().getMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(_duelId);
				temp.setTeam(2);
				temp.broadcastUserInfo();
				broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
			}

			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(1);
			ExDuelStart start = new ExDuelStart(1);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
		}
		else
		{
			// set isInDuel() state
			_playerA.setIsInDuel(_duelId);
			_playerA.setTeam(1);
			_playerB.setIsInDuel(_duelId);
			_playerB.setTeam(2);

			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(0);
			ExDuelStart start = new ExDuelStart(0);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);

			broadcastToTeam1(new ExDuelUpdateUserInfo(_playerB));
			broadcastToTeam2(new ExDuelUpdateUserInfo(_playerA));

			_playerA.broadcastUserInfo();
			_playerB.broadcastUserInfo();
		}

		// play sound
		PlaySound ps = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
		broadcastToTeam1(ps);
		broadcastToTeam2(ps);

		// start duelling task
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDuelTask(this), 1000);
	}

	/**
	 * Save the current player condition: hp, mp, cp, location
	 */
	public void savePlayerConditions()
	{
		if(_partyDuel)
		{
			_playerConditions.addAll(_playerA.getParty().getMembers().stream().map(temp -> new PlayerCondition(temp, _partyDuel)).collect(Collectors.toList()));
			_playerConditions.addAll(_playerB.getParty().getMembers().stream().map(temp -> new PlayerCondition(temp, _partyDuel)).collect(Collectors.toList()));
		}
		else
		{
			_playerConditions.add(new PlayerCondition(_playerA, _partyDuel));
			_playerConditions.add(new PlayerCondition(_playerB, _partyDuel));
		}
	}

	/**
	 * Restore player conditions
	 *
	 * @param abnormalDuelEnd the duel canceled?
	 */
	public void restorePlayerConditions(boolean abnormalDuelEnd)
	{
		// update isInDuel() state for all players
		if(_partyDuel)
		{
			for(L2PcInstance temp : _playerA.getParty().getMembers())
			{
				temp.getInstanceController().setInstanceId(0);
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
			for(L2PcInstance temp : _playerB.getParty().getMembers())
			{
				temp.getInstanceController().setInstanceId(0);
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
		}
		else
		{
			_playerA.setIsInDuel(0);
			_playerA.setTeam(0);
			_playerA.broadcastUserInfo();
			_playerB.setIsInDuel(0);
			_playerB.setTeam(0);
			_playerB.broadcastUserInfo();
		}

		// if it is an abnormal DuelEnd do not restore hp, mp, cp
		if(abnormalDuelEnd)
		{
			return;
		}

		// restore player conditions
		/*for (FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); (e = e.getNext()) != end;)
        {
            e.getValue().restoreCondition();
        }*/
		for(PlayerCondition condition : _playerConditions)
		{
			condition.restoreCondition();
		}
	}

	// ========================================================
	// Method - Private

	/**
	 * Get the duel id
	 *
	 * @return id
	 */
	public int getId()
	{
		return _duelId;
	}

	// ========================================================
	// Method - Public

	/**
	 * Returns the remaining time
	 *
	 * @return remaining time
	 */
	public int getRemainingTime()
	{
		return (int) (_duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Get the player that requestet the duel
	 *
	 * @return duel requester
	 */
	public L2PcInstance getPlayerA()
	{
		return _playerA;
	}

	/**
	 * Get the player that was challenged
	 *
	 * @return challenged player
	 */
	public L2PcInstance getPlayerB()
	{
		return _playerB;
	}

	/**
	 * Returns whether this is a party duel or not
	 *
	 * @return is party duel
	 */
	public boolean isPartyDuel()
	{
		return _partyDuel;
	}

	public boolean getFinished()
	{
		return _finished;
	}

	public void setFinished(boolean mode)
	{
		_finished = mode;
	}

	/**
	 * teleport all players to the given coordinates
	 */
	public void teleportPlayers()
	{
		//TODO: adjust the values if needed... or implement something better (especially using more then 1 arena)
		if(!_partyDuel)
		{
			return;
		}

		int instanceId = InstanceManager.getInstance().createDynamicInstance("PartyDuel.xml");
		InstanceWorld world = new DuelWorld();
		world.instanceId = instanceId;
		world.templateId = InstanceZoneId.PARTY_DUEL.getId();
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);

		int offset = 0;

		for(L2PcInstance temp : _playerA.getParty().getMembers())
		{
			temp.teleToLocation(-81421 + offset - Rnd.get(180), -245670 - Rnd.get(150), -3300);
			world.allowed.add(temp.getObjectId());
			temp.getInstanceController().setInstanceId(instanceId);
			offset += 40;
		}
		offset = 0;
		for(L2PcInstance temp : _playerB.getParty().getMembers())
		{
			temp.teleToLocation(-82429 + offset - Rnd.get(180), -245675 - Rnd.get(150), -3300);
			world.allowed.add(temp.getObjectId());
			temp.getInstanceController().setInstanceId(instanceId);
			offset += 40;
		}
	}

	/**
	 * Broadcast a packet to the challanger team
	 */
	public void broadcastToTeam1(L2GameServerPacket packet)
	{
		if(_playerA == null)
		{
			return;
		}

		if(_partyDuel && _playerA.getParty() != null)
		{
			for(L2PcInstance temp : _playerA.getParty().getMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			_playerA.sendPacket(packet);
		}
	}

	/**
	 * Broadcast a packet to the challenged team
	 */
	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if(_playerB == null)
		{
			return;
		}

		if(_partyDuel && _playerB.getParty() != null)
		{
			for(L2PcInstance temp : _playerB.getParty().getMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			_playerB.sendPacket(packet);
		}
	}

	/**
	 * Get the duel winner
	 *
	 * @return winner
	 */
	public L2PcInstance getWinner()
	{
		if(!_finished || _playerA == null || _playerB == null)
		{
			return null;
		}
		if(_playerA.getDuelState() == DuelState.WINNER)
		{
			return _playerA;
		}
		if(_playerB.getDuelState() == DuelState.WINNER)
		{
			return _playerB;
		}
		return null;
	}

	/**
	 * Get the duel looser
	 *
	 * @return looser
	 */
	public L2PcInstance getLooser()
	{
		if(!_finished || _playerA == null || _playerB == null)
		{
			return null;
		}
		if(_playerA.getDuelState() == DuelState.WINNER)
		{
			return _playerB;
		}
		if(_playerB.getDuelState() == DuelState.WINNER)
		{
			return _playerA;
		}
		return null;
	}

	/**
	 * Playback the bow animation for all loosers
	 */
	public void playKneelAnimation()
	{
		L2PcInstance looser = getLooser();

		if(looser == null)
		{
			return;
		}

		if(_partyDuel && looser.getParty() != null)
		{
			for(L2PcInstance temp : looser.getParty().getMembers())
			{
				temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
			}
		}
		else
		{
			looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
		}
	}

	/**
	 * Do the countdown and send message to players if necessary
	 *
	 * @return current count
	 */
	public int countdown()
	{
		_countdown--;

		if(_countdown > 3)
		{
			return _countdown;
		}

		// Broadcast countdown to duelists
		SystemMessage sm = null;
		if(_countdown > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
			sm.addNumber(_countdown);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}

		broadcastToTeam1(sm);
		broadcastToTeam2(sm);

		return _countdown;
	}

	/**
	 * The duel has reached a state in which it can no longer continue
	 *
	 * @param result duel result
	 */
	public void endDuel(DuelResult result)
	{
		if(_playerA == null || _playerB == null)
		{
			//clean up
			_playerConditions.clear();
			_playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}

		// inform players of the result
		SystemMessage sm = null;
		switch(result)
		{
			case Team1Win:
			case Team2Surrender:
				restorePlayerConditions(false);
				// send SystemMessage
				sm = _partyDuel ? SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL) : SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				sm.addString(_playerA.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team1Surrender:
			case Team2Win:
				restorePlayerConditions(false);
				// send SystemMessage
				sm = _partyDuel ? SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL) : SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				sm.addString(_playerB.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Canceled:
				stopFighting();
				// dont restore hp, mp, cp
				restorePlayerConditions(true);
				//TODO: is there no other message for a canceled duel?
				// send SystemMessage
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Timeout:
				stopFighting();
				// hp,mp,cp seem to be restored in a timeout too...
				restorePlayerConditions(false);
				// send SystemMessage
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
		}

		// Send end duel packet
		ExDuelEnd duelEnd = null;
		duelEnd = _partyDuel ? new ExDuelEnd(1) : new ExDuelEnd(0);

		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);

		//clean up
		_playerConditions.clear();
		_playerConditions = null;
		DuelManager.getInstance().removeDuel(this);
	}

	/**
	 * Did a situation occur in which the duel has to be ended?
	 *
	 * @return DuelResultEnum duel status
	 */
	public DuelResult checkEndDuelCondition()
	{
		// one of the players might leave during duel
		if(_playerA == null || _playerB == null)
		{
			return DuelResult.Canceled;
		}

		// got a duel surrender request?
		if(_surrenderRequest != 0)
		{
			return _surrenderRequest == 1 ? DuelResult.Team1Surrender : DuelResult.Team2Surrender;
		}
		// duel timed out
		if(getRemainingTime() <= 0)
		{
			return DuelResult.Timeout;
		}
		// Has a player been declared winner yet?
		if(_playerA.getDuelState() == DuelState.WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResult.Team1Win;
		}
		if(_playerB.getDuelState() == DuelState.WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResult.Team2Win;
		}

		// More end duel conditions for 1on1 duels
		if(!_partyDuel)
		{
			// Duel was interrupted e.g.: player was attacked by mobs / other players
			if(_playerA.getDuelState() == DuelState.INTERRUPTED || _playerB.getDuelState() == DuelState.INTERRUPTED)
			{
				return DuelResult.Canceled;
			}

			// Are the players too far apart?
			if(!_playerA.isInsideRadius(_playerB, 1600, false, false))
			{
				return DuelResult.Canceled;
			}

			// Did one of the players engage in PvP combat?
			if(isDuelistInPvp(true))
			{
				return DuelResult.Canceled;
			}

			// is one of the players in a CastleSiegeEngine, Peace or PvP zone?
			if(_playerA.isInsideZone(L2Character.ZONE_PEACE) || _playerB.isInsideZone(L2Character.ZONE_PEACE) || _playerA.isInsideZone(L2Character.ZONE_SIEGE) || _playerB.isInsideZone(L2Character.ZONE_SIEGE) || _playerA.isInsideZone(L2Character.ZONE_PVP) || _playerB.isInsideZone(L2Character.ZONE_PVP))
			{
				return DuelResult.Canceled;
			}
		}

		return DuelResult.Continue;
	}

	/**
	 * Register a surrender request
	 *
	 * @param player surrendering player
	 */
	public void doSurrender(L2PcInstance player)
	{
		// already recieved a surrender request
		if(_surrenderRequest != 0)
		{
			return;
		}

		// stop the fight
		stopFighting();

		// TODO: Can every party member cancel a party duel? or only the party leaders?
		if(_partyDuel)
		{
			if(_playerA.getParty().getMembers().contains(player))
			{
				_surrenderRequest = 1;
				for(L2PcInstance temp : _playerA.getParty().getMembers())
				{
					temp.setDuelState(DuelState.DEAD);
				}
				for(L2PcInstance temp : _playerB.getParty().getMembers())
				{
					temp.setDuelState(DuelState.WINNER);
				}
			}
			else if(_playerB.getParty().getMembers().contains(player))
			{
				_surrenderRequest = 2;
				for(L2PcInstance temp : _playerB.getParty().getMembers())
				{
					temp.setDuelState(DuelState.DEAD);
				}
				for(L2PcInstance temp : _playerA.getParty().getMembers())
				{
					temp.setDuelState(DuelState.WINNER);
				}

			}
		}
		else
		{
			if(player.equals(_playerA))
			{
				_surrenderRequest = 1;
				_playerA.setDuelState(DuelState.DEAD);
				_playerB.setDuelState(DuelState.WINNER);
			}
			else if(player.equals(_playerB))
			{
				_surrenderRequest = 2;
				_playerB.setDuelState(DuelState.DEAD);
				_playerA.setDuelState(DuelState.WINNER);
			}
		}
	}

	/**
	 * This function is called whenever a player was defeated in a duel
	 *
	 * @param player dieing player
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		// Set player as defeated
		player.setDuelState(DuelState.DEAD);

		if(_partyDuel)
		{
			boolean teamdefeated = true;
			for(L2PcInstance temp : player.getParty().getMembers())
			{
				if(temp.getDuelState() == DuelState.DUELLING)
				{
					teamdefeated = false;
					break;
				}
			}

			if(teamdefeated)
			{
				L2PcInstance winner = _playerA;
				if(_playerA.getParty().getMembers().contains(player))
				{
					winner = _playerB;
				}

				for(L2PcInstance temp : winner.getParty().getMembers())
				{
					temp.setDuelState(DuelState.WINNER);
				}
			}
		}
		else
		{
			if(!player.equals(_playerA) && !player.equals(_playerB))
			{
				_log.log(Level.WARN, "Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
			}

			if(_playerA.equals(player))
			{
				_playerB.setDuelState(DuelState.WINNER);
			}
			else
			{
				_playerA.setDuelState(DuelState.WINNER);
			}
		}
	}

	/**
	 * This function is called whenever a player leaves a party
	 *
	 * @param player leaving player
	 */
	public void onRemoveFromParty(L2PcInstance player)
	{
		// if it isnt a party duel ignore this
		if(!_partyDuel)
		{
			return;
		}

		// this player is leaving his party during party duel
		// if hes either playerA or playerB cancel the duel and port the players back
		if(player.equals(_playerA) || player.equals(_playerB))
		{
			for(FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); !(e = e.getNext()).equals(end); )
			{
				e.getValue().teleportBack();
				e.getValue().getPlayer().setIsInDuel(0);
			}

			_playerA = null;
			_playerB = null;
		}
		else // teleport the player back & delete his PlayerCondition record
		{
			for(FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); !(e = e.getNext()).equals(end); )
			{
				if(e.getValue().getPlayer().equals(player))
				{
					e.getValue().teleportBack();
					_playerConditions.remove(e.getValue());
					break;
				}
			}
			player.setIsInDuel(0);
		}
	}

	public void onBuff(L2PcInstance player, L2Effect debuff)
	{
		for(FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); !(e = e.getNext()).equals(end); )
		{
			if(e.getValue().getPlayer().equals(player))
			{
				e.getValue().registerDebuff(debuff);
				return;
			}
		}
	}

	public static class PlayerCondition
	{
		private L2PcInstance _player;
		private double _hp;
		private double _mp;
		private double _cp;
		private boolean _paDuel;
		private int _x;
		private int _y;
		private int _z;
		private FastList<L2Effect> _debuffs;

		public PlayerCondition(L2PcInstance player, boolean partyDuel)
		{
			if(player == null)
			{
				return;
			}
			_player = player;
			_hp = _player.getCurrentHp();
			_mp = _player.getCurrentMp();
			_cp = _player.getCurrentCp();
			_paDuel = partyDuel;

			if(_paDuel)
			{
				_x = _player.getX();
				_y = _player.getY();
				_z = _player.getZ();
			}
		}

		public void restoreCondition()
		{
			if(_player == null)
			{
				return;
			}
			_player.setCurrentHp(_hp);
			_player.setCurrentMp(_mp);
			_player.setCurrentCp(_cp);

			if(_paDuel)
			{
				teleportBack();
			}
			if(_debuffs != null) // Debuff removal
			{
				_debuffs.stream().filter(temp -> temp != null).forEach(L2Effect::exit);
			}
		}

		public void registerDebuff(L2Effect debuff)
		{
			if(_debuffs == null)
			{
				_debuffs = new FastList<>();
			}

			_debuffs.add(debuff);
		}

		public void teleportBack()
		{
			if(_paDuel)
			{
				_player.getInstanceController().setInstanceId(0);
				_player.teleToLocation(_x, _y, _z);
			}
		}

		public L2PcInstance getPlayer()
		{
			return _player;
		}
	}

	public static class ScheduleStartDuelTask implements Runnable
	{
		private Duel _duel;

		public ScheduleStartDuelTask(Duel duel)
		{
			_duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				// start/continue countdown
				int count = _duel.countdown();

				if(count == 4)
				{
					// count: 4
					// Дуэль Пати на Пати.
					// players need to be teleportet first
					//TODO: stadia manager needs a function to return an unused stadium for duels
					// currently only teleports to the same stadium
					_duel.savePlayerConditions();
					_duel.teleportPlayers();

					// give players 20 seconds to complete teleport and get ready (its ought to be 30 on offical..)
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
				}
				else if(count > 0) // duel not started yet - continue countdown
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				else
				{
					// count: -1
					//Обычный дуэль 1 на 1
					_duel.savePlayerConditions();
					_duel.startDuel();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	public static class ScheduleEndDuelTask implements Runnable
	{
		private Duel _duel;
		private DuelResult _result;

		public ScheduleEndDuelTask(Duel duel, DuelResult result)
		{
			_duel = duel;
			_result = result;
		}

		@Override
		public void run()
		{
			try
			{
				_duel.endDuel(_result);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class DuelWorld extends InstanceWorld
	{
	}

	public class ScheduleDuelTask implements Runnable
	{
		private Duel _duel;

		public ScheduleDuelTask(Duel duel)
		{
			_duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				DuelResult status = _duel.checkEndDuelCondition();

				if(status == DuelResult.Canceled)
				{
					// do not schedule duel end if it was interrupted
					setFinished(true);
					_duel.endDuel(status);
				}
				else if(status != DuelResult.Continue)
				{
					setFinished(true);
					playKneelAnimation();
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndDuelTask(_duel, status), 5000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}
