package org.dromara.hutool.json;

import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.json.writer.NumberWriteMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class IssueIALQ0NTest {

	@Test
	void toJsonStrTest() {
		final Map<String, Object> map = new HashMap<>();
		map.put("id", 1826166955313201152L);
		map.put("createdDate", DateUtil.parse("2024-08-24"));
		final String jsonStr = JSONUtil.toJsonStr(map, JSONConfig.of()
			.setDateFormat("yyyy-MM-dd HH:mm:ss")
			.setNumberWriteMode(NumberWriteMode.STRING));
		Assertions.assertEquals("{\"createdDate\":\"2024-08-24 00:00:00\",\"id\":\"1826166955313201152\"}", jsonStr);
	}
}