/*
 * MIT License
 *
 * Copyright (c) 2020 Jakub Zagórski (jaqobb)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.jaqobb.messageeditor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MessageEdit")
public final class MessageEdit implements ConfigurationSerializable {

	private final Pattern messageBeforePattern;
	private final String messageAfter;

	public MessageEdit(String messageBeforePattern, String messageAfter) {
		this.messageBeforePattern = Pattern.compile(messageBeforePattern);
		this.messageAfter = messageAfter;
	}

	public Pattern getMessageBeforePattern() {
		return this.messageBeforePattern;
	}

	public String getMessageAfter() {
		return this.messageAfter;
	}

	public boolean matches(String message) {
		// TODO: Cache for the same messages?
		return this.messageBeforePattern.matcher(message).matches();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new LinkedHashMap<>(2, 1.0F);
		data.put("message-before-pattern", this.messageBeforePattern.pattern());
		data.put("message-after", this.messageAfter);
		return data;
	}

	public static MessageEdit deserialize(Map<String, Object> data) {
		return new MessageEdit((String) data.get("message-before-pattern"), (String) data.get("message-after"));
	}
}
