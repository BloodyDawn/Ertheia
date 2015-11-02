package dwo.refactor;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.stats.StatsSet;

public class SkillTreeRefactoring2 {
    private static final Map<Integer, Map<Integer, L2SkillLearn>> l2sSkillLearn = new HashMap<>();

    public static void main(String[] args) throws Exception {
        loadL2SSkillLearn();
        Config.load();
        SkillTreesData.getInstance();

        SAXBuilder builder = new SAXBuilder();

        File dir = new File("/home/alf/Desktop/L2WT-Ertheia/Data/game/data/stats/player/skillTrees/classSkillTree/class_skill_tree");

        for (Map.Entry<Integer, Map<Integer, L2SkillLearn>> entry : l2sSkillLearn.entrySet()) {
            ClassId classId = ClassId.getClassId(entry.getKey());

            Map<Integer, L2SkillLearn> existMap = SkillTreesData.getInstance().getCompleteClassSkillTree(classId);

            for (Map.Entry<Integer, L2SkillLearn> skillLearnEntry : entry.getValue().entrySet()) {

                L2SkillLearn skillLearn = skillLearnEntry.getValue();
                if (!existMap.containsKey(skillLearnEntry.getKey())) {
                    System.out.println("Skill learn is not exist: " + skillLearnEntry.getValue() + ", classId " + entry.getKey());

                    File file = new File(dir, "[" + entry.getKey() + "] " + classId.name() + ".xml");
                    if (!file.exists()) {
                        File[] files = dir.listFiles();
                        for (File f : files) {
                            if (f.getName().contains(classId.getId() + "")) {
                                file = f;
                                break;
                            }
                        }
                    }

                    Document document = builder.build(file);
                    Element rootElement = document.getRootElement();
                    for (Element treeElement : rootElement.getChildren()) {
                        String name = treeElement.getName();
                        if (!name.equals("skillTree"))
                            continue;

                        Element element = new Element("skill");
                        element.setAttribute("minLevel", String.valueOf(skillLearn.getMinLevel()));
                        element.setAttribute("name", String.valueOf(skillLearn.getName()));
                        element.setAttribute("skillId", String.valueOf(skillLearn.getSkillId()));
                        element.setAttribute("skillLvl", String.valueOf(skillLearn.getSkillLevel()));
                        element.setAttribute("sp", String.valueOf(skillLearn.getLevelUpSp()));


                        treeElement.addContent(element);

                        XMLOutputter xmlOutput = new XMLOutputter();

                        // display nice nice
                        xmlOutput.setFormat(Format.getPrettyFormat().setIndent("\t"));
                        xmlOutput.output(document, new FileWriter(file));

                    }
                }
            }
        }
    }

    private static void loadL2SSkillLearn() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        File dir = new File("/home/alf/Desktop/l2s_ertheia/Data/gameserver/data/skill_tree/normal_skill_tree");
        File[] files = dir.listFiles();
        for (File file : files) {
            Document document = builder.build(file);
            Element rootElement = document.getRootElement();

            for (Element treeElement : rootElement.getChildren()) {
                String name = treeElement.getName();

                if (!name.equals("normal_skill_tree"))
                    continue;

                for (Element classElement : treeElement.getChildren()) {
                    name = classElement.getName();
                    if (!name.equals("class"))
                        continue;

                    int classId = Integer.parseInt(classElement.getAttributeValue("id"));

                    Map<Integer, L2SkillLearn> classList = new HashMap<>();

                    for (Element skillElement : classElement.getChildren()) {
                        name = skillElement.getName();
                        if (!name.equals("skill"))
                            continue;

                        if (skillElement.getAttributeValue("race") != null)
                            continue;

                        if (skillElement.getAttributeValue("min_level") == null)
                            continue;

                        L2SkillLearn sl = parseL2S(skillElement);

                        classList.put(SkillTable.getSkillHashCode(sl.getSkillId(), sl.getSkillLevel()), sl);
                    }

                    l2sSkillLearn.put(classId, classList);
                }

            }
        }
    }

    private static L2SkillLearn parseL2S(Element element) {
        StatsSet set = new StatsSet();
        set.set("name", element.getAttributeValue("name"));
        set.set("skillId", Integer.parseInt(element.getAttributeValue("id")));
        set.set("skillLvl", Integer.parseInt(element.getAttributeValue("level")));

        if (element.getAttributeValue("cost") == null) {
            set.set("autoLearn", true);
        } else {
            set.set("sp", Integer.parseInt(element.getAttributeValue("cost")));
        }

        set.set("minLevel", Integer.parseInt(element.getAttributeValue("min_level")));

        return new L2SkillLearn(set);
    }
}
