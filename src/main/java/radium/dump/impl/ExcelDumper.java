package radium.dump.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import radium.dump.Dumper;
import com.google.common.collect.Lists;

public class ExcelDumper implements Dumper {

	final public static String EXTENSION = "xslx";
	
	private String objectName;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private int rowCount;
	private List<String> columnLabels;
	private OutputStream outputStream;

	public ExcelDumper() {
		super();
	}

	@Override
	public void onBegin(String objectName, OutputStream outputStream) {
		this.objectName = objectName;
		workbook = new XSSFWorkbook();
		sheet = (XSSFSheet) workbook.createSheet(objectName);
		columnLabels = Lists.newArrayList();
		this.outputStream = outputStream;
		this.rowCount = 0;
	}

	@Override
	public void onIteration(ResultSet resultSet) throws SQLException {
		if (rowCount == 0) {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			handleResultSetMetaData(resultSetMetaData);
		}

		handleResultSet(resultSet);
		rowCount++;
	}

	private void handleResultSet(final ResultSet resultSet) throws SQLException {
		XSSFRow row = (XSSFRow) sheet.createRow(rowCount + 1); // To take the first line into account
		for (int i = 1; i <= columnCount(); i++) {
			String string = resultSet.getString(i);
			XSSFCell cell = (XSSFCell) row.createCell(i - 1);
			cell.setCellValue(string);
		}
	}

	private void handleResultSetMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
		int columnCount = resultSetMetaData.getColumnCount();
		XSSFRow headerRow = (XSSFRow) sheet.createRow(0);
		for (int i = 1; i <= columnCount; i++) {
			String columnLabel = resultSetMetaData.getColumnLabel(i);
			columnLabels.add(columnLabel);
			XSSFCell cell = (XSSFCell) headerRow.createCell(i - 1);
			cell.setCellValue(columnLabel);
		}
	}

	public int rowCount() {
		return rowCount;
	}

	public int columnCount() {
		return columnLabels.size();
	}

	@Override
	public void onEnd() throws IOException {
		long ctTableID = 1;
		int columnCount = columnCount();

		CTTable table = sheet.createTable().getCTTable();
		CTTableStyleInfo tableStyleInfo = table.addNewTableStyleInfo();
		tableStyleInfo.setName("TableStyleMedium16");
		tableStyleInfo.setShowColumnStripes(false);
		tableStyleInfo.setShowRowStripes(true);

		AreaReference dataRange = new AreaReference(new CellReference(0, 0), new CellReference(rowCount, columnCount - 1));
		table.setRef(dataRange.formatAsString());
		table.setDisplayName(objectName);
		table.setName(objectName);
		table.setId(ctTableID++);
		CTTableColumns columns = table.addNewTableColumns();
		columns.setCount((long) columnCount); // define number of columns

		// Define headers
		for (int i = 0; i < columnCount; i++) {
			CTTableColumn column = columns.addNewTableColumn();
			String columnLabel = columnLabelAt(i);
			column.setName(columnLabel);
			column.setId(i + 1);
		}

		// Auto-filter of the table
		CTAutoFilter autoFilter = CTAutoFilter.Factory.newInstance();
		System.out.println("columnCount = " + columnCount);
		autoFilter.setRef(new CellRangeAddress(0, rowCount, 0, Math.max(0, columnCount - 1)).formatAsString());

		table.setAutoFilter(autoFilter);

		hideColumns(sheet, columnCount);
		
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}
		
		workbook.write(outputStream);
	}

	public String columnLabelAt(final int index) {
		return columnLabels.get(index);
	}
	
	public static void hideColumns(XSSFSheet sheet, int startIndex) {
		CTWorksheet ctSheet = sheet.getCTWorksheet();
		List<CTCols> ctCols = ctSheet.getColsList();
		CTCol ctCol = ctCols.get(0).addNewCol();
		ctCol.setMin(startIndex + 1);
		ctCol.setMax(16384);
		ctCol.setHidden(true);
	}
	
	public String getExtension() {
		return EXTENSION;
	}

}
