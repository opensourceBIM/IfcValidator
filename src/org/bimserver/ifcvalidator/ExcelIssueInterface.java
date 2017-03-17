package org.bimserver.ifcvalidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.bimserver.validationreport.Issue;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueContainerSerializer;
import org.bimserver.validationreport.IssueValidationException;
import org.bimserver.validationreport.Type;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelIssueInterface implements IssueContainerSerializer {

	private WritableCellFormat times;
	private WritableCellFormat timesbold;
	private WritableSheet sheet;
	private ByteArrayOutputStream byteArrayOutputStream;
	private WritableWorkbook workbook;
	private int row;
	private WritableCellFormat error;
	private WritableCellFormat ok;
	private Translator translator;
	private WorkbookSettings wbSettings;

	public ExcelIssueInterface(Translator translator) {
	    this.translator = translator;
		try {
	    	wbSettings = new WorkbookSettings();
	    	
	    	wbSettings.setLocale(new Locale("en", "EN"));
	    	
	    	WritableFont times10pt = new WritableFont(WritableFont.ARIAL, 10);
	    	times = new WritableCellFormat(times10pt);
	    	
	    	WritableFont times10ptbold = new WritableFont(WritableFont.ARIAL, 10);
			times10ptbold.setBoldStyle(WritableFont.BOLD);
			timesbold = new WritableCellFormat(times10ptbold);
			
			error = new WritableCellFormat(times10pt);
			error.setBackground(Colour.RED);

			ok = new WritableCellFormat(times10pt);
			ok.setBackground(Colour.LIGHT_GREEN);
			
			init(translator, wbSettings);
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init(Translator translator, WorkbookSettings wbSettings) throws IOException, WriteException, RowsExceededException {
		byteArrayOutputStream = new ByteArrayOutputStream();
		workbook = Workbook.createWorkbook(byteArrayOutputStream, wbSettings);
		
		sheet = workbook.createSheet("Sheet 1", 0);
		
		sheet.addCell(new Label(0, 0, translator.translate("REPORT_HEADER"), timesbold));
		sheet.addCell(new Label(1, 0, translator.translate("REPORT_TYPE"), timesbold));
		sheet.addCell(new Label(2, 0, translator.translate("REPORT_GUID_OID"), timesbold));
		sheet.addCell(new Label(3, 0, translator.translate("REPORT_MESSAGE"), timesbold));
		sheet.addCell(new Label(4, 0, translator.translate("REPORT_VALUE_IS"), timesbold));
		sheet.addCell(new Label(5, 0, translator.translate("REPORT_VALUE_SHOULD_BE"), timesbold));
		
		row = 1;
	}
	
//	@Override
//	public Issue add(Type messageType, String type, String guid, Long oid, String message, Object is, String shouldBe) throws IssueException {
//		Issue issue = new Issue();
//		try {
//			CellFormat cellFormat = messageType == Type.ERROR ? error : ok;
//			sheet.addCell(new Label(1, row, type, cellFormat));
//			if (guid != null) {
//				sheet.addCell(new Label(2, row, guid, cellFormat));
//			} else {
//				if (oid == null || oid == -1) {
//					sheet.addCell(new Label(2, row, "", cellFormat));
//				} else {
//					sheet.addCell(new Label(2, row, "" + oid, cellFormat));
//				}
//			}
//			sheet.addCell(new Label(3, row, message, cellFormat));
//			sheet.addCell(new Label(4, row, "" + is, cellFormat));
//			sheet.addCell(new Label(5, row, shouldBe, cellFormat));
//			row++;
//		} catch (RowsExceededException e) {
//			e.printStackTrace();
//		} catch (WriteException e) {
//			e.printStackTrace();
//		}
//		return issue;
//	}

	@Override
	public byte[] getBytes(IssueContainer issueContainer) throws IOException {
		for (Issue issue : issueContainer.list()) {
			
		}
		for (int x = 0; x < 6; x++) {
			CellView cell = sheet.getColumnView(x);
			cell.setAutosize(true);
			sheet.setColumnView(x, cell);
		}
		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			throw new IOException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}
}