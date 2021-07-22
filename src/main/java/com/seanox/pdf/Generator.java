/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generator, generates data by filling placeholders (tags) in a template/model.
 * A value list with keys is passed to the template. If the keys correspond to
 * the placeholders (case insensitive), the placeholders are replaced by the
 * values.<br>
 * <br>
 * The generator worked at byte level.<br>
 * Values are therefore expected to be prim&auml;r as byte arrays. All other
 * data types are converted using {@code String.valueOf(value).getBytes()}.<br>
 * <br>
 * Placeholders can be used for values and segments.<br>
 * Segments are partial structures that can be nested up to a depth of 65535
 * levels. These substructures can be used and filled globally or by segment
 * name dedicted/partially.<br>
 * The placeholders of segments remain after filling and can be reused
 * iteratively.<br>
 * The data types {@link Collection} and {@link Map} are expected as values for
 * segments. A {@link Map} then contains the values for the placeholders within
 * the segment. A {@link Collection} causes to an iteration over a set of
 * {@link Map} and is comparable to the iterative call of the method
 * {@link #set(String, Map)}.<br>
 * Both {@link Map} and {@link Collection} create deep, complex, possibly
 * repetitive and recursive structures.
 *
 * <h3>Description of the syntax</h3>
 * The syntax of the placeholders is case-insensitive, must begin with a letter
 * and is limited to the following characters:
 *     <dir>{@code a-z A-Z 0-9 _-}</dir>
 *      
 * <h3>Structure and description of the placeholders</h3>
 * <table>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[value]}
 *     </td>
 *     <td valign="top">
 *       Inserts the value for &lt;value&gt; and removes the placeholder.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[scope[[...]]]}
 *     </td>
 *     <td valign="top">
 *       Defines a segment/scope. The nesting and use of further segments is
 *       possible. Since the placeholders for inserting segments are preserved,
 *       they can be used to build lists.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[0x0A]}<br>
 *       {@code #[0x4578616D706C6521]}
 *     </td>
 *     <td valign="top">
 *       Escaping from one or more characters. The conversion is done with
 *       {@link #extract(String, Map)}, {@link #extract(String)} or
 *       {@link #extract()} at the end of the generation.
 *     </td>
 *   </tr>
 * </table>
 *  
 * <h3>Functionality</h3>
 * The model (byte array) is parsed initially.
 * All placeholders are checked for syntactic correctness.
 * If necessary, invalid placeholders are removed. In addition, the scopes with
 * the segments (partial templates) are determined and replaced by a simple
 * placeholder. After parsing, a final model with optimized placeholders and
 * extracted segments is created, which cannot be changed at runtime.<br>
 * <br>
 * For the use of the model different possibilities are then available.<br>
 * <br>
 * With {@link #set(Map)} the placeholders in the model are replaced with the
 * values passed over. Placeholders for which no values exist are retained.
 * Placeholders that represent a segment/scope are also replaced if a
 * corresponding key exists in the values. For segments/scopes, the placeholder
 * is retained for reuse and directly follows the inserted value.<br>
 * <br>
 * With {@link #set(String, Map)} only the specified scope is filled. For this,
 * a copy of the segment (sub-template) is created and filled with the values
 * passed, all placeholders are removed and the content is inserted as a value
 * before the placeholder. Thus, this segment/scope placeholder is also
 * preserved for reuse.<br>
 * <br>
 * The methods {@link #extract(String)} and {@link #extract(String, Map)} use
 * exclusive segments (subtemplates), which are partially filled and prepared.
 * Both methods produce final results that correspond to the call of
 * {@link #set(Map)} in combination with {@link #extract()}, but focus on only
 * one segment.<br>
 * <br>
 * Generator 5.2.3 20210722<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.2.3 20210722
 */
class Generator {

    /** Segments of the template */
    private HashMap<String, Object> scopes;

    /** Model, data buffer of the template */
    private byte[] model;

    /** Constructor, create an empty generator. */
    private Generator() {
        this.scopes = new HashMap<>();
    }

    /**
     * Creates a new generator based on the transferred template.
     * @param  model Template as bytes
     * @return the generator with the template passed as bytes
     */
    static Generator parse(byte[] model) {
        
        Generator generator = new Generator();
        generator.model = generator.scan(model);
        return generator;
    }
    
    /**
     * Determines whether a valid placeholder starts at the specified position
     * in a model (segment). In this case the length of the complete placeholder
     * is returned. If no placeholder can be determined, the return value is 0.
     * If no further data is available in the model for analysis (end of data is
     * reached) a negative value is returned.
     * @param  model  Model(Fragment)
     * @param  cursor Position
     * @return the position of the next placeholder or segment, otherwise a
     *         negative value
     */
    private static int scan(byte[] model, int cursor) {
        
        if (model == null
                || cursor >= model.length)
            return -1;        

        //Phase 0: Identification of a placeholder
        //  - supported formats: #[...], #[...[[...]]]
        //  - characteristic are the first two characters
        //  - all placeholders begin with #[...
        if (cursor +1 >= model.length
                || model[cursor] != '#'
                || model[cursor +1] != '[')
            return 0;
            
        int offset = cursor;
        int deep   = 0;

        int[] stack = new int[65535];
        while (cursor < model.length) {

            //The current level is determined.
            int level = 0;
            if (deep > 0)
                level = stack[deep];

            //Phase 1: Recognition of the start of a placeholder
            //  - supported formats: #[...], #[...[[...]]]
            //  - characteristic are the first two characters
            //  - all placeholders begin with #[...
            //A placeholder can only begin if no stack and therefore no
            //placeholder exists or if a segment placeholder has been determined
            //before. In both cases the level is not equal to 1 and another
            //stack with level 1 starts.
            if (cursor +1 < model.length
                    && model[cursor] == '#'
                    && model[cursor +1] == '['
                    && level != 1) {
                stack[++deep] = 1;
                cursor += 2;
                continue;
            }
            
            //Phase 1A: Qualification of a segment placeholder
            //  - active level 1 is expected
            //  - character string [[ is found
            //The current stack is set to level 2.
            if (cursor +1 < model.length
                    && model[cursor] == '['
                    && model[cursor +1] == '['
                    && level == 1) {
                stack[deep] = 2;
                cursor += 2;
                continue;
            }

            //Phase 2: Detecting the end of a detected placeholder
            //The level must be 1 and the character [ must be found.
            //Then the current stack is removed, because the search is finished
            //here.
            if (model[cursor] == ']'
                    && level == 1) {
                if (--deep  <= 0)
                    break;
                cursor += 1;
                continue;
            }

            //Phase 2A: Detecting the end of a detected placeholder
            //The level must be 1 and the character [ must be found.
            //Then the current stack is removed, because the search here is
            //completed.
            if (cursor +2 < model.length
                    && model[cursor +0] == ']'
                    && model[cursor +1] == ']'
                    && model[cursor +2] == ']'
                    && level == 2) {
                cursor += 2;
                if (--deep <= 0)
                    break;
                cursor += 1;
                continue;
            }
            
            cursor++;
        }
        
        //Case 1: The stack is not empty
        //Thus, a placeholder was detected which is not completed.
        //The scan is hungry and assumes an incomplete placeholder.
        //Therefore the offset from start position to the end is from the model.
        if (deep > 0)
            return model.length -offset;
        
        //Case 2: The stack is empty
        //The placeholder was determined completely and the offset corresponds
        //to the length of the complete placeholder with possibly contained
        //segments.
        return cursor -offset +1;
    }

    /**
     * Analyzes the model and prepares it for final processing.
     * All placeholders are checked for syntactic correctness. Invalid
     * placeholders are removed if necessary. In addition, the scopes with the
     * segments (partial templates) are determined and replaced by a simple
     * placeholder. After parsing, a final model with optimized placeholders and
     * extracted segments is created, which cannot be changed at runtime.
     * @param  model Model
     * @return the final prepared model
     */
    private byte[] scan(byte[] model) {
        
        if (model == null)
            return new byte[0];
        
        int cursor = 0;
        while (true) {
            int offset = Generator.scan(model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;            

            byte[] patch = new byte[0];
            String fetch = new String(model, cursor, offset);
            if (fetch.matches("^(?si)#\\[[a-z]([\\w\\-]*\\w)?\\[\\[.*\\]\\]\\]$")) {
                
                //scope is determined from: #[scope[[segment]]
                String scope = fetch.substring(2);
                scope = scope.substring(0, scope.indexOf('['));
                scope = scope.toLowerCase();
                
                //segment is extracted from the model
                byte[] cache = new byte[offset -scope.length() -7];
                System.arraycopy(model, cursor +scope.length() +4, cache, 0, cache.length);
                
                //scope is registered with the segment if scope does not exist
                if (!this.scopes.containsKey(scope))
                    this.scopes.put(scope, this.scan(cache));
                
                //as new placeholder only the scope is used
                patch = ("#[").concat(scope).concat("]").getBytes();
            } else if (fetch.matches("^(?i)#\\[[a-z]([\\w-]*\\w)?\\]$")) {
                patch = fetch.toLowerCase().getBytes();
            } else if (fetch.matches("^(?i)#\\[0x([0-9a-f]{2})+\\]$")) {
                cursor += fetch.length() +1;
                continue;
            }
            
            //model is rebuilt with the patch
            byte[] cache = new byte[model.length -offset +patch.length];
            System.arraycopy(model, 0, cache, 0, cursor);
            System.arraycopy(patch, 0, cache, cursor, patch.length);
            System.arraycopy(model, cursor +offset, cache, cursor +patch.length, model.length -cursor -offset);
            model = cache;
            
            cursor += patch.length;
        }
        
        return model;
    }
    
    /**
     * Fills the current model with the transferred values.
     * Optionally, the filling can be limited to one segment by specifying a
     * scope and/or {@code clean} can be used to specify whether the return
     * value should be finalized and all outstanding placeholders removed or
     * resolved.
     * @param  scope  Scope or segment
     * @param  values Values
     * @param  clean  {@code true} for final cleanup
     * @return the filled model (segment)
     */    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private byte[] assemble(String scope, Map<String, Object> values, boolean clean) {
        
        Object object;
        String fetch;
        
        byte[] cache;
        byte[] model;
        byte[] patch;

        if (this.model == null)
            return new byte[0];

        //Normalization of the values (lower case + smoothing of the keys)
        if (values == null)
            values = new HashMap<>();
        values = values.entrySet().stream().collect(
                Collectors.toMap(
                        (entry) -> entry.getKey().toLowerCase().trim(),
                        (entry) -> entry.getValue(),
                        (existing, value) -> value));
        
        //Optionally the scope is determined.
        if (scope != null) {
            scope = scope.toLowerCase().trim();

            //If one is specified that does not exist, nothing is to be done.
            if (!this.scopes.containsKey(scope))
                return this.model;
            
            //Scopes are prepared independently and later processed like a
            //simple but exclusive placeholder.
            patch = this.extract(scope, values);
            
            values.clear();
            values.put(scope, patch);
        }
        
        int cursor = 0;
        while (true) {
            int offset = Generator.scan(this.model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;

            patch = new byte[0];
            fetch = new String(this.model, cursor, offset);
            if (fetch.matches("^(?i)#\\[[a-z]([\\w-]*\\w)?\\]$")) {
                fetch = fetch.substring(2, fetch.length() -1);
                
                //the placeholders of not transmitted keys are ignored, with
                //'clean' the placeholders are deleted
                if (!values.containsKey(fetch)
                        && !clean) {
                    cursor += fetch.length() +3 +1;
                    continue;
                }
                
                //patch is determined by the key
                object = values.get(fetch);

                //If the key is a segment and the value is a map with values,
                //the segment is filled recursively. To protect against infinite
                //recursions, the current scope is removed from the value list.
                //  e.g. #[A[[#[B[[#[A[[...]]...]]...]]
                if (this.scopes.containsKey(fetch)
                        && object instanceof Map) {
                    patch = this.extract(fetch, (Map)object);
                } else if (this.scopes.containsKey(fetch)
                        && object instanceof Collection) {
                    //Collections generates complex structures/tables through
                    //deep, repetitive recursive generation.
                    for (Object entry : ((Collection)object)) {
                        if (entry instanceof Map) {
                            model = this.extract(fetch, (Map)entry);
                        } else if (entry instanceof byte[]) {
                            model = (byte[])entry;
                        } else if (entry != null) {
                            model = String.valueOf(entry).getBytes();
                        } else continue;
                        cache = new byte[patch.length +model.length];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(model, 0, cache, patch.length, model.length);
                        patch = cache; 
                    }
                } else if (object instanceof byte[]) {
                    patch = (byte[])object;
                } else if (object != null) {
                    patch = String.valueOf(object).getBytes();
                }
                
                if (!clean) {
                
                    //if necessary the # characters are encoded to protect the
                    //placeholders and structure in the model
                    int index = 0;
                    while (index < patch.length) {
                        if (patch[index++] != '#')
                            continue;
                        cache = new byte[patch.length +6];
                        System.arraycopy(patch, 0, cache, 0, index);
                        System.arraycopy(("[0x23]").getBytes(), 0, cache, index, 6);
                        System.arraycopy(patch, index, cache, index +6, patch.length -index);
                        patch = cache;
                    }
                    
                    if (this.scopes.containsKey(fetch)) {
                        fetch = ("#[").concat(fetch).concat("]");
                        cache = new byte[patch.length +fetch.length()];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(fetch.getBytes(), 0, cache, patch.length, fetch.length());
                        patch = cache;
                    }
                }
                
            } else if (fetch.matches("^(?i)#\\[0x([0-9a-f]{2})+\\]$")) {
                
                //Hexadecimal placeholders are only resolved with clean, because
                //they can contain unwanted (control) characters, which hinders
                //rendering.
                if (!clean) {
                    cursor += fetch.length() +1;
                    continue;            
                }
                
                //hexadecimal code is converted into bytes
                fetch = fetch.substring(4, fetch.length() -1); 
                fetch = ("ff").concat(fetch);
                patch = new BigInteger(fetch, 16).toByteArray();
                patch = Arrays.copyOfRange(patch, 2, patch.length);                
            }
            
            //model is rebuilt with the patch
            cache = new byte[this.model.length -offset +patch.length];
            System.arraycopy(this.model, 0, cache, 0, cursor);
            System.arraycopy(patch, 0, cache, cursor, patch.length);
            System.arraycopy(this.model, cursor +offset, cache, cursor +patch.length, this.model.length -cursor -offset);
            this.model = cache;
            
            cursor += patch.length;
        }
        
        return this.model;
    }

    /**
     * Return all scopes of the segments as enumeration.
     * Free scopes (without segment) are not included.
     * @return all scopes of the segments as enumeration
     */
    Enumeration<String> scopes() {
        return Collections.enumeration(this.scopes.keySet());
    }

    /**
     * Returns the currently filled template.
     * @return the currently filled template
     */
    byte[] extract() {
        return this.assemble(null, null, true).clone();
    }
    
    /**
     * Extracts a specified segment and sets the data there.
     * The data of the template are not affected by this.
     * @param  scope Segment
     * @return the filled segment, if this cannot be determined, an empty byte
     *         array is returned
     */
    byte[] extract(String scope) {
        return this.extract(scope, null);
    }
    
    /**
     * Extracts a specified segment and sets the data there.
     * The data of the template are not affected by this.
     * @param  scope  Segment
     * @param  values List of values
     * @return the filled segment, if this cannot be determined, an empty byte
     *         array is returned
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    byte[] extract(String scope, Map<String, Object> values) {
        
        if (scope != null)
            scope = scope.toLowerCase().trim();
        if (scope == null
                || !scope.matches("^[a-z]([\\w-]*\\w)*$"))
            return new byte[0];
        
        //Internally, a copy of the generator is created for the segment
        //(partial model) and thus partially filled.
        Generator generator = new Generator();
        generator.scopes = (HashMap)this.scopes.clone();
        generator.scopes.remove(scope);
        generator.model = (byte[])this.scopes.get(scope);
        if (generator.model == null)
            generator.model = new byte[0];
        return generator.assemble(null, values, true);
    }

    /**
     * Sets the data for a scope or a segment.
     * @param values Values
     */
    void set(Map<String, Object> values) {
        this.set(null, values);
    }

    /**
     * Sets the data for a scope or a segment.
     * @param scope  Scope or segment
     * @param values Values
     */
    void set(String scope, Map<String, Object> values) {

        if (scope != null)
            scope = scope.toLowerCase().trim();
        if (scope != null
                && !scope.matches("^[a-z]([\\w-]*\\w)*$"))
            return;
        this.model = this.assemble(scope, values, false);
    }
}