/*
 * WebScarab.java
 *
 * Created on 21 February 2006, 09:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab;

import org.springframework.richclient.application.ApplicationLauncher;

/**
 * 
 * @author rdawes
 */
public class WebScarab {

    public static void main(String[] args) {
        try {
            String rootContextDirectoryClassPath = "/org/owasp/webscarab";

            String startupContextPath = rootContextDirectoryClassPath
                    + "/ui/richclient-startup-context.xml";

            String dataLayerContextPath = rootContextDirectoryClassPath
                    + "/jdbc/data-layer-context.xml";

            String businessLayerContextPath = rootContextDirectoryClassPath
                    + "/ctx/common/business-layer-context.xml";

            String richclientApplicationContextPath = rootContextDirectoryClassPath
                    + "/ui/richclient-application-context.xml";

            new ApplicationLauncher(startupContextPath, new String[] {
                    dataLayerContextPath, businessLayerContextPath,
                    richclientApplicationContextPath });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
