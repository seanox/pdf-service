/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2021 Seanox Software Solutions
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.seanox.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/** 
 * Pixel-based comparison of PDF files.<br>
 * <br>
 * Compare 4.1.0 20210821<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.1.0 20210821
 */
public class Compare {
    
    /**
     * Main entry for the console application.
     * @param  options optional list with paths and filters/globs of templates
     *     Without, the current working directory is used.
     * @throws Exception
     *     In case of unexpected errors.
     */    
    public static void main(final String... options)
            throws Exception {
        
        System.out.println("Seanox PDF Comparator [Version 0.0.0 00000000]");
        System.out.println("Copyright (C) 0000 Seanox Software Solutions");
        System.out.println();

        if (Objects.isNull(options)
                || options.length < 2) {
            System.out.println("usage: java -cp seanox-pdf-tools.jar com.seanox.pdf.Compare <master> <compare>");
            return;
        }

        for (int loop = 0; loop <= 1; loop++)
            if (!new File(options[loop]).exists()
                    || !new File(options[loop]).isFile()) {
                System.out.println("Invalid file: " + options[loop]);
                return;
            }
        
        final File[] files = Compare.compare(new File(options[0]), new File(options[1]));
        if (files != null
                && files.length > 0) {
            System.out.println("Following differences were found:");
            System.out.println("----");
            for (final File file : files)
                System.out.println(file.getName());
        } else System.out.println("No differences were found.");
    }
    
    /**
     * Calculates the increase of a color tone.
     * The increase can be positive or negative.
     * @param  color
     * @param  factor
     * @return the increased color tone
     */
    private static int increaseColor(final int color, final int factor) {
        return Math.min(Math.max(color -factor, 0), 255);
    }

    /**
     * Calculates the increase of a delta color tone.
     * Color tones less than 127 are increased as negation.
     * @param  color
     * @return the increased delta color tone
     */    
    private static int increaseColorDelta(final int color) {
        return color < 127 ? 255 -color : color;
    }
    
    /**
     * Calculates the increase of a color tone.
     * The increase can be positive or negative.
     * @param  rgba
     * @param  tone
     * @param  factor
     * @return the calculated RGBA value
     */
    private static int increaseColorTone(final int rgba, final Color tone, final int factor) {

        Color color = new Color(rgba, true);
        final int r = color.getRed();
        final int g = color.getGreen();
        final int b = color.getBlue();
        final int a = color.getAlpha();
        if (Color.RED.equals(tone))
            color = new Color(
                    Compare.increaseColorDelta(r),
                    Compare.increaseColor(g, factor),
                    Compare.increaseColor(b, factor),
                    a);
        if (Color.GREEN.equals(tone))
            color = new Color(
                    Compare.increaseColor(r, factor),
                    Compare.increaseColorDelta(g),
                    Compare.increaseColor(b, factor),
                    a);
        if (Color.BLUE.equals(tone))
            color = new Color(
                    Compare.increaseColor(r, factor),
                    Compare.increaseColor(g, factor),
                    Compare.increaseColorDelta(b),
                    a);
        return color.getRGB();
    }
    
