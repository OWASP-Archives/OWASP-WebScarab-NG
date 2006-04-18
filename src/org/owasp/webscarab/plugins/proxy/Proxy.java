/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

/**
 * @author rdawes
 *
 */
public class Proxy {

    public Proxy(){
    }
    
    public void setListener(Listener listener) {
            Thread t = new Thread(listener);
            t.setDaemon(true);
            t.start();
    }
    
}
