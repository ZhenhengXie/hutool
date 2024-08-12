/*
 * Copyright (c) 2024 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          https://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.dromara.hutool.poi.excel.sax;

import org.dromara.hutool.core.io.IORuntimeException;
import org.dromara.hutool.core.io.IoUtil;
import org.dromara.hutool.core.reflect.method.MethodUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.poi.excel.sax.handler.RowHandler;
import org.dromara.hutool.poi.POIException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Sax方式读取Excel文件<br>
 * Excel2007格式说明见：<a href="http://www.cnblogs.com/wangmingshun/p/6654143.html">http://www.cnblogs.com/wangmingshun/p/6654143.html</a>
 *
 * @author Looly
 * @since 3.1.2
 */
public class Excel07SaxReader implements ExcelSaxReader<Excel07SaxReader> {

	private final SheetDataSaxHandler handler;

	/**
	 * 构造
	 *
	 * @param rowHandler        行处理器
	 */
	public Excel07SaxReader(final RowHandler rowHandler) {
		this(rowHandler, false);
	}

	/**
	 * 构造
	 *
	 * @param rowHandler        行处理器
	 * @param padCellAtEndOfRow 是否对齐数据，即在行尾补充null cell
	 */
	public Excel07SaxReader(final RowHandler rowHandler, final boolean padCellAtEndOfRow) {
		this.handler = new SheetDataSaxHandler(rowHandler, padCellAtEndOfRow);
	}

	/**
	 * 设置行处理器
	 *
	 * @param rowHandler 行处理器
	 * @return this
	 */
	public Excel07SaxReader setRowHandler(final RowHandler rowHandler) {
		this.handler.setRowHandler(rowHandler);
		return this;
	}

	// ------------------------------------------------------------------------------ Read start
	@Override
	public Excel07SaxReader read(final File file, final int rid) throws POIException {
		return read(file, RID_PREFIX + rid);
	}

	@Override
	public Excel07SaxReader read(final File file, final String idOrRidOrSheetName) throws POIException {
		try (final OPCPackage open = OPCPackage.open(file, PackageAccess.READ)) {
			return read(open, idOrRidOrSheetName);
		} catch (final InvalidFormatException | IOException e) {
			throw new POIException(e);
		}
	}

	@Override
	public Excel07SaxReader read(final InputStream in, final int rid) throws POIException {
		return read(in, RID_PREFIX + rid);
	}

	@Override
	public Excel07SaxReader read(final InputStream in, final String idOrRidOrSheetName) throws POIException {
		try (final OPCPackage opcPackage = OPCPackage.open(in)) {
			return read(opcPackage, idOrRidOrSheetName);
		} catch (final IOException e) {
			throw new IORuntimeException(e);
		} catch (final InvalidFormatException e) {
			throw new POIException(e);
		}
	}

	/**
	 * 开始读取Excel，Sheet编号从0开始计数
	 *
	 * @param opcPackage {@link OPCPackage}，Excel包，读取后不关闭
	 * @param rid        Excel中的sheet rid编号，如果为-1处理所有编号的sheet
	 * @return this
	 * @throws POIException POI异常
	 */
	public Excel07SaxReader read(final OPCPackage opcPackage, final int rid) throws POIException {
		return read(opcPackage, RID_PREFIX + rid);
	}

