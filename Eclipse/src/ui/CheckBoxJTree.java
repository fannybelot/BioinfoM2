package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;


public class CheckBoxJTree extends JTree {
	private static final long serialVersionUID = 8066950848067367101L;
	
	CheckBoxTreeNode root;
	
	public CheckBoxJTree(CheckBoxTreeNode root){
		super(root);
		this.root = root;
		this.setCellRenderer(new CheckBoxNodeRenderer());
		this.setCellEditor(new CheckBoxNodeEditor(this));
		this.setEditable(true);
	}
	
	public CheckBoxTreeNode getRoot(){
		return root;
	}
	
    public Vector<String> getCheckedNodes(){
    	Vector<String> checkedNodes = new Vector<String>();
    	@SuppressWarnings("unchecked")
		Enumeration<CheckBoxTreeNode> enumAll = ((DefaultMutableTreeNode) root).depthFirstEnumeration();
    	CheckBoxNode curCheckBox;
    	while(enumAll.hasMoreElements()){
    		curCheckBox = (CheckBoxNode) enumAll.nextElement().getUserObject();
    		
    		if(curCheckBox.isSelected() == true){
    			checkedNodes.add(curCheckBox.getText());
    		}
    	}
		return checkedNodes;
    }
    
    public void setNodeColor(String nodeName, Color color){
    	CheckBoxTreeNode node = findCheckBoxTreeNodeByName(nodeName);
    	if(node == null) return;
    	
		node.setColor(color);
		updateNodeColor((CheckBoxTreeNode) node.getParent());
    	this.revalidate();
    	this.repaint();
    }
    
    public void updateNodeColor(CheckBoxTreeNode node){
    	if(node == null){
    		return;
    	}
    	int nbChildren = node.getChildCount();
    	if(nbChildren == 0) return;
    	
    	CheckBoxTreeNode curChild = (CheckBoxTreeNode) node.getChildAt(0);
    	Color color = curChild.getColor();
    	for (int i = 1; i<nbChildren; i++){
    		curChild = (CheckBoxTreeNode) node.getChildAt(i);
    		if(curChild != null){
    			if(!curChild.hasSpecificColor()){
    				return;
    			}
    			if(!curChild.getColor().equals(color)){
    				return;
    			}
    		}
    	}
    	//from here on, we know that all children have same color
		node.setColor(color);
		updateNodeColor((CheckBoxTreeNode) node.getParent());
    }
    
	public CheckBoxTreeNode findCheckBoxTreeNodeByName(String name){
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> enumAll = root.depthFirstEnumeration();
		DefaultMutableTreeNode curNode;
		while(enumAll.hasMoreElements()){
			curNode = enumAll.nextElement();
			if(curNode instanceof CheckBoxTreeNode){
				if(curNode.toString().equals(name)){
					return (CheckBoxTreeNode) curNode;
				}
			}
		}
		return null;
	}
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
	private JCheckBox leafRenderer = new JCheckBox();

	//private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();

	Color selectionBorderColor, selectionForeground, selectionBackground,
	textForeground, textBackground;

	protected JCheckBox getLeafRenderer() {
		return leafRenderer;
	}

	public CheckBoxNodeRenderer() {
		Font fontValue;
		fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) {
			leafRenderer.setFont(fontValue);
		}
		Boolean booleanValue = (Boolean) UIManager
				.get("Tree.drawsFocusBorderAroundIcon");
		leafRenderer.setFocusPainted((booleanValue != null)
				&& (booleanValue.booleanValue()));

		selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		String stringValue = tree.convertValueToText(value, selected,
				expanded, leaf, row, false);
		leafRenderer.setText(stringValue);
		leafRenderer.setSelected(false);

		leafRenderer.setEnabled(tree.isEnabled());

		if (selected) {
			leafRenderer.setForeground(selectionForeground);
			leafRenderer.setBackground(selectionBackground);
		} else {
			leafRenderer.setForeground(textForeground);
			leafRenderer.setBackground(textBackground);
		}

