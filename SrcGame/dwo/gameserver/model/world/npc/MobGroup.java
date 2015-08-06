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
package dwo.gameserver.model.world.npc;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2ControllableMobAI;
import dwo.gameserver.model.actor.instance.L2ControllableMobInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2GroupSpawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author littlecrow
 */
public class MobGroup
{
	private L2NpcTemplate _npcTemplate;
	private int _groupId;
	private int _maxMobCount;

	private List<L2ControllableMobInstance> _mobs;

	public MobGroup(int groupId, L2NpcTemplate npcTemplate, int maxMobCount)
	{
		_groupId = groupId;
		_npcTemplate = npcTemplate;
		_maxMobCount = maxMobCount;
	}

	public int getActiveMobCount()
	{
		return getMobs().size();
	}

	public int getGroupId()
	{
		return _groupId;
	}

	public int getMaxMobCount()
	{
		return _maxMobCount;
	}

	public List<L2ControllableMobInstance> getMobs()
	{
		if(_mobs == null)
		{
			_mobs = new FastList<>();
		}

		return _mobs;
	}

	public String getStatus()
	{
		try
		{
			L2ControllableMobAI mobGroupAI = (L2ControllableMobAI) getMobs().get(0).getAI();

			switch(mobGroupAI.getAlternateAI())
			{
				case L2ControllableMobAI.AI_NORMAL:
					return "Idle";
				case L2ControllableMobAI.AI_FORCEATTACK:
					return "Force Attacking";
				case L2ControllableMobAI.AI_FOLLOW:
					return "Following";
				case L2ControllableMobAI.AI_CAST:
					return "Casting";
				case L2ControllableMobAI.AI_ATTACK_GROUP:
					return "Attacking Group";
				default:
					return "Idle";
			}
		}
		catch(Exception e)
		{
			return "Unspawned";
		}
	}

	public L2NpcTemplate getTemplate()
	{
		return _npcTemplate;
	}

	public boolean isGroupMember(L2ControllableMobInstance mobInst)
	{
		for(L2ControllableMobInstance groupMember : getMobs())
		{
			if(groupMember == null)
			{
				continue;
			}

			if(groupMember.getObjectId() == mobInst.getObjectId())
			{
				return true;
			}
		}

		return false;
	}

	public void spawnGroup(int x, int y, int z)
	{
		if(getActiveMobCount() > 0) // can't spawn mob if already done
		{
			return;
		}

		try
		{
			for(int i = 0; i < _maxMobCount; i++)
			{
				L2GroupSpawn spawn = new L2GroupSpawn(_npcTemplate);

				int signX = Rnd.get(2) == 0 ? -1 : 1;
				int signY = Rnd.get(2) == 0 ? -1 : 1;
				int randX = Rnd.get(MobGroupTable.RANDOM_RANGE);
				int randY = Rnd.get(MobGroupTable.RANDOM_RANGE);

				spawn.setLocx(x + signX * randX);
				spawn.setLocy(y + signY * randY);
				spawn.setLocz(z);
				spawn.stopRespawn();

				SpawnTable.getInstance().addNewSpawn(spawn);
				getMobs().add((L2ControllableMobInstance) spawn.doGroupSpawn());
			}
		}
		catch(Exception ignored)
		{
		}
	}

	public void spawnGroup(L2PcInstance activeChar)
	{
		spawnGroup(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}

	public void teleportGroup(L2PcInstance player)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			if(!mobInst.isDead())
			{
				int x = player.getX() + Rnd.get(50);
				int y = player.getY() + Rnd.get(50);

				mobInst.teleToLocation(x, y, player.getZ(), true);
				L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
				ai.follow(player);
			}
		}
	}

	public L2ControllableMobInstance getRandomMob()
	{
		removeDead();

		if(getActiveMobCount() == 0)
		{
			return null;
		}

		return getMobs().get(Rnd.get(getActiveMobCount()));
	}

	public void unspawnGroup()
	{
		removeDead();

		if(getActiveMobCount() == 0)
		{
			return;
		}

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			if(!mobInst.isDead())
			{
				mobInst.getLocationController().delete();
			}

			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn());
		}

		getMobs().clear();
	}

	public void killGroup(L2PcInstance activeChar)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			if(!mobInst.isDead())
			{
				mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1, activeChar, null);
			}

			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn());
		}

		getMobs().clear();
	}

	public void setAttackRandom()
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public void setAttackTarget(L2Character target)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.forceAttack(target);
		}
	}

	public void setIdleMode()
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.stop();
		}
	}

	public void returnGroup(L2Character activeChar)
	{
		setIdleMode();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			int signX = Rnd.get(2) == 0 ? -1 : 1;
			int signY = Rnd.get(2) == 0 ? -1 : 1;
			int randX = Rnd.get(MobGroupTable.RANDOM_RANGE);
			int randY = Rnd.get(MobGroupTable.RANDOM_RANGE);
			Location loc = new Location(activeChar.getX() + signX * randX, activeChar.getY() + signY * randY, activeChar.getZ());
			((L2ControllableMobAI) mobInst.getAI()).move(loc);
		}
	}

	public void setFollowMode(L2Character character)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.follow(character);
		}
	}

	public void setCastMode()
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_CAST);
		}
	}

	public void setNoMoveMode(boolean enabled)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setNotMoving(enabled);
		}
	}

	protected void removeDead()
	{
		List<L2ControllableMobInstance> deadMobs = getMobs().stream().filter(mobInst -> mobInst != null && mobInst.isDead()).collect(Collectors.toCollection(FastList::new));

		getMobs().removeAll(deadMobs);
	}

	public void setInvul(boolean invulState)
	{
		removeDead();

		getMobs().stream().filter(mobInst -> mobInst != null).forEach(mobInst -> mobInst.setInvul(invulState));
	}

	public void setAttackGroup(MobGroup otherGrp)
	{
		removeDead();

		for(L2ControllableMobInstance mobInst : getMobs())
		{
			if(mobInst == null)
			{
				continue;
			}

			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.forceAttackGroup(otherGrp);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
}