/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2022 Seanox Software Solutions
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
 * Generator fills placeholders in a template (model) with the values of
 * key-value pairs from a dictionary ({@link Map}) if the keys match the
 * identifier of the placeholder (case-insensitive).<br>
 * <br>
 * Filling works at byte level, which uses the values as byte arrays. Other data
 * types are converted via {@code String.valueOf(value).getBytes()}.<br>
 * <br>
 * Placeholders represent values and structures. Structures are nested
 * placeholders with a depth of up to 65535 levels, which use a tree-like
 * dictionary with key-value pairs in the form of a {@link Map}. In addition,
 * structures provide scopes, these are comparable with sub-templates, which
 * can be inserted at any place by simple placeholders and can be dedicated or
 * partially filled and extracted on basis of the structure identifier. Because
 * placeholders of structures are preserved after filling, they can be reused
 * iteratively.<br>
 * <br>
 * Structures use {@link Collection} and {@link Map} as values. A {@link Map}
 * then contains the values for the placeholders within the structure. A
 * {@link Collection} iterates over a set of {@link Map} objects, which is
 * similar to the iterative call of the {@link #set(String, Map)} method.
 * {@link Map} and {@link Collection} create deep, complex, and recursive
 * structures.
 *
 * <h3>Description of the syntax</h3>
 * Placeholders support values, structures and static texts. The identifier is
 * case-insensitive and based on the conventions of Java variables. Thus, the
 * identifier begins with one of the following characters: a-z A-Z _ $ and ends
 * on a word character: 0-9 a-z A-Z _ or $. In between, all word characters 0-9
 * a-z A-Z _ as well as the currency symbol ($) and the minus sign can be used.
 *
 * <h3>Structure and description of the placeholders</h3>
 * <table>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <b>Value Placeholder</b><br>
 *       <code>#[identifier]</code><br>
 *     </td>
 *     <td valign="top">
 *       Placeholders represent a value to the corresponding key of a level of a
 *       structured or branched dictionary with key-value pairs. If to the identifier
 *       a structure with the same name exists, this is applied to the value.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <b>Structure Placeholder</b><br>
 *       <code>#[identifier[[...]]]</code><br>
 *     </td>
 *     <td valign="top">
 *       Structures are complex nested constructs for the output of values,
 *       nested data structures as well as lists and function like templates.
 *       Structures are defined once and can then be reused anywhere with
 *       simple placeholders of the same identifier. They are rendered only if
 *       the key-value dictionary at the appropriate level contains a key
 *       matching the identifier. For the data type {@link Collection} and
 *       {@link Map}, the placeholders remain after rendering and can thus be
 *       (re)used iteratively for lists or recursive for complex nested
 *       outputs.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <b>Disposable Structure Placeholder</b><br>
 *       <code>#[identifier{{...}}]</code><br>
 *     </td>
 *     <td valign="top">
 *       Disposable structures are bound to their place and, unlike a normal
 *       structure, can be defined differently multiple times, but cannot be
 *       reused or extracted based on their identifier.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <b>Disposable Value Placeholder</b><br>
 *       <code>#[identifier{{... #[#] ...}}]</code>
 *     </td>
 *     <td valign="top">
 *       The disposable structure placeholder can also be used for a value,
 *       which is then represented by the placeholder {@code #[#]}.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <code>#[identifier{{...}}]</code>
 *     </td>
 *     <td valign="top">
 *       Also, a conditional output of text instead of the value is possible.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       <b>Escaped Placeholders</b><br>
 *       <code>#[0x...]</code>
 *     </td>
 *     <td valign="top">
 *       For the output of special and control characters a hexadecimal escape
 *       sequence can be used, for which the identifier from the placeholder
 *       must start with {@code 0x} and is followed by the hexadecimal code
 *       sequence.
 *     </td>
 *   </tr>
 * </table>
 *
 * <h3>Functionality</h3>
 * The model (byte array) is parsed initially. All placeholders are checked for
 * syntactic correctness. If necessary, invalid placeholders are removed.
 * Structures (partial templates) are determined and replaced by a simple
 * placeholder. After parsing, a final model with optimized placeholders and
 * extracted structures is created, which cannot be changed at runtime.<br>
 * <br>
 * For the use of the model different possibilities are then available.<br>
 * <br>
 * With {@link #set(Map)} the placeholders in the model are replaced with the
 * values passed over. Placeholders for which no values exist are retained.
 * Placeholders that represent a structure are also replaced if a corresponding
 * key exists in the values. For structures, the placeholder is retained for
 * reuse and follows the inserted value.<br>
 * <br>
 * With {@link #set(String, Map)} only the specified scope, means a
 * corresponding structure with the same name. For this,  copy of the structure
 * is created and filled with the passed values, all placeholders are removed
 * and the content is inserted as a value before the placeholder. Thus, this
 * structure placeholder is also preserved for reuse.<br>
 * <br>
 * The methods {@link #extract(String)} and {@link #extract(String, Map)} use
 * exclusive structures, which are partially filled and prepared. Both methods
 * produce final results that correspond to the call of {@link #set(Map)} in
 * combination with {@link #extract()}, but focus on only one structure.
 *
 * @author  Seanox Software Solutions
 * @version 4.1.0 20220811
 */
class Generator {

    /** Pattern fragment of an identifier */
    private final static String TEXT_PATTERN_IDENTIFIER = "([_a-zA-Z$](?:[\\w-$]*[\\w$])?)";

    /** Pattern fragment of an disposable identifier */
    private final static String TEXT_PATTERN_IDENTIFIER_OPTIONAL_DISPOSABLE = "^" + TEXT_PATTERN_IDENTIFIER + "(:\\d+)?$";

    /** Pattern fragment of a structure placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_STRUCTURE = "^(?s)#\\[" + TEXT_PATTERN_IDENTIFIER + "\\[\\[.*\\]{3}$";

    /** Pattern fragment of a disposable structure placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_STRUCTURE_DISPOSABLE = "^(?s)#\\[" + TEXT_PATTERN_IDENTIFIER + "\\{\\{.*\\}\\}\\]$";

    /** Pattern fragment of a structure value placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_STRUCTURE_VALUE = "#[#]";

    /** Pattern fragment of a value placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_VALUE = "^#\\[" + TEXT_PATTERN_IDENTIFIER + "\\]$";

    /** Pattern fragment of a disposable value placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_VALUE_DISPOSABLE = "^#\\[" + TEXT_PATTERN_IDENTIFIER + "(:\\d+)\\]$";

    /** Pattern fragment of an optional disposable value placeholder */
    private final static String TEXT_PATTERN_PLACEHOLDER_VALUE_OPTIONAL_DISPOSABLE = "^#\\[" + TEXT_PATTERN_IDENTIFIER + "(:\\d+)?\\]$";

    /** Pattern fragment of a hexadecimal encodes value */
    private final static String TEXT_PATTERN_PLACEHOLDER_VALUE_HEXADECIMAL = "^(?i)#\\[0x([0-9a-f]{2})+\\]$";

    /** Scopes with structures of the template */
    private HashMap<String, Structure> scopes;

    /** Model, data buffer of the template */
    private byte[] model;

    /** Internal incremental counter used for serials */
    private long serial;

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
        final var generator = new Generator();
        generator.model = generator.scan(model);
        return generator;
    }
    
    /**
     * Determines whether a valid placeholder starts at the specified position
     * in a model (structure). In this case the length of the complete
     * placeholder is returned. If no placeholder can be determined, the return
     * value is 0. If no further data is available in the model for analysis,
     * end of data is reached, a negative value is returned.
     * @param  model  Model(Fragment)
     * @param  cursor Position
     * @return the position of the next placeholder or structure, otherwise a
     *         negative value
     */
    private static int scan(byte[] model, int cursor) {
        
        if (model == null
                || cursor >= model.length)
            return -1;        

        // Phase 0: Identification of a placeholder
        // - supported formats: #[...], #[...[[...]]], #[...{{...}}]
        // - characteristic are the first two characters
        // - all placeholders begin with #[...
        if (cursor +1 >= model.length
                || model[cursor] != '#'
                || model[cursor +1] != '[')
            return 0;
            
        var offset = cursor;
        var deep   = 0;

        var stack = new int[65535];
        while (cursor < model.length) {

            // The current mode is determined.
            var mode = deep > 0 ? Math.abs(stack[deep]) : 0;

            // Phase 0-1: Recognition of the start of a placeholder
            // - supported formats: #[...], #[...[[...]]], #[...{{...}}]
            // - characteristic are the first two characters
            // - all placeholders begin with #[...
            // A placeholder can only begin if no stack and therefore no
            // placeholder exists or if a structure placeholder has been
            // determined before. In both cases the mode is not equal to 1 and
            // another stack with mode 1 starts.
            if (cursor +1 < model.length
                    && mode != 1) {
                if (model[cursor] == '#'
                        && model[cursor +1] == '[') {
                    if (deep < 65535)
                        stack[++deep] = 1;
                    cursor += 2;
                    continue;
                }
            }
            
            // Phase 1-2: Qualification of a structure placeholder
            // - active mode 1 is expected
            // - character string [[ or {{ is found
            // The current stack is set to mode 2.
            if (cursor +1 < model.length
                    && mode == 1) {
                if ((model[cursor] == '[' && model[cursor +1] == '[')
                        || (model[cursor] == '{' && model[cursor +1] == '{')) {
                    stack[deep] = model[cursor] == '{' ? -2 : 2;
                    cursor += 2;
                    continue;
                }
            }

            // Phase 1-0: Detecting the end of a detected placeholder
            // The mode must be 1 and characters ] must be found.
            // Then the current stack is removed, because the search is finished
            // here.
            if (model[cursor] == ']'
                    && mode == 1) {
                if (--deep <= 0)
                    break;
                cursor += 1;
                continue;
            }

            // Phase 2-0: Detecting the end of a detected placeholder
            // The mode must be 2 and the sequence ]]] or }}] must be found.
            // Then the current stack is removed, because the search here is
            // completed.
            if (cursor +2 < model.length
                    && mode == 2) {
                final var symbol = mode == 2 ? stack[deep] > 0 ? ']' : '}' : 0;
                if (model[cursor +0] == symbol
                        && model[cursor +1] == symbol
                        && model[cursor +2] == ']') {
                    cursor += 2;
                    if (--deep <= 0)
                        break;
                    cursor += 1;
                    continue;
                }
            }

            cursor++;
        }
        
        // Case 1: The stack is not empty
        // Thus, a placeholder was detected which is not completed.
        // The scan is hungry and assumes an incomplete placeholder and so the
        // offset is from the start position to the end of the model.
        if (deep > 0)
            return model.length -offset;
        
        // Case 2: The stack is empty
        // The placeholder was determined completely and the offset corresponds
        // to the length of the complete placeholder with possibly contained
        // structures.
        return cursor -offset +1;
    }

    /**
     * Analyzes the model and prepares it for final processing.
     * All placeholders are checked for syntactic correctness. Invalid
     * placeholders are removed if necessary. In addition, all structures
     * (sub-templates) are determined, which then also define the scopes and
     * are then replaced by a simple placeholder. After parsing, a final model
     * with optimized placeholders and extracted structures is created, which
     * cannot be changed at runtime.
     * @param  model Model
     * @return the final prepared model
     */
    private byte[] scan(byte[] model) {
        
        if (model == null)
            return new byte[0];
        
        var cursor = 0;
        while (true) {
            var offset = Generator.scan(model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;            

            var patch = new byte[0];
            var fetch = new String(model, cursor, offset);
            if (fetch.matches(TEXT_PATTERN_PLACEHOLDER_STRUCTURE)) {

                // scope is determined from: #[scope[[structure]]
                var scope = fetch.substring(2);
                scope = scope.substring(0, scope.indexOf('['));
                scope = scope.toLowerCase();

                // structure is extracted from the model
                final var cache = new byte[offset -scope.length() -7];
                System.arraycopy(model, cursor +scope.length() +4, cache, 0, cache.length);

                // scope and structure are registered if scope does not exist
                if (!this.scopes.containsKey(scope))
                    this.scopes.put(scope, new Structure(this.scan(cache)));

                // as new placeholder only the scope is used
                patch = ("#[").concat(scope).concat("]").getBytes();

            } else if (fetch.matches(TEXT_PATTERN_PLACEHOLDER_STRUCTURE_DISPOSABLE)) {

                // scope is determined from: #[scope{{structure}}]
                var scope = fetch.substring(2);
                scope = scope.substring(0, scope.indexOf('{'));
                scope = scope.toLowerCase();

                // structure is extracted from the model
                final var cache = new byte[offset - scope.length() - 7];
                System.arraycopy(model, cursor + scope.length() + 4, cache, 0, cache.length);

                // unique scope is registered with the structure
                scope = String.format("%s:%d", scope, ++this.serial);
                this.scopes.put(scope, new Structure(this.scan(cache)));

                // as new placeholder only the unique scope is used
                patch = ("#[").concat(scope).concat("]").getBytes();

            } else if (fetch.matches(TEXT_PATTERN_PLACEHOLDER_VALUE)) {

                patch = fetch.toLowerCase().getBytes();

            } else if (fetch.matches(TEXT_PATTERN_PLACEHOLDER_VALUE_DISPOSABLE)) {

                // The internal syntax #[scope:id] for scanned placeholders of
                // structures must be escaped, because this must not be used
                // directly in the template, because the impact and outcome is
                // not predictable.

                fetch = fetch.substring(2, fetch.indexOf(']'));
                fetch = new BigInteger(fetch.getBytes()).toString(16);
                patch = ("#[").concat(fetch).concat("]").getBytes();

            } else if (TEXT_PATTERN_PLACEHOLDER_STRUCTURE_VALUE.equals(fetch)) {

                cursor += fetch.length() +1;
                continue;

            } else if (fetch.matches(TEXT_PATTERN_PLACEHOLDER_VALUE_HEXADECIMAL)) {

                cursor += fetch.length() +1;
                continue;
            }
            
            // model is rebuilt with the patch
            final var cache = new byte[model.length -offset +patch.length];
            System.arraycopy(model, 0, cache, 0, cursor);
            System.arraycopy(patch, 0, cache, cursor, patch.length);
            System.arraycopy(model, cursor +offset, cache, cursor +patch.length, model.length -cursor -offset);
            model = cache;
            
            cursor += patch.length;
        }
        
        return model;
    }

    /**
     * Extracts and fills a specified structure and sets the data there.
     * The data of the template are not affected by this. In difference to
     * {@link #extract(String, Map)}, the only internally use method also
     * supports structures as scope.
     * @param  scope  Scope
     * @param  values List of values
     * @return the filled structure, if this cannot be determined, an empty byte
     *         array is returned
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private byte[] assemble(String scope, Map<String, Object> values) {

        if (scope == null)
            return new byte[0];
        scope = scope.toLowerCase().trim();
        if (!scope.matches(TEXT_PATTERN_IDENTIFIER_OPTIONAL_DISPOSABLE))
            return new byte[0];

        // Internally, a copy of the generator is created for the structure
        // (partial model) and thus partially filled.
        final var generator = new Generator();
        generator.scopes = (HashMap)this.scopes.clone();
        generator.scopes.remove(scope);
        generator.model = this.scopes.get(scope).data;
        if (generator.model == null)
            generator.model = new byte[0];
        return generator.assemble(null, values, true);
    }

    /**
     * Fills the current model with the transferred values.
     * Optionally, the filling can be limited to one structure by specifying a
     * scope and/or {@code clean} can be used to specify whether the return
     * value should be finalized and all outstanding placeholders removed or
     * resolved.
     * @param  scope  Scope
     * @param  values Values
     * @param  clean  {@code true} for final cleanup
     * @return the filled model (structure)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private byte[] assemble(String scope, Map<String, Object> values, boolean clean) {
        
        Object object;

        byte[] cache;
        byte[] model;
        byte[] patch;

        if (this.model == null)
            return new byte[0];

        // Normalization of the values (lower case + smoothing of the keys)
        if (values == null)
            values = new HashMap<>();
        if (!(values instanceof StructureValue))
            values = values.entrySet().stream().collect(
                    Collectors.toMap(
                            (entry) -> entry.getKey().toLowerCase().trim(),
                            (entry) -> entry.getValue(),
                            (existing, value) -> value));
        
        // Optionally the scope is determined.
        if (scope != null) {
            scope = scope.toLowerCase().trim();

            // If one is specified that does not exist, nothing is to be done.
            if (!this.scopes.containsKey(scope))
                return this.model;
            
            // Scopes are prepared independently and later processed like a
            // simple but exclusive placeholder.
            patch = this.assemble(scope, values);
            
            values.clear();
            values.put(scope, patch);
        }
        
        int cursor = 0;
        while (true) {
            final var offset = Generator.scan(this.model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;

            patch = new byte[0];
            scope = new String(this.model, cursor, offset);
            if (scope.matches(TEXT_PATTERN_PLACEHOLDER_VALUE_OPTIONAL_DISPOSABLE)
                    || (TEXT_PATTERN_PLACEHOLDER_STRUCTURE_VALUE.equals(scope)
                            && (values instanceof StructureValue))) {
                scope = scope.substring(2, scope.length() -1);

                // the placeholders of not transmitted keys are ignored, with
                // 'clean' the placeholders are deleted
                final var key = scope.replaceAll(":\\d+$", "");
                if (!values.containsKey(key)
                        && !clean) {
                    cursor += scope.length() +3 +1;
                    continue;
                }
                
                // patch is determined by the key
                object = values.get(key);

                // If the key is a structure and the value is a map with
                // values, then is filled recursively. To protect against
                // infinite recursions, the current scope is removed from the
                // value list.
                //   e.g. #[A[[#[B[[#[A[[...]]...]]...]]
                if (this.scopes.containsKey(scope)
                        && Structure.Type.OBJECT.equals(this.scopes.get(scope).type)
                        && object instanceof Map) {
                    patch = this.assemble(scope, (Map)object);
                } else if (this.scopes.containsKey(scope)
                        && Structure.Type.OBJECT.equals(this.scopes.get(scope).type)
                        && object instanceof Collection) {
                    // Collection generate complex structures/tables through
                    // deep, repetitive recursive generation.
                    for (Object entry : ((Collection)object)) {
                        if (entry instanceof Map) {
                            model = this.assemble(scope, (Map)entry);
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
                } else if (this.scopes.containsKey(scope)
                        && Structure.Type.VALUE.equals(this.scopes.get(scope).type)
                        && object != null) {
                    patch = this.assemble(scope, new StructureValue(object));
                } else if (object instanceof byte[]) {
                    patch = (byte[])object;
                } else if (object != null) {
                    patch = String.valueOf(object).getBytes();
                }
                
                if (!clean) {
                
                    // if necessary the # characters are encoded to protect the
                    // placeholders and structure in the model
                    var index = 0;
                    while (index < patch.length) {
                        if (patch[index++] != '#')
                            continue;
                        cache = new byte[patch.length +6];
                        System.arraycopy(patch, 0, cache, 0, index);
                        System.arraycopy(("[0x23]").getBytes(), 0, cache, index, 6);
                        System.arraycopy(patch, index, cache, index +6, patch.length -index);
                        patch = cache;
                    }
                    
                    if (this.scopes.containsKey(scope)) {
                        scope = ("#[").concat(scope).concat("]");
                        cache = new byte[patch.length +scope.length()];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(scope.getBytes(), 0, cache, patch.length, scope.length());
                        patch = cache;
                    }
                }
                
            } else if (scope.matches(TEXT_PATTERN_PLACEHOLDER_VALUE_HEXADECIMAL)) {
                
                // Hexadecimal placeholders are only resolved with clean, because
                // they can contain unwanted (control) characters, which hinders
                // rendering.
                if (!clean) {
                    cursor += scope.length() +1;
                    continue;            
                }
                
                // hexadecimal code is converted into bytes
                scope = scope.substring(4, scope.length() -1);
                scope = ("ff").concat(scope);
                patch = new BigInteger(scope, 16).toByteArray();
                patch = Arrays.copyOfRange(patch, 2, patch.length);                
            }
            
            // model is rebuilt with the patch
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
     * Return all scopes of the structures as enumeration.
     * The scopes of disposable structure are not included.
     * @return all scopes of the structures as enumeration
     */
    Enumeration<String> scopes() {
        return Collections.enumeration(this.scopes.keySet().stream()
                .filter(key -> !key.contains(":"))
                .collect(Collectors.toList()));
    }

    /**
     * Returns the currently filled template.
     * @return the currently filled template
     */
    byte[] extract() {
        return this.assemble(null, null, true).clone();
    }
    
    /**
     * Extracts a specified structure and sets the data there.
     * The data of the template are not affected by this.
     * @param  scope Scope
     * @return the filled structure, if this cannot be determined, an empty byte
     *         array is returned
     */
    byte[] extract(String scope) {
        return this.extract(scope, null);
    }

    /**
     * Extracts a specified structure and sets the data there.
     * The data of the template are not affected by this.
     * @param  scope  Scope
     * @param  values List of values
     * @return the filled structure, if this cannot be determined, an empty byte
     *         array is returned
     */
    byte[] extract(String scope, Map<String, Object> values) {
        if (scope != null
                && !scope.matches("^" + TEXT_PATTERN_IDENTIFIER + "$"))
            return new byte[0];
        return this.assemble(scope, values);
    }

    /**
     * Sets the data in the complete model.
     * @param values Values
     */
    void set(Map<String, Object> values) {
        this.set(null, values);
    }

    /**
     * Sets the data for a specific scope.
     * @param scope  Scope
     * @param values Values
     */
    void set(String scope, Map<String, Object> values) {
        if (scope != null)
            scope = scope.toLowerCase().trim();
        if (scope != null
                && !scope.matches("^" + TEXT_PATTERN_IDENTIFIER + "$"))
            return;
        this.model = this.assemble(scope, values, false);
    }

    private static class StructureValue extends HashMap<String, Object> {
        private StructureValue(Object value) {
            super();
            this.put("#", value);
        }
    }

    private static class Structure {

        private final byte[] data;

        private final Type type;

        private Structure(byte[] data) {

            this.data = data;

            var cursor = 0;
            while (true) {
                final var offset = Generator.scan(data, cursor++);
                if (offset < 0)
                    break;
                if (offset == 0)
                    continue;
                cursor--;
                final var fetch = new String(data, cursor, offset);
                if (!fetch.matches(TEXT_PATTERN_PLACEHOLDER_STRUCTURE)
                        && !fetch.matches(TEXT_PATTERN_PLACEHOLDER_STRUCTURE_DISPOSABLE)
                        && !fetch.matches(TEXT_PATTERN_PLACEHOLDER_VALUE)) {
                    cursor += fetch.length() +1;
                    continue;
                }
                this.type = Type.OBJECT;
                return;
            }
            this.type = Type.VALUE;
        }

        private enum Type {
            OBJECT, VALUE
        }
    }
}