/**
 *
 */
package org.owasp.webscarab.util;

import java.util.prefs.Preferences;

/**
 * @author rdawes
 *
 */
public class PreferencesJdbcConnectionDetails extends JdbcConnectionDetails {

    public static String DRIVER_CLASSNAME = "DriverClass";

    public static String URL = "Url";

    public static String USERNAME = "UserName";

    private Preferences prefs;

    public PreferencesJdbcConnectionDetails(String pathName) {
        prefs = Preferences.userRoot().node(pathName);
        super.setDriverClassName(prefs.get(DRIVER_CLASSNAME, "org.hsqldb.jdbcDriver"));
        String tmp = System.getProperty("java.io.tmpdir");
        String slash = System.getProperty("file.separator");
        if (! tmp.endsWith(slash))
            tmp = tmp + slash;
        super.setUrl(prefs.get(URL, "jdbc:hsqldb:file:" + tmp + "webscarab"));
        super.setUsername(prefs.get(USERNAME, "sa"));
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.JdbcConnectionDetails#setDriverClassName(java.lang.String)
     */
    @Override
    public void setDriverClassName(String driverClassName) {
        super.setDriverClassName(driverClassName);
        prefs.put(DRIVER_CLASSNAME, driverClassName);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.JdbcConnectionDetails#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        prefs.put(URL, url);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.JdbcConnectionDetails#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(String username) {
        super.setUsername(username);
        prefs.put(USERNAME, username);
    }

}
