package ui;

import java.awt.Color;

import javax.swing.tree.DefaultMutableTreeNode;

public class CheckBoxTreeNode extends DefaultMutableTreeNode{
	private static final long serialVersionUID = 4650508859433460473L;
	private Color color = null;

	public CheckBoxTreeNode(CheckBoxNode node){
		super(node);
		this.color = null;
	}
	
	/**
	 * Constructor that copies a DefautlMutableTreeNode and its children,
	 * transforming every user-objet into checkboxNodes with the label
	 * obtained by calling "toString()" onto the user-object
	 * @param root
	 */
	public CheckBoxTreeNode(DefaultMutableTreeNode root){
		super(new CheckBoxNode(root.toString(), false));
		DefaultMutableTreeNode curChild;
		for(int i = 0; i<root.getChildCount(); i++){
			curChild = (DefaultMutableTreeNode) root.getChildAt(i);
			this.add(new CheckBoxTreeNode(curChild));
		}
		this.color = null;
	}
	
	public void add(CheckBoxTreeNode node){
		super.add(node);
	}
	
	public Color getColor(){
		return this.color;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public Boolean hasSpecificColor(){
		return (color != null);
	}
}
