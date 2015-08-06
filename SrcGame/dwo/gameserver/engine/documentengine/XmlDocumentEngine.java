package dwo.gameserver.engine.documentengine;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.items.XmlDocumentItem;
import dwo.gameserver.engine.documentengine.skills.XmlDocumentSkill;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.Util;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author mkizub
 */

public class XmlDocumentEngine
{
	private static final Logger _log = LogManager.getLogger(XmlDocumentEngine.class);

	private XmlDocumentEngine()
	{
	}

	public static XmlDocumentEngine getInstance()
	{
		return SingletonHolder._instance;
	}

	public List<L2Skill> loadSkills(File file)
	{
		if(file == null)
		{
			_log.log(Level.WARN, "Skill file not found.");
			return null;
		}
		XmlDocumentSkill doc = new XmlDocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}

	public void loadAllSkills(Map<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for(File file : Util.getAllFileList(FilePath.SKILL_DATA, ".xml"))
		{
			List<L2Skill> s = loadSkills(file);
			if(s == null)
			{
				continue;
			}
            for(L2Skill skill : s)
            {
                allSkills.put(SkillTable.getSkillHashCode(skill.getId(), skill.getLevel()), skill);
                count++;
            }
		}
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + count + " Skill templates from XML files.");
	}

	/**
	 * Return created items
	 * @return List of {@link L2Item}
	 */
	public Collection<L2Item> loadItems(boolean reload)
	{
		Map<Integer, L2Item> list = new FastMap<>();
		for(File f : Util.getAllFileList(FilePath.ITEMS_DIR, ".xml"))
		{
			XmlDocumentItem document = new XmlDocumentItem(f);
			document.parse();

			List<L2Item> arr = document.getItemList();
			if(arr == null)
			{
				continue;
			}

			for(L2Item item : arr)
			{
				if(!reload && list.containsKey(item.getItemId()))
				{
					_log.log(Level.ERROR, getClass().getSimpleName() + ": Item " + item + " duplicated!!!");
				}
				list.put(item.getItemId(), item);
			}
		}
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + list.size() + " Items templates from XML files.");
		return list.values();
	}

	private static class SingletonHolder
	{
		protected static final XmlDocumentEngine _instance = new XmlDocumentEngine();
	}
}
