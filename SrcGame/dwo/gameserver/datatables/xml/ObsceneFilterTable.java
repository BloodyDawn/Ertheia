package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.07.12
 * Time: 2:15
 */

public class ObsceneFilterTable extends XmlDocumentParser
{
    private static final List<Equivalent> _equivalents = new ArrayList<>();
    private static final List<BadWordSet> _badWordSet = new ArrayList<>();

    protected static ObsceneFilterTable instance;

    private ObsceneFilterTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ObsceneFilterTable getInstance()
    {
        return instance == null ? instance = new ObsceneFilterTable() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _equivalents.clear();
        _badWordSet.clear();
        parseFile(FilePath.OBSCENE_FILTER_TABLE);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _badWordSet.size() + " Obscene patterns.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("equivalents"))
            {
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("equivalent"))
                    {
                        int i = 0;
                        String find = null, replace = null;
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("find"))
                            {
                                find = element2.getText();
                                i++;
                            }
                            else if(name2.equalsIgnoreCase("replace"))
                            {
                                replace = element2.getText();
                                i++;
                            }
                            if(i == 2)
                            {
                                _equivalents.add(new Equivalent(find, replace));
                                find = null;
                                replace = null;
                                i = 0;
                            }
                        }
                    }
                }
            }
            else if(name.equalsIgnoreCase("badWords"))
            {
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("badWordSet"))
                    {
                        if(element1.getChildren() == null)
                        {
                            _badWordSet.add(new BadWordSet(Pattern.compile(element1.getText()), null));
                        }
                        else
                        {
                            int i = 0;
                            Pattern include = null;
                            Pattern exclude = null;
                            for(Element element2 : element1.getChildren())
                            {
                                final String name2 = element2.getName();
                                if(name2.equalsIgnoreCase("include"))
                                {
                                    include = Pattern.compile(element2.getText());
                                    i++;
                                }
                                else if(name2.equalsIgnoreCase("exclude"))
                                {
                                    exclude = Pattern.compile(element2.getText());
                                    i++;
                                }
                                if(i == 2)
                                {
                                    _badWordSet.add(new BadWordSet(include, exclude));
                                    include = null;
                                    exclude = null;
                                    i = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param word проверяемое слово
     * @return {@code true} если предложение содержит мат
     */
    public boolean isObsceneWord(String word)
    {
        word = replaceWithEquivalents(word);
        for(BadWordSet bw : _badWordSet)
        {
            if(bw.getInclude().matcher(word).matches())
            {
                if(bw.getExclude() == null || !bw.getExclude().matcher(word).matches())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param word проверяемое предложение
     * @return предложение с замененными буками их эквивалентами
     */
    private String replaceWithEquivalents(String word)
    {
        for(Equivalent eq : _equivalents)
        {
            if(word.contains(eq.getFind()))
            {
                word = word.replace(eq.getFind(), eq.getReplace());
            }
        }
        return word;
    }
    
    private class Equivalent
    {
        protected final String _find;
        protected final String _replace;

        public Equivalent(String find, String replace)
        {
            _find = find;
            _replace = replace;
        }

        public String getFind()
        {
            return _find;
        }

        public String getReplace()
        {
            return _replace;
        }
    }

    private class BadWordSet
    {
        protected final Pattern _include;
        protected final Pattern _exclude;

        public BadWordSet(Pattern include, Pattern exclude)
        {
            _include = include;
            _exclude = exclude;
        }

        public Pattern getInclude()
        {
            return _include;
        }

        public Pattern getExclude()
        {
            return _exclude;
        }
    }
}