package org.terasology.kallisti.base.util.keyboard;

import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_INSERT;

public class TranslationAWTLWJGL {
	private static final Map<Integer, Integer> vkCode = new HashMap<>();
	private static final Map<Integer, Integer> codeVk = new HashMap<>();

	private static void addMapping(int a, int b) {
		vkCode.put(a, b);
		codeVk.put(b, a);
	}

	private static void addToAwtOnlyMapping(int a, int b) {
		codeVk.put(b, a);
	}

	static {
		int i, j;

		// 0-9
		for (j = 2, i = VK_1; i <= VK_9; i++, j++) addMapping(i, j);
		addMapping(VK_0, 0x0B);

		// A-Z
		int[] chars = new int[] { 0x1E, 0x30, 0x2E, 0x20, 0x12, 0x21, 0x22, 0x23, 0x17, 0x24, 0x25, 0x26, 0x32, 0x31, 0x18, 0x19, 0x10, 0x13, 0x1F, 0x14, 0x16, 0x2F, 0x11, 0x2D, 0x15, 0x2C };
		for (j = 0, i = VK_A; i <= VK_Z; i++, j++) addMapping(i, chars[j]);

		// F1-F12
		for (j = 0x3B, i = VK_F1; i <= VK_F12; i++, j++) addMapping(i, j);

		addMapping(VK_QUOTE, 0x28);
		addMapping(VK_AT, 0x91);
		addMapping(VK_BACK_SPACE, 0x0E);
		addMapping(VK_BACK_SLASH, 0x2B);
		addMapping(VK_CAPS_LOCK, 0x3A);
		addMapping(VK_COLON, 0x92);
		addMapping(VK_COMMA, 0x33);
		addMapping(VK_ENTER, 0x1C);
		addMapping(VK_EQUALS, 0x0D);
		addMapping(VK_BACK_QUOTE, 0x29);
		addMapping(VK_OPEN_BRACKET, 0x1A);
		addMapping(VK_CONTROL, 0x1D);
		// lmenu = 0x38
		addMapping(VK_SHIFT, 0x2A);
		addMapping(VK_MINUS, 0x0C);
		addMapping(VK_NUM_LOCK, 0x45);
		addMapping(VK_PAUSE, 0xC5);
		addMapping(VK_PERIOD, 0x34);
		addMapping(VK_CLOSE_BRACKET, 0x1B);
		// rcontrol = 0x9D
		addToAwtOnlyMapping(VK_CONTROL, 0x9D);
		// rmenu = 0xB8
		// rshift = 0x36
		addToAwtOnlyMapping(VK_SHIFT, 0x36);
		addMapping(VK_SCROLL_LOCK, 0x46);
		addMapping(VK_SEMICOLON, 0x27);
		addMapping(VK_SLASH, 0x35);
		addMapping(VK_SPACE, 0x39);
		addMapping(VK_STOP, 0x95);
		addMapping(VK_TAB, 0x0F);
		addMapping(VK_UNDEFINED, 0x93);

		// keypad
		addMapping(VK_UP, 0xC8);
		addMapping(VK_DOWN, 0xD0);
		addMapping(VK_LEFT, 0xCB);
		addMapping(VK_RIGHT, 0xCD);
		addMapping(VK_HOME, 0xC7);
		addMapping(VK_END, 0xCF);
		addMapping(VK_PAGE_UP, 0xC9);
		addMapping(VK_PAGE_DOWN, 0xD1);
		addMapping(VK_INSERT, 0xD2);
		addMapping(VK_DELETE, 0xD3);

		// numpad
	}

	public static int toLwjgl(int a) {
		return vkCode.get(a);
	}

	public static int toAwt(int a){
		return codeVk.get(a);
	}

	public static boolean hasLwjgl(int code) {
		return codeVk.containsKey(code);
	}

	public static boolean hasAwt(int code) {
		return vkCode.containsKey(code);
	}
}
