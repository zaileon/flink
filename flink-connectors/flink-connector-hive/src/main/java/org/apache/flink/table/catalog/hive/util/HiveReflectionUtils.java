/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.catalog.hive.util;

import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.catalog.hive.client.HiveShim;
import org.apache.flink.table.functions.hive.FlinkHiveUDFException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.serde2.Deserializer;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.JavaConstantDateObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.JavaConstantTimestampObjectInspector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utilities for accessing Hive class or methods via Java reflection.
 *
 * <p>They are put here not for code sharing. Rather, this is a boiler place for managing similar code that involves
 * reflection. (In fact, they could be just private method in their respective calling class.)
 *
 * <p>Relevant Hive methods cannot be called directly because shimming is required to support different, possibly
 * incompatible Hive versions.
 */
public class HiveReflectionUtils {

	public static Properties getTableMetadata(HiveShim hiveShim, Table table) {
		try {
			Method method = hiveShim.getMetaStoreUtilsClass().getMethod("getTableMetadata", Table.class);
			return (Properties) method.invoke(null, table);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new CatalogException("Failed to invoke MetaStoreUtils.getTableMetadata()", e);
		}
	}

	public static List<FieldSchema> getFieldsFromDeserializer(HiveShim hiveShim, String tableName, Deserializer deserializer)
			throws SerDeException, MetaException {
		try {
			Method method = hiveShim.getHiveMetaStoreUtilsClass().getMethod("getFieldsFromDeserializer", String.class, Deserializer.class);
			return (List<FieldSchema>) method.invoke(null, tableName, deserializer);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new CatalogException("Failed to invoke MetaStoreUtils.getFieldsFromDeserializer()", e);
		}
	}

	public static Deserializer getDeserializer(HiveShim hiveShim, Configuration conf, Table table, boolean skipConfError)
			throws MetaException {
		try {
			Method method = hiveShim.getHiveMetaStoreUtilsClass().getMethod("getDeserializer", Configuration.class,
				Table.class, boolean.class);
			return (Deserializer) method.invoke(null, conf, table, skipConfError);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new CatalogException("Failed to invoke MetaStoreUtils.getDeserializer()", e);
		}
	}

	public static List<String> getPvals(HiveShim hiveShim, List<FieldSchema> partCols, Map<String, String> partSpec) {
		try {
			Method method = hiveShim.getMetaStoreUtilsClass().getMethod("getPvals", List.class, Map.class);
			return (List<String>) method.invoke(null, partCols, partSpec);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new CatalogException("Failed to invoke MetaStoreUtils.getFieldsFromDeserializer", e);
		}
	}

	public static JavaConstantDateObjectInspector createJavaConstantDateObjectInspector(HiveShim hiveShim, Object value) {
		Constructor<?> meth = null;
		try {
			meth = JavaConstantDateObjectInspector.class.getDeclaredConstructor(hiveShim.getDateDataTypeClass());
			meth.setAccessible(true);
			return (JavaConstantDateObjectInspector) meth.newInstance(value);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new FlinkHiveUDFException("Failed to instantiate JavaConstantDateObjectInspector");
		}
	}

	public static JavaConstantTimestampObjectInspector createJavaConstantTimestampObjectInspector(HiveShim hiveShim, Object value) {
		Constructor<?> meth = null;
		try {
			meth = JavaConstantTimestampObjectInspector.class.getDeclaredConstructor(hiveShim.getDateDataTypeClass());
			meth.setAccessible(true);
			return (JavaConstantTimestampObjectInspector) meth.newInstance(value);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new FlinkHiveUDFException("Failed to instantiate JavaConstantTimestampObjectInspector");
		}
	}

	public static Object convertToHiveDate(HiveShim hiveShim, String s) throws FlinkHiveUDFException {
		try {
			Method method = hiveShim.getDateDataTypeClass().getMethod("valueOf", String.class);
			return method.invoke(null, s);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new FlinkHiveUDFException("Failed to invoke Hive's Date.valueOf()", e);
		}
	}

	public static Object convertToHiveTimestamp(HiveShim hiveShim, String s) throws FlinkHiveUDFException {
		try {
			Method method = hiveShim.getTimestampDataTypeClass().getMethod("valueOf", String.class);
			return method.invoke(null, s);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new FlinkHiveUDFException("Failed to invoke Hive's Timestamp.valueOf()", e);
		}
	}

}