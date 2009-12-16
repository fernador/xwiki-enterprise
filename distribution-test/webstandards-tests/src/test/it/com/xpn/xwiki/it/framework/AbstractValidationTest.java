/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.it.framework;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xwiki.validator.Validator;

import com.xpn.xwiki.plugin.packaging.Package;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AbstractValidationTest extends TestCase
{
    protected HttpClient client;

    protected String fullPageName;

    public AbstractValidationTest(String name)
    {
        super(name);
    }

    public static Test suite(Class< ? extends AbstractValidationTest> validationTest, Validator validator) throws Exception
    {
        TestSuite suite = new TestSuite();
        String path = System.getProperty("localRepository") + "/" + System.getProperty("pathToXWikiXar");
        String patternFilter = System.getProperty("documentsToTest");
        HttpClient client = new HttpClient();

        Credentials defaultcreds = new UsernamePasswordCredentials("Admin", "admin");
        client.getState().setCredentials(AuthScope.ANY, defaultcreds);

        for (String pageName : readXarContents(path, patternFilter)) {
            suite.addTest(validationTest.getConstructor(String.class, HttpClient.class, Validator.class).newInstance(
                pageName, client, validator));
        }

        return suite;
    }

    public static List<String> readXarContents(String fileName, String patternFilter) throws Exception
    {
        FileInputStream fileIS = new FileInputStream(fileName);
        ZipInputStream zipIS = new ZipInputStream(fileIS);

        ZipEntry entry;
        Document tocDoc = null;
        while ((entry = zipIS.getNextEntry()) != null) {
            if (entry.getName().compareTo(Package.DefaultPackageFileName) == 0) {
                SAXReader reader = new SAXReader();
                tocDoc = reader.read(zipIS);
                break;
            }
        }

        if (tocDoc == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();

        Element filesElement = tocDoc.getRootElement().element("files");
        List<Element> fileElementList = filesElement.elements("file");
        for (Element el : fileElementList) {
            String docFullName = el.getStringValue();

            if (patternFilter == null || docFullName.matches(patternFilter)) {
                result.add(docFullName);
            }
        }

        return result;
    }
}
