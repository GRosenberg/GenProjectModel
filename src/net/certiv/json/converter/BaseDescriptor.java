/*******************************************************************************
 * // Copyright ==========
 * Copyright (c) 2008-2014 G Rosenberg.
 * // Copyright ==========
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 * // Contributor ==========
 *		G Rosenberg - initial API and implementation
 * // Contributor ==========
 * 
 * // Description ==========
 * Part of GenPackage
 * // Description ==========
 *
 * Versions:
 * // Version ==========
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 * // Version ==========
 *******************************************************************************/
// BaseDescriptorClass ==========
package net.certiv.json.converter;

import java.util.ArrayList;
import java.util.List;

import net.certiv.json.symbol.Symbol;
import net.certiv.json.types.StmtType;
import net.certiv.json.util.Log;
import net.certiv.json.util.Strings;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

public abstract class BaseDescriptor implements IDescriptor {

	// reference to carry data
	public PhaseState state;

	// context reference
	public ParserRuleContext ctx;

	// local state data
	public boolean resolved = false; 	// node resolved?
	public Value value; 				// the node value
	public List<String> properties;		// node properties

	// Comment Collection ///////////////////////////

	// Helpers are in PhaseBase
	public boolean collectComments = false;
	public String commentLeft;
	public String commentRight;

	// BaseDescriptor Helpers /////////////////////

	public BaseDescriptor(ParserRuleContext ctx) {
		this.ctx = ctx;
	}

	public void setPhaseState(PhaseState state) {
		this.state = state;
	}

	public BaseDescriptor getDescriptor(ParseTree ctx) {
		return state.nodeContextMap.get(ctx);
	}

	@Override
	public void initialize() {
		value = Value.TRUE;
	}

	@Override
	public Value processOnEntry() {
		return value;
	}

	@Override
	public Value processOnExit() {
		return process();
	}

	@Override
	public Value process(String property) {
		if (properties == null) {
			properties = new ArrayList<>();
		}
		properties.add(property);
		return process();
	}

	@Override
	public Value process() {
		return value;
	}

	@Override
	public String toString() {
		String strValue = value.toString() != null ? value.toString() : "<Null Value>";
		if (collectComments) {
			return commentLeft + strValue + Strings.SP + commentRight;
		} else {
			return strValue;
		}
	}

	// Symbol Table Helpers ///////////////////////////

	protected String vName = "";
	protected Symbol sym = null;

	public Symbol findVarByName(String name) {
		return state.symTable.currentScope().resolve(name);
	}

	public boolean inCurrentScope(Symbol sym) {
		return state.symTable.currentScope().genId == sym.genId();
	}

	public Value getVarValue(String name) {
		Symbol sym = findVarByName(name);
		if (sym != null) {
			value = sym.getDescriptor().value;
		} else {
			value = Value.INVALID;
		}
		return value;
	}

	public void setVarValue(Value value) {
		if (sym != null) {
			sym.getDescriptor().value = value;
			this.value = value;
		} else {
			value = Value.INVALID;
		}
	}

	// Element Association Helpers ////////////////////////////
	// TODO: depreciate

	public List<ElementAssoc> assocElement; // elements of context

	public class ElementAssoc {

		public StmtType type;
		public int index;
		public Token token;
		public ParseTree tree;

		public ElementAssoc(StmtType type, Token token, ParseTree tree) {
			super();
			this.type = type;
			this.index = token.getTokenIndex();
			this.token = token;
			this.tree = tree;
		}
	}

	public List<ParseTree> getContextElements(ParserRuleContext ctx) {
		return ctx.children;
	}

	public ParseTree firstElement(ParserRuleContext ctx) {
		return ctx.children.get(0);
	}

	public ParseTree lastElement(ParserRuleContext ctx) {
		return ctx.children.get(ctx.children.size() - 1);
	}

	public ParseTree priorElement(ParserRuleContext ctx, ParseTree node) {
		buildElementAssociations(ctx);
		int index = indexOfElement(ctx, node);
		if (index < 1 || index >= assocElement.size()) return null;
		return assocElement.get(index - 1).tree;
	}

	public ParseTree nextElement(ParserRuleContext ctx, ParseTree node) {
		buildElementAssociations(ctx);
		int index = indexOfElement(ctx, node);
		if (index < 0 || index > assocElement.size() - 2) return null;
		return assocElement.get(index + 1).tree;
	}

	public ParseTree getElementAt(ParserRuleContext ctx, int index) {
		buildElementAssociations(ctx);
		if (index < 0 || index > assocElement.size() - 1) return null;
		return assocElement.get(index).tree;
	}

	public int indexOfElement(ParserRuleContext ctx, ParseTree node) {
		buildElementAssociations(ctx);
		int index = node.getSourceInterval().a;
		for (int idx = 0; idx < assocElement.size(); idx++) {
			ElementAssoc e = assocElement.get(idx);
			if (e.index == index) {
				return idx;
			}
		}
		return -1;
	}

	public void buildElementAssociations(ParserRuleContext ctx) {
		if (assocElement == null) {
			assocElement = new ArrayList<>();
			for (ParseTree node : ctx.children) {
				if (node instanceof ParserRuleContext) {
					Token t = ((ParserRuleContext) node).getStart();
					assocElement.add(new ElementAssoc(StmtType.RULE, t, node));
				} else if (node instanceof TerminalNodeImpl) {
					Token t = ((TerminalNodeImpl) node).getSymbol();
					if (node instanceof ErrorNodeImpl) {
						assocElement.add(new ElementAssoc(StmtType.ERROR, t, node));
					} else {
						assocElement.add(new ElementAssoc(StmtType.TERMINAL, t, node));
					}
				} else {
					Log.warn(this, "Unexpected node type skipped: " + node.getClass().getName());
				}
			}
		}
	}
}

// BaseDescriptorClass ==========
