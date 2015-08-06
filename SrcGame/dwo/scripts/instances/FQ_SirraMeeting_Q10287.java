package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.scripts.quests._10287_StoryOfThoseLeft;

public class FQ_SirraMeeting_Q10287 extends Quest
{
	// Точка входа
	private static final Location ENTRY_POINT = new Location(-23530, -8963, -5388);
	private static FQ_SirraMeeting_Q10287 _instanceQ10287;

	private static boolean checkConditions(L2PcInstance player)
	{
		if(player.getLevel() < 82)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		QuestState st = player.getQuestState(_10287_StoryOfThoseLeft.class);
		if(st == null || !st.isStarted())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}

		st.setCond(2);
		st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);

		return true;
	}

	public static void main(String[] args)
	{
		_instanceQ10287 = new FQ_SirraMeeting_Q10287();
	}

	public static FQ_SirraMeeting_Q10287 getInstance()
	{
		return _instanceQ10287;
	}

	public void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof SirraMeetingWorld3))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
		else
		{
			if(!checkConditions(player))
			{
				return;
			}

			int instanceId = InstanceManager.getInstance().createDynamicInstance("FQ_SirraMeeting_Q10287.xml");

			world = new SirraMeetingWorld3();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.JINIA_GUILD_HIDEOUT_LEFTS_STORY.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTRY_POINT, instanceId);
		}
	}

	private class SirraMeetingWorld3 extends InstanceWorld
	{
		public SirraMeetingWorld3()
		{
		}
	}
}
