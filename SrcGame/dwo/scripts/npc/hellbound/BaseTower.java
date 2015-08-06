package dwo.scripts.npc.hellbound;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;

import java.util.HashMap;
import java.util.Map;

public class BaseTower extends Quest
{
	private static final int GUZEN = 22362;
	private static final int KENDAL = 32301;
	private static final int BODY_DESTROYER = 22363;

	private static final Map<Integer, L2PcInstance> BODY_DESTROYER_TARGET_LIST = new HashMap<>();

	private static final SkillHolder DEATH_WORD = new SkillHolder(5256, 1);

	public BaseTower()
	{
		addKillId(GUZEN, BODY_DESTROYER);
		addFirstTalkId(KENDAL);
		addAggroRangeEnterId(BODY_DESTROYER);
	}

	public static void main(String[] args)
	{
		new BaseTower();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch(npc.getNpcId())
		{
			case GUZEN:
				// Should Kendal be despawned before Guzen's spawn? Or it will be crowd of Kendal's
				addSpawn(KENDAL, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, npc.getSpawn().getRespawnDelay(), false);
				DoorGeoEngine.getInstance().getDoor(20260003).openMe();
				DoorGeoEngine.getInstance().getDoor(20260004).openMe(60000);
				break;
			case BODY_DESTROYER:
				if(BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId()))
				{
					L2PcInstance pl = BODY_DESTROYER_TARGET_LIST.get(npc.getObjectId());
					if(pl != null && pl.isOnline() && !pl.isDead())
					{
						L2Effect e = pl.getFirstEffect(DEATH_WORD.getSkill());
						if(e != null)
						{
							e.exit();
						}
					}

					BODY_DESTROYER_TARGET_LIST.remove(npc.getObjectId());
				}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		ClassId classId = player.getClassId();
		return classId.equalsOrChildOf(ClassId.hellKnight) || classId.equalsOrChildOf(ClassId.soultaker) ? "wanderingghost_kendal002.htm" : "wanderingghost_kendal001.htm";
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(!BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId()))
		{
			BODY_DESTROYER_TARGET_LIST.put(npc.getObjectId(), player);
			npc.setTarget(player);
			npc.doSimultaneousCast(DEATH_WORD.getSkill());
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}