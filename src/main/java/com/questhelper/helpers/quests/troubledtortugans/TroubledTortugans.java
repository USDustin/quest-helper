package com.questhelper.helpers.quests.troubledtortugans;

import com.questhelper.collections.ItemCollections;
import com.questhelper.panel.PanelDetails;
import com.questhelper.questhelpers.BasicQuestHelper;
import com.questhelper.questinfo.QuestHelperQuest;
import com.questhelper.requirements.Requirement;
import com.questhelper.requirements.conditional.Conditions;
import com.questhelper.requirements.item.ItemRequirement;
import com.questhelper.requirements.player.SkillRequirement;
import com.questhelper.requirements.quest.QuestRequirement;
import static com.questhelper.requirements.util.LogicHelper.*;
import com.questhelper.requirements.util.LogicType;
import com.questhelper.requirements.var.VarbitRequirement;
import com.questhelper.requirements.zone.Zone;
import com.questhelper.rewards.ExperienceReward;
import com.questhelper.rewards.UnlockReward;
import com.questhelper.steps.ConditionalStep;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.NpcStep;
import com.questhelper.steps.ObjectStep;
import com.questhelper.steps.QuestStep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.java.Log;
import net.runelite.api.NpcID;
import net.runelite.api.Skill;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

public class TroubledTortugans extends BasicQuestHelper
{

	// Required items
	ItemRequirement axe;
	ItemRequirement hammer;
	ItemRequirement saw;


	// Mid quest required items
	ItemRequirement palmLeaf, seaweed, palmLeafHighlighted, seaweedHighlighted, bandage, jatobaLogs10, seaShells6,
		tortuganScutes6, seaShell1, seaShell2, tortuganScute1, tortuganScute2, jatobaLogs1, jatobaLogs2;

	// Requirements
	Requirement inConch, allRepairItems, krillsStallRepaired, krillsWallRepaired, cocosStallRepaired;

	// Steps
	DetailedQuestStep talkToTortugan, getSeaweed, shakePalmTree, getPalmLeaf, usePalmLeafOnSeaweed,
		talkToFloopaWithBandage, boardBoat, disembarkAtTheGreatConch, talkToFloopaOnDocks, talkToRaley, collectJatobaLogs,
		collectSeaShells, collectTortuganScutes, makeRepairs, repairKrillsWall, repairKrillsStall, repairStromsWall, repairStromsCrate, repairCocosStall, repairCocosCrate;

	// Zones
	Zone conch;


	/**
	 * @return
	 */
	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		Map<Integer, QuestStep> steps = new HashMap<>();
		initializeRequirements();
		setupConditions();
		setupSteps();

		// DEBUG: print current quest var to log
		if (client != null)
		{
			int qVar = client.getVarbitValue(QuestHelperQuest.TROUBLED_TORTUGANS.getId());
			System.out.println("[TroubledTortugans] quest var = " + qVar);

			int qVar2 = client.getVarbitValue(VarbitID.TT);
			System.out.println("[TroubledTortugans] VarbitID.TT = " + qVar2);
		}

		steps.put(0, talkToTortugan);
		steps.put(1, getSeaweed);
		steps.put(2, shakePalmTree);
		steps.put(3, getPalmLeaf);
		ConditionalStep makeBandage = new ConditionalStep(this, getSeaweed);
		makeBandage.addStep(new Conditions(palmLeafHighlighted, seaweedHighlighted), usePalmLeafOnSeaweed);
		steps.put(4, makeBandage);
		steps.put(5, talkToFloopaWithBandage);
		steps.put(6, boardBoat);
		steps.put(7, disembarkAtTheGreatConch);
		steps.put(8, talkToFloopaOnDocks);
		steps.put(9, talkToRaley);

		ConditionalStep gatherSupplies = new ConditionalStep(this, collectJatobaLogs, axe, jatobaLogs10);
		// Only have logs: collect shells
		gatherSupplies.addStep(
			new Conditions(jatobaLogs10, new Conditions(LogicType.NOR, seaShells6)), collectSeaShells
		);

		// Logs and shells: collect scutes
		gatherSupplies.addStep(
			new Conditions(jatobaLogs10, seaShells6, new Conditions(LogicType.NOR, tortuganScutes6)), collectTortuganScutes
		);

		steps.put(17, gatherSupplies);