		if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
			Object userObject = ((DefaultMutableTreeNode) value)
					.getUserObject();
			if (userObject instanceof CheckBoxNode) {
				CheckBoxNode node = (CheckBoxNode) userObject;
				leafRenderer.setText(node.getText());
				leafRenderer.setSelected(node.isSelected());
			}
		}
		
		//add color if needed
		if ((value != null) && (value instanceof CheckBoxTreeNode)) {
			if(((CheckBoxTreeNode) value).hasSpecificColor()){
				leafRenderer.setForeground(((CheckBoxTreeNode) value).getColor());
			}
		}
		return leafRenderer;
	}
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
	private static final long serialVersionUID = 7757884176632849946L;

	CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

	ChangeEvent changeEvent = null;

	JTree tree;

	public CheckBoxNodeEditor(JTree tree) {
		this.tree = tree;
	}

	public Object getCellEditorValue() {
		JCheckBox checkbox = renderer.getLeafRenderer();
		CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(),
				checkbox.isSelected());
		return checkBoxNode;
	}

	public boolean isCellEditable(EventObject event) {
		boolean returnValue = false;
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(),
					mouseEvent.getY());
			if (path != null) {
				Object node = path.getLastPathComponent();
				if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
					Object userObject = treeNode.getUserObject();
					returnValue = (userObject instanceof CheckBoxNode);
				}
			}
		}
		return returnValue;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row) {

		Component editor = renderer.getTreeCellRendererComponent(tree, value,
				true, expanded, leaf, row, true);

		// editor always selected / focused
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (stopCellEditing()) {
					fireEditingStopped();
				}
			}
		};
		if (editor instanceof JCheckBox) {
			((JCheckBox) editor).addItemListener(itemListener);
		}
		CheckNodeCallback(tree, (DefaultMutableTreeNode) value);

		return editor;
	}
	
	/**
	 * Function called when the user checks "noeud". This functions makes sure
	 * that the tree is left in a coherent check state : for instance, if you
	 * check a node, all subnodes must be checked
	 * @param tree
	 * @param noeud
	 */
	private void CheckNodeCallback(JTree tree, DefaultMutableTreeNode noeud){
    	Boolean statusCochage = ! ((CheckBoxNode) noeud.getUserObject()).isSelected();
    	//Cette fonction est appellée avant que la case soit effectivement cochée,
    	//d'ou la présence du "not"

    	//on change le status de tous les enfants
    	DefaultMutableTreeNode noeudAct;
    	@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> enumEnfants = noeud.breadthFirstEnumeration();
    	while(enumEnfants.hasMoreElements()){
    		noeudAct = enumEnfants.nextElement();
    		((CheckBoxNode) noeudAct.getUserObject()).setSelected(statusCochage);
    		//System.out.println(noeudAct.getUserObject().is);
    	}
    	
    	//on regarde les frères, s'ils ont tous le même status, on met a jour le pere
    	updateNode((DefaultMutableTreeNode) noeud.getParent());
    	
    	//coche les parents si statusCochage = true
    	DefaultMutableTreeNode parentAct = noeud;
    	if(statusCochage){
    		while((parentAct = (DefaultMutableTreeNode) parentAct.getParent()) != null)
    		((CheckBoxNode) parentAct.getUserObject()).setSelected(true);
    	}
    	
    	tree.revalidate();
    	tree.repaint();
    }
	
    /**
     * Helps keep the tree in a coherent check state. For example,
     * when all child of a node are unchecked, this node needs to go "unchecked"
     * @param pere
     */
    private void updateNode(DefaultMutableTreeNode pere){
    	if(pere == null) return;
    	
    	int nbEnfants = pere.getChildCount();
    	if(nbEnfants == 0) return;
    	
    	DefaultMutableTreeNode enfantAct = (DefaultMutableTreeNode) pere.getChildAt(0);
    	Boolean statusCochage = ((CheckBoxNode) enfantAct.getUserObject()).isSelected();
    	for (int i = 1; i<nbEnfants; i++){
    		enfantAct = (DefaultMutableTreeNode) pere.getChildAt(i);
    		if(enfantAct != null){
    			if(((CheckBoxNode) enfantAct.getUserObject()).isSelected() != statusCochage){
    				return;
    			}
    		}
    	}
    	//from here on, we know that all children have the same check-status
		((CheckBoxNode) pere.getUserObject()).setSelected(statusCochage);
		updateNode((DefaultMutableTreeNode) pere.getParent());
    }
}

class CheckBoxNode {
	private String text;

	private boolean selected;

	public CheckBoxNode(String text, boolean selected) {
		this.text = text;
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean newValue) {
		selected = newValue;
	}

	public String getText() {
		return text;
	}

	public void setText(String newValue) {
		text = newValue;
	}

	public String toString() {
		return text;
	}
}