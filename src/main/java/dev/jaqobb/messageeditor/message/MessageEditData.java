/*
 * MIT License
 *
 * Copyright (c) 2020-2021 Jakub Zagórski (jaqobb)
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

package dev.jaqobb.messageeditor.message;

import dev.jaqobb.messageeditor.MessageEditorConstants;

public final class MessageEditData {

    private final String originalOldMessage;
    private final boolean originalOldMessageJson;
    private String oldMessage;
    private String oldMessagePattern;
    private boolean oldMessageJson;
    private String oldMessagePatternKey;
    private final MessagePlace oldMessagePlace;
    private final String originalNewMessage;
    private final boolean originalNewMessageJson;
    private String newMessage;
    private boolean newMessageJson;
    private String newMessageCache;
    private String newMessageKey;
    private MessagePlace newMessagePlace;
    private Mode currentMode;

    public MessageEditData(final MessageData messageData) {
        this(
            messageData.getMessage(),
            messageData.isMessageJson(),
            messageData.getMessagePlace(),
            messageData.getMessage(),
            messageData.isMessageJson(),
            messageData.getMessagePlace()
        );
    }

    public MessageEditData(
        final String oldMessage,
        final boolean oldMessageJson,
        final MessagePlace oldMessagePlace,
        final String newMessage,
        final boolean newMessageJson,
        final MessagePlace newMessagePlace
    ) {
        this.originalOldMessage = oldMessage;
        this.originalOldMessageJson = oldMessageJson;
        this.oldMessage = oldMessage;
        this.oldMessagePattern = oldMessage.replaceAll(MessageEditorConstants.SPECIAL_REGEX_CHARACTERS, "\\\\$0");
        this.oldMessageJson = oldMessageJson;
        this.oldMessagePatternKey = "";
        this.oldMessagePlace = oldMessagePlace;
        this.originalNewMessage = newMessage;
        this.originalNewMessageJson = newMessageJson;
        this.newMessage = newMessage;
        this.newMessageJson = newMessageJson;
        this.newMessageCache = "";
        this.newMessageKey = "";
        this.newMessagePlace = newMessagePlace;
        this.currentMode = Mode.NONE;
    }

    public String getOriginalOldMessage() {
        return this.originalOldMessage;
    }

    public boolean isOriginalOldMessageJson() {
        return this.originalOldMessageJson;
    }

    public String getOldMessage() {
        return this.oldMessage;
    }

    public void setOldMessage(final String oldMessage) {
        this.oldMessage = oldMessage;
    }

    public String getOldMessagePattern() {
        return this.oldMessagePattern;
    }

    public void setOldMessagePattern(final String oldMessagePattern) {
        this.oldMessagePattern = oldMessagePattern;
    }

    public boolean isOldMessageJson() {
        return this.oldMessageJson;
    }

    public void setOldMessageJson(final boolean oldMessageJson) {
        this.oldMessageJson = oldMessageJson;
    }

    public String getOldMessagePatternKey() {
        return this.oldMessagePatternKey;
    }

    public void setOldMessagePatternKey(final String oldMessagePatternKey) {
        this.oldMessagePatternKey = oldMessagePatternKey;
    }

    public MessagePlace getOldMessagePlace() {
        return this.oldMessagePlace;
    }

    public String getOriginalNewMessage() {
        return this.originalNewMessage;
    }

    public boolean isOriginalNewMessageJson() {
        return this.originalNewMessageJson;
    }

    public String getNewMessage() {
        return this.newMessage;
    }

    public void setNewMessage(final String newMessage) {
        this.newMessage = newMessage;
    }

    public boolean isNewMessageJson() {
        return this.newMessageJson;
    }

    public void setNewMessageJson(final boolean newMessageJson) {
        this.newMessageJson = newMessageJson;
    }

    public String getNewMessageCache() {
        return this.newMessageCache;
    }

    public void setNewMessageCache(final String newMessageCache) {
        this.newMessageCache = newMessageCache;
    }

    public String getNewMessageKey() {
        return this.newMessageKey;
    }

    public void setNewMessageKey(final String newMessageKey) {
        this.newMessageKey = newMessageKey;
    }

    public MessagePlace getNewMessagePlace() {
        return this.newMessagePlace;
    }

    public void setNewMessagePlace(final MessagePlace newMessagePlace) {
        this.newMessagePlace = newMessagePlace;
    }

    public Mode getCurrentMode() {
        return this.currentMode;
    }

    public void setCurrentMode(final Mode currentMode) {
        this.currentMode = currentMode;
    }

    public static enum Mode {

        NONE(true),
        EDITING_OLD_MESSAGE_PATTERN_KEY(false),
        EDITING_OLD_MESSAGE_PATTERN_VALUE(false),
        EDITING_NEW_MESSAGE(false),
        EDITING_NEW_MESSAGE_KEY(false),
        EDITING_NEW_MESSAGE_VALUE(false),
        EDITING_NEW_MESSAGE_PLACE(false);

        private final boolean shouldInvalidateCache;

        private Mode(final boolean shouldInvalidateCache) {
            this.shouldInvalidateCache = shouldInvalidateCache;
        }

        public boolean shouldInvalidateCache() {
            return this.shouldInvalidateCache;
        }
    }
}
