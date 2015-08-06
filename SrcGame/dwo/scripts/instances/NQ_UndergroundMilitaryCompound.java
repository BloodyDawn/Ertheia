package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.scripts.quests._10323_GoingIntoARealWar;

public class NQ_UndergroundMilitaryCompound extends Quest
{
	private static final int HOLDEN = 33194;

	private static final int TRAINING_KEY = 17574;

	private static final Location ENTER_POINT = new Location(-113808, 247733, -7873);

	public NQ_UndergroundMilitaryCompound()
	{
		addAskId(HOLDEN, -3500);
	}

	public static void main(String[] args)
	{
		new NQ_UndergroundMilitaryCompound();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -3500)
		{
			if(reply == 1)
			{
				QuestState questswitch = player.getQuestState(_10323_GoingIntoARealWar.class);

				if(questswitch == null)
				{
					return "si_illusion_tel15002.htm";
				}
				else if(questswitch.isCompleted())
				{
					return "si_illusion_tel15004.htm";
				}
				else if(player.getInventory().getItemsByItemId(TRAINING_KEY) == null)
				{
					return "si_illusion_tel15003.htm";
				}
				else
				{
					if(questswitch.getCond() == 1)
					{
						questswitch.setCond(2);
						questswitch.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					enterInstance(player);
				}
			}
		}
		return null;
	}

	protected void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof UMCorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTER_POINT, world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("NQ_UndergroundMilitaryCompound.xml");

			world = new UMCorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.UNDERGROUND_MILITARY_COMPOUND.getId();
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTER_POINT, instanceId);
		}
	}

	private class UMCorld extends InstanceWorld
	{
		public UMCorld()
		{
		}
	}
}