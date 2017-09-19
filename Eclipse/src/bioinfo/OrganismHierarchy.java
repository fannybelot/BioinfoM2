package bioinfo;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author sindarus
 * Représente et contient la liste des organismes sous forme de hierarchie.
 */
public class OrganismHierarchy {
	private static Boolean isCreated = false;
	private static DefaultMutableTreeNode root = null;
	private static Vector<Organism> list = null;
	
	public static Boolean isCreated() {
		return isCreated;
	}
	
	public static DefaultMutableTreeNode getHierarchyRoot(){
		return root;
	}
	
	public static void createHierarchy(Vector<Organism> listOrganism){
		list = listOrganism;
		root = new DefaultMutableTreeNode("Organismes disponibles à l'analyse");
		
    	DefaultMutableTreeNode curKingdom = null;
    	DefaultMutableTreeNode curGroup = null;
    	DefaultMutableTreeNode curSubGroup = null;
    	for(Organism curOrganism : listOrganism){
			//Find the kingdom or create it
			curKingdom = findChild(root, curOrganism.getKingdom());
			if(curKingdom == null){	//if the kingdom is not in the hierarchy
				curKingdom = new DefaultMutableTreeNode(curOrganism.getKingdom());
				root.add(curKingdom);
			}
			//Find the group or creates it
			curGroup = findChild(curKingdom, curOrganism.getGroup());
			if(curGroup == null){
				curGroup = new DefaultMutableTreeNode(curOrganism.getGroup());
				curKingdom.add(curGroup);
			}
			//Find the sub-group or creates it
			curSubGroup = findChild(curGroup, curOrganism.getSubGroup());
			if(curSubGroup == null){
				curSubGroup = new DefaultMutableTreeNode(curOrganism.getSubGroup());
				curGroup.add(curSubGroup);
			}
			//Add the organism to the hierarchy
			curSubGroup.add(new DefaultMutableTreeNode(curOrganism));
		}
    	isCreated = true;
	}
	
	public static Vector<Organism> getAsList(){
		if(isCreated){
			return list;
		}
		else{
			return null;
		}
	}
	
	public static Organism findOrganismByName(String name){
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> enumAll = root.depthFirstEnumeration();
		DefaultMutableTreeNode curNode;
		Organism curOrganism;
		while(enumAll.hasMoreElements()){
			curNode = enumAll.nextElement();
			if(curNode.getUserObject().getClass() == Organism.class){
				curOrganism = (Organism) curNode.getUserObject();
				if (curOrganism.getName() == name){
					return curOrganism;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param noeud
	 * @param nom
	 * @return Renvoie le noeud enfant qui s'appelle "nom", null s'il n'y en a pas
	 */
	private static DefaultMutableTreeNode findChild(DefaultMutableTreeNode noeud, String nom){
		for(int i=0; i<noeud.getChildCount(); i++){
			if(noeud.getChildAt(i).toString().equals(nom)){
				return (DefaultMutableTreeNode) noeud.getChildAt(i);
			}
		}
		return null;
	}
}