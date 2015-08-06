package dwo.config.main;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.util.StringUtil;
import gnu.trove.map.hash.TIntFloatHashMap;
import org.apache.log4j.Level;

public class ConfigRates extends Config
{
	private static final String path = RATES_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			RATE_XP = getFloat(properties, "RateXp", 1.0F);
			RATE_SP = getFloat(properties, "RateSp", 1.0F);
			RATE_PARTY_XP = getFloat(properties, "RatePartyXp", 1.0F);
			RATE_PARTY_SP = getFloat(properties, "RatePartySp", 1.0F);
			RATE_HB_TRUST_INCREASE = getFloat(properties, "RateHellboundTrustIncrease", 1.0F);
			RATE_HB_TRUST_DECREASE = getFloat(properties, "RateHellboundTrustDecrease", 1.0F);
			RATE_EXTR_FISH = getFloat(properties, "RateExtractFish", 1.0F);
			RATE_DROP_ITEMS = getFloat(properties, "RateDropItems", 1.0F);
			RATE_DROP_ITEMS_BY_RAID = getFloat(properties, "RateRaidDropItems", 1.0F);
			RATE_DROP_SPOIL = getFloat(properties, "RateDropSpoil", 1.0F);
			RATE_DROP_MANOR = getInt(properties, "RateDropManor", 1);
			RATE_QUEST_DROP = getFloat(properties, "RateQuestDrop", 1.0F);
			RATE_QUEST_REWARD = getFloat(properties, "RateQuestReward", 1.0F);
			RATE_QUEST_REWARD_XP = getFloat(properties, "RateQuestRewardXP", 1.0F);
			RATE_QUEST_REWARD_SP = getFloat(properties, "RateQuestRewardSP", 1.0F);
			RATE_QUEST_REWARD_ADENA = getFloat(properties, "RateQuestRewardAdena", 1.0F);
			RATE_QUEST_REWARD_USE_MULTIPLIERS = getBoolean(properties, "UseQuestRewardMultipliers", false);
			RATE_QUEST_REWARD_POTION = getFloat(properties, "RateQuestRewardPotion", 1.0F);
			RATE_QUEST_REWARD_SCROLL = getFloat(properties, "RateQuestRewardScroll", 1.0F);
			RATE_QUEST_REWARD_RECIPE = getFloat(properties, "RateQuestRewardRecipe", 1.0F);
			RATE_QUEST_REWARD_MATERIAL = getFloat(properties, "RateQuestRewardMaterial", 1.0F);
			RATE_VITALITY_LOST = getFloat(properties, "RateVitalityLost", 1.0F);
			RATE_VITALITY_GAIN = getFloat(properties, "RateVitalityGain", 1.0F);
			RATE_VITALITY = getFloat(properties, "RateVitality", 3.0F);
			RATE_CAMPAINS = getFloat(properties, "RateCampains", 1);
			RATE_BADREPUTATION_EXP_LOST = getFloat(properties, "RateBadReputationExpLost", 1.0F);
			RATE_DROP_COMMON_HERBS = getFloat(properties, "RateCommonHerbs", 15.0F);
			RATE_DROP_HP_HERBS = getFloat(properties, "RateHpHerbs", 10.0F);
			RATE_DROP_MP_HERBS = getFloat(properties, "RateMpHerbs", 4.0F);
			RATE_DROP_SPECIAL_HERBS = getFloat(properties, "RateSpecialHerbs", 0.2F) * 10;
			PLAYER_DROP_LIMIT = getInt(properties, "PlayerDropLimit", 3);
			PLAYER_RATE_DROP = getInt(properties, "PlayerRateDrop", 5);
			PLAYER_RATE_DROP_ITEM = getInt(properties, "PlayerRateDropItem", 70);
			PLAYER_RATE_DROP_EQUIP = getInt(properties, "PlayerRateDropEquip", 25);
			PLAYER_RATE_DROP_EQUIP_WEAPON = getInt(properties, "PlayerRateDropEquipWeapon", 5);
			PET_XP_RATE = getFloat(properties, "PetXpRate", 1.0F);
			PET_FOOD_RATE = getInt(properties, "PetFoodRate", 1);
			SINEATER_XP_RATE = getFloat(properties, "SinEaterXpRate", 1.0F);
			BADREPUTATION_DROP_LIMIT = getInt(properties, "BadReputationDropLimit", 10);
			BADREPUTATION_RATE_DROP = getInt(properties, "BadReputationRateDrop", 70);
			BADREPUTATION_RATE_DROP_ITEM = getInt(properties, "BadReputationRateDropItem", 50);
			BADREPUTATION_RATE_DROP_EQUIP = getInt(properties, "BadReputationRateDropEquip", 40);
			BADREPUTATION_RATE_DROP_EQUIP_WEAPON = getInt(properties, "BadReputationRateDropEquipWeapon", 10);
			PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE + 1];
			for(int i = 0; i <= Byte.MAX_VALUE; i++)
			{
				PLAYER_XP_PERCENT_LOST[i] = 1.0;
			}

			// Now loading into table parsed values
			try
			{
				String[] values = getStringArray(properties, "PlayerXPPercentLost", new String[]{
					"0,39-7.0", "40,75-4.0", "76,76-2.5", "77,77-2.0", "78,78-1.5"
				}, ";");
				for(String s : values)
				{
					int min;
					int max;
					double val;

					String[] vals = s.split("-");
					String[] mM = vals[0].split(",");

					min = Integer.parseInt(mM[0]);
					max = Integer.parseInt(mM[1]);
					val = Double.parseDouble(vals[1]);

					for(int i = min; i <= max; i++)
					{
						PLAYER_XP_PERCENT_LOST[i] = val;
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while loading Player XP percent lost", e);
			}

			String[] propertySplit = getStringArray(properties, "RateDropItemsById", new String[]{}, ";");
			RATE_DROP_ITEMS_ID = new TIntFloatHashMap(propertySplit.length);
			if(!propertySplit[0].isEmpty())
			{
				for(String item : propertySplit)
				{
					String[] itemSplit = item.split(",");
					if(itemSplit.length == 2)
					{
						try
						{
							RATE_DROP_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!item.isEmpty())
							{
								_log.log(Level.WARN, StringUtil.concat("load(): invalid config property -> RateDropItemsById \"", item, "\""));
							}
						}
					}
					else
					{
						_log.log(Level.WARN, StringUtil.concat("load(): invalid config property -> RateDropItemsById \"", item, "\""));
					}
				}
			}
			// for Adena rate if not defined
			if(RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID) == 0.0f)
			{
				RATE_DROP_ITEMS_ID.put(PcInventory.ADENA_ID, RATE_DROP_ITEMS);
			}

			WEAPON_BLESSED_ENCHANT_BONUS = getFloat(properties, "WeaponBlessedEnchantBonus", 1.5F);
			ARMOR_BLESSED_ENCHANT_BONUS = getFloat(properties, "ArmorBlessedEnchantBonus", 1.5F);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
