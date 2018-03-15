package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

public class CarparkAccessibilityConfiguration {
	private int ratioHandicappedToRegularParking;
	private int regularCarparkWidth;
	private int regularCarparkDepth;
	private int regularCarparkVariation;
	private int handicappedCarparkWidth;
	private int handicappedCarparkDepth;
	private int handicappedCarparkVariation;

	public CarparkAccessibilityConfiguration() {
		this.ratioHandicappedToRegularParking = 10;
		this.regularCarparkDepth = 4800;
		this.regularCarparkWidth = 2400;
		this.regularCarparkVariation = 100;
		this.handicappedCarparkDepth = 4800;
		this.handicappedCarparkWidth = 3600;
		this.handicappedCarparkVariation = 100;
	}

	public int getRatioHandicappedToRegularParking() {
		return ratioHandicappedToRegularParking;
	}

	public int getRegularCarparkWidth() {
		return regularCarparkWidth;
	}

	public void setRegularCarparkWidth(int regularCarparkWidth) {
		this.regularCarparkWidth = regularCarparkWidth;
	}

	public int getRegularCarparkDepth() {
		return regularCarparkDepth;
	}

	public void setRegularCarparkDepth(int regularCarparkDepth) {
		this.regularCarparkDepth = regularCarparkDepth;
	}

	public int getRegularCarparkVariation() {
		return regularCarparkVariation;
	}

	public void setRegularCarparkVariation(int regularCarparkVariation) {
		this.regularCarparkVariation = regularCarparkVariation;
	}

	public int getHandicappedCarparkWidth() {
		return handicappedCarparkWidth;
	}

	public void setHandicappedCarparkWidth(int handicappedCarparkWidth) {
		this.handicappedCarparkWidth = handicappedCarparkWidth;
	}

	public int getHandicappedCarparkDepth() {
		return handicappedCarparkDepth;
	}

	public void setHandicappedCarparkDepth(int handicappedCarparkDepth) {
		this.handicappedCarparkDepth = handicappedCarparkDepth;
	}

	public int getHandicappedCarparkVariation() {
		return handicappedCarparkVariation;
	}

	public void setHandicappedCarparkVariation(int handicappedCarparkVariation) {
		this.handicappedCarparkVariation = handicappedCarparkVariation;
	}

	public void setRatioHandicappedToRegularParking(int ratioHandicappedToRegularParking) {
		this.ratioHandicappedToRegularParking = ratioHandicappedToRegularParking;
	}
}