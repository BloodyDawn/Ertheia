package dwo.gameserver.model.skills.base.funcs;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}

	@Override
	public void calc(Env env)
	{
		if(cond != null && !cond.test(env))
		{
			return;
		}
		L2ItemInstance item = (L2ItemInstance) funcOwner;

		// Бонус заточки для Благославленных предметов
		float blessedBonus = 1;
		if(item.isArmor() && item.isBlessedItem())
		{
			blessedBonus = Config.ARMOR_BLESSED_ENCHANT_BONUS;
		}
		if(item.isWeapon() && item.isBlessedItem())
		{
			blessedBonus = Config.WEAPON_BLESSED_ENCHANT_BONUS;
		}

		int enchant = item.getEnchantLevel();

		if(enchant <= 0)
		{
			return;
		}

		int overenchant = 0;

		if(enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}

		if(env.getCharacter() != null && env.getCharacter() instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) env.getCharacter();
			if(player.getOlympiadController().isParticipating() && Config.ALT_OLY_ENCHANT_LIMIT >= 0 && enchant + overenchant > Config.ALT_OLY_ENCHANT_LIMIT)
			{
				if(Config.ALT_OLY_ENCHANT_LIMIT > 3)
				{
					overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
				}
				else
				{
					overenchant = 0;
					enchant = Config.ALT_OLY_ENCHANT_LIMIT;
				}
			}
		}

		if(stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE || stat == Stats.SHIELD_DEFENCE)
		{
			switch(item.getItem().getItemGradeSPlus())
			{
				case R:
					// 0-3 по +2 / 3-6 по +4 / 6++ по +6
					if(overenchant <= 3)
					{
						env.addValue(2 * blessedBonus * enchant + 4 * blessedBonus * overenchant);
					}
					if(overenchant >= 4)
					{
						env.addValue(6 * blessedBonus * overenchant);
					}
					break;
				default:
					env.addValue(enchant + 3 * overenchant);
			}
			return;
		}

		if(stat == Stats.MAGIC_ATTACK)
		{
			switch(item.getItem().getItemGradeSPlus())
			{
				case R:
					// 0-3 по +5 / 3-6 по +10 / 7-9 по +15 / 10-12 по +20 / 13++ по +25
					if(overenchant <= 3)
					{
						env.addValue(5 * blessedBonus * enchant + 10 * blessedBonus * overenchant);
					}
					if(overenchant >= 4 && overenchant <= 6)
					{
						env.addValue(15 * blessedBonus * overenchant);
					}
					if(overenchant >= 7 && overenchant <= 9)
					{
						env.addValue(20 * blessedBonus * (overenchant - 1.5));
					}
					if(overenchant >= 10)
					{
						env.addValue(25 * blessedBonus * (overenchant - 3));
					}
					break;
				case S:
					// M. Atk. increases by 4 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					env.addValue(4 * enchant + 8 * overenchant);
					break;
				case A:
				case B:
				case C:
					// M. Atk. increases by 3 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					env.addValue(3 * enchant + 6 * overenchant);
					break;
				case D:
				case NONE:
					// M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
					// Starting at +4, M. Atk. bonus double.
					env.addValue(2 * enchant + 4 * overenchant);
					break;
			}
			return;
		}

		if(item.isWeapon())
		{
			L2WeaponType type = (L2WeaponType) item.getItemType();

			switch(item.getItem().getItemGradeSPlus())
			{
				case R:
					switch(type)
					{
						case BOW:
						case CROSSBOW:
							//     case TWOHANDCROSSBOW:
							// 0-3 по +12 / 3-6 по +24 / 7-9 по +36 / 10-12 по +48 / 13++ по +60
							if(overenchant <= 3)
							{
								env.addValue(12 * blessedBonus * enchant + 24 * blessedBonus * overenchant);
							}
							if(overenchant >= 4 && overenchant <= 6)
							{
								env.addValue(36 * blessedBonus * overenchant);
							}
							if(overenchant >= 7 && overenchant <= 9)
							{
								env.addValue(48 * blessedBonus * (overenchant - 1.5));
							}
							if(overenchant >= 10)
							{
								env.addValue(60 * blessedBonus * (overenchant - 3));
							}
							break;
						default:
							if(item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
							{
								// Для двуручников
								// 0-3 по +7 / 3-6 по +14 / 7-9 по +21 / 10-12 по +28 / 13++ по +35
								if(overenchant <= 3)
								{
									env.addValue(7 * blessedBonus * enchant + 14 * blessedBonus * overenchant);
								}
								if(overenchant >= 4 && overenchant <= 6)
								{
									env.addValue(21 * blessedBonus * overenchant);
								}
								if(overenchant >= 7 && overenchant <= 9)
								{
									env.addValue(28 * blessedBonus * (overenchant - 1.5));
								}
								if(overenchant >= 10)
								{
									env.addValue(35 * blessedBonus * (overenchant - 3));
								}
							}
							else
							{
								// Для одноручников
								// 0-3 по +6 / 3-6 по +12 / 7-9 по +18 / 10-12 по +24 / 13++ по +30
								if(overenchant <= 3)
								{
									env.addValue(6 * blessedBonus * enchant + 12 * blessedBonus * overenchant);
								}
								if(overenchant >= 4 && overenchant <= 6)
								{
									env.addValue(18 * blessedBonus * overenchant);
								}
								if(overenchant >= 7 && overenchant <= 9)
								{
									env.addValue(24 * blessedBonus * (overenchant - 1.5));
								}
								if(overenchant >= 10)
								{
									env.addValue(30 * blessedBonus * (overenchant - 3));
								}
							}
							break;
					}
					break;
				case S:
					switch(type)
					{
						case BOW:
						case CROSSBOW:
						case TWOHANDCROSSBOW:
							// P. Atk. increases by 10 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue(10 * enchant + 20 * overenchant);
							break;
						default:
							if(item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
							{
								// Для двуручников
								// P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(6 * enchant + 12 * overenchant);
							}
							else
							{
								// Для одноручников
								// P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(5 * enchant + 10 * overenchant);
							}
							break;
					}
					break;
				case A:
					switch(type)
					{
						case BOW:
						case CROSSBOW:
						case TWOHANDCROSSBOW:
							// P. Atk. increases by 8 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue(8 * enchant + 16 * overenchant);
							break;
						default:
							if(item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
							{
								// Для двуручников
								// P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(5 * enchant + 10 * overenchant);
							}
							else
							{
								// Для одноручников
								// P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(4 * enchant + 8 * overenchant);
							}
							break;
					}
					break;
				case B:
				case C:
					switch(type)
					{
						case BOW:
						case CROSSBOW:
						case TWOHANDCROSSBOW:
							// P. Atk. increases by 6 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue(6 * enchant + 12 * overenchant);
							break;
						default:
							if(item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
							{
								// Для двуручников
								// P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(4 * enchant + 8 * overenchant);
							}
							else
							{
								// Для одноручников
								// P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
								// Starting at +4, P. Atk. bonus double.
								env.addValue(3 * enchant + 6 * overenchant);
							}
							break;
					}
					break;
				case D:
				case NONE:
					switch(type)
					{
						case BOW:
						case CROSSBOW:
						case TWOHANDCROSSBOW:
							// Bows increase by 4.
							// Starting at +4, P. Atk. bonus double.
							env.addValue(4 * enchant + 8 * overenchant);
							break;
						default:
							// P. Atk. increases by 2 for all weapons with the exception of bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue(2 * enchant + 4 * overenchant);
							break;
					}
					break;
			}
		}
	}
}