package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.01.12
 * Time: 4:07
 */

public class _00465_WeAreFriends extends Quest
{
	// Квестовые персонажи
	private static final int _fairy = 32921;
	private static final int _fairyFromCocone = 32923;
	private static final int littleCocone = 32919;

	// Квестовые предметы
	private static final int _gratitudeSign = 17377;
	private static final int _forestFairyHorn = 17378;
	private static final int _proofPromises = 30384;

	public _00465_WeAreFriends()
	{
		addStartNpc(_fairy);
		addTalkId(_fairy, _fairyFromCocone);
		addAttackId(littleCocone);
		addSkillSeeId(littleCocone);
		questItemIds = new int[]{_gratitudeSign};
	}

	public static void main(String[] args)
	{
		new _00465_WeAreFriends();
	}

	@Override
	public int getQuestId()
	{
		return 465;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(Rnd.getChance(3) && npc.getNpcId() == littleCocone || npc.getNpcId() == littleCocone)
		{
			L2Npc fairy = addSpawn(_fairyFromCocone, npc.getLoc(), 0, false, 0);
			fairy.setOwner(attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("32921-02.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32921-05.htm"))
		{
			st.giveItem(_forestFairyHorn);
			st.giveItems(_proofPromises, 3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.DAILY);
		}
		else if(event.equalsIgnoreCase("32923-01.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			st.giveItem(_gratitudeSign);
			if(st.getQuestItemsCount(_gratitudeSign) >= 2)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));
			npc.getLocationController().delete();
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == _fairy)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32921-noday.htm";
				case CREATED:
					if(player.getLevel() >= 88)
					{
						return "32921-00.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32921-nolvl.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32921-03.htm";
					}
					if(st.getCond() == 2)
					{
						return "32921-04.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == _fairyFromCocone)
		{
			if(st.isStarted())
			{
				return npc.getOwner().equals(player) ? "32923-00.htm" : "32923-np.htm";
			}
			else
			{
				return "32923-nq.htm";
			}
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(skill.getId() == 12002)
		{
			if(Rnd.getChance(5) && npc.getNpcId() == littleCocone || npc.getNpcId() == littleCocone)
			{
				L2Npc fairy = addSpawn(_fairyFromCocone, npc.getLoc(), 0, false, 0);
				fairy.setOwner(caster);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() < 88;

	}
}