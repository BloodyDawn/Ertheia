package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.L2ArmorSet;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.fileio.filters.XmlFilter;
import dwo.gameserver.util.fileio.readers.XMLReader;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Переделать на XmlDocumentParser
 */

public class ArmorSetsTable
{
	private static Logger _log = LogManager.getLogger(ArmorSetsTable.class);

	private static Map<int[], L2ArmorSet> _armorSets = new HashMap<>();
	private static List<File> _files = new ArrayList<>();

	private ArmorSetsTable()
	{
		_armorSets.clear();
		loadData();
	}

	public static ArmorSetsTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private void hashFiles(File dir, List<File> hash)
	{
		if(!dir.exists())
		{
			_log.log(Level.WARN, "Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		Collections.addAll(hash, dir.listFiles(new XmlFilter()));
	}

	private void loadData()
	{
		hashFiles(FilePath.ARMOR_SETS_DIR, _files);
		List<File> allFiles = new ArrayList<>();

		if(_files != null && !_files.isEmpty())
		{
			_files.stream().filter(f -> f != null && !allFiles.contains(f)).forEach(allFiles::add);
		}
		else
		{
			_log.log(Level.ERROR, "ArmorSetsTable: Failed to load any xml file! Please check existing of their!");
			return;
		}

		if(!allFiles.isEmpty())
		{
			for(File file : allFiles)
			{
				if(file == null)
				{
					continue;
				}

				if(!file.exists())
				{
					continue;
				}

				XMLReader parser = new XMLReader(file, "armorSet");
				List<StatsSet> sets;
				sets = parser.parseDocument();
				List<L2Skill> skillInfo;
				if(!sets.isEmpty())
				{
					for(StatsSet set : sets)
					{
						int[] chest = set.getIntegerArray("chest");
						int[] legs = set.getIntegerArray("legs");
						int[] head = set.getIntegerArray("head");
						int[] gloves = set.getIntegerArray("gloves");
						int[] feet = set.getIntegerArray("feet");
						String skillData = set.getString("skillId");
						String[] skill = skillData.split(";");
						skillInfo = new ArrayList<>();
						for(String s : skill)
						{
							String[] skillLvlId = s.split("-");
							skillInfo.add(SkillTable.getInstance().getInfo(Integer.parseInt(skillLvlId[0]), Integer.parseInt(skillLvlId[1])));
						}
						int skill_parts_id = set.getInteger("skillPartsId", 0);
						int[] shield = set.getIntegerArray("shield");
						int shield_skill_id = set.getInteger("shieldSkillId", 0);

						int[] mw_legs = set.getIntegerArray("mwLegs");
						int[] mw_head = set.getIntegerArray("mwHead");
						int[] mw_gloves = set.getIntegerArray("mwGloves");
						int[] mw_feet = set.getIntegerArray("mwFeet");
						int[] mw_shield = set.getIntegerArray("mwShield");

						FastMap<Integer, SkillHolder> _enchantSkill = null;
						String sk = set.getString("enchant_skill", null);
						if(sk != null)
						{
							_enchantSkill = new FastMap<>();
							String[] split = sk.split(";");
							for(String part : split)
							{
								try
								{
									String[] enc = part.split(",");
									int enchant = Integer.parseInt(enc[0]);
									String[] info = enc[1].split("-");
									if(info != null && info.length == 2)
									{
										int id = Integer.parseInt(info[0]);
										int level = Integer.parseInt(info[1]);
										if(id > 0 && level > 0)
										{
											_enchantSkill.put(enchant, new SkillHolder(id, level));
										}
									}
								}
								catch(Exception nfe)
								{
									_log.log(Level.ERROR, StringUtil.concat("> Couldnt parse ", sk, " in ArmorSets enchant skills! item ", toString()));
								}
							}
						}

						_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skillInfo, skill_parts_id, shield, shield_skill_id, _enchantSkill, mw_legs, mw_head, mw_gloves, mw_feet, mw_shield));
					}
				}
			}
			_files.clear();
		}
		_log.log(Level.INFO, "ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
	}

	/**
	 * @param chestId ID тела от сета
	 * @return сет, принадлежащий телу с itemId
	 */
	public L2ArmorSet getSet(int chestId)
	{
		for(L2ArmorSet set : _armorSets.values())
		{
			if(set.containItem(chestId))
			{
				return set;
			}
		}
		return null;
	}

	private static class SingletonHolder
	{
		protected static final ArmorSetsTable _instance = new ArmorSetsTable();
	}
}