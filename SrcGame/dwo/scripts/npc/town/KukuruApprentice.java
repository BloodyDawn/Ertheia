package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.11.12
 * Time: 18:34
 */

public class KukuruApprentice extends Quest
{
	// От этого гнома начинаются гонки
	private static final int PracticantRace = 33199;

	// Этот гном просто дает покататься на Кукуру
	private static final int PracticantOrdinal = 33124;

	// Птичко, за которой надо гнаться
	private static final int RunningKukuru = 33200;
	private static final SkillHolder TransformToKukuru = new SkillHolder(9204, 1);
	// Финишная зона: либо ты, либо курица - покажи ей кто мужик! :D
	private L2ZoneType FinishZone = ZoneManager.getInstance().getZoneById(33008);

	public KukuruApprentice()
	{
		addAskId(PracticantOrdinal, -3530);
		addAskId(PracticantRace, -3530);
		addEnterZoneId(FinishZone.getId());
	}

	public static void main(String[] args)
	{
		new KukuruApprentice();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == PracticantOrdinal)
		{
			if(player.getPets().isEmpty() && !player.isMounted() && !player.isTransformed())
			{
				TransformToKukuru.getSkill().getEffects(player, player);
			}
			return null;
		}
		if(npc.getNpcId() == PracticantRace)
		{
			boolean isKukuruAlreadyRun = player.getVariablesController().get(getClass().getSimpleName(), Boolean.class, false);
			if(isKukuruAlreadyRun)
			{
				return null;
			}
			else
			{
				TransformToKukuru.getSkill().getEffects(player, player);
				player.getVariablesController().set(getClass().getSimpleName(), true);
				startKukuru(player);
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(zone.equals(FinishZone))
		{
			if(character instanceof L2PcInstance)
			{
				boolean isKukuruAlreadyRun = character.getActingPlayer().getVariablesController().get(getClass().getSimpleName(), Boolean.class, false);
				if(isKukuruAlreadyRun)
				{
					character.getActingPlayer().addItem(ProcessType.NPC, PcInventory.ADENA_ID, 50, null, true);
					character.getActingPlayer().sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1600029), ExShowScreenMessage.TOP_CENTER, 3000));
					character.getActingPlayer().getVariablesController().unset(getClass().getSimpleName());
				}
				else
				{
					return super.onEnterZone(character, zone);
				}
			}
			else if(character instanceof L2MonsterInstance && ((L2MonsterInstance) character).getNpcId() == RunningKukuru)
			{
				if(((L2MonsterInstance) character).getOwner() != null)
				{
					L2PcInstance owner = (L2PcInstance) ((L2MonsterInstance) character).getOwner();
					boolean isKukuruAlreadyRun = owner.getActingPlayer().getVariablesController().get(getClass().getSimpleName(), Boolean.class, false);
					if(isKukuruAlreadyRun)
					{
						owner.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1600030), ExShowScreenMessage.TOP_CENTER, 3000));
						owner.getVariablesController().unset(getClass().getSimpleName());
					}
					((L2MonsterInstance) character).setOwner(null);
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	/***
	 * Заставляем курицу бежать к финишу
	 * @param player игрок, участвующий в этом безобразии
	 */
	public void startKukuru(L2PcInstance player)
	{
		L2Npc kukuruInstance = spawnNpc(RunningKukuru, -109310, 246810, -3008);
		kukuruInstance.setOwner(player);
		kukuruInstance.setIsInvul(true);
		WalkingManager.getInstance().startMoving(kukuruInstance, 11);
	}

	/***
	 * Спауним курицу
	 * @param npcId ID курицы
	 * @param x x
	 * @param y y
	 * @param z z
	 * @return заспауненный объект курицы
	 */
	private L2Npc spawnNpc(int npcId, int x, int y, int z)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocx(x);
			npcSpawn.setLocy(y);
			npcSpawn.setLocz(z);
			npcSpawn.setHeading(0);
			npcSpawn.setAmount(1);
			SpawnTable.getInstance().addNewSpawn(npcSpawn);
			L2Npc cucuru = npcSpawn.spawnOne(false);
			cucuru.setRunning();
			return cucuru;
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
}
