package dwo.scripts.events;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.util.Util;

public class Allegria extends Quest
{
	private static final int AllegriaNPC = 32600;
	private static final SkillHolder birthDayBuff = new SkillHolder(5950, 1);
	private static final int[] GateKeepers = {
		30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275,
		31320, 31964, 32163
	};
	private static boolean isAllegriaSpawned;

	public Allegria()
	{
		addAskId(AllegriaNPC, 20001);
		addAskId(GateKeepers, 20003);
		addStartNpc(AllegriaNPC);
		addFirstTalkId(AllegriaNPC);
		addTalkId(AllegriaNPC);
		addStartNpc(GateKeepers);
		addTalkId(GateKeepers);
	}

	public static void main(String[] args)
	{
		new Allegria();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("despawn_npc"))
		{
			npc.doDie(player);
			isAllegriaSpawned = false;
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 20001)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(13488) > 0)
				{
					return "event_alegria005.htm";
				}

				if(player.getDaysToBirthDay() == 0)
				{
					// Даем плюшки на день рождения
					player.addItem(ProcessType.NPC, 13488, 1, npc, true);
					npc.setTarget(player);
					birthDayBuff.getSkill().getEffects(npc, player);
					npc.broadcastPacket(new MagicSkillUse(player, birthDayBuff.getSkillId(), birthDayBuff.getSkillLvl(), 1000, 0));

					// Удаляем Аллегрию
					npc.doDie(player);
					isAllegriaSpawned = false;
				}
				else
				{
					return "event_alegria003.htm";
				}
			}
		}
		else if(ask == 20003)
		{
			if(player.getDaysToBirthDay() == 0)
			{
				if(player.getItemsCount(13488) > 0)
				{
					return "nobirthday002.htm";
				}
				else if(isAllegriaSpawned)
				{
					return "event_alegria008.htm";
				}
				else
				{
					if(Util.checkIfInRange(50, npc, player, true))
					{
						return "event_alegria007.htm";
					}
					else
					{
						player.sendPacket(new PlaySound(1, "HB01", 0, 0, 0, 0, 0));
						L2Npc spawned = addSpawn(32600, (player.getX() + npc.getX()) / 2, (player.getY() + npc.getY()) / 2, player.getZ(), Util.calculateHeadingFrom(player, npc) * 182, false, 0, true);
						startQuestTimer("despawn_npc", 60000, spawned, player);
						isAllegriaSpawned = true;
					}
				}
			}
			else
			{
				return "nobirthday001.htm";
			}
		}
		return null;
	}
}