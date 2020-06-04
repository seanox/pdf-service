/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2020 Seanox Software Solutions
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/** 
 * Pixel-based comparison of PDF files.<br>
 * <br>
 * Compare 1.1.0 20200530<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.1.0 20200530
 */
public class Compare {
    
    /**
     * Main entry for the console application.
     * @param  options optional list with paths and filters/globs of templates
     *     Without, the current working directory is used.
     * @throws Exception
     *     In case of unexpected errors.
     */    
    public static void main(String[] options)
            throws Exception {
        
        System.out.println("Seanox PDF Comparator [Version 1.1.0 20200530]");
        System.out.println("Copyright (C) 2020 Seanox Software Solutions");
        System.out.println();
        
        if (options == null
                || options.length < 2) {
            System.out.println();
            System.out.println("usage: java -jar pdf-tools.jar <master.pdf> <compare.pdf>");
        }
        
        Compare.compare(new File(options[0]), new File(options[1]));
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
    public static File[] compare(File master, File compare)
            throws IOException {
        
        List<BufferedImage> baseImages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(master)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page)
                baseImages.add(pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB));
        }

        List<BufferedImage> compareImages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(compare)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page)
                compareImages.add(pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB));
        }
        
        while (baseImages.size() < compareImages.size())
            baseImages.add(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR));
        while (baseImages.size() > compareImages.size())
            compareImages.add(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR));
        
        String deltaTimestamp = String.format("%tY%<tm%<td%<tH%<tM%<tS", new Date()); 
        List<File> deltas = new ArrayList<>();
        for (int page = 0; page < baseImages.size(); ++page) {
            String deltaName = "_diffs_page_" + (page +1) + "_" + deltaTimestamp;
            File deltaFile = new File(compare.getParentFile(), compare.getName().replaceAll("\\.\\w+$", deltaName + ".png"));
            BufferedImage delta = Compare.compareImage(baseImages.get(page), compareImages.get(page));
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
    private static BufferedImage compareImage(BufferedImage master, BufferedImage compare) {
        
        Dimension dimension = new Dimension(
                Math.max(master.getWidth(), compare.getWidth()),
                Math.max(master.getHeight(), compare.getHeight()));
        BufferedImage delta = new BufferedImage((int)dimension.getWidth(), (int)dimension.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
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
                    //case height or width mismatch without pixel
                    //use a gray color value
                    control = false;
                    delta.setRGB(x, y, 0xFFC0C0C0);
                } else if (pixelM == null
                        || pixelC == null) {
                    //case pixel differences with height or width mismatch
                    //use the inverted color value
                    control = false;
                    delta.setRGB(x, y, (0xFFFFFF -(pixelM != null ? pixelM.intValue() : pixelC.intValue())) | 0xFF000000);                    
                } else if (pixelM.equals(pixelC)) {
                    //case pixel matches without height or width mismatch 
                    delta.setRGB(x, y, master.getRGB(x, y));
                } else {
                    //case pixel differences without height or width mismatch 
                    control = false;
                    int a = 0xFF | pixelM.intValue() >> 24;
                    int r = 0xFF & pixelM.intValue() >> 16;
                    int g = 0x00 & pixelM.intValue() >> 8;
                    int b = 0x00 & pixelM.intValue();
                    int modifiedRGB = a << 24 | r << 16 | g << 8 | b;
                    delta.setRGB(x, y, modifiedRGB);
                }
            }
        }
        if (control)
            return null;
        return delta;
    }
}