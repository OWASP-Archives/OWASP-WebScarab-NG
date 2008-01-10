/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.Image;
import java.io.IOException;

import org.owasp.webscarab.ui.rcp.forms.support.ArrayChangeDetector;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.image.AwtImageResource;
import org.springframework.richclient.image.DefaultIconSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * @author rdawes
 *
 */
public abstract class WebScarabUITestCase extends SpringRichTestCase{

    protected void registerAdditionalServices(
            DefaultApplicationServices defaultapplicationservices) {
        try {
            ImageSource imageSource = new ImageSource() {
                AwtImageResource brokenImageIndicatorResource = new AwtImageResource(
                        new ClassPathResource("images/alert/error_obj.gif"));

                Image brokenImageIndicator = brokenImageIndicatorResource
                        .getImage();

                public Image getImage(String key) {
                    return brokenImageIndicator;
                }

                public AwtImageResource getImageResource(String key) {
                    return brokenImageIndicatorResource;
                }
            };

            defaultapplicationservices.setImageSource(imageSource);
            defaultapplicationservices.setIconSource(new DefaultIconSource(
                    imageSource));
            defaultapplicationservices.setValueChangeDetector(new ArrayChangeDetector());
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            messageSource.setBasenames(new String[] {"org.springframework.richclient.application.messages", "org.owasp.webscarab.ui.messages"});
            defaultapplicationservices.setMessageSource(messageSource);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
