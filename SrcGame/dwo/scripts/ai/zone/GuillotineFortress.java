package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 21.07.12
 * Time: 23:35
 * На мобах в Гильятине должен висеть баф 15208 9го лвла, делающих мобов непробиваемыми, когда бьешь моба с каким-то шансом баф
 * падает делая моба ватным. Так же моб может закричать и вернуться на точку спавна.
 */

public class GuillotineFortress extends Quest
{
	private static final int ПрямоходящийЗверьСента = 23203;
	private static final int АдидайуМертвыйКлык = 23204;
	private static final int БышийМастерПытокСамита = 23206;
	private static final int ИстребленныйХакал = 23202;

	// Нежить
	/**
	 * Рыбомутант Нагду
	 * Разведчик Крепости Газем
	 * Душа Розении
	 * Смотритель Глаза Вечности Исад
	 * Садиак Обезглавливающий
	 * Блуждающий Дух Хаскал
	 * Кельвара Душегуб с Парными Мечами
	 * Призрак-Расчленитель Тюран
	 * Скальдисект Адского Пламени
	 */

	private static final int РыбомутантНагду = 23201;
	private static final int ДушаРозении = 23208;
	private static final int Газем = 23207;

	// Нежить ночная
	/**
	 * Крутати
	 * Папюлон
	 */

	//Щит
	private static final SkillHolder _chaos_shield = new SkillHolder(15208, 9);
	private static final int CHAOS_SHIELD = 15208;

	public GuillotineFortress()
	{

		addSpawnId(ПрямоходящийЗверьСента, АдидайуМертвыйКлык, БышийМастерПытокСамита, ДушаРозении, Газем, РыбомутантНагду, ИстребленныйХакал);
		addAttackId(ПрямоходящийЗверьСента, АдидайуМертвыйКлык, БышийМастерПытокСамита, ДушаРозении, Газем, РыбомутантНагду, ИстребленныйХакал);

		//addKillId(ПрямоходящийЗверьСента);
		//addKillId(АдидайуМертвыйКлык);
		//addKillId(БышийМастерПытокСамита);
		//addKillId(Газем);
		//addKillId(РыбомутантНагду);

		addKillId(РыбомутантНагду, ДушаРозении, Газем);

		onSpawnRerun(ПрямоходящийЗверьСента, АдидайуМертвыйКлык, БышийМастерПытокСамита, ДушаРозении, Газем, РыбомутантНагду, ИстребленныйХакал);
	}

	public static void main(String[] args)
	{
		new GuillotineFortress();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == ИстребленныйХакал)
		{
			// В шаут кричит текст если бить моба 1801403
			if(Rnd.getChance(5))
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1801403));
				npc.setRunning();
				returnToSpawn(npc);
			}
		}
		else if(npc.getNpcId() == ДушаРозении)
		{
			// В шаут кричит текст если бить моба 1801458
			if(Rnd.getChance(5))
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1801458));
				npc.setRunning();
				returnToSpawn(npc);
			}
		}

		if(npc.getCurrentHp() <= (npc.getMaxHp() << 1) / 4)
		{
			npc.stopSkillEffects(CHAOS_SHIELD);
		}

		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		L2Attackable mob = null;

		if(npc instanceof L2Attackable)
		{
			mob = (L2Attackable) npc;
		}

		if(mob == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("Skill"))
		{
			int npcId = npc.getNpcId();
			if(npcId == ПрямоходящийЗверьСента || npcId == АдидайуМертвыйКлык || npcId == БышийМастерПытокСамита || npcId == ДушаРозении || npcId == РыбомутантНагду || npcId == ИстребленныйХакал)
			{
				npc.setTarget(npc);
				npc.doCast(_chaos_shield.getSkill());
			}
			//startQuestTimer("Skill", 15000, npc, null);
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("Skill", 3000, npc, null);
		return super.onSpawn(npc);
	}

	private void returnToSpawn(L2Npc npc)
	{
		L2Attackable attackable = npc.getAttackable();

		attackable.clearAggroList();

		if(npc.getSpawn() != null)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npc.getSpawn().getLoc());
		}
	}
}