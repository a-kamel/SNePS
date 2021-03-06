package snip.Rules.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import sneps.CaseFrame;
import sneps.CustomException;
import sneps.Network;
import sneps.RCFP;
import sneps.Relation;
import sneps.Nodes.MolecularNode;
import sneps.Nodes.Node;
import sneps.Nodes.NodeSet;
import sneps.Nodes.VariableNode;
import sneps.SemanticClasses.Entity;
import sneps.SemanticClasses.Individual;
import sneps.match.Binding;
import sneps.match.LinearSubstitutions;
import sneps.match.Substitutions;
import snip.Rules.DataStructures.FlagNode;
import snip.Rules.DataStructures.FlagNodeSet;
import snip.Rules.DataStructures.RuleUseInfo;
import snip.Rules.DataStructures.RuleUseInfoSet;
import snip.Rules.DataStructures.SIndex;
import snip.Rules.Interfaces.NodeWithVar;

public class RuisSIndexTest {
	public static void main(String[] args) throws Exception {
		CaseFrame.createRuleCaseFrame();
		Relation[] relations = new Relation[12];
		for (int i = 0; i < relations.length; i += 4) {
			relations[i] = Network.defineRelation((char) ('A' + i / 4) + "1",
					"Infimum", "none", 1);
			relations[i + 1] = Network.defineRelation((char) ('A' + i / 4)
					+ "2", "Infimum", "none", 1);
			relations[i + 2] = Network.defineRelation((char) ('A' + i / 4)
					+ "3", "Infimum", "none", 1);
			relations[i + 3] = Network.defineRelation((char) ('A' + i / 4)
					+ "4", "Infimum", "none", 1);
		}

		VariableNode[] varNodes = getVarNodes(5);

		ArrayList<LinkedList<RCFP>> relationsProperties = getRelProp(relations);

		CaseFrame[] caseFrames = getCaseFrames(relationsProperties);

		NodeWithVar[] antNodesTemp = buildNodes(varNodes, relations, caseFrames);

		System.out.println("Relations: " + Arrays.toString(relations));
		System.out.println("Variable Nodes: " + Arrays.toString(varNodes));
		System.out
				.println("Antecedent Nodes: " + Arrays.toString(antNodesTemp));

		NodeSet nodeSet = new NodeSet();
		for (NodeWithVar mn : antNodesTemp)
			nodeSet.addNode((Node) mn);

		HashSet<Integer> varsIds = new HashSet<>();
		varsIds.add(varNodes[0].getId());
		varsIds.add(varNodes[1].getId());

		SIndex indexing = new SIndex(-1, varsIds, SIndex.RUIS, nodeSet);

		Node bob = Network.buildBaseNode("Bob", new Individual());
		Node mary = Network.buildBaseNode("mary", new Individual());
		Node tweety = Network.buildBaseNode("tweety", new Individual());
		Node mussab = Network.buildBaseNode("mussab", new Individual());
		Node eldash = Network.buildBaseNode("eldash", new Individual());

		System.out.println("====================================");
		System.out.println("Inserting a RUIS in the SIndex");
		insertIntoSIndexing(indexing, bob, mary, tweety, mussab, eldash,
				antNodesTemp, varNodes);

		System.out.println("Inserting another RUIS in the SIndex");
		insertIntoSIndexing(indexing, mussab, bob, tweety, eldash, mary,
				antNodesTemp, varNodes);

		System.out.println("Inserting yet another RUIS in the SIndex");
		insertIntoSIndexing(indexing, mussab, bob, eldash, mary, tweety,
				antNodesTemp, varNodes);

		System.out
				.println("The number of different substitutions of the shared variables: "
						+ indexing.getSize());
	}

	private static VariableNode[] getVarNodes(int n) {
		VariableNode[] varNodes = new VariableNode[n];
		for (int i = 0; i < n; i++) {
			Entity e = new Entity();
			varNodes[i] = Network.buildVariableNode(e);
		}
		return varNodes;
	}

	private static ArrayList<LinkedList<RCFP>> getRelProp(Relation[] relations) {
		ArrayList<LinkedList<RCFP>> res = new ArrayList<LinkedList<RCFP>>();
		for (int i = 0; i < relations.length; i += 4) {
			RCFP rel1 = Network.defineRelationPropertiesForCF(relations[i],
					"none", 1);
			RCFP rel2 = Network.defineRelationPropertiesForCF(relations[i + 1],
					"none", 1);
			RCFP rel3 = Network.defineRelationPropertiesForCF(relations[i + 2],
					"none", 1);
			RCFP rel4 = Network.defineRelationPropertiesForCF(relations[i + 3],
					"none", 1);
			LinkedList<RCFP> relCF1 = new LinkedList<RCFP>();
			relCF1.add(rel1);
			relCF1.add(rel2);
			relCF1.add(rel3);
			relCF1.add(rel4);
			res.add(relCF1);
		}
		return res;
	}

