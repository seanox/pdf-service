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
 * Compare 1.0.0 20200530<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.0.0 20200530
 */
public class Compare {

    /**
     * Pixel-based comparison of two PDF files.
     * Returned is a file list with delta images, if differences were found.
     * @param  base
     * @param  compare
     * @return file list with delta images, otherwise {@code null}
     * @throws IOException
     */
    public static File[] compare(File base, File compare)
            throws IOException {
        
        List<BufferedImage> baseImages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(base)) {
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
            String deltaName = "_delta_" + (page +1) + "_page_" + deltaTimestamp;
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
     * @param  base
     * @param  compare
     * @return delta as image, otherwise {@code null}
     */
    private static BufferedImage compareImage(BufferedImage base, BufferedImage compare) {
        
        // Based on: Neeraj Vishwakarma
        //     http://mundrisoft.com/tech-bytes/compare-images-using-java 

        Dimension dimension = new Dimension(
                Math.max(base.getWidth(), compare.getWidth()),
                Math.max(base.getHeight(), compare.getHeight()));
        BufferedImage delta = new BufferedImage((int)dimension.getWidth(), (int)dimension.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        boolean control = true;
        for (int y = 0; y < dimension.getHeight(); y++) {
            for (int x = 0; x < dimension.getWidth(); x++) {
                try {
                    int pixelC = compare.getRGB(x, y);
                    int pixelB = base.getRGB(x, y);
                    if (pixelB != pixelC) {
                        control = false;
                        int a = 0xFF | base.getRGB(x, y) >> 24;
                        int r = 0xFF & base.getRGB(x, y) >> 16;
                        int g = 0x00 & base.getRGB(x, y) >> 8, b = 0x00 & base.getRGB(x, y);
                        int modifiedRGB = a << 24 | r << 16 | g << 8 | b;
                        delta.setRGB(x, y, modifiedRGB);
                    } else delta.setRGB(x, y, base.getRGB(x, y));
                } catch (Exception exception) {
                    // handled height or width mismatch
                    control = false;
                    delta.setRGB(x, y, 0x80FF0000);
                }
            }
        }
        if (control)
            return null;
        return delta;
    }
}