    /**
     * Pixel-based comparison of two PDF files.
     * Returned is a file list with delta images, if differences were found.
     * The path of the delta images is derived from the path of the file to be
     * compared. The comparison is performed page by page. The delta images are
     * therefore created for each page with differences.
     * @param  master
     * @param  compare
     * @return file list with delta images, otherwise {@code null}
     * @throws IOException
     */
    public static File[] compare(final File master, final File compare)
            throws IOException {

        final List<BufferedImage> masterImages = new ArrayList<>();
        try (final PDDocument document = PDDocument.load(master)) {
            final PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page)
                masterImages.add(pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB));
        }

        final List<BufferedImage> compareImages = new ArrayList<>();
        try (final PDDocument document = PDDocument.load(compare)) {
            final PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page)
                compareImages.add(pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB));
        }
        
        while (masterImages.size() < compareImages.size())
            masterImages.add(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR));
        while (masterImages.size() > compareImages.size())
            compareImages.add(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR));

        final String deltaTimestamp = String.format("%tY%<tm%<td%<tH%<tM%<tS", new Date());
        final List<File> deltas = new ArrayList<>();
        for (int page = 0; page < masterImages.size(); ++page) {
            final String deltaName = "_diffs_page_" + (page +1) + "_" + deltaTimestamp;
            final File deltaFile = new File(compare.getParentFile(), compare.getName().replaceAll("\\.\\w+$", deltaName + ".png"));
            final BufferedImage delta = Compare.compareImage(masterImages.get(page), compareImages.get(page));
            if (delta == null)
                continue;
            deltas.add(deltaFile);
            ImageIO.write(delta, "png", deltaFile);
        }
        
        if (deltas.isEmpty())
            return null;
        return deltas.toArray(new File[0]);
    }
    
    /**
     * Pixel-based comparison of two BufferedImage.
     * Returned is a delta image, if differences were found.
     * @param  master
     * @param  compare
     * @return delta as image, otherwise {@code null}
     */
    private static BufferedImage compareImage(final BufferedImage master, final BufferedImage compare) {

        final Dimension dimension = new Dimension(
                Math.max(master.getWidth(), compare.getWidth()),
                Math.max(master.getHeight(), compare.getHeight()));
        final BufferedImage delta = new BufferedImage((int)dimension.getWidth(), (int)dimension.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Graphics graphics;

        final BufferedImage masterGray = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        graphics = masterGray.getGraphics();
        graphics.drawImage(master, 0, 0, null);
        graphics.dispose();

        final BufferedImage compareGray = new BufferedImage(compare.getWidth(), compare.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        graphics = compareGray.getGraphics();
        graphics.drawImage(compare, 0, 0, null);
        graphics.dispose();

        graphics = delta.getGraphics();
        graphics.drawImage(masterGray, 0, 0, null);
        graphics.dispose();

        final int COLOR_TONE_FACTOR = 127;

        boolean control = true;
        for (int y = 0; y < dimension.getHeight(); y++) {
            for (int x = 0; x < dimension.getWidth(); x++) {
                Integer pixelM = null;
                if (x < master.getWidth()
                        && y < master.getHeight())
                    pixelM = Integer.valueOf(master.getRGB(x, y));
                Integer pixelC = null;
                if (x < compare.getWidth()
                        && y < compare.getHeight())
                    pixelC = Integer.valueOf(compare.getRGB(x, y));
                if (pixelM == null
                        && pixelC == null) {
                    // case height or width mismatch without pixel
                    // use a gray color value
                    control = false;
                    delta.setRGB(x, y, new Color(255, 255, COLOR_TONE_FACTOR).getRGB());
                } else if (pixelC == null) {
                    // case pixel differences with height or width mismatch
                    // draw the grayscale pixel
                    control = false;
                    final Color color = new Color(masterGray.getRGB(x, y));
                    delta.setRGB(x, y, Compare.increaseColorTone(color.getRGB(), Color.GREEN, COLOR_TONE_FACTOR));                    
                } else if (pixelM == null) {
                    // case pixel differences with height or width mismatch
                    // draw the grayscale pixel
                    final Color color = new Color(compareGray.getRGB(x, y));
                    delta.setRGB(x, y, Compare.increaseColorTone(color.getRGB(), Color.BLUE, COLOR_TONE_FACTOR));                    
                } else if (pixelM.equals(pixelC)) {
                    // case pixel matches without height or width mismatch
                    // the pixels already exist grayscale
                } else {
                    // case pixel differences without height or width mismatch
                    // draw the grayscale pixel
                    control = false;
                    final Color colorM = new Color(masterGray.getRGB(x, y));
                    final Color colorC = new Color(compareGray.getRGB(x, y));
                    final Color colorD = new Color(
                            (colorM.getRed() +colorC.getRed()) /2,
                            (colorM.getGreen() +colorC.getGreen()) /2,
                            (colorM.getBlue() +colorC.getBlue()) /2,
                            (colorM.getAlpha() +colorC.getAlpha()) /2);
                    delta.setRGB(x, y, Compare.increaseColorTone(colorD.getRGB(), Color.RED, COLOR_TONE_FACTOR));
                }
            }
        }
        if (control)
            return null;
        return delta;
    }
}