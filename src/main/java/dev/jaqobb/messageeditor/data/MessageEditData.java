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

    private final MessagePlace oldMessagePlace;
    private String oldMessage;
    private MessagePlace newMessagePlace;
    private String newMessage;
    private boolean shouldDestroy;

    public MessageEditData(
        final MessagePlace oldMessagePlace,
        final String oldMessage,
        final MessagePlace newMessagePlace,
        final String newMessage
    ) {
        this.oldMessagePlace = oldMessagePlace;
        this.oldMessage = oldMessage;
        this.newMessagePlace = newMessagePlace;
        this.newMessage = newMessage;
        this.shouldDestroy = true;
    }

    public MessagePlace getOldMessagePlace() {
        return this.oldMessagePlace;
    }

    public String getOldMessage() {
        return this.oldMessage;
    }

    public void setOldMessage(final String oldMessage) {
        this.oldMessage = oldMessage;
    }

    public MessagePlace getNewMessagePlace() {
        return this.newMessagePlace;
    }

    public void setNewMessagePlace(final MessagePlace newMessagePlace) {
        this.newMessagePlace = newMessagePlace;
    }

    public String getNewMessage() {
        return this.newMessage;
    }

    public void setNewMessage(final String newMessage) {
        this.newMessage = newMessage;
    }

    public boolean shouldDestroy() {
        return this.shouldDestroy;
    }

    public void setShouldDestroy(final boolean shouldDestroy) {
        this.shouldDestroy = shouldDestroy;
    }
}
