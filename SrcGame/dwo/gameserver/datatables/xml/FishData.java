package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.fishing.L2Fish;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishData extends XmlDocumentParser
{
    private static final Map<Integer, L2Fish> _fishsNormal = new HashMap<>();
    private static final Map<Integer, L2Fish> _fishsEasy = new HashMap<>();
    private static final Map<Integer, L2Fish> _fishsHard = new HashMap<>();

    protected static FishData _instance;

    private FishData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static FishData getInstance()
    {
        return _instance == null ? _instance = new FishData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _fishsEasy.clear();
        _fishsNormal.clear();
        _fishsHard.clear();
        parseFile(FilePath.FISH_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + (_fishsEasy.size() + _fishsNormal.size() + _fishsHard.size()) + " Fishes.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("fish"))
            {
                final StatsSet set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                final L2Fish fish = new L2Fish(set);
                switch(fish.getFishGrade())
                {
                    case 0:
                        _fishsEasy.put(fish.getFishId(), fish);
                        break;
                    case 1:
                        _fishsNormal.put(fish.getFishId(), fish);
                        break;
                    case 2:
                        _fishsHard.put(fish.getFishId(), fish);
                        break;
                }
            }
        }
    }

    /**
     * @param level the fish Level
     * @param group the fish Group
     * @param grade the fish Grade
     * @return List of Fish that can be fished
     */
    public List<L2Fish> getFish(int level, int group, int grade)
    {
        List<L2Fish> result = new ArrayList<>();
        Map<Integer, L2Fish> _Fishs = null;
        switch(grade)
        {
            case 0:
                _Fishs = _fishsEasy;
                break;
            case 1:
                _Fishs = _fishsNormal;
                break;
            case 2:
                _Fishs = _fishsHard;
                break;
        }
        if(_Fishs == null)
        {
            // the fish list is empty
            _log.log(Level.WARN, getClass().getSimpleName() + ": Fish are not defined !");
            return null;
        }
        for(L2Fish f : _Fishs.values())
        {
            if(f.getFishLevel() != level)
            {
                continue;
            }
            if(f.getFishGroup() != group)
            {
                continue;
            }

            result.add(f);
        }
        if(result.isEmpty())
        {
            _log.log(Level.WARN, getClass().getSimpleName() + ": Cant Find Any Fish!? - Lvl: " + level + " Group: " + group + " Grade: " + grade);
        }
        return result;
    }
}