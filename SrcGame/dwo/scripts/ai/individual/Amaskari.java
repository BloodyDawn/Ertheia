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
package dwo.scripts.ai.individual;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * Manages Amaskari's and minions' chat and some skill usage.
 * @author GKR
 */
public class Amaskari extends Quest
{
	private static final int AMASKARI = 22449;
	private static final int AMASKARI_PRISONER = 22450;

	private static final int BUFF_ID = 4632;
	private static SkillHolder[] BUFF = {
		new SkillHolder(BUFF_ID, 1), new SkillHolder(BUFF_ID, 2), new SkillHolder(BUFF_ID, 3)
	};
	// private static SkillHolder INVINCIBILITY = new SkillHolder(5417, 1);

	private static final NpcStringId[] AMASKARI_NPCSTRING_ID = {
		NpcStringId.ILL_MAKE_EVERYONE_FEEL_THE_SAME_SUFFERING_AS_ME,
		NpcStringId.HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY, NpcStringId.MORE_NEED_MORE_SEVERE_PAIN,
		NpcStringId.SOMETHING_IS_BURNING_INSIDE_MY_BODY
	};

	private static final NpcStringId[] MINIONS_NPCSTRING_ID = {
		NpcStringId.AHH_MY_LIFE_IS_BEING_DRAINED_OUT, NpcStringId.THANK_YOU_FOR_SAVING_ME,
		NpcStringId.IT_WILL_KILL_EVERYONE, NpcStringId.EEEK_I_FEEL_SICKYOW
	};

	public Amaskari()
	{
		addKillId(AMASKARI);
		addKillId(AMASKARI_PRISONER);
		addAttackId(AMASKARI);
		addSpawnId(AMASKARI_PRISONER);
	}

	public static void main(String[] args)
	{
		new Amaskari();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if(npc.getNpcId() == AMASKARI && Rnd.get(1000) < 25)
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), AMASKARI_NPCSTRING_ID[0]));
			((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().stream().filter(minion -> minion != null && !minion.isDead() && Rnd.get(10) == 0).forEach(minion -> {
				minion.broadcastPacket(new NS(minion.getObjectId(), ChatType.ALL, minion.getNpcId(), MINIONS_NPCSTRING_ID[0]));
				minion.setCurrentHp(minion.getCurrentHp() - minion.getCurrentHp() / 5);
			});
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("stop_toggle"))
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), AMASKARI_NPCSTRING_ID[2]));
			((L2MonsterInstance) npc).clearAggroList();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			npc.setIsInvul(false);
			// npc.doCast(INVINCIBILITY.getSkill())
		}
		else if(event.equalsIgnoreCase("onspawn_msg") && npc != null && !npc.isDead())
		{
			if(Rnd.getChance(80))
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), MINIONS_NPCSTRING_ID[2]));
			}
			else if(Rnd.getChance(60))
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), MINIONS_NPCSTRING_ID[3]));
			}
			startQuestTimer("onspawn_msg", (Rnd.get(8) + 1) * 30000, npc, null);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == AMASKARI_PRISONER)
		{
			L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if(master != null && !master.isDead())
			{
				master.broadcastPacket(new NS(master.getObjectId(), ChatType.ALL, master.getNpcId(), AMASKARI_NPCSTRING_ID[1]));
				L2Effect e = master.getFirstEffect(BUFF_ID);
				if(e != null && e.getAbnormalLvl() == 3 && master.isInvul())
				{
					master.setCurrentHp(master.getCurrentHp() + master.getCurrentHp() / 5);
				}
				else
				{
					master.clearAggroList();
					master.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					if(e == null)
					{
						master.doCast(BUFF[0].getSkill());
					}
					else if(e.getAbnormalLvl() < 3)
					{
						master.doCast(BUFF[e.getAbnormalLvl()].getSkill());
					}
					else
					{
						master.broadcastPacket(new NS(master.getObjectId(), ChatType.ALL, master.getNpcId(), AMASKARI_NPCSTRING_ID[3]));
						// master.doCast(INVINCIBILITY.getSkill())
						master.setIsInvul(true);
						startQuestTimer("stop_toggle", 10000, master, null);
					}
				}
			}
		}
		else if(npc.getNpcId() == AMASKARI)
		{
			((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().stream().filter(minion -> minion != null && !minion.isDead()).forEach(minion -> {
				if(Rnd.get(1000) > 300)
				{
					minion.broadcastPacket(new NS(minion.getObjectId(), ChatType.ALL, minion.getNpcId(), MINIONS_NPCSTRING_ID[1]));
				}

				HellboundManager.getInstance().updateTrust(30, true);
				minion.getLocationController().delete();
			});
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			startQuestTimer("onspawn_msg", (Rnd.get(3) + 1) * 30000, npc, null);
		}
		return super.onSpawn(npc);
	}
}
