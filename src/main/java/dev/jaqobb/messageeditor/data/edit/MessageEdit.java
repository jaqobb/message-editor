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

package dev.jaqobb.messageeditor.data.edit;

import dev.jaqobb.messageeditor.data.place.MessagePlace;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MessageEdit")
public final class MessageEdit implements ConfigurationSerializable {

    private final Pattern messageBeforePattern;
    private final MessagePlace messageBeforePlace;
    private final String messageAfter;
    private final MessagePlace messageAfterPlace;

    public MessageEdit(String messageBeforePattern, MessagePlace messageBeforePlace, String messageAfter, MessagePlace messageAfterPlace) {
        this.messageBeforePattern = Pattern.compile(messageBeforePattern);
        this.messageBeforePlace = messageBeforePlace;
        this.messageAfter = messageAfter;
        this.messageAfterPlace = messageAfterPlace;
    }

    public Pattern getMessageBeforePattern() {
        return this.messageBeforePattern;
    }

    public MessagePlace getMessageBeforePlace() {
        return this.messageBeforePlace;
    }

    public String getMessageBefore() {
        return this.messageBeforePattern.pattern();
    }

    public String getMessageAfter() {
        return this.messageAfter;
    }

    public MessagePlace getMessageAfterPlace() {
        return this.messageAfterPlace;
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
        Map<String, Object> data = new LinkedHashMap<>(3, 1.0F);
        data.put("message-before-pattern", this.messageBeforePattern.pattern());
        if (this.messageBeforePlace != null) {
            data.put("message-before-place", this.messageBeforePlace.name());
        }
        data.put("message-after", this.messageAfter);
        if (this.messageAfterPlace != null) {
            data.put("message-after-place", this.messageAfterPlace.name());
        }
        return data;
    }

    public static MessageEdit deserialize(Map<String, Object> data) {
        String messageBeforePattern = (String) data.get("message-before-pattern");
        MessagePlace messageBeforePlace = null;
        if (data.containsKey("message-before-place")) {
            messageBeforePlace = MessagePlace.fromName((String) data.get("message-before-place"));
        }
        String messageAfter = (String) data.get("message-after");
        MessagePlace messageAfterPlace = null;
        if (data.containsKey("message-after-place")) {
            messageAfterPlace = MessagePlace.fromName((String) data.get("message-after-place"));
        }
        return new MessageEdit(messageBeforePattern, messageBeforePlace, messageAfter, messageAfterPlace);
    }
}
