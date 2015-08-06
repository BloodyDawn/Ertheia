package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 10.06.12
 * Time: 16:20
 */

public class _00642_APowerfulPrimevalCreature extends Quest
{
	// Квестовые нпц
	private static final int DINN = 32105;

	// Квестовые предметы
	private static final int DINOSAUR_TISSUE = 8774;
	private static final int DINOSAUR_EGG = 8775;

	private static final Map<Integer, Integer> MOBS_TISSUE = new HashMap<>();

	static
	{
		MOBS_TISSUE.put(22196, 309); // Velociraptor
		MOBS_TISSUE.put(22197, 309); // Velociraptor
		MOBS_TISSUE.put(22198, 309); // Velociraptor
		MOBS_TISSUE.put(22199, 309); // Pterosaur
		MOBS_TISSUE.put(22215, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22216, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22217, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22218, 309); // Velociraptor
		MOBS_TISSUE.put(22223, 309); // Velociraptor
	}

	private static final int ANCIENT_EGG = 18344;

	public _00642_APowerfulPrimevalCreature()
	{
		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(ANCIENT_EGG);

		MOBS_TISSUE.keySet().forEach(this::addKillId);

		questItemIds = new int[]{
			DINOSAUR_TISSUE, DINOSAUR_EGG
		};
	}

	public static void main(String[] args)
	{
		new _00642_APowerfulPrimevalCreature();
	}

	@Override
	public int getQuestId()
	{
		return 642;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		switch(event)
		{
			case "32105-05.html":
				st.startQuest();
				break;
			case "32105-06.htm":
				st.exitQuest(QuestType.REPEATABLE);
				break;
			case "32105-09.html":
				if(st.hasQuestItems(DINOSAUR_TISSUE))
				{
					st.giveAdena(5000 * st.getQuestItemsCount(DINOSAUR_TISSUE), true);
					st.takeItems(DINOSAUR_TISSUE, -1);
				}
				break;
			case "exit":
				if(st.hasQuestItems(DINOSAUR_TISSUE))
				{
					st.giveAdena(5000 * st.getQuestItemsCount(DINOSAUR_TISSUE), true);
					st.exitQuest(QuestType.REPEATABLE);
					return "32105-12.html";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32105-13.html";
				}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return null;
		}

		QuestState st = partyMember.getQuestState(getClass());
		int npcId = npc.getNpcId();
		if(MOBS_TISSUE.containsKey(npcId))
		{
			int chance = (int) (MOBS_TISSUE.get(npcId) * Config.RATE_QUEST_DROP % 1000);
			if(Rnd.get(1000) < chance)
			{
				st.rewardItems(DINOSAUR_TISSUE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		else if(npcId == ANCIENT_EGG)
		{
			st.rewardItems(DINOSAUR_EGG, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		switch(st.getState())
		{
			case CREATED:
				if(player.getLevel() < 75)
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32105-01.htm";
				}
				else
				{
					return "32105-02.htm";
				}
			case STARTED:
				return !st.hasQuestItems(DINOSAUR_TISSUE) && !st.hasQuestItems(DINOSAUR_EGG) ? "32105-07.html" : "32105-08.html";
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 75;
	}
}