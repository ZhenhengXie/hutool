/*
 * Copyright (c) 2023 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.dromara.hutool.core.reflect.lookup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LookupUtilTest {
	@Test
	public void findMethodTest() throws Throwable {
		MethodHandle handle = LookupUtil.findMethod(Duck.class, "quack",
			MethodType.methodType(String.class));
		Assertions.assertNotNull(handle);
		// 对象方法自行需要绑定对象或者传入对象参数
		final String invoke = (String) handle.invoke(new BigDuck());
		Assertions.assertEquals("Quack", invoke);

		// 对象的方法获取
		handle = LookupUtil.findMethod(BigDuck.class, "getSize",
			MethodType.methodType(int.class));
		Assertions.assertNotNull(handle);
		final int invokeInt = (int) handle.invoke(new BigDuck());
		Assertions.assertEquals(36, invokeInt);
	}

	@Test
	public void findStaticMethodTest() throws Throwable {
		final MethodHandle handle = LookupUtil.findMethod(Duck.class, "getDuck",
			MethodType.methodType(String.class, int.class));
		Assertions.assertNotNull(handle);

		// static 方法执行不需要绑定或者传入对象，直接传入参数即可
		final String invoke = (String) handle.invoke(12);
		Assertions.assertEquals("Duck 12", invoke);
	}

	@Test
	public void findPrivateMethodTest() throws Throwable {
		final MethodHandle handle = LookupUtil.findMethod(BigDuck.class, "getPrivateValue",
			MethodType.methodType(String.class));
		Assertions.assertNotNull(handle);

		final String invoke = (String) handle.invoke(new BigDuck());
		Assertions.assertEquals("private value", invoke);
	}

	@Test
	public void findSuperMethodTest() throws Throwable {
		// 查找父类的方法
		final MethodHandle handle = LookupUtil.findMethod(BigDuck.class, "quack",
			MethodType.methodType(String.class));
		Assertions.assertNotNull(handle);

		final String invoke = (String) handle.invoke(new BigDuck());
		Assertions.assertEquals("Quack", invoke);
	}

	@Test
	public void findPrivateStaticMethodTest() throws Throwable {
		final MethodHandle handle = LookupUtil.findMethod(BigDuck.class, "getPrivateStaticValue",
			MethodType.methodType(String.class));
		Assertions.assertNotNull(handle);

		final String invoke = (String) handle.invoke();
		Assertions.assertEquals("private static value", invoke);
	}

	interface Duck {
		default String quack() {
			return "Quack";
		}

		static String getDuck(final int count){
			return "Duck " + count;
		}
	}

	static class BigDuck implements Duck {
		public int getSize(){
			return 36;
		}

		@SuppressWarnings("unused")
		private String getPrivateValue(){
			return "private value";
		}

		@SuppressWarnings("unused")
		private static String getPrivateStaticValue(){
			return "private static value";
		}
	}
}