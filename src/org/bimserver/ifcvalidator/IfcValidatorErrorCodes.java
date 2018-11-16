package org.bimserver.ifcvalidator;

import org.bimserver.bimbots.BimBotErrorCode;

public enum IfcValidatorErrorCodes implements BimBotErrorCode {
	ISSUE_EXCEPTION(1), IO_EXCEPTION(2);

	private int code;

	private IfcValidatorErrorCodes(int code) {
		this.code = code;
	}
	
	@Override
	public int getErrorCode() {
		return code;
	}
}
