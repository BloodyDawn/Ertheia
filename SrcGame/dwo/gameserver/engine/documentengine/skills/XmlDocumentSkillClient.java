package dwo.gameserver.engine.documentengine.skills;

import dwo.gameserver.engine.documentengine.XmlDocumentBase;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class XmlDocumentSkillClient extends XmlDocumentBase
{
    public List<L2Skill> skills = new ArrayList<>();

    public XmlDocumentSkillClient(File file)
    {
        super(file);
    }

    @Override
    protected int getCurrentId()
    {
        return 0;
    }

    @Override
    protected void parseDocument(Document doc)
    {
        for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if("list".equalsIgnoreCase(n.getNodeName()))
            {
                for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if("skill".equalsIgnoreCase(d.getNodeName()))
                    {
                        parseSkill(d);
                    }
                }
            }
            else if("skill".equalsIgnoreCase(n.getNodeName()))
            {
                parseSkill(n);
            }
        }
    }

    @Override
    protected StatsSet getStatsSet()
    {
        return null;
    }

    @Override
    protected String getTableValue(String name)
    {
        return null;
    }

    @Override
    protected String getTableValue(String name, int idx)
    {
        return null;
    }

    private void parseSkill(Node n)
    {
        StatsSet set = new StatsSet();
        Node first = n.getFirstChild();
        for(n = first; n != null; n = n.getNextSibling())
        {
            if("set".equalsIgnoreCase(n.getNodeName()))
            {
                parseBeanSet(n, set, 1);
            }
        }
        L2Skill skill = new L2Skill(set);
        skills.add(skill);
    }

    public List<L2Skill> getSkills()
    {
        return skills;
    }
}