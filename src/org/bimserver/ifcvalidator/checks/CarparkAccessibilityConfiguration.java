package org.bimserver.ifcvalidator.checks;

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