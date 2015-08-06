package dwo.scripts.npc.fort;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.01.13
 * Time: 1:41
 */

public class FortBallista extends Quest
{
	private static final int[] NPCs = {
		35685, 35723, 35754, 35792, 35823, 35854, 35892, 35923, 35961, 35999, 36030, 36068, 36106, 36137, 36168, 36206,
		36244, 36282, 36313, 36351, 36389, 36313, 36351, 36389
	};

	// Переменные из скриптов
	private static final int Armor = 40;
	private static final int BombSkillId = 2342;
	private static final int pledge_lv_req = 5;

	public FortBallista()
	{
		addSpawnId(NPCs);
		addKillId(NPCs);
		addSkillSeeId(NPCs);
	}

	public static void main(String[] args)
	{
		new FortBallista();
	}

	@Override
	public String onNpcDie(L2Npc npc, L2Character killer)
	{
		if(npc.getAiVar("i_ai0") == null)
		{
			return super.onNpcDie(npc, killer);
		}
		if(!killer.isPlayer() || !(npc.getAiVar("i_ai0") instanceof L2PcInstance))
		{
			return super.onNpcDie(npc, killer);
		}
		L2PcInstance player = (L2PcInstance) npc.getAiVar("i_ai0");
		if(npc.getFort().getSiege().isInProgress())
		{
			if(player.getClan() == null || player.getClan().getLevel() < pledge_lv_req)
			{
				return super.onNpcDie(npc, killer);
			}
			player.getClan().addReputationScore(Config.BALLISTA_POINTS, true);
			player.sendPacket(SystemMessageId.BALLISTA_DESTROYED_CLAN_REPU_INCREASED);
			npc.getSpawn().stopRespawn();
		}
		return super.onNpcDie(npc, killer);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(ArrayUtils.contains(targets, npc) && skill.getId() == BombSkillId && Rnd.get(100) < 100 - Armor)
		{
			npc.setAiVar("i_ai0", caster);
			npc.setIsInvul(false);
			npc.setIsMortal(true);
			npc.reduceCurrentHp(npc.getMaxHp() + 1, caster, skill);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setAutoAttackable(true);
		npc.setIsNoAnimation(true);
		npc.setIsInvul(true);
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}
}