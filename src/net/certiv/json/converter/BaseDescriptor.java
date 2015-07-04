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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import net.certiv.json.symbol.Symbol;
import net.certiv.json.util.Strings;

public abstract class BaseDescriptor implements IDescriptor {

	// reference to carry data
	public PhaseState state;

	// context reference
	public ParserRuleContext ctx;

	// local state data
	protected StringBuilder sb;
	public boolean resolved = false; // node resolved?
	public Value value; // the node value
	public List<String> properties; // node properties

	// Comment Collection ///////////////////////////

	// Helpers are in PhaseBase
	private boolean collectComments = false;
	private String commentLeft = "";
	private String commentRight = "";

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
	public String content() {
		return sb.toString();
	}

	@Override
	public void initialize() {
		value = Value.TRUE;
	}

	@Override
	public Value processOnEntry() {
		return process();
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
			return getLeftComment() + strValue + Strings.SP + getRightComment();
		} else {
			return strValue;
		}
	}

	@Override
	public void setCollectComments(boolean collect) {
		this.collectComments = collect;
	}

	@Override
	public boolean getCollectComments() {
		return this.collectComments;
	}

	@Override
	public void setLeftComment(String comment) {
		this.commentLeft = comment;
	}

	@Override
	public void setRightComment(String comment) {
		this.commentRight = comment;
	}

	@Override
	public String getLeftComment() {
		return commentLeft;
	}

	@Override
	public String getRightComment() {
		return commentRight;
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
}

// BaseDescriptorClass ==========
