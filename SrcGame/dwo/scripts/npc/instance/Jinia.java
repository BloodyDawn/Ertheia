package dwo.scripts.npc.instance;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.scripts.instances.RB_Freya;
import dwo.scripts.quests._10283_RequestOfIceMerchant;
import dwo.scripts.quests._10284_AcquisitionOfDivineSword;
import dwo.scripts.quests._10285_MeetingSirra;
import dwo.scripts.quests._10286_ReunionWithSirra;
import dwo.scripts.quests._10287_StoryOfThoseLeft;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.12
 * Time: 9:59
 */

public class Jinia extends Quest
{
	private static final int JiniaNPC = 32760;
	private static final int JiniaNPC2 = 32781;

	public Jinia()
	{
		addFirstTalkId(JiniaNPC, JiniaNPC2);
		addAskId(JiniaNPC2, -2314);
	}

	public static void main(String[] args)
	{
		new Jinia();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == JiniaNPC2)
		{
			if(ask == -2314)
			{
				switch(reply)
				{
					case 1:
						RB_Freya.getInstance().enterInstance(player, InstanceZoneId.ICE_QUEENS_CASTLE_2.getId());
						break;
					case 2:
						RB_Freya.getInstance().enterInstance(player, InstanceZoneId.ICE_QUEENS_CASTLE_ULTIMATE_BATTLE.getId());
						break;
					case 3:
						QuestState st = player.getQuestState(_10286_ReunionWithSirra.class);
						if(player.getItemsCount(15469) > 0 || player.getItemsCount(15470) > 0)
						{
							return "jinia_npc2009.htm";
						}
						else if(st != null && st.isCompleted())
						{
							player.addItem(ProcessType.QUEST, 15469, 1, npc, true);
							return "jinia_npc2008.htm";
						}
						else
						{
							player.addItem(ProcessType.QUEST, 15470, 1, npc, true);
							return "jinia_npc2008.htm";
						}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == JiniaNPC)
		{
			QuestState st10283 = player.getQuestState(_10283_RequestOfIceMerchant.class);
			QuestState st10284 = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
			QuestState st10285 = player.getQuestState(_10285_MeetingSirra.class);
			QuestState st10286 = player.getQuestState(_10286_ReunionWithSirra.class);
			QuestState st10287 = player.getQuestState(_10287_StoryOfThoseLeft.class);

			InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(st10283 != null && st10284 == null)
			{
				if(st10283.getMemoState() == 2)
				{
					return npc.getOwner().getObjectId() == player.getObjectId() ? "jinia_npc001a.htm" : "jinia_npc001b.htm";
				}
				else if(st10283.isCompleted())
				{
					return "jinia_npc_q10283_04.htm";
				}
			}

			if(st10284 != null && st10285 == null)
			{
				if(st10284.getMemoState() == 1)
				{
					if(st10284.getMemoStateEx(1) == 0 && st10284.getMemoStateEx(2) == 0 && st10284.getMemoStateEx(3) == 0)
					{
						return "jinia_npc_q10284_01.htm";
					}
					else if(st10284.getMemoStateEx(1) == 1 && st10284.getMemoStateEx(2) == 0 && st10284.getMemoStateEx(3) == 0)
					{
						return "jinia_npc_q10284_01a.htm";
					}
					else if(st10284.getMemoStateEx(1) == 0 && st10284.getMemoStateEx(2) == 1 && st10284.getMemoStateEx(3) == 0)
					{
						return "jinia_npc_q10284_01b.htm";
					}
					else if(st10284.getMemoStateEx(1) == 0 && st10284.getMemoStateEx(2) == 0 && st10284.getMemoStateEx(3) == 1)
					{
						return "jinia_npc_q10284_01c.htm";
					}
					else if(st10284.getMemoStateEx(1) == 0 && st10284.getMemoStateEx(2) == 1 && st10284.getMemoStateEx(3) == 1)
					{
						return "jinia_npc_q10284_01d.htm";
					}
					else if(st10284.getMemoStateEx(1) == 1 && st10284.getMemoStateEx(2) == 0 && st10284.getMemoStateEx(3) == 1)
					{
						return "jinia_npc_q10284_01e.htm";
					}
					else if(st10284.getMemoStateEx(1) == 1 && st10284.getMemoStateEx(2) == 1 && st10284.getMemoStateEx(3) == 0)
					{
						return "jinia_npc_q10284_01f.htm";
					}
					else if(st10284.getMemoStateEx(1) == 1 && st10284.getMemoStateEx(2) == 1 && st10284.getMemoStateEx(3) == 1)
					{
						return "jinia_npc_q10284_01g.htm";
					}
				}
			}
			if(st10285 != null && st10286 == null)
			{
				if(st10285.getMemoState() == 1)
				{
					switch(st10285.getMemoStateEx(1))
					{
						case 0:
							return "jinia_npc_q10285_01.htm";
						case 1:
							return "jinia_npc_q10285_03.htm";
						case 2:
							return "jinia_npc_q10285_04.htm";
						case 3:
							return "jinia_npc_q10285_07.htm";
						case 4:
							return "jinia_npc_q10285_08.htm";
						case 5:
							return "jinia_npc_q10285_13.htm";

					}
				}
			}
			if(st10286 != null && st10287 == null)
			{
				if(st10286.getMemoState() == 1)
				{
					switch(st10286.getMemoStateEx(1))
					{
						case 0:
							if(world != null && world.templateId == 145)
							{
								return "jinia_npc_q10286_01.htm";
							}
							break;
						case 1:
							if(world != null && world.templateId == 145)
							{
								return "jinia_npc_q10286_07.htm";
							}
							break;
						case 2:
							if(world != null && world.templateId == 145)
							{
								return "jinia_npc_q10286_08.htm";
							}
							break;
					}
				}
			}
			if(st10287 != null)
			{
				if(st10287.getMemoState() == 1)
				{
					switch(st10287.getMemoStateEx(1))
					{
						case 0:
							if(world != null && world.templateId == 146)
							{
								return "jinia_npc_q10287_01.htm";
							}
							break;
						case 1:
							if(world != null && world.templateId == 146)
							{
								if(st10287.getMemoStateEx(2) == 0)
								{
									return "jinia_npc_q10287_04.htm";
								}
								else if(st10287.getMemoStateEx(2) == 1)
								{
									st10287.setMemoState(2);
									st10287.setMemoStateEx(1, 0);
									st10287.setMemoStateEx(2, 0);
									st10287.setCond(5);
									st10287.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									InstanceManager.getInstance().destroyInstance(player.getInstanceId());
									return "jinia_npc_q10287_05.htm";
								}
							}
							break;
					}
				}
			}
			return "jinia_npc001.htm";
		}
		if(npc.getNpcId() == JiniaNPC2)
		{
			QuestState st10285 = player.getQuestState(_10285_MeetingSirra.class);
			QuestState st10286 = player.getQuestState(_10286_ReunionWithSirra.class);

			if(st10285 != null && st10285.isStarted() && (st10285.getCond() == 8 || st10285.getCond() == 9) && st10286 == null)
			{
				return "jinia_npc2_q10285_01.htm";
			}
			if(st10286 != null && st10286.isStarted())
			{
				if(st10286.getMemoState() == 2)
				{
					return "jinia_npc2_q10286_01.htm";
				}
				else if(st10286.getMemoState() == 10)
				{
					st10286.addExpAndSp(2152200, 181070);
					st10286.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st10286.exitQuest(QuestType.ONE_TIME);
					return "jinia_npc2_q10286_08.htm";
				}
				else
				{
					return "jinia_npc2_q10286_10.htm";
				}
			}
			return player.getLevel() < 82 ? "jinia_npc2001.htm" : "jinia_npc2002.htm";
		}
		return null;
	}
}