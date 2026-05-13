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

package org.tocharian.uyghur.morphology.affix;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UyghurAffixInventoryTest {
    private UyghurAffixInventory inventory;

    @Before
    public void setUp() throws IOException {
        inventory = UyghurAffixInventory.loadDefault();
    }

    @Test
    public void loadsCuratedAffixDefinitions() {
        assertTrue(inventory.all().size() >= 60);
        assertNotNull(inventory.getById("N_DAT"));
        assertNotNull(inventory.getById("V_PAST_DEF"));
        assertNotNull(inventory.getById("CL_Q_MU"));
    }

    @Test
    public void indexesSurfaceFormsAcrossHarmonyVariants() {
        AffixDefinition dative = inventory.getById("N_DAT");

        assertEquals("-GA", dative.getArchiform());
        assertTrue(dative.getSurfaces().contains("قا"));
        assertTrue(dative.getSurfaces().contains("گە"));
        assertTrue(inventory.findBySurface("قا").contains(dative));
        assertTrue(inventory.findBySurface("گە").contains(dative));
    }

    @Test
    public void distinguishesSuffixesFromClitics() {
        AffixDefinition question = inventory.getById("CL_Q_MU");
        AffixDefinition accusative = inventory.getById("N_ACC");

        assertEquals(AffixKind.CLITIC, question.getKind());
        assertEquals(AffixKind.SUFFIX, accusative.getKind());
        assertFalse(inventory.findByCategory("Q.POLAR").isEmpty());
    }

    @Test
    public void supportsSlotQueriesForMorphotactics() {
        assertTrue(inventory.findBySlot("N3").stream()
            .anyMatch(definition -> "GEN".equals(definition.getCategory())));
        assertTrue(inventory.findBySlot("V5").stream()
            .anyMatch(definition -> "COND".equals(definition.getCategory())));
    }
}
