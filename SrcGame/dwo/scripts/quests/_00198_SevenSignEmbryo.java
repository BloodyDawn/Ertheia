package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;

import java.util.ArrayList;
import java.util.List;

public class _00198_SevenSignEmbryo extends Quest
{
	private static final int jeina = 32617;
	private static final int franz = 32597;
	private static final int wood = 32593;
	private static final int[] NPCkill = {27399, 27402, 27346};
	private static final int main_mob = 27346;
	// Телепорты
	private static final Location ENTRY_POINT = new Location(-23749, -8959, -5384);
	private static final Location EXIT_POINT = new Location(147031, 23788, -1984);

	public _00198_SevenSignEmbryo()
	{
		addStartNpc(wood);
		addTalkId(jeina, franz, wood);
		addKillId(main_mob);
	}

	public static void main(String[] args)
	{
		new _00198_SevenSignEmbryo();
	}

	private void enterInstance(L2PcInstance player)
	{
		synchronized(this)
		{
			InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(world instanceof HideoutWorld)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
				return;
			}
			world = new HideoutWorld();
			world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			world.templateId = InstanceZoneId.HIDEOUT_OF_THE_DAWN.getId(); // TODO: Strange, EQ_HideoutOfTheDawn has same instance template id
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			int time = 3600000;
			instance.setDuration(time);
			instance.setEmptyDestroyTime(10);
			instance.setSpawnLoc(EXIT_POINT);
			InstanceManager.getInstance().addWorld(world);
			instance.setName("Hideout of the Dawn");
			((HideoutWorld) world).franz = addSpawn(franz, -23976, -8964, -5384, 0, false, 0, false, world.instanceId);
			addSpawn(jeina, -23961, -8892, -5384, 0, false, 0, false, world.instanceId);
			player.teleToInstance(ENTRY_POINT, world.instanceId);
		}
	}

	@Override
	public int getQuestId()
	{
		return 198;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "32593-02.htm":
				st.startQuest();
				break;
			case "32593-03.htm":
				enterInstance(player);
				break;
			case "32597-04.htm":
				if(npc.getInstanceId() > 0)
				{
					HideoutWorld world = (HideoutWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
					if(world.mobSpawned)
					{
						return "32597-05.htm";
					}
					else
					{
						for(int mobid : NPCkill)
						{
							L2Npc mob = addSpawn(mobid, -23734, -9184, -5384, 0, false, 900000, false, npc.getInstanceId());
							mob.setRunning();
							((L2Attackable) mob).addDamageHate(player, 0, 999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
							world.mobs.add(mob);
						}
						world.mobSpawned = true;
					}
				}
				break;
			case "32597-09.htm":
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.WE_WILL_BE_WITH_YOU_ALWAYS));
				st.takeItems(14360, -1);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32582-01.htm":
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		if(npc.getInstanceId() > 0)
		{
			HideoutWorld world = (HideoutWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(st.getCond() == 1)
			{
				world.franz.broadcastPacket(new NS(world.franz.getObjectId(), ChatType.NPC_ALL, world.franz.getNpcId(), NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP).addStringParameter(player.getName()));
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
				for(L2Npc i : world.mobs)
				{
					i.getLocationController().delete();
				}
				st.giveItems(14360, 1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_EMBRYO);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState st1 = player.getQuestState(_00197_SevenSignTheSacredBookOfSeal.class);
		String htmltext = getNoQuestMsg(player);
		if(st == null || player.getLevel() < 79)
		{
			return getLowLevelMsg(79);
		}
		int cond = st.getCond();
		if(st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		else if(npc.getNpcId() == wood)
		{
			if(st1 != null && st1.isCompleted())
			{
				if(cond == 0)
				{
					if(player.getLevel() >= 79)
					{
						htmltext = "32593-00.htm";
					}
					else
					{
						htmltext = "32593-0a.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else if(cond < 3)
				{
					htmltext = "32593-02.htm";
				}
				else if(cond == 3)
				{
					if(player.isSubClassActive())
					{
						return "subclass_forbidden.htm";
					}
					else
					{
						st.rewardItems(5575, 1500000);
						if(player.getItemsCount(15312) == 0)
						{
							st.giveItem(15312);
						}
						st.addExpAndSp(67500000, 15000000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						htmltext = "32593-04.htm";
					}
				}
			}
			else
			{
				htmltext = "32593-0a.htm";
			}
		}
		else if(npc.getNpcId() == franz)
		{
			if(cond == 1)
			{
				htmltext = "32597-00.htm";
			}
			else if(cond == 2)
			{
				htmltext = "32597-06.htm";
			}
			else if(cond >= 3)
			{
				htmltext = "32597-10.htm";
			}
		}
		else if(npc.getNpcId() == jeina)
		{
			htmltext = "32582-00.htm";
		}
		return htmltext;
	}

	private class HideoutWorld extends InstanceManager.InstanceWorld
	{
		private List<L2Npc> mobs = new ArrayList<>();
		private boolean mobSpawned;
		private L2Npc franz;
	}
}

