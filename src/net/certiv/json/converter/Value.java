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
 * Versions:
 * // Version ==========
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 * // Version ==========
 *******************************************************************************/
// ValueClass ==========
package net.certiv.json.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.certiv.json.types.Op;
import net.certiv.json.util.Strings;

public class Value {

	private static final Pattern pattern = Pattern.compile("^\\s*([+-]?[0-9]*[.]?[0-9]+)\\s*(\\w*)\\s*$");

	public static final Value EMPTY = new Value("", "", true);
	public static final Value TRUE = new Value(1, "", true);
	public static final Value FALSE = new Value(0, "", true);
	public static final Value INVALID = new Value(0, "", false);

	public final String asString;
	public final String basis;
	public final String unit;
	public final boolean numeric;
	public final boolean valid;

	public Value(String value) {
		if (value == null) value = "";
		this.asString = value;
		Matcher m = pattern.matcher(value);
		if (m.matches()) {
			this.basis = m.group(1);
			this.unit = m.group(2);
			this.numeric = Strings.numeric(this.asString);
		} else {
			this.basis = value;
			this.unit = "";
			this.numeric = false;
		}
		valid = true;
	}

	public Value(double number, String unit, boolean valid) {
		this.asString = String.valueOf(number) + " " + unit;
		this.basis = String.valueOf(number);
		this.unit = unit;
		this.numeric = true;
		this.valid = valid;
	}

	public Value(String value, String unit, boolean valid) {
		this.asString = value + " " + unit;
		this.basis = value;
		this.unit = unit;
		this.numeric = Strings.numeric(this.asString);
		this.valid = valid;
	}

	public Value add(Value o) {
		if (comparable(o)) {
			String u = unit.equals("") ? o.unit : unit;
			if (numeric) {
				return new Value(num() + o.num(), u, true);
			} else {
				return new Value(basis + o.basis, u, true);
			}
		}
		return new Value(basis, unit, false);
	}

	public Value sub(Value o) {
		if (comparable(o)) {
			String u = unit.equals("") ? o.unit : unit;
			if (numeric) {
				return new Value(num() - o.num(), u, true);
			} else {
				return new Value(removeLast(basis, o.basis), u, true);
			}
		}
		return new Value(basis, unit, false);
	}

	public Value mult(Value o) {
		if (comparable(o) && numeric) {
			return new Value(num() * o.num(), unit, true);
		} else if (comparableMixed(o)) {
			int rep = Double.valueOf(numeric ? num() : o.num()).intValue();
			String n = numeric ? o.basis : basis;
			StringBuilder sb = new StringBuilder(n);
			for (int cnt = 0; cnt < rep - 1; cnt++) {
				sb.append(n);
			}
			String u = unit.equals("") ? o.unit : unit;
			return new Value(sb.toString(), u, true);
		}
		return new Value(basis + o.basis, unit, false);
	}

	public Value div(Value o) {
		if (comparable(o) && numeric) {
			if (o.num() != 0) {
				return new Value(num() / o.num(), unit, true);
			} else {
				return new Value(0, unit, false);
			}
		} else if (comparableMixed(o) && o.numeric) { // an int divisor of segments
			int rep = Double.valueOf(o.num()).intValue();
			String[] v = asString.split("[,\\. ]");
			rep = v.length / rep;
			StringBuilder sb = new StringBuilder();
			for (int cnt = 0; cnt < rep; cnt++) {
				sb.append(v[cnt]);
			}
			return new Value(sb.toString(), unit, true);
		}
		return new Value(basis, unit, false);
	}

	public Value compute(Op op, Value o) {
		switch (op) {
			case EQ:
				return o; // assignment
			case EQV:
			case NEQ:
			case LT:
			case LTE:
			case GT:
			case GTE:
				return compare(op, o);
			case PLUS:
				return add(o);
			case MINUS:
				return sub(o);
			case MULT:
				return mult(o);
			case DIV:
				return div(o);
			default:
				return INVALID;
		}
	}

	public Value compare(Op op, Value o) {
		if (comparable(o) && numeric) {
			switch (op) {
				case EQV:
					return num() == o.num() ? TRUE : FALSE;
				case NEQ:
					return num() != o.num() ? TRUE : FALSE;
				case GT:
					return num() > o.num() ? TRUE : FALSE;
				case GTE:
					return num() >= o.num() ? TRUE : FALSE;
				case LT:
					return num() < o.num() ? TRUE : FALSE;
				case LTE:
					return num() <= o.num() ? TRUE : FALSE;
				default:
					return INVALID;
			}
		}
		if (comparableStrings(o)) {
			switch (op) {
				case EQV:
					return basis.compareTo(o.basis) == 0 ? TRUE : FALSE;
				case NEQ:
					return basis.compareTo(o.basis) != 0 ? TRUE : FALSE;
				case GT:
					return basis.compareTo(o.basis) > 0 ? TRUE : FALSE;
				case GTE:
					return basis.compareTo(o.basis) >= 0 ? TRUE : FALSE;
				case LT:
					return basis.compareTo(o.basis) < 0 ? TRUE : FALSE;
				case LTE:
					return basis.compareTo(o.basis) <= 0 ? TRUE : FALSE;
				default:
					return INVALID;
			}
		}
		return INVALID;
	}

	public double num() {
		if (valid && numeric) {
			return Double.valueOf(basis).doubleValue();
		}
		return 0;
	}

	public boolean comparable(Value o) {
		if (valid && o.valid
				&& (o.unit.equals(unit)
						|| o.unit.equals("")
						|| unit.equals(""))
				&& o.numeric == numeric) return true;
		return false;
	}

	// ignore numeric and units, treat as strings
	public boolean comparableStrings(Value o) {
		if (valid && o.valid) return true;
		return false;
	}

	// one numeric and the other not
	public boolean comparableMixed(Value o) {
		if (valid && o.valid
				&& (o.unit.equals(unit)
						|| o.unit.equals("")
						|| unit.equals(""))
				&& o.numeric != numeric) return true;
		return false;
	}

	private String removeLast(String value, String obj) {
		int idx = value.lastIndexOf(obj);
		if (idx == -1) return value;
		StringBuilder sb = new StringBuilder(value);
		sb.delete(idx, idx + obj.length());
		return sb.toString().trim();
	}
}

// ValueClass ==========
