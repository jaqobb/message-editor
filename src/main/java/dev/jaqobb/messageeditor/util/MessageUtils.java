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

package dev.jaqobb.messageeditor.util;

import dev.jaqobb.messageeditor.data.MessagePlace;

public final class MessageUtils {

    private static final char[] CHARACTERS = {'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U', 'i', 'I', 'o', 'O', 'p', 'P', 'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J', 'k', 'K', 'l', 'L', 'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    private MessageUtils() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }

    public static String composeMessageId(
        final MessagePlace messagePlace,
        final String message
    ) {
        String messagePlaceId = messagePlace.getId();
        String messageHashCode = message.hashCode() < 0 ? String.valueOf(-message.hashCode() * 2L) : String.valueOf(message.hashCode());
        String messageId = "";
        for (int messageHashCodeIndex = 0; messageHashCodeIndex < messageHashCode.length(); messageHashCodeIndex++) {
            if (messageHashCodeIndex + 1 < messageHashCode.length()) {
                String messageHashCodeNumberFirst = String.valueOf(messageHashCode.charAt(messageHashCodeIndex));
                String messageHashCodeNumberSecond = String.valueOf(messageHashCode.charAt(messageHashCodeIndex + 1));
                int messageHashCodeNumber = Integer.parseInt(messageHashCodeNumberFirst + messageHashCodeNumberSecond);
                if (messageHashCodeNumber < CHARACTERS.length) {
                    messageId += CHARACTERS[messageHashCodeNumber];
                } else {
                    messageId += CHARACTERS[Integer.parseInt(messageHashCodeNumberFirst)];
                    messageId += CHARACTERS[Integer.parseInt(messageHashCodeNumberSecond)];
                }
                messageHashCodeIndex++;
            } else {
                messageId += CHARACTERS[Integer.parseInt(String.valueOf(messageHashCode.charAt(messageHashCodeIndex)))];
            }
        }
        return messagePlaceId + messageId;
    }
}
