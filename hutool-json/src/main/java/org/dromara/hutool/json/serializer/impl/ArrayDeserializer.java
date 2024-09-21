/*
 * Copyright (c) 2024 Hutool Team and hutool.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hutool.json.serializer.impl;

import org.dromara.hutool.core.reflect.TypeUtil;
import org.dromara.hutool.json.JSON;
import org.dromara.hutool.json.JSONArray;
import org.dromara.hutool.json.JSONObject;
import org.dromara.hutool.json.serializer.MatcherJSONDeserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map.Entry反序列化器，用于将JSON对象转换为Map.Entry对象。
 *
 * @author looly
 * @since 6.0.0
 */
public class ArrayDeserializer implements MatcherJSONDeserializer<Object> {

	/**
	 * 单例
	 */
	public static final ArrayDeserializer INSTANCE = new ArrayDeserializer();

	@Override
	public boolean match(final JSON json, final Type deserializeType) {
		if (json instanceof JSONArray || json instanceof JSONObject) {
			return TypeUtil.getClass(deserializeType).isArray();
		}
		return false;
	}

	@Override
	public Object deserialize(final JSON json, final Type deserializeType) {
		final int size = json.size();
		final Class<?> componentType = TypeUtil.getClass(deserializeType).getComponentType();
		final Object result = Array.newInstance(componentType, size);
		if (json instanceof JSONObject) {
			fill((JSONObject) json, result, componentType);
		} else {
			fill((JSONArray) json, result, componentType);
		}
		return result;
	}

	/**
	 * 将JSONObject填充到数组
	 *
	 * @param json          JSONObject
	 * @param result        结果集合
	 * @param componentType 元素类型
	 */
	private void fill(final JSONObject json, final Object result, final Type componentType) {
		int i = 0;
		for (final Map.Entry<String, JSON> entry : json) {
			Array.set(result, i, entry.getValue().toBean(componentType));
			i++;
		}
	}

	/**
	 * 将JSONObject填充到数组
	 *
	 * @param json          JSONObject
	 * @param result        结果集合
	 * @param componentType 元素类型
	 */
	private void fill(final JSONArray json, final Object result, final Type componentType) {
		int i = 0;
		for (final JSON element : json) {
			Array.set(result, i, element.toBean(componentType));
			i++;
		}
	}
}