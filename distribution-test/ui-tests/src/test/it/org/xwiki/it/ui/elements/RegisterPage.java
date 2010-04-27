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
package org.xwiki.it.ui.elements;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import org.xwiki.it.ui.framework.TestUtils;

/**
 * Represents the actions possible on the Registration Page
 *
 * @version $Id$
 * @since 2.3M1
 */
public class RegisterPage extends ViewPage
{
    @FindBy(id = "register")
    private WebElement registerFormElement;

    @FindBy(xpath = "//form[@id='register']/div/span/input[@type='submit']")
    private WebElement submitButton;

    private FormPage form;

    public RegisterPage(WebDriver driver)
    {
        super(driver);
    }

    /** To put the registration page someplace else, subclass this class and change this method. */
    public boolean isOnRegisterPage()
    {
        if (registerFormElement != null && !getDriver().findElements(By.name("register_password")).isEmpty()) {
            if (form == null) {
                form = new FormPage(registerFormElement, getDriver());
            }
            return true;
        }
        return false;
    }

    /** To put the registration page someplace else, subclass this class and change this method. */
    public void gotoRegisterPage()
    {
        HomePage homePage = new HomePage(getDriver());
        homePage.gotoHomePage();

        if (homePage.isAuthenticated()) {
            homePage.clickLogout();
        }

        homePage.clickRegister();
        form = new FormPage(registerFormElement, getDriver());
    }

    public void fillInJohnSmithValues()
    {
        fillRegisterForm("John", "Smith", "JohnSmith", "WeakPassword", "WeakPassword", "johnsmith@xwiki.org");
    }

    public void fillRegisterForm(final String firstName,
                                 final String lastName,
                                 final String username,
                                 final String password,
                                 final String confirmPassword,
                                 final String email)
    {
        checkAtRegisterPage();
        Map map = new HashMap();
        if (firstName != null) {
            map.put("register_first_name", firstName);
        }
        if (lastName != null) {
            map.put("register_last_name", lastName);
        }
        if (username != null) {
            map.put("xwikiname", username);
        }
        if (password != null) {
            map.put("register_password", password);
        }
        if (confirmPassword != null) {
            map.put("register2_password", confirmPassword);
        }
        if (email != null) {
            map.put("register_email", email);
        }
        form.fillFieldsByName(map);
    }

    /** @return true if registration is successful, false if user couldn't be registered. */
    public boolean register()
    {
        checkAtRegisterPage();
        submitButton.click();
        
        List<WebElement> infos = getDriver().findElements(By.className("infomessage"));
        for (WebElement info : infos) {
            if (info.getText().indexOf("Registration successful.") != -1) {
                return true;
            }
        }
        return false;
    }

    /** @return a list of WebElements representing validation failure messages. Use after calling register() */
    public List<WebElement> getValidationFailureMessages()
    {
        return getDriver().findElements(By.xpath("//dd/span[@class='LV_validation_message LV_invalid']"));
    }

    /** @return Is the specified message included in the list of validation failure messages. */
    public boolean validationFailureMessagesInclude(String message)
    {
        for (WebElement messageElement : getValidationFailureMessages()) {
            if (messageElement.getText().equals(message)) {
                return true;
            }
        }
        return false;
    }

    public boolean liveValidationEnabled()
    {
        return !getDriver().findElements(By.xpath("/html/body/div/div/div[3]/div/div/div/div/div/script")).isEmpty();
    }

    /** Try to make LiveValidation validate the forms. Focus on an unvalidated field (register_first_name)*/
    public void triggerLiveValidation()
    {
        // Click on all of the validated fields to prevent "Passwords don't match" sticking -> flickering test.
        // The right solution is to have a better way to fire LiveValidation without excessive js.
        registerFormElement.findElement(By.name("register2_password")).click();
        registerFormElement.findElement(By.name("register_password")).click();
        registerFormElement.findElement(By.name("xwikiname")).click();

        registerFormElement.findElement(By.name("register_first_name")).click();
    }

    private void checkAtRegisterPage()
    {
        if (!isOnRegisterPage()) {
            throw new WebDriverException("Must go to the registration page before using it's functions.");
        }
    }
}
