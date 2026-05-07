/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tocharian.uyghur.morphology.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UyghurCharacterUtilsTest {
    @Test
    public void normalizeTextRemovesJoinersAndTatweel() {
        assertEquals("كومپيۇتېرلارنى", UyghurCharacterUtils.normalizeText("كومپيۇتېرلار\u0640\u200C\u200Dنى"));
    }

    @Test
    public void normalizeTextConvertsArabicAndPersianDigits() {
        assertEquals("2019 345", UyghurCharacterUtils.normalizeText("\u0662\u06F0\u0661\u06F9   \u06F3\u0664\u06F5"));
    }
}
