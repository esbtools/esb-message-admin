/*
 Copyright 2015 esbtools Contributors and/or its affiliates.

 This file is part of esbtools.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.esbtools.message.admin.common;

import org.esbtools.message.admin.common.utility.EncryptionUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test to demonstrate using the KeyExtractor Util
 *
 * @author ykoer
 */

public class EncryptionUtilityTest {

    @Test
    public void testEncrypter() {
        EncryptionUtility util = new EncryptionUtility("myPassisBIG12345");
        Assert.assertNotEquals("text", util.encrypt("text"));
        Assert.assertEquals("text", util.decrypt(util.encrypt("text")));
    }

    @Test
    public void testEncrypterDoesntWorkWithDifferentPassword() {
        EncryptionUtility util = new EncryptionUtility("myPassisBIG12345");
        EncryptionUtility util2 = new EncryptionUtility("myPassisBIG12346");
        Assert.assertNull((util2.decrypt(util.encrypt("text"))));
    }
}
