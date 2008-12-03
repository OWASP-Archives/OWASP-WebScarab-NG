/**
 * 
 */
package org.owasp.webscarab.util.rcp;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;

import org.springframework.richclient.settings.Settings;
import org.springframework.richclient.settings.support.WindowMemento;

/**
 * @author rdawes
 * 
 */
public class ScreenAwareWindowMemento extends WindowMemento {

    public ScreenAwareWindowMemento(Window window) {
        super(window);
    }

    public ScreenAwareWindowMemento(Window window, String key) {
        super(window, key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.richclient.settings.support.WindowMemento#restoreState(org.springframework.richclient.settings.Settings)
     */
    @Override
    public void restoreState(Settings settings) {
        restoreBounds(settings);
        restoreMaximizedState(settings);
    }

    public void restoreMaximizedState(Settings settings) {
        Window window = getWindow();
        String key = getKey();
        if (window instanceof Frame) {
            Frame frame = (Frame) window;
            frame
                    .setExtendedState((settings.getBoolean(key + ".maximized") ? Frame.MAXIMIZED_BOTH
                            : Frame.NORMAL));
        }
    }

    void restoreBounds(Settings settings) {
        Window window = getWindow();
        String key = getKey();
        Rectangle bounds = new Rectangle(0,0,0,0);
        if (settings.contains(key + ".height")
                && settings.contains(key + ".width")) {
            bounds.width = settings.getInt(key + ".width");
            bounds.height = settings.getInt(key + ".height");
        }
        if (settings.contains(key + ".x") && settings.contains(key + ".y")) {
            bounds.x = settings.getInt(key + ".x");
            bounds.y = settings.getInt(key + ".y");
        }
        System.out.println("Stored bounds = " + bounds);
        ensureVisible(bounds);
        System.out.println("Used bounds = " + bounds + " : empty? " + bounds.isEmpty());
        if (!bounds.isEmpty())
            window.setBounds(bounds);
    }

    private void ensureVisible(Rectangle stored) {
        Dimension size = stored.getSize();
        GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        int maxArea = 0;
        int bestScreen = 0;
        for (int i = 0; i < gds.length; i++) {
            Rectangle screen = gds[i].getDefaultConfiguration().getBounds();
            if (screen.contains(stored))
                return;
            Rectangle intersection = screen.intersection(stored);
            if (! intersection.isEmpty() && maxArea < intersection.width * intersection.height) {
                System.out.println("Screen " + i + " : maxArea was " + maxArea + ", now " + (intersection.width * intersection.height));
                maxArea = intersection.width * intersection.height;
                bestScreen = i;
            }
        }
        Rectangle screen = gds[bestScreen].getDefaultConfiguration().getBounds();
        Rectangle.intersect(screen, stored, stored);
    }

}
