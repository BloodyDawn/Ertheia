package dwo.scripts.npc;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.01.12
 * Time: 5:44
 * TODO: Мобы деспаунятся через 40-50 секунд если их не бить
 */

public class FairyCocons extends Quest
{
	private static final int littleCocone = 32919;
	private static final int bigCocone = 32920;

	private static final int[] little_simpleAttackMobs = {
		22863, // Воин Фей
		22871, // Терзатель Фей
		22879, // Мститель Фей
		22887, // Сатир Чародей
		22895, // Сатир Призыватель
		22903, // Сатир Колдунья
	};

	private static final int[] little_nightAttackMobs = {
		22864, // Воин Фей Сдерживающий Тьму
		22872, // Терзатель Фей Сдерживающий Тьму
		22880, // Мститель Фей Сдерживающий Тьму
		22888, // Сатир Чародей Сдерживающий Тьму
		22896, // Сатир Призыватель Сдерживающий Тьму
		22904, // Сатир Колдунья Сдерживающий Тьму
	};

	private static final int[] little_itemSkillMobs = {
		22865, // Воин Фей Завершивший Мутацию
		22873, // Терзатель Фей Завершивший Мутацию
		22881, // Мститель Фей Завершивший Мутацию
		22889, // Сатир Чародей Завершивший Мутацию
		22897, // Сатир Призыватель Завершивший Мутацию
		22905, // Сатир Колдунья Завершивший Мутацию
	};

	private static final int[] little_skillAttackMobs = {
		22866, // Воин Фей Прекративший Мутацию
		22874, // Терзатель Фей Прекративший Мутацию
		22882, // Мститель Фей Прекративший Мутацию
		22890, // Сатир Чародей Прекративший Мутацию
		22898, // Сатир Призыватель Прекративший Мутацию
		22906, // Сатир Колдунья Прекративший Мутацию
	};

	private static final int[] big_simpleAttackMobs = {
		22867, // Воин Фей Буйный
		22875, // Терзатель Фей Буйный
		22883, // Мститель Фей Буйный
		22891, // Сатир Чародей Буйный
		22899, // Сатир Призыватель Буйный
		22907, // Сатир Колдунья Буйный
	};

	private static final int[] big_nightAttackMobs = {
		22868, // Воин Фей Сдерживающий Тьму Буйный
		22876, // Терзатель Фей Сдерживающий Тьму Буйный
		22884, // Мститель Фей Сдерживающий Тьму Буйный
		22892, // Сатир Чародей Сдерживающий Тьму Буйный
		22900, // Сатир Призыватель Сдерживающий Тьму Буйный
		22908, // Сатир Колдунья Сдерживающий Тьму Буйный
	};

	private static final int[] big_itemSkillMobs = {
		22869, // Воин Фей Завершивший Мутацию Буйный
		22877, // Терзатель Фей Завершивший Мутацию Буйный
		22885, // Мститель Фей Завершивший Мутацию Буйный
		22893, // Сатир Чародей Завершивший Мутацию Буйный
		22901, // Сатир Призыватель Завершивший Мутацию Буйный
		22909, // Сатир Колдунья Завершивший Мутацию Буйный
	};

	private static final int[] big_skillAttackMobs = {
		22870, // Воин Фей Прекративший Мутацию Буйный
		22878, // Терзатель Фей Прекративший Мутацию Буйный
		22886, // Мститель Фей Прекративший Мутацию Буйный
		22894, // Сатир Чародей Прекративший Мутацию Буйный
		22898, // Сатир Призыватель Прекративший Мутацию Буйный
		22910, // Сатир Колдунья Прекративший Мутацию Буйный
	};

	public FairyCocons()
	{
		addAskId(littleCocone, 1000);
		addAskId(bigCocone, 1000);
		addAskId(littleCocone, 2000);
		addAskId(bigCocone, 2000);
		addSkillSeeId(littleCocone, bigCocone);
		addAttackId(littleCocone, bigCocone);
		addSpawnId(littleCocone, bigCocone);
	}

	public static void main(String[] args)
	{
		new FairyCocons();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if(GameTimeController.getInstance().isNight())
		{
			if(npc.getNpcId() == littleCocone)
			{
				L2Npc mob = addSpawn(little_nightAttackMobs[Rnd.get(little_nightAttackMobs.length)], npc.getLoc());
				npc.doDie(attacker);
				npc.getLocationController().decay();
				mob.getAttackable().attackCharacter(attacker);
			}
			else
			{
				L2Npc mob = null;
				for(int i = 0; i < 3; i++)
				{
					mob = addSpawn(big_nightAttackMobs[Rnd.get(big_nightAttackMobs.length)], npc.getLoc());
					mob.getAttackable().attackCharacter(attacker);
				}
				npc.doDie(attacker);
				npc.getLocationController().decay();
			}
		}
		else
		{
			if(npc.getNpcId() == littleCocone)
			{
				if(skill == null)
				{
					L2Npc mob = addSpawn(little_simpleAttackMobs[Rnd.get(little_simpleAttackMobs.length)], npc.getLoc());
					npc.doDie(attacker);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(attacker);
				}
				else
				{
					L2Npc mob = addSpawn(little_skillAttackMobs[Rnd.get(little_skillAttackMobs.length)], npc.getLoc());
					npc.doDie(attacker);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(attacker);
				}
			}
			else
			{
				if(skill == null)
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_simpleAttackMobs[Rnd.get(big_simpleAttackMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(attacker);
					}
					npc.doDie(attacker);
					npc.getLocationController().decay();
				}
				else
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_skillAttackMobs[Rnd.get(big_skillAttackMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(attacker);
					}
					npc.doDie(attacker);
					npc.getLocationController().decay();
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(GameTimeController.getInstance().isNight())
		{
			if(npc.getNpcId() == littleCocone)
			{
				L2Npc mob = addSpawn(little_nightAttackMobs[Rnd.get(little_nightAttackMobs.length)], npc.getLoc());
				npc.doDie(player);
				npc.getLocationController().decay();
				mob.getAttackable().attackCharacter(player);
			}
			else
			{
				L2Npc mob;
				for(int i = 0; i < 3; i++)
				{
					mob = addSpawn(big_nightAttackMobs[Rnd.get(big_nightAttackMobs.length)], npc.getLoc());
					mob.getAttackable().attackCharacter(player);
				}
				npc.doDie(player);
				npc.getLocationController().decay();
			}
		}
		else
		{
			if(ask == 1000) // Обычная атака
			{
				if(npc.getNpcId() == littleCocone)
				{
					L2Npc mob = addSpawn(little_simpleAttackMobs[Rnd.get(little_simpleAttackMobs.length)], npc.getLoc());
					npc.doDie(player);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(player);
				}
				else
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_simpleAttackMobs[Rnd.get(big_simpleAttackMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(player);
					}
					npc.doDie(player);
					npc.getLocationController().decay();
				}
			}
			else if(ask == 2000) // Атака скиллом
			{
				if(npc.getNpcId() == littleCocone)
				{
					L2Npc mob = addSpawn(little_skillAttackMobs[Rnd.get(little_simpleAttackMobs.length)], npc.getLoc());
					npc.doDie(player);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(player);
				}
				else
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_skillAttackMobs[Rnd.get(big_simpleAttackMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(player);
					}
					npc.doDie(player);
					npc.getLocationController().decay();
				}
			}
		}
		return "magmeld_cocoon002.htm";
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(skill.getId() == 12002)
		{
			if(GameTimeController.getInstance().isNight())
			{
				if(npc.getNpcId() == littleCocone)
				{
					L2Npc mob = addSpawn(little_nightAttackMobs[Rnd.get(little_nightAttackMobs.length)], npc.getLoc());
					npc.doDie(caster);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(caster);
				}
				else
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_nightAttackMobs[Rnd.get(big_nightAttackMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(caster);
					}
					npc.doDie(caster);
					npc.getLocationController().decay();
				}
			}
			else
			{
				if(npc.getNpcId() == littleCocone)
				{
					L2Npc mob = addSpawn(little_itemSkillMobs[Rnd.get(little_itemSkillMobs.length)], npc.getLoc());
					npc.doDie(caster);
					npc.getLocationController().decay();
					mob.getAttackable().attackCharacter(caster);
				}
				else
				{
					L2Npc mob;
					for(int i = 0; i < 3; i++)
					{
						mob = addSpawn(big_itemSkillMobs[Rnd.get(big_itemSkillMobs.length)], npc.getLoc());
						mob.getAttackable().attackCharacter(caster);
					}
					npc.doDie(caster);
					npc.getLocationController().decay();
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoAttackingBack(true);
		return super.onSpawn(npc);
	}
}
