package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2EventMonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class Maguen extends Quest
{
	private static final int MAGUEN = 18839;

	private static final int COLLECTOR_SKILL = 9060;

	private static final int COLLAR = 15488;
	private static final int ELITE_COLLAR = 15489;

	private static final int B_PLASMA1 = 6367;
	private static final int C_PLASMA1 = 6368;
	private static final int R_PLASMA1 = 6369;

	private static final int B_BUFF = 6343;
	private static final int C_BUFF = 6365;
	private static final int R_BUFF = 6366;

	private static final int[] TRIGGERING_MOBS = {
		22750, 22751, 22752, 22753, 22757, 22758, 22759
	};

	public Maguen()
	{
		addFirstTalkId(MAGUEN);
		addSkillSeeId(MAGUEN);
		addSpellFinishedId(MAGUEN);
		addKillId(TRIGGERING_MOBS);
	}

	public static void main(String[] args)
	{
		new Maguen();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		L2EventMonsterInstance mag = (L2EventMonsterInstance) npc;
		if(mag != null)
		{
			if(event.equalsIgnoreCase("spawn"))
			{
				mag.eventSetBlockMeleeAttack(true);
				mag.setIsNoAttackingBack(true);
				mag.setIsNoRndWalk(true);

				L2PcInstance spawner = mag.eventGetSpawner();
				if(spawner != null)
				{
					if(mag.getCustomInt() == 0)
					{
						spawner.sendPacket(new ExShowScreenMessage(NpcStringId.MAGUEN_APPEARANCE, ExShowScreenMessage.MIDDLE_CENTER, 2000));
					}

					mag.setTitle(spawner.getName());
					mag.getAI().startFollow(spawner, 60);
					mag.setIsRunning(true);

					startQuestTimer("DIST_CHECK_TIMER", 1000, npc, null);
				}
				else
				{
					mag.getLocationController().delete(); // Should not happend
				}
			}
			else if(event.equalsIgnoreCase("DIST_CHECK_TIMER"))
			{
				// There is distance check in AI
				// but i decide to skip it ;)
				startQuestTimer("FIRST_TIMER", 4000, npc, null);
			}
			else if(event.equalsIgnoreCase("FIRST_TIMER"))
			{
				changeNpcState(mag);
				startQuestTimer("SECOND_TIMER", 5000 + Rnd.get(300), npc, null);
			}
			else if(event.equalsIgnoreCase("SECOND_TIMER"))
			{
				changeNpcState(mag);
				startQuestTimer("THIRD_TIMER", 4600 + Rnd.get(600), npc, null);
			}
			else if(event.equalsIgnoreCase("THIRD_TIMER"))
			{
				changeNpcState(mag);
				startQuestTimer("FORTH_TIMER", 4200 + Rnd.get(900), npc, null);
			}
			else if(event.equalsIgnoreCase("FORTH_TIMER"))
			{
				mag.setDisplayEffect(4);
				startQuestTimer("END_TIMER", 1000, npc, null);
			}
			else if(event.equalsIgnoreCase("END_TIMER"))
			{
				// If it's 'test' Maguen
				if(mag.getCustomInt() == 1)
				{
					if(mag.eventGetSpawner() != null)
					{
						mag.eventGetSpawner().stopSkillEffects(B_PLASMA1);
						mag.eventGetSpawner().stopSkillEffects(C_PLASMA1);
						mag.eventGetSpawner().stopSkillEffects(R_PLASMA1);
					}
				}
				mag.doDie(mag);
			}
			else if(event.equalsIgnoreCase("check_buffs"))
			{
				int b = getEffectLevel(player, B_PLASMA1);
				int c = getEffectLevel(player, C_PLASMA1);
				int r = getEffectLevel(player, R_PLASMA1);

				if(b == 3 && c == 0 && r == 0)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.ENOUGH_MAGUEN_PLASMA_BISTAKON_HAVE_GATHERED, ExShowScreenMessage.MIDDLE_CENTER, 2000));

					player.stopSkillEffects(B_PLASMA1);
					castRandBuff(npc, player, B_BUFF);
					giveCollar(npc, player);
				}
				else if(b == 0 && c == 3 && r == 0)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.ENOUGH_MAGUEN_PLASMA_COKRAKON_HAVE_GATHERED, ExShowScreenMessage.MIDDLE_CENTER, 2000));

					player.stopSkillEffects(C_PLASMA1);
					castRandBuff(npc, player, C_BUFF);
					giveCollar(npc, player);
				}
				else if(b == 0 && c == 0 && r == 3)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.ENOUGH_MAGUEN_PLASMA_LEPTILIKON_HAVE_GATHERED, ExShowScreenMessage.MIDDLE_CENTER, 2000));
					player.stopSkillEffects(R_PLASMA1);
					castRandBuff(npc, player, R_BUFF);
					giveCollar(npc, player);
				}
				else if(b + c + r == 3)
				{
					if(b == 1 && c == 1 && r == 1)
					{
						player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_AND_ARE_HARMONIZED, ExShowScreenMessage.MIDDLE_CENTER, 2000));
						player.stopSkillEffects(B_PLASMA1);
						player.stopSkillEffects(C_PLASMA1);
						player.stopSkillEffects(R_PLASMA1);
						switch(Rnd.get(3))
						{
							case 0:
								castRandBuff(npc, player, B_BUFF);
								break;
							case 1:
								castRandBuff(npc, player, C_BUFF);
								break;
							case 2:
								castRandBuff(npc, player, R_BUFF);
								break;
						}
						giveCollar(npc, player);
					}
					else
					{
						player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_BUT_THEY_ARE_RAMMING_INTO_EACH_OTHER_EXPLODING_AND_DYING, ExShowScreenMessage.MIDDLE_CENTER, 2000));
						player.stopSkillEffects(B_PLASMA1);
						player.stopSkillEffects(C_PLASMA1);
						player.stopSkillEffects(R_PLASMA1);
					}
				}

				npc.setDisplayEffect(4);

				startQuestTimer("END_TIMER", 3000, npc, null);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(killer.isInParty())
		{
			L2Party pt = killer.getParty();
			if(pt != null)
			{
				L2PcInstance lucky = pt.getMembers().get(Rnd.get(pt.getMemberCount()));
				int chance = 10 + 10 * pt.getMemberCount();

				if(lucky != null && lucky.isInsideRadius(npc, 1500, false, true) && Rnd.get(1000) < chance)
				{
					L2EventMonsterInstance mag = (L2EventMonsterInstance) addSpawn(MAGUEN, lucky.getX(), lucky.getY(), lucky.getZ(), lucky.getHeading(), true, 0, true);
					mag.eventSetSpawner(lucky);
					startQuestTimer("spawn", 1000, mag, null);
				}
			}
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(caster == null || npc == null)
		{
			return null;
		}

		if(npc.getNpcId() == MAGUEN && ArrayUtils.contains(targets, npc) && skill.getId() == COLLECTOR_SKILL)
		{
			L2EventMonsterInstance mag = (L2EventMonsterInstance) npc;
			if(mag.getDisplayEffect() > 0 && mag.eventGetSpawner().equals(caster))
			{
				cancelQuestTimer("FIRST_TIMER", npc, null);
				cancelQuestTimer("SECOND_TIMER", npc, null);
				cancelQuestTimer("THIRD_TIMER", npc, null);
				cancelQuestTimer("FORTH_TIMER", npc, null);
				cancelQuestTimer("END_TIMER", npc, null);

				int eff = mag.getDisplayEffect();
				if(eff == 1)
				{
					makeCast(npc, caster, B_PLASMA1);
				}
				else if(eff == 2)
				{
					makeCast(npc, caster, C_PLASMA1);
				}
				else if(eff == 3)
				{
					makeCast(npc, caster, R_PLASMA1);
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.getNpcId() == MAGUEN && ((L2EventMonsterInstance) npc).eventGetSpawner().equals(player))
		{
			if(skill.getId() == B_PLASMA1 || skill.getId() == C_PLASMA1 || skill.getId() == R_PLASMA1)
			{
				startQuestTimer("check_buffs", 1000, npc, player);
			}
		}

		return super.onSpellFinished(npc, player, skill);
	}

	private void castRandBuff(L2Npc npc, L2PcInstance player, int skillId)
	{
		SkillHolder sk = new SkillHolder(skillId, Rnd.getChance(70) ? 1 : 2);
		if(npc.getCustomInt() == 0)
		{
			npc.setTarget(player);
			npc.doCast(sk.getSkill());
		}
	}

	private void giveCollar(L2Npc npc, L2PcInstance player)
	{
		// no collar for 'test' maguens
		if(npc.getCustomInt() == 1)
		{
			return;
		}

		int i4 = Rnd.get(100);
		int i5 = Rnd.get(10);

		if(i4 == 0 && i5 != 0)
		{
			player.addItem(ProcessType.QUEST, COLLAR, 1, npc, true);
		}
		else if(i4 == 0 && i5 == 0)
		{
			player.addItem(ProcessType.QUEST, ELITE_COLLAR, 1, npc, true);
		}
	}

	private void changeNpcState(L2EventMonsterInstance mag)
	{
		mag.setDisplayEffect(4);
		int i0 = Rnd.get(3) + 1;
		mag.setDisplayEffect(i0);
		mag.broadcastPacket(new SocialAction(mag.getObjectId(), i0));
	}

	private int getEffectLevel(L2PcInstance player, int skillId)
	{
		int lvl = 0;

		L2Effect current = player.getFirstEffect(skillId);
		if(current != null)
		{
			lvl = current.getAbnormalLvl();
		}

		return lvl;
	}

	private void makeCast(L2Npc npc, L2PcInstance player, int skillId)
	{
		if(npc == null || player == null)
		{
			return;
		}

		int level = getEffectLevel(player, skillId);

		if(level < 3)
		{
			level++;
		}

		SkillHolder skill = new SkillHolder(skillId, level);
		npc.setTarget(player);
		npc.doCast(skill.getSkill());
	}
}