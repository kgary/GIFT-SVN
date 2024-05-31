/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import java.io.IOException;
import java.text.ParseException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * A widget wrapping an HTML5 color input element (i.e. &lt;input type="color"&gt;). This widget allows users to select a hexadecimal
 * color value using their operating system's built in color picker, which is then returned as a string to the widget's change
 * handlers.
 * <br/><br/>
 * It might seem odd that this class extends {@link ValueBoxBase}, since most modern browsers won't show any text input,
 * but in reality, the rendered color picker is backed by an ordinary text input element, which replaces the color picker in
 * older browsers that don't support the "color" input type (such as IE 11). Because of this, many of the methods from
 * {@link ValueBoxBase} that are used to manipulate the widget's text won't have any visual effect in modern browsers. Generally
 * speaking, the only methods that will normally be used are {@link #setValue(String)}, {@link #getValue()}, and
 * {@link #addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)}.
 * <br/><br/>
 * For more information on the underlying color input element, see
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/color">here</a>.
 *
 * @author nroberts
 */
public class ColorBox extends ValueBoxBase<String>{

    /**
     * Represents each time of Css Color where the components are represented as
     * integers.
     */
    private enum CssColorIntegralRegex {
        /** A base 10 RGB color (e.g. rgb(10, 11, 12) */
        RGB_REGEX("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)", 10),

        /** A base 16 RGB color (e.g. #0F020A) */
        HEX_REGEX("#([0-9a-zA-Fa-f]{2})([0-9a-zA-Fa-f]{2})([0-9a-zA-Fa-f]{2})", 16);

        /** The regex that is used to recognize the color */
        private final RegExp regex;

        /** The number base components are represented in */
        private final int base;

        /**
         * Constructs a {@link CssColorIntegralRegex}
         *
         * @param regex The regex string to use for recognizing the colors.
         *        Can't be null.
         * @param base The base that the color format represents numbers in.
         *        Must be greater than 0.
         */
        private CssColorIntegralRegex(String regex, int base) {
            if (regex == null) {
                throw new IllegalArgumentException("The parameter 'regex' cannot be null.");
            } else if (base <= 0) {
                throw new IllegalArgumentException("The parameter 'base' cannot be less than 1.");
            }

            this.regex = RegExp.compile(regex);
            this.base = base;
        }

        /**
         * Gets the specified color component represented by a given color
         * string.
         *
         * @param input The color string to extract the color value from. (e.g.
         *        "rgb(10, 11, 12)" "#0F020A"). Can't be null.
         * @param componentIndex The index of the color component to retrieve.
         *        Must be between 1 and 3 inclusively.
         *        <ul>
         *        <li>Red = 1</li>
         *        <li>Green = 2</li>
         *        <li>Blue = 3</li>
         *        </ul>
         * @return The value of the requested color as an {@link Integer} in the
         *         inclusive range 0-255. Will return null if the input could
         *         not be parsed as this {@link CssColorIntegralRegex}.
         */
        public Integer getComponent(String input, int componentIndex) {
            if (input == null) {
                throw new IllegalArgumentException("The parameter 'input' cannot be null.");
            } else if (componentIndex < 1 || componentIndex > 3) {
                throw new IllegalArgumentException("The parameter 'componentIndex' must be between 1 and 3 inclusively");
            }


            /* Test the input against the current regular expression. If the
             * input doesn't match, try the next regex */
            MatchResult result = regex.exec(input);
            if (result == null) {
                return null;
            }

            /* Try to parse the matched sub string as an Integer. If it fails
             * return null (based on our authored regular expressions this
             * shouldn't happen). */
            try {
                return Integer.parseInt(result.getGroup(componentIndex), base);
            } catch (@SuppressWarnings("unused") NumberFormatException numFormEx) {
                /* Couldn't parse the string as an integer of the given base */
                return null;
            }
        }
    }

    /**
     * Constructs a new instance of a {@link ColorBox}.
     */
    public ColorBox() {
        super(createColorInput(), new Renderer<String>() {

            @Override
            public String render(String object) {
                return object;
            }

            @Override
            public void render(String object, Appendable appendable) throws IOException {
                appendable.append(object);
            }
        },
        new Parser<String>() {

            @Override
            public String parse(CharSequence text) throws ParseException {
                return text.toString();
            }
        });
    }

    /**
     * Creates a color input element (i.e. &lt;input type="color"&gt;).
     *
     * @return the color input element
     */
    private static Element createColorInput(){

        Element inputElement = Document.get().createElement("INPUT");
        inputElement.setAttribute("type", "color");

        return inputElement;
    }

    /**
     * Gets the red component of the currently defined color as an integer.
     *
     * @return Returns the value of the red component as an int between 0 and
     *         255 inclusively. If a color is not defined, 0 is returned.
     */
    public int getRedInt() {
        return getIntComponent(getValue(), 1);
    }

    /**
     * Gets the green component of the currently defined color as an integer.
     *
     * @return Returns the value of the green component as an int between 0 and
     *         255 inclusively. If a color is not defined, 0 is returned.
     */
    public int getGreenInt() {
        return getIntComponent(getValue(), 2);
    }

    /**
     * Gets the blue component of the currently defined color as an integer.
     *
     * @return Returns the value of the blue component as an int between 0 and
     *         255 inclusively. If a color is not defined, 0 is returned.
     */
    public int getBlueInt() {
        return getIntComponent(getValue(), 3);
    }

    /**
     * Gets the specified color component represented by a given color string.
     *
     * @param input The color string to extract the color value from. (e.g.
     *        "rgb(10, 11, 12)" "#0F020A"). Can't be null.
     * @param componentIndex The index of the color component to retrieve. Must
     *        be between 1 and 3 inclusively.
     *        <ul>
     *        <li>Red = 1</li>
     *        <li>Green = 2</li>
     *        <li>Blue = 3</li>
     *        </ul>
     * @return The value of the requested color as an {@link Integer} in the
     *         inclusive range 0-255. Will return 0 if the input could not be
     *         parsed as any of the available color formats.
     */
    private int getIntComponent(String input, int componentIndex) {
        for (CssColorIntegralRegex regex : CssColorIntegralRegex.values()) {
            Integer result = regex.getComponent(input, componentIndex);
            if(result == null) {
                continue;
            } else {
                return result;
            }
        }

        /* If there wasn't a match, just return 0. */
        return 0;
    }

    /**
     * Sets the color value based on three integral color components.
     *
     * @param red The red value between 0 and 255 inclusive. All values less
     *        than or greater than the range are set to 0 and 255 respectively.
     * @param green The green value between 0 and 255 inclusive. All values less
     *        than or greater than the range are set to 0 and 255 respectively.
     * @param blue The blue value between 0 and 255 inclusive. All values less
     *        than or greater than the range are set to 0 and 255 respectively.
     */
    public void setValue(int red, int green, int blue) {
        /* Ensure that the values are within range */
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        /* Create initial hex strings */
        String redHex = Integer.toHexString(red);
        String greenHex = Integer.toHexString(green);
        String blueHex = Integer.toHexString(blue);

        /* Ensure the hex strings are two digits wide */
        if (red <= 16) {
            redHex = "0" + redHex;
        }

        if (green <= 16) {
            greenHex = "0" + greenHex;
        }

        if (blue <= 16) {
            blueHex = "0" + blueHex;
        }

        setValue("#" + redHex + greenHex + blueHex);
    }
}