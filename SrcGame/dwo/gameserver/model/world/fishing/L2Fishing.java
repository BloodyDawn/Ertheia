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
package dwo.gameserver.model.world.fishing;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.FishingMonstersData;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExFishingHpRegen;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExFishingStartCombat;
import dwo.gameserver.util.Rnd;

import java.util.concurrent.Future;

public class L2Fishing implements Runnable
{
	// Fish datas
	private final int _fishId;
	private final int _fishMaxHp;
	private final double _regenHp;
	private final boolean _isUpperGrade;
	private L2PcInstance _fisher;
	private int _time;
	private int _stop;
	private int _goodUse;
	private int _anim;
	private int _mode;
	private int _deceptiveMode;
	private Future<?> _fishAiTask;
	private boolean _thinking;
	private int _fishCurHp;
	private int _lureType;

	public L2Fishing(L2PcInstance Fisher, L2Fish fish, boolean isNoob, boolean isUpperGrade)
	{
		_fisher = Fisher;
		_fishMaxHp = fish.getFishHp();
		_fishCurHp = _fishMaxHp;
		_regenHp = fish.getHpRegen();
		_fishId = fish.getItemId();
		_time = fish.getCombatDuration();
		_isUpperGrade = isUpperGrade;
		if(isUpperGrade)
		{
			_deceptiveMode = Rnd.getChance(10) ? 1 : 0;
			_lureType = 2;
		}
		else
		{
			_deceptiveMode = 0;
			_lureType = isNoob ? 0 : 1;
		}
		_mode = Rnd.getChance(20) ? 1 : 0;

		ExFishingStartCombat efsc = new ExFishingStartCombat(_fisher, _time, _fishMaxHp, _mode, _lureType, _deceptiveMode);
		_fisher.broadcastPacket(efsc);
		_fisher.sendPacket(new PlaySound(1, "SF_S_01", 0, 0, 0, 0, 0));
		// Succeeded in getting a bite
		_fisher.sendPacket(SystemMessageId.GOT_A_BITE);

		if(_fishAiTask == null)
		{
			_fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}

	}

	@Override
	public void run()
	{
		if(_fisher == null)
		{
			return;
		}

		if(_fishCurHp >= _fishMaxHp << 1)
		{
			// The fish got away
			_fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
			doDie(false);
		}
		else if(_time <= 0)
		{
			// Time is up, so that fish got away
			_fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
			doDie(false);
		}
		else
		{
			aiTask();
		}
	}

	public void changeHp(int hp, int pen)
	{
		_fishCurHp -= hp;
		if(_fishCurHp < 0)
		{
			_fishCurHp = 0;
		}

		ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, pen, _deceptiveMode);
		_fisher.broadcastPacket(efhr);
		_anim = 0;
		if(_fishCurHp > _fishMaxHp << 1)
		{
			_fishCurHp = _fishMaxHp << 1;
			doDie(false);
		}
		else if(_fishCurHp == 0)
		{
			doDie(true);
		}
	}

	public void doDie(boolean win)
	{
		synchronized(this)
		{
			if(_fishAiTask != null)
			{
				_fishAiTask.cancel(false);
				_fishAiTask = null;
			}

			if(_fisher == null)
			{
				return;
			}

			if(win)
			{
				L2FishingMonster fishingMonster = FishingMonstersData.getInstance().getFishingMonster(_fisher.getLevel());

				if(fishingMonster == null)
				{
					return;
				}

				if(Rnd.getChance(fishingMonster.getProbability()))
				{
					L2NpcTemplate monster = NpcTable.getInstance().getTemplate(fishingMonster.getFishingMonsterId());
					_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
					spawnMonster(monster);
				}
				else
				{
					_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
					_fisher.addItem(ProcessType.FISHING, _fishId, 1, null, true);
				}
			}
			_fisher.endFishing(win);
			_fisher = null;
		}
	}

	protected void aiTask()
	{
		if(_thinking)
		{
			return;
		}
		_thinking = true;
		_time--;

		try
		{
			if(_mode == 1)
			{
				if(_deceptiveMode == 0)
				{
					_fishCurHp += (int) _regenHp;
				}
			}
			else
			{
				if(_deceptiveMode == 1)
				{
					_fishCurHp += (int) _regenHp;
				}
			}
			if(_stop == 0)
			{
				_stop = 1;
				if(Rnd.getChance(30))
				{
					_mode = _mode == 0 ? 1 : 0;
				}
				if(_isUpperGrade)
				{
					if(Rnd.getChance(10))
					{
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
					}
				}
			}
			else
			{
				_stop--;
			}
		}
		finally
		{
			_thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode);
			if(_anim == 0)
			{
				_fisher.sendPacket(efhr);
			}
			else
			{
				_fisher.broadcastPacket(efhr);
			}
		}
	}

	public void useReeling(int dmg, int pen)
	{
		_anim = 2;
		if(Rnd.getChance(10))
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if(_fisher == null)
		{
			return;
		}
		if(_mode == 1)
		{
			if(_deceptiveMode == 0)
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if(pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if(_deceptiveMode == 0)
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if(pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}

	public void usePumping(int dmg, int pen)
	{
		_anim = 1;
		if(Rnd.getChance(10))
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if(_fisher == null)
		{
			return;
		}
		if(_mode == 0)
		{
			if(_deceptiveMode == 0)
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if(pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if(_deceptiveMode == 0)
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if(pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}

	private void spawnMonster(L2NpcTemplate monster)
	{
		if(monster != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(monster);
				spawn.setLocx(_fisher.getX());
				spawn.setLocy(_fisher.getY());
				spawn.setLocz(_fisher.getZ());
				spawn.setAmount(1);
				spawn.setHeading(_fisher.getHeading());
				spawn.stopRespawn();
				spawn.doSpawn();
				spawn.getLastSpawn().setTarget(_fisher);
			}
			catch(Exception ignored)
			{
			}
		}
	}
}
