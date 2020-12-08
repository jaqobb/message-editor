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

package dev.jaqobb.messageeditor.data;

public final class MessageEditData {

    private String oldMessage;
    private boolean oldMessageJson;
    private final MessagePlace oldMessagePlace;
    private String newMessage;
    private boolean newMessageJson;
    private String newMessageCache;
    private MessagePlace newMessagePlace;
    private Mode mode;
    private boolean shouldDestroy;

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
        this.oldMessage = oldMessage;
        this.oldMessageJson = oldMessageJson;
        this.oldMessagePlace = oldMessagePlace;
        this.newMessage = newMessage;
        this.newMessageJson = newMessageJson;
        this.newMessageCache = "";
        this.newMessagePlace = newMessagePlace;
        this.mode = Mode.NONE;
        this.shouldDestroy = true;
    }

    public String getOldMessage() {
        return this.oldMessage;
    }

    public void setOldMessage(final String oldMessage) {
        this.oldMessage = oldMessage;
    }

    public boolean isOldMessageJson() {
        return this.oldMessageJson;
    }

    public void setOldMessageJson(final boolean oldMessageJson) {
        this.oldMessageJson = oldMessageJson;
    }

    public MessagePlace getOldMessagePlace() {
        return this.oldMessagePlace;
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

    public MessagePlace getNewMessagePlace() {
        return this.newMessagePlace;
    }

    public void setNewMessagePlace(final MessagePlace newMessagePlace) {
        this.newMessagePlace = newMessagePlace;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    public boolean shouldDestroy() {
        return this.shouldDestroy;
    }

    public void setShouldDestroy(final boolean shouldDestroy) {
        this.shouldDestroy = shouldDestroy;
    }

    public static enum Mode {

        NONE,
        EDITTING_NEW_MESSAGE
    }
}
