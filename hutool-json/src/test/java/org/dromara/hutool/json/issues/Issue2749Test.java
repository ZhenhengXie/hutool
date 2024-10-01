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

package org.dromara.hutool.json.issues;

import org.dromara.hutool.json.JSONObject;
import org.dromara.hutool.json.JSONUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/dromara/hutool/issues/2749
 * <p>
 * 由于使用了递归方式解析和写出，导致JSON太长的话容易栈溢出。
 */
public class Issue2749Test {

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	public void jsonObjectTest() {
		final Map<String, Object> map = new HashMap<>(1, 1f);
		Map<String, Object> node = map;
		for (int i = 0; i < 1000; i++) {
			node = (Map<String, Object>) node.computeIfAbsent("a", k -> new HashMap<String, Object>(1, 1f));
		}
		node.put("a", 1);
		final String jsonStr = JSONUtil.toJsonStr(map);

		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
		final JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
		Assertions.assertNotNull(jsonObject);

		// 栈溢出
		//noinspection ResultOfMethodCallIgnored
		jsonObject.toString();
	}
}