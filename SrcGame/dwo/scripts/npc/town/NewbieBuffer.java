package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 03.03.13
 * Time: 22:21
 * TODO: blessing_list002.htm - html'ka про петов, пока не понятно что с ней.
 */
public class NewbieBuffer extends Quest
{
	// Список ньюби баферов
	private static final int[] NPCs = {31076, 31077, 33454, 32327};

	// Список бафов
	private final int[][] BUFFs = {
		{15642, 15643, 15644, 15645, 15646, 15647, 15651, 15652, 15653, 15648},
		{15642, 15643, 15644, 15645, 15646, 15647, 15651, 15652, 15653, 15649},
		{15642, 15643, 15644, 15645, 15646, 15647, 15651, 15652, 15653, 15650}
	};

	public NewbieBuffer()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -7);
		addAskId(NPCs, -484);
		addAskId(NPCs, -771);
	}

	public static void main(String[] args)
	{
		new NewbieBuffer();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -7)
		{
			switch(reply)
			{
				case 2:
					if(player.getLevel() > 90)
					{
						return "Вы не можете получить вспомогательную магию после достижения 91-го уровня.";
					}

					for(int skillId : BUFFs[0])
					{
						//npc.setTarget(player);
						L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
						//npc.doCast(skill); //Через этот метод не хочет бафать на игрока сонаты
						skill.getEffects(npc, player);

						if(player.getPets() != null)
						{
							for(L2Summon pet : player.getPets())
							{
								skill.getEffects(npc, pet);
							}
						}
					}
					break;
				// Бафаем защиту от ПК-ашникв.
				case 3:
					int player_level = player.getLevel();

					if(player_level < 39 && player.getClassId().level() <= 2)
					{
						npc.setTarget(player);
						L2Skill skill = SkillTable.FrequentSkill.BLESSING_OF_PROTECTION.getSkill();
						npc.doCast(skill);
					}
					else
					{
						return "pk_protect002.htm";
					}
					break;
				case 4:
					// Вызывается из диалога blessing_list002.htm
					player.sendMessage("Реализуй меня :D");
					break;
				case 5:
					// Ослабить Дыхание Шилен 3го уровня.
					int shillenLvl = player.getDeathPenaltyController().getDeathPenaltyLevel();

					if(shillenLvl > 0)
					{
						if(shillenLvl > 2)
						{
							player.getDeathPenaltyController().reduceDeathPenalty();
						}
						else
						{
							return "blessing_benefector001a.htm"; //TODO: Нужен офф диалог, не удалось подобрать!
						}
					}
					else
					{
						return "blessing_benefector001b.htm"; //TODO: Нужен офф диалог, не удалось подобрать!
					}
					break;
				case 21:
					if(player.getLevel() > 90)
					{
						return "Вы не можете получить вспомогательную магию после достижения 91-го уровня.";
					}

					for(int skillId : BUFFs[1])
					{
						//npc.setTarget(player);
						L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
						//npc.doCast(skill); //Через этот метод не хочет бафать на игрока сонаты
						skill.getEffects(npc, player);

						if(player.getPets() != null)
						{
							for(L2Summon pet : player.getPets())
							{
								skill.getEffects(npc, pet);
							}
						}
					}
					break;
				case 22:
					if(player.getLevel() > 90)
					{
						return "Вы не можете получить вспомогательную магию после достижения 91-го уровня.";
					}

					for(int skillId : BUFFs[2])
					{
						//npc.setTarget(player);
						L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
						//npc.doCast(skill); //Через этот метод не хочет бафать на игрока сонаты
						skill.getEffects(npc, player);

						if(player.getPets() != null)
						{
							for(L2Summon pet : player.getPets())
							{
								skill.getEffects(npc, pet);
							}
						}
					}
					break;
			}
		}
		else if(ask == -484)
		{
			switch(reply)
			{
				// Итем можно взять раз в сутки!
				case 1:
					if(!player.getVariablesController().get("newbieBonus", Boolean.class, false))
					{
						L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.NPC, 32241, 1, player);
						player.addItem(ProcessType.NPC, item, npc, true);
						player.getVariablesController().set("newbieBonus", true);
					}
					return "blessing_benefector002.htm"; //TODO: Нужен офф диалог, не удалось подобрать!
			}
		}
		else if(ask == -771)
		{
			switch(reply)
			{
				// TODO: Вообще хз что тут дается или обменивается, по гугло переводу там описывается адена и какие-то коины.
				case 1:
					return player.getItemsCount(PcInventory.ADENA_ID) >= 500000 ? "iron_gate_coin_gamble002.htm" : "iron_gate_coin_gamble002.htm";
				case 2:
					return player.getItemsCount(PcInventory.ADENA_ID) >= 50000 ? "iron_gate_coin_gamble002.htm" : "iron_gate_coin_gamble002.htm";
				case 3:
					return player.getItemsCount(PcInventory.ADENA_ID) >= 500000 ? "iron_gate_coin_gamble002.htm" : "iron_gate_coin_gamble002.htm";
			}
			return null;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.isCursedWeaponEquipped())
		{
			return "Я не общаюсь с убийцами! Прочь!"; // Вероятно есть какой-то диалог!
		}

		return "blessing_benefector001.htm";
	}
}
