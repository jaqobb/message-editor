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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MessageEdit")
public final class MessageEdit implements ConfigurationSerializable {

	private final Pattern messageBeforePattern;
	private final String messageAfter;
	private final Cache<String, String> cachedMessages;

	public MessageEdit(String messageBeforePattern, String messageAfter) {
		this.messageBeforePattern = Pattern.compile(messageBeforePattern);
		this.messageAfter = messageAfter;
		this.cachedMessages = CacheBuilder.newBuilder()
			.expireAfterAccess(15L, TimeUnit.MINUTES)
			.build();
	}

	public Pattern getMessageBeforePattern() {
		return this.messageBeforePattern;
	}

	public String getMessageBefore() {
		return this.messageBeforePattern.pattern();
	}

	public String getMessageAfter() {
		return this.messageAfter;
	}

	public Set<String> getCachedMessages() {
		return Collections.unmodifiableSet(this.cachedMessages.asMap().keySet());
	}

	public String getCachedMessage(String messageBefore) {
		return this.cachedMessages.getIfPresent(messageBefore);
	}

	public boolean isMessageCached(String messageBefore) {
		return this.cachedMessages.asMap().containsKey(messageBefore);
	}

	public void cacheMessage(String messageBefore, String messageAfter) {
		this.cachedMessages.put(messageBefore, messageAfter);
	}

	public void uncacheMessage(String messageBefore) {
		this.cachedMessages.invalidate(messageBefore);
	}

	public void clearCachedMessages() {
		this.cachedMessages.cleanUp();
	}

	public Matcher getMatcher(String messageBefore) {
		Matcher matcher = this.messageBeforePattern.matcher(messageBefore);
		if (!matcher.matches()) {
			return null;
		}
		return matcher;
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