	private static CaseFrame[] getCaseFrames(
			ArrayList<LinkedList<RCFP>> relationsProperties)
			throws CustomException {
		CaseFrame[] res = new CaseFrame[relationsProperties.size()];
		int index = 0;
		for (LinkedList<RCFP> link : relationsProperties)
			res[index++] = Network.defineCaseFrame("Proposition", link);
		return res;
	}

	private static NodeWithVar[] buildNodes(VariableNode[] varNodes,
			Relation[] relations, CaseFrame[] caseFrames) throws Exception {
		NodeWithVar[] res = new NodeWithVar[(int) Math
				.ceil(relations.length / 4)];
		res[0] = (NodeWithVar) buildMolecularNode(varNodes[0], varNodes[1],
				varNodes[2], varNodes[3], relations[0], relations[1],
				relations[2], relations[3], caseFrames[0]);
		res[1] = (NodeWithVar) buildMolecularNode(varNodes[0], varNodes[1],
				varNodes[2], varNodes[4], relations[4], relations[5],
				relations[6], relations[7], caseFrames[1]);
		res[2] = (NodeWithVar) buildMolecularNode(varNodes[0], varNodes[1],
				varNodes[3], varNodes[4], relations[8], relations[9],
				relations[10], relations[11], caseFrames[2]);
		return res;
	}

	private static MolecularNode buildMolecularNode(VariableNode variableNode,
			VariableNode variableNode2, VariableNode variableNode3,
			VariableNode variableNode4, Relation relation, Relation relation2,
			Relation relation3, Relation relation4, CaseFrame caseFrame)
			throws Exception {
		Object[][] a1 = new Object[4][2];
		a1[0][0] = relation;
		a1[0][1] = variableNode;
		a1[1][0] = relation2;
		a1[1][1] = variableNode2;
		a1[2][0] = relation3;
		a1[2][1] = variableNode3;
		a1[3][0] = relation4;
		a1[3][1] = variableNode4;
		return Network.buildMolecularNode(a1, caseFrame);
	}

	private static RuleUseInfo getPosRui(Node signature, VariableNode var1,
			VariableNode var2, VariableNode var3, VariableNode var4, Node sub1,
			Node sub2, Node sub3, Node sub4) {
		FlagNode fn = new FlagNode(signature, null, 1);
		FlagNodeSet fns = new FlagNodeSet();
		fns.insert(fn);
		Substitutions sub = new LinearSubstitutions();
		sub = sub.insert(new Binding(var1, sub1));
		sub = sub.insert(new Binding(var2, sub2));
		sub = sub.insert(new Binding(var3, sub3));
		sub = sub.insert(new Binding(var4, sub4));
		RuleUseInfo rui = new RuleUseInfo(sub, 1, 0, fns);
		return rui;
	}

	private static RuleUseInfo getNegRui(Node signature, VariableNode var1,
			VariableNode var2, VariableNode var3, VariableNode var4, Node sub1,
			Node sub2, Node sub3, Node sub4) {
		FlagNode fn = new FlagNode(signature, null, 2);
		FlagNodeSet fns = new FlagNodeSet();
		fns.insert(fn);
		Substitutions sub = new LinearSubstitutions();
		sub = sub.insert(new Binding(var1, sub1));
		sub = sub.insert(new Binding(var2, sub2));
		sub = sub.insert(new Binding(var3, sub3));
		sub = sub.insert(new Binding(var4, sub4));
		RuleUseInfo rui = new RuleUseInfo(sub, 0, 1, fns);
		return rui;
	}

	private static void insertIntoSIndexing(SIndex sindexing, Node c1, Node c2,
			Node c3, Node c4, Node c5, NodeWithVar[] antNodesTemp,
			VariableNode[] varNodes) {
		RuleUseInfo rui = getPosRui((Node) antNodesTemp[0], varNodes[0],
				varNodes[1], varNodes[2], varNodes[3], c1, c2, c3, c4);
		printRuis(sindexing.insertRUI(rui));

		rui = getNegRui((Node) antNodesTemp[1], varNodes[0], varNodes[1],
				varNodes[2], varNodes[4], c1, c2, c3, c5);
		printRuis(sindexing.insertRUI(rui));

		rui = getPosRui((Node) antNodesTemp[2], varNodes[0], varNodes[1],
				varNodes[3], varNodes[4], c1, c2, c4, c5);
		printRuis(sindexing.insertRUI(rui));
	}

	private static void printRuis(RuleUseInfoSet ruis) {
		System.out.println(ruis.cardinality());
		for (RuleUseInfo tRui : ruis) {
			System.out.println("====================================");
			System.out.println("Positive flags: " + tRui.getPosCount());
			System.out.println("Negative flags: " + tRui.getNegCount());
			for (int i = 0; i < tRui.getSub().cardinality(); i++) {
				System.out.print(tRui.getSub().getBinding(i).getVariable()
						.getIdentifier());
				System.out.print(" is Bound to: ");
				System.out.println(tRui.getSub().getBinding(i).getNode()
						.getIdentifier());
			}
		}
		System.out.println("====================================");
		System.out.println("====================================");
	}
}