		ConditionalStep repairList = new ConditionalStep(this, repairKrillsWall, hammer, saw, jatobaLogs2, tortuganScute2);
		repairList.addStep();

		// Logs, shells, and scutes: make repairs
//		gatherSupplies.addStep(
//			new Conditions(jatobaLogs10, seaShells6, tortuganScutes6, hammer, saw), repairCocosStall
//		);

//		gatherSupplies.addStep(jatobaLogs10, collectTortuganScutes);
//		gatherSupplies.addStep(and(jatobaLogs10, tortuganScutes6), collectSeaShells);
//		gatherSupplies.addStep(and(jatobaLogs10, tortuganScutes6, seaShells6), repairKrillsStall);
		//ConditionalStep gatherSupplies = new ConditionalStep(this, collectJatobaLogs, axe, hammer, saw,
		//	jatobaLogs10, seaShells6, tortuganScutes6);

		//gatherSupplies.addStep(new Conditions(jatobaLogs10), collectSeaShells);
		//gatherSupplies.addStep(new Conditions(seaShells6), collectTortuganScutes);
		//gatherSupplies.addStep(new Conditions(tortuganScutes6), repairKrillsStall);
		steps.put(18, repairList);
		//steps.put(10, collectJatobaLogs);
		//steps.put(11, collectSeaShells);
		//steps.put(12, collectTortuganScutes);

