package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00693_DefeatingDragonkinRemnants extends Quest
{
	// NPC's
	private static final int EDRIC = 32527;
	private static final int REWARD_CHANCE = 60;

	public _00693_DefeatingDragonkinRemnants()
	{

		addStartNpc(EDRIC);
		addTalkId(EDRIC);
	}

	public static void main(String[] args)
	{
		new _00693_DefeatingDragonkinRemnants();
	}

	private boolean giveReward(QuestState st, int finishDiff)
	{
		if(Rnd.getChance(REWARD_CHANCE))
		{
			if(finishDiff == 0)
			{
				return false;
			}
			if(finishDiff < 5)
			{
				st.giveItems(14638, 1);
			}
			else if(finishDiff < 10)
			{
				st.giveItems(14637, 1);
			}
			else if(finishDiff < 15)
			{
				st.giveItems(14636, 1);
			}
			else if(finishDiff < 20)
			{
				st.giveItems(14635, 1);
			}
			return true;
		}
		return false;
	}

	private String prepareHtml(L2PcInstance player, String filename, String replace)
	{
		return getHtm(player.getLang(), filename).replace("%replace%", replace);
	}

	@Override
	public int getQuestId()
	{
		return 693;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == EDRIC)
		{
			if(event.equalsIgnoreCase("32527-05.htm"))
			{
				st.startQuest();
				st.unset("timeDiff"); // if any
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == EDRIC)
		{
			if(player.getLevel() < 75)
			{
				return "32527-00.htm";
			}
			else if(st.isCreated())
			{
				return "32527-01.htm";
			}
			else if(player.isGM())
			{
				st.startQuest();
				return "32527-10.html";
			}
			else if(st.getCond() == 1)
			{
				L2Party party = player.getParty();
				if(st.getInt("timeDiff") > 0)
				{
					if(giveReward(st, st.getInt("timeDiff")))
					{
						// Clear quest
						st.unset("timeDiff");
						st.unset("cond");
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.REPEATABLE);
						return "32527-reward.html";
					}
					else
					{
						// Clear quest
						st.unset("timeDiff");
						st.unset("cond");
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.REPEATABLE);
						return "32527-noreward.html";
					}
				}
				else if(party == null)
				{
					return "32527-noparty.html";
				}
				else if(!party.getLeader().equals(player))
				{
					return prepareHtml(player, "32527-noleader.html", party.getLeader().getName());
				}
				else
				{
					for(L2PcInstance pm : party.getMembers())
					{
						QuestState state = pm.getQuestState(getClass());
						if(state == null || state.getCond() != 1)
						{
							return prepareHtml(player, "32527-noquest.html", pm.getName());
						}
					}
					return "32527-10.html";
				}
			}
		}
		return null;
	}
}