	/**
	 * 开始读取Excel，Sheet编号从0开始计数
	 *
	 * @param opcPackage         {@link OPCPackage}，Excel包，读取后不关闭
	 * @param idOrRidOrSheetName Excel中的sheet id或者rid编号或sheet名，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
	 * @return this
	 * @throws POIException POI异常
	 */
	public Excel07SaxReader read(final OPCPackage opcPackage, final String idOrRidOrSheetName) throws POIException {
		try {
			return read(new XSSFReader(opcPackage), idOrRidOrSheetName);
		} catch (final OpenXML4JException e) {
			throw new POIException(e);
		} catch (final IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * 开始读取Excel，Sheet编号从0开始计数
	 *
	 * @param xssfReader         {@link XSSFReader}，Excel读取器
	 * @param idOrRidOrSheetName Excel中的sheet id或者rid编号或sheet名，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
	 * @return this
	 * @throws POIException POI异常
	 * @since 5.4.4
	 */
	public Excel07SaxReader read(final XSSFReader xssfReader, final String idOrRidOrSheetName) throws POIException {
		// 获取共享样式表，样式非必须
		try {
			this.handler.stylesTable = xssfReader.getStylesTable();
		} catch (final IOException | InvalidFormatException ignore) {
			// ignore
		}

		// 获取共享字符串表
		// POI-5.2.0开始返回值有所变更，导致实际使用时提示方法未找到，此处使用反射调用，解决不同版本返回值变更问题
		//this.handler.sharedStrings = xssfReader.getSharedStringsTable();
		this.handler.sharedStrings = MethodUtil.invoke(xssfReader, "getSharedStringsTable");

		return readSheets(xssfReader, idOrRidOrSheetName);
	}
	// ------------------------------------------------------------------------------ Read end

	// --------------------------------------------------------------------------------------- Private method start

	/**
	 * 开始读取Excel，Sheet编号从0开始计数
	 *
	 * @param xssfReader         {@link XSSFReader}，Excel读取器
	 * @param idOrRidOrSheetName Excel中的sheet id或者rid编号或sheet名，从0开始，rid必须加rId前缀，例如rId0，如果为-1处理所有编号的sheet
	 * @return this
	 * @throws POIException POI异常
	 * @since 5.4.4
	 */
	private Excel07SaxReader readSheets(final XSSFReader xssfReader, final String idOrRidOrSheetName) throws POIException {
		this.handler.sheetIndex = getSheetIndex(xssfReader, idOrRidOrSheetName);
		InputStream sheetInputStream = null;
		try {
			if (this.handler.sheetIndex > -1) {
				// 根据 rId# 或 rSheet# 查找sheet
				sheetInputStream = xssfReader.getSheet(RID_PREFIX + (this.handler.sheetIndex + 1));
				ExcelSaxUtil.readFrom(sheetInputStream, this.handler);
				this.handler.rowHandler.doAfterAllAnalysed();
			} else {
				this.handler.sheetIndex = -1;
				// 遍历所有sheet
				final Iterator<InputStream> sheetInputStreams = xssfReader.getSheetsData();
				while (sheetInputStreams.hasNext()) {
					// 重新读取一个sheet时行归零
					this.handler.index = 0;
					this.handler.sheetIndex++;
					sheetInputStream = sheetInputStreams.next();
					ExcelSaxUtil.readFrom(sheetInputStream, this.handler);
					this.handler.rowHandler.doAfterAllAnalysed();
				}
			}
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new POIException(e);
		} finally {
			IoUtil.closeQuietly(sheetInputStream);
		}
		return this;
	}

	/**
	 * 获取sheet索引，从0开始
	 * <ul>
	 *     <li>传入'rId'开头，直接去除rId前缀</li>
	 *     <li>传入纯数字，表示sheetIndex，通过{@link SheetRidReader}转换为rId</li>
	 *     <li>传入其它字符串，表示sheetName，通过{@link SheetRidReader}转换为rId</li>
	 * </ul>
	 *
	 * @param xssfReader         {@link XSSFReader}，Excel读取器
	 * @param idOrRidOrSheetName Excel中的sheet id或者rid编号或sheet名称，从0开始，rid必须加rId前缀，例如rId0，如果为-1处理所有编号的sheet
	 * @return sheet索引，从0开始
	 * @since 5.5.5
	 */
	private int getSheetIndex(final XSSFReader xssfReader, String idOrRidOrSheetName) {
		// rid直接处理
		if (StrUtil.startWithIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) {
			return Integer.parseInt(StrUtil.removePrefixIgnoreCase(idOrRidOrSheetName, RID_PREFIX));
		}

		// sheetIndex需转换为rid
		final SheetRidReader ridReader = SheetRidReader.parse(xssfReader);

		if (StrUtil.startWithIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX)) {
			// name:开头的被认为是sheet名称直接处理
			idOrRidOrSheetName = StrUtil.removePrefixIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX);
			final Integer rid = ridReader.getRidByNameBase0(idOrRidOrSheetName);
			if (null != rid) {
				return rid;
			}
		} else {
			// 尝试查找名称
			Integer rid = ridReader.getRidByNameBase0(idOrRidOrSheetName);
			if (null != rid) {
				return rid;
			}

			try {
				final int sheetIndex = Integer.parseInt(idOrRidOrSheetName);
				rid = ridReader.getRidBySheetIdBase0(sheetIndex);
				// 如果查找不到对应index，则认为用户传入的直接是rid
				return ObjUtil.defaultIfNull(rid, sheetIndex);
			} catch (final NumberFormatException ignore) {
				// 非数字，说明非index，且没有对应名称，抛出异常
			}
		}

		throw new IllegalArgumentException("Invalid rId or id or sheetName: " + idOrRidOrSheetName);
	}
	// --------------------------------------------------------------------------------------- Private method end
}
