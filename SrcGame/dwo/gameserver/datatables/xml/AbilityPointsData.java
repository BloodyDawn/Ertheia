package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.holders.RangeAbilityPointsHolder;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class AbilityPointsData extends XmlDocumentParser
{
    private final List<RangeAbilityPointsHolder> _points = new ArrayList<>();

    protected static AbilityPointsData _instance;

    protected AbilityPointsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static AbilityPointsData getInstance()
    {
        return _instance == null ? _instance = new AbilityPointsData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _points.clear();
        parseFile(FilePath.ABILITY_SKILL_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded: " + _points.size() + " range fees.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for (Element element : rootElement.getChildren())
        {
            String name = element.getName();
            if (name.equalsIgnoreCase("points"))
            {
                int from = Integer.parseInt(element.getAttributeValue("from"));
                int to = Integer.parseInt(element.getAttributeValue("to"));
                int costs = Integer.parseInt(element.getAttributeValue("costs"));

                _points.add(new RangeAbilityPointsHolder(from, to, costs));
            }
        }
    }

    public RangeAbilityPointsHolder getHolder(int points)
    {
        for (RangeAbilityPointsHolder holder : _points)
        {
            if ((holder.getMin() <= points) && (holder.getMax() >= points))
            {
                return holder;
            }
        }
        return null;
    }

    public int getPrice(int points)
    {
        points++; //для след. очков умений

        final RangeAbilityPointsHolder holder = getHolder(points);

        if (holder == null)
        {
            final RangeAbilityPointsHolder prevHolder = getHolder(points - 1);

            if (prevHolder != null)
            {
                return prevHolder.getSP();
            }

            return points >= 13 ? 1_000_000_000 : points >= 9 ? 750_000_000 : points >= 5 ? 500_000_000 : 250_000_000;
        }
        return holder.getSP();
    }
}