		return steps;
	}

	/**
	 * This method should not be called directly.
	 * It is used internally by {@link #initializeRequirements()}.
	 */
	@Override
	protected void setupRequirements()
	{
		axe = new ItemRequirement("Any axe", ItemCollections.AXES).isNotConsumed();
		hammer = new ItemRequirement("Hammer", ItemCollections.HAMMER).isNotConsumed();
		saw = new ItemRequirement("Saw", ItemCollections.SAW).isNotConsumed();

		palmLeaf = new ItemRequirement("Palm leaf", ItemID.PALM_LEAF);
		seaweed = new ItemRequirement("Seaweed", ItemID.SEAWEED);
		palmLeafHighlighted = new ItemRequirement("Palm leaf", ItemID.PALM_LEAF);
		palmLeafHighlighted.setHighlightInInventory(true);
		seaweedHighlighted = new ItemRequirement("Seaweed", ItemID.SEAWEED);
		seaweedHighlighted.setHighlightInInventory(true);

		jatobaLogs10 = new ItemRequirement("Jatoba logs", ItemID.JATOBA_LOGS, 10);
		seaShells6 = new ItemRequirement("Sea shell", ItemID.SEA_SHELL, 6);
		tortuganScutes6 = new ItemRequirement("Tortugan scute", ItemID.TORTUGAN_SCUTE, 6);

		jatobaLogs1 = new ItemRequirement("Jatoba logs", ItemID.JATOBA_LOGS, 1);
		jatobaLogs2 = new ItemRequirement("Jatoba logs", ItemID.JATOBA_LOGS, 2);
		seaShell1 = new ItemRequirement("Sea shell", ItemID.SEA_SHELL, 1);
		seaShell2 = new ItemRequirement("Sea shell", ItemID.SEA_SHELL, 2);
		tortuganScute1 = new ItemRequirement("Tortugan scute", ItemID.TORTUGAN_SCUTE, 1);
		tortuganScute2 = new ItemRequirement("Tortugan scute", ItemID.TORTUGAN_SCUTE, 2);

	}

	public void setupConditions()
	{
		allRepairItems = new Conditions(LogicType.AND, jatobaLogs10, seaShells6, tortuganScutes6);
		//krillsStallRepaired = new VarbitRequirement()
	}

	public void setupSteps()
	{
		talkToTortugan = new NpcStep(this, NpcID.INJURED_TORTUGAN, new WorldPoint(2962, 2604, 0), "Talk to the injured Tortugan on the remote island west of The Great Conch");
		getSeaweed = new ObjectStep(this, ItemID.SEAWEED, new WorldPoint(2962, 2600, 0), "Collect some seaweed from the shore");
		shakePalmTree = new ObjectStep(this, ObjectID.TT_PALM_2, new WorldPoint(2964, 2604, 0), "Shake the palm tree to get a palm leaf");
		getPalmLeaf = new ObjectStep(this, ItemID.PALM_LEAF, "Pick up the palm leaf");
		usePalmLeafOnSeaweed = new DetailedQuestStep(this, "Use the palm leaf on the seaweed to create a bandage", palmLeafHighlighted, seaweedHighlighted);
		talkToFloopaWithBandage = new NpcStep(this, NpcID.INJURED_TORTUGAN, new WorldPoint(2962, 2604, 0), "Talk to the injured Tortugan with the bandage in your inventory");
		boardBoat = new ObjectStep(this, ObjectID.SAILING_MOORING_REMOTE_ISLAND, new WorldPoint(2973, 2603, 0), "Board your boat");
		disembarkAtTheGreatConch = new ObjectStep(this, ObjectID.SAILING_GANGPLANK_PROXY, new WorldPoint(3174, 2367, 0), "Disembark at The Great Conch");
		talkToFloopaOnDocks = new NpcStep(this, NpcID.FLOOPA,"Talk to Floopa on the docks of The Great Conch.");
		talkToRaley = new NpcStep(this, NpcID.ELDER_RALEY, new WorldPoint(3187, 2404, 0), "Talk to Elder Raley. His hut is the first one east of the entrance");
		collectJatobaLogs = new ObjectStep(this, ObjectID.JATOBA_TREE, new WorldPoint(3112, 2412, 0), "Collect 10 Jatoba logs from the Jatoba trees on the western side of the island", axe, jatobaLogs10);
		collectSeaShells = new ObjectStep(this, ItemID.SEA_SHELL, new WorldPoint(3179, 2384, 0), "Collect 6 sea shells from the shore on the south side of the island", seaShells6);
		collectTortuganScutes = new ObjectStep(this, ItemID.TORTUGAN_SCUTE, "Collect 6 Tortugan scutes from around the village", tortuganScutes6);
		repairKrillsStall = new ObjectStep(this, ObjectID.TT_REPAIR_KRILL_STALL, "Repair Krill's stall", hammer, saw, axe, jatobaLogs2, seaShell1, tortuganScute1);
		repairKrillsWall = new ObjectStep(this, ObjectID.TT_REPAIR_WALL_BROKEN, new WorldPoint(3166, 2419, 0), "Repair Krill's wall", hammer, saw, jatobaLogs2, tortuganScute2);
		repairCocosStall = new ObjectStep(this, ObjectID.TT_REPAIR_STALL_COCO_BROKEN_NOOP, new WorldPoint(3170, 2406, 0), "Repair Coco's stall", hammer, saw, jatobaLogs2, seaShell1, tortuganScute1);
		repairKrillsStall = new ObjectStep(this, ObjectID.TT_REPAIR_KRILL_STALL, "Repair Krill's stall", hammer, saw, axe, jatobaLogs10, seaShells6, tortuganScutes6);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(hammer, saw, axe);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList();
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Arrays.asList("Gryphon (level 95), Shellbane Gryphon (level 235)");
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Arrays.asList(
			new ExperienceReward(Skill.SAILING, 10000),
			new ExperienceReward(Skill.SLAYER, 8000)

		);
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
			new UnlockReward("Great Conch Access"),
			new UnlockReward("1 quest point")
		);
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Finding the stranded Tortugan",
			Arrays.asList(talkToTortugan, getSeaweed, shakePalmTree, getPalmLeaf, usePalmLeafOnSeaweed,
				talkToFloopaWithBandage), palmLeaf, seaweed));
		allSteps.add(new PanelDetails("The Great Conch",
			Arrays.asList(boardBoat, disembarkAtTheGreatConch, talkToFloopaOnDocks, talkToRaley, collectJatobaLogs,
				collectSeaShells, collectTortuganScutes), hammer, saw, axe, jatobaLogs10, seaShells6, tortuganScutes6));

		return allSteps;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> requirements = new ArrayList<>();
		//requirements.add(new QuestRequirement(QuestHelperQuest.Pandemonium))
		requirements.add(new SkillRequirement(Skill.SLAYER, 51, false));
		requirements.add(new SkillRequirement(Skill.CONSTRUCTION, 48, true));
		requirements.add(new SkillRequirement(Skill.SAILING, 45, false));
		requirements.add(new SkillRequirement(Skill.HUNTER, 45, false));
		requirements.add(new SkillRequirement(Skill.WOODCUTTING, 40, true));
		requirements.add(new SkillRequirement(Skill.CRAFTING, 34, true));
		return requirements;
	}

}
