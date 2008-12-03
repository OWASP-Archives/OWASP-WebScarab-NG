/**
 *
 */
package org.owasp.webscarab.util;

import java.util.regex.Pattern;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.richclient.application.DefaultConversionServiceFactoryBean;

/**
 * @author rdawes
 *
 */
public class ConversionServiceFactoryBean extends DefaultConversionServiceFactoryBean {

    /* (non-Javadoc)
     * @see org.springframework.binding.convert.support.DefaultConversionService#addDefaultConverters()
     */
    @Override
    protected ConversionService createConversionService() {
        DefaultConversionService conversionService = (DefaultConversionService) super.createConversionService();
        conversionService.addConverter(new TextToPattern());
        conversionService.addConverter(new PatternToText());
        return conversionService;
    }

    public static class PatternToText extends AbstractConverter {

        public Class<?>[] getSourceClasses() {
            return (new Class[] {Pattern.class });
        }

        public Class<?>[] getTargetClasses() {
            return (new Class[] { String.class });
        }

        @SuppressWarnings("unchecked")
		protected Object doConvert(Object source, Class targetClass,
                ConversionContext context) throws Exception {
            return ((Pattern)source).pattern();
        }

    }

    public static class TextToPattern extends AbstractConverter {

        public Class<?>[] getSourceClasses() {
            return (new Class[] { java.lang.String.class });
        }

        public Class<?>[] getTargetClasses() {
            return (new Class[] { Pattern.class });
        }

        @SuppressWarnings("unchecked")
		protected Object doConvert(Object source, Class targetClass,
                ConversionContext context) throws Exception {
            return Pattern.compile((String) source);
        }

    }
}
