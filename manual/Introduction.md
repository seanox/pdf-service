# Introduction


## What is Seanox PDF-Service?

Seanox PDF-Service for generating/rendering PDFs based on
[Open HTML to PDF](https://github.com/danfickle/openhtmltopdf).

The static service contains an abstraction of templates, an API for templates
and markup generators, a markup generator with preview function and mockup data
support.  
The templates supports includes and independent areas for header, content and
footer, which are assembled by overlay for each page. Header and footer are
borderless overlays and can therefore also use the border area of the content.  
Locale dependent CSS and native page numbers and total page number are
supported.


# Features

- Built-in markup generator  
very simple syntax, supports placeholders, structures and includes
- Built-in preview and designer with mock-up support  
simplifies the design process by allowing previewing without the target
application  
- Independent header, data and footer areas in one template (without magic)  
header, footer and data area are merged by overlaysand can therefore be
formatted independently of the data area  
e.g. margins of the document can be used by the header and footer
- Native support of page numbers
- I18n support  
language setting is also transferred to the template and thus fonts matching the
language can be used
- Creation of markup as preview e.g. for the frontend
- API for templates and other markup generators
abstract templates for individual generators
- PDF comparison for test automation  
pixel- and color-based with difference image generation
- PDF Tools as standalone Java applications  
includes Compare, Designer, Preview as command line applications  
designing and testing outside and independent of projects


## Contents Overview

* [Getting Started](#getting-started)
  * [Integration](#integration)
  * [Implementation](#implementation)
  * [Markup](#markup)
  * [Meta Tags](#meta-tags)
  * [Placeholder](#placeholder)
    * [Static Placeholder](#static-placeholder)
    * [Data Value Placeholder](#data-structure-placeholder)
    * [Data Structure Placeholder](#data-structure-placeholder)
    * [Escaped Placeholders](#escaped-placeholders)
    * [Standard Value Placeholder](#standard-value-placeholder)
* [Test](#test)
* [Mock-Up](#mock-up)
* [Template API](#template-api)
* [Generator API](#generator-api)
* [PDF-Tools](#pdf-tools)
  * [Compare](#compare)
  * [Preview](#preview)
  * [Designer](#designer)


## Getting Started

### Integration

Use the current version dependency  
https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service  
or use the Java archive of the Seanox PDF-Tools, which contains all libraries  
https://github.com/seanox/pdf-service/raw/master/releases

### Implementation

The PDF service is a static component with static methods for rendering, which
are called directly at any point in Java.

```java
import com.seanox.pdf.Service;
import ...

byte[] data = Service.render(..., ...);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

The data is passed to the renderer as a meta object.  
The meta object provides various constructors, getters, and setters for this.  
The data is divided into data structure and static texts.  

Internationalization (i18n) can be set via the locale when using locale
dependent resources like fonts. 

```java
import com.seanox.pdf.Service;
import ...

Meta meta = new Meta(Locale.GERMANY);
meta.setData(...);
meta.setStatics(...);

byte[] data = Service.render(..., meta);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

Static texts are mapped as simple Map<String, String> and are used e.g. for
labels and text output.

```java
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

Map<String, String> statics = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L; {
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

Meta meta = new Meta(Locale.GERMANY);
meta.setData(...);
meta.setStatics(statics);

byte[] data = Service.render(..., meta);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

The data structure is a structured map for data objects (entities).  
The keys are always of data type String.  
The values can be of the data type Collection, Map, and Object.  
Collections are used iteratively, maps recursively, and other objects as string
values.  
The creation of the data structure can be done manually, partially manually and
with tools, e.g. with a ObjectMapper.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

Map<String, String> statics = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L; {
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

Meta meta = new Meta(Locale.GERMANY);
meta.setData(new HashMap<>());
meta.setStatics(statics);

// Data access is only hinted at here.
meta.getData().put("outlet", new ObjectMapper().convertValue(..., Map.class));
meta.getData().put("articles", ...stream().map(
    entity -> new ObjectMapper().convertValue(entity, Map.class)
).collect(Collectors.toList()));

byte[] data = Service.render(..., meta);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

The render method needs one more template.  
Why as a template implementation?  
The implementation makes the template usage traceable for the compiler and
protects against errors.  
Another point is the Template and Generator API, whose implementation is defined
by the templates. The service only uses the API and has no own template and
generator.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

Map<String, String> statics = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L; {
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

Meta meta = new Meta(Locale.GERMANY);
meta.setData(new HashMap<>());
meta.setStatics(statics);

// Data access is only hinted at here.
meta.getData().put("outlet", new ObjectMapper().convertValue(..., Map.class));
meta.getData().put("articles", ...stream().map(
    entity -> new ObjectMapper().convertValue(entity, Map.class)
).collect(Collectors.toList()));

byte[] data = Service.render(ExampleTemplate.class, meta);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

```java
import com.seanox.pdf.Template;

public static class ExampleTemplate extends Template {
}
```

Template need the annotation `@Resources`.  
This annotation determines where in the ClassPath the resources (e.g. styles,
images, fonts, ...) for the template are located and, if necessary, determines
the file name of the template if this cannot be derived from the Java class.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

Map<String, String> statics = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L; {
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

Meta meta = new Meta(Locale.GERMANY);
meta.setData(new HashMap<>());
meta.setStatics(statics);

// Data access is only hinted at here.
meta.getData().put("outlet", new ObjectMapper().convertValue(..., Map.class));
meta.getData().put("articles", ...stream().map(
    entity -> new ObjectMapper().convertValue(entity, Map.class)
).collect(Collectors.toList()));

byte[] data = Service.render(ExampleTemplate.class, meta);
File output = new File("example.pdf");
Files.write(output.toPath(), data, StandardOpenOption.CREATE);
```

```java
import com.seanox.pdf.Template;

@Resources(base="/pdf")
public static class ExampleTemplate extends Template {
}
```

The complete example can be found here:  
https://github.com/seanox/pdf-service/blob/master/src/test/java/com/seanox/pdf/example/UsageTemplate.java#L1

### Markup

The template is a pure (X)HTML document with CSS support and meta tags and
placeholder for the generator.  

```html
<html>
  <head>
    <style type="text/css">
      @page {
        margin:36mm 18mm 40mm 18mm;
        size:A4 portrait;
      }
      ...
    </style>
  </head>
  <body>
  </body>
</html>
```

The renderer recognizes three parts in the template: header, content and footer.  
Content is what is not BODY > HEADER and not BODY > FOOTER.  
Headers and footers are extracted as independent and borderless templates.  
The CSS is completely taken over and the borderless layout is extended.  
All documents are rendered separately and merged into one PDF file via page
overlay. 

```html
<html>
  <head>
    <style type="text/css">
      @page {
        margin:36mm 18mm 40mm 18mm;
        size:A4 portrait;
      }
      ...
    </style>
  </head>
  <body>
    <header>
      ...
    </header>
    ... 
    <footer>
      ...
    </footer>
  </body>
</html>
```

Open HTML to PDF and the used frameworks come with some custom CSS extensions,
which are described on the homepage.  
https://github.com/danfickle/openhtmltopdf/wiki/Page-features  
The specials for headers, footers and page numbers can be used or ignored
because they have been implemented alternatively in the PDF service.

```html
<html>
  <head>
    <style type="text/css">
      @page {
        margin:36mm 18mm 40mm 18mm;
        size:A4 portrait;
      }
      body {
        ...
      }  
      header {
        ...
      }
      footer {
        ...
      }  
      ...
    </style>
  </head>
  <body>
    <header>
      ...
    </header>
    ... 
    <footer>
      ...
    </footer>
  </body>
</html>
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/blob/master/src/test/resources/com/seanox/pdf/example/UsageTemplate%24ExampleTemplate.html#L1  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf

### Meta Tags

Meta tags are not an HTML standard.  
They are additional instructions for the generator.  
Meta tags works exclusive, line-based and start with a hash.
The description refers to the template generator which is integrated in Seanox
PDF-Service.  

#### #include

Inserts markup fragments from other resources.  
The path of the resources is always relative to the ClassPath of the using
resource.

```html
<html>
  <head>
    ...
  </head>
  <body>
    <header>
      #include shares/header.html
    </header>
    #include shares/outlet.html
    #include shares/article.html
    ... 
    <footer>
      #include shares/footer.html
    </footer>
  </body>
</html>
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/blob/master/src/test/resources/pdf/articleIncludeX.html#L1  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf

### Placeholder

This section depends on the template generator used.  
The description refers to the template generator which is integrated in Seanox
PDF-Service.

Placeholders can be used for static texts, values and data structures.  
The syntax of the placeholders is case-insensitive, must begin with a letter and
is limited to the following characters:  
`a-z A-Z 0-9 _-`

#### Static Placeholder

For the output of static texts from Meta-Statics.  
Meta-Statics is a strict string based key value map.  
The placeholder is replaced by the value.  
If no value exists, the placeholder is removed without replacement.

```
![NAME]
```
 
#### Data Value Placeholder

For output of single values from Meta-Data.
Meta-Data is a structured map like a tree.  
The keys are always strings.  
The values can be of the data type Collection, Map, and Object.
Collections are used iteratively and maps recursively.
For other objects, the string value is then used.  

```
#[NAME]
```

The name of the placeholder always refers to the current branch in the tree.  
The placeholder syntax has no syntax for navigating the tree.  
This is made using the nesting of the data structure placeholders.  

#### Data Structure Placeholder

For output of data structures from meta data.  
Meta-Data is a structured map like a tree.  
The keys are always strings.  
The values can be of the data type Collection, Map, and Object.
Collections are used iteratively and maps recursively.
For other objects, the string value is then used.  
The name of the placeholder always refers to the current branch in the tree.

```
#[NAME[[...]]]
```
 
The placeholder syntax has no syntax for navigating the tree.  
This is made using the nesting of the data structure placeholders.  

```
#[A[[
  #[B[[
    #[C]
  ]]]
]]]
```

The example uses in the map the value of: `A -> B -> C'

```java
Map<String, Object> data = new HashMap<>() {{
    put("A", new HashMap<>() {{
        put("B", new HashMap<>() {{
            put("C", "Value");
        }});
    }});
}};
```

Data structure placeholders are retained for iterative use.  
The value is inserted before the placeholder.
They are only removed when the generation is complete.

Data structure placeholders work like independent segments and can contain
markup and other placeholders. 

#### Escaped Placeholders

For output of special and control characters.  
These placeholders are only resolved when the generation is completed.

```
#[0x0A]
#[0x4578616D706C6521]
```

#### Standard Value Placeholder

The generator provides a few additional value placeholders.

The placeholder `#[locale]` is provided from the Meta-Locale and can be used
for internationalization (i18n). 

```html
<html>
  <head>
    ...
  </head>
  <body lang="#[locale]">
    <header>
      ...
    </header>
    ... 
    <footer>
      ...
    </footer>
  </body>
</html>
```

The placeholders `#[page]` and `#[pages]` are available in the header and
footer and contain the current page and total number of pages.

```html
<html>
  <head>
    ...
  </head>
  <body lang="#[locale]">
    <header>
      ...
    </header>
    ... 
    <footer>
      #[page] of #[pages]
    </footer>
  </body>
</html>
```

For each key and placeholder an exists-placeholder is provided.  
This can be used in the markup and in combination with CSS to output/display
markup depending on the existence of values in meta-data.
    
```html
<html>
  <head>
    ...
  </head>
  <body lang="#[locale]">
    <header>
      ...
    </header>
    #[outlet[[
    <article id="outlet">
      <h1>#[name]</h1>
      <p exists="#[street-exists]">#[street]</p>
      <p exists="#[location-exists]">#[zipCode] #[location]</p>
      <p exists="#[phone-exists]">![ADDRESS_TEL]: #[phone]</p>
      <p exists="#[fax-exists]">![ADDRESS_FAX]: #[fax]</p>
      <p exists="#[email-exists]">![ADDRESS_E_MAIL]: #[email]</p>
      <p exists="#[websiteUrl-exists]">![ADDRESS_WEB]: #[websiteUrl]</p>
    </article>
    ]]]
    <footer>
      #[page] of #[pages]
    </footer>
  </body>
</html>
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/blob/master/src/test/resources/pdf/articleA.html#L1  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf


## Test

PDF Service is primarily optimized for the design process.  
Therefore the test of PDFs is pixel- and color-based.
For automated tests the Compare with the static method of the same name is
available.

```java
import com.seanox.pdf.Compare;

Compare.compare(new File("master.pdf"), new File("compare.pdf"));
```

During the comparison, differences are searched for page by page, pixel by pixel
and color by color.  
If a difference is found between master and compare on the same page, a
difference image in the path of compare is created.  
The image is based on the grayscale image of the master.  
The differences, which compare causes, are marked in red.  
If there are discrepancies in resolution or image mass, overlaps occur, which
are displayed in blue (only in compare) and green (only in master).

The return value of the compare method is an array with paths to any created
difference images.  
If no differences were found, the return value is `null`.


## Mock-Up

Mock-up is part of the preview and design process to design and test the markup
of the PDFs independently of the project. For this purpose, a property file with
the same name can be provided parallel to the template file.

The properties creates a nested map structure for a data object, comparable to
JSON. The nesting is based on the dot as separator in the key.  
The data structure supports the data types: Collection, Map, Markup, Text.  
Collection and Map are only used for nesting.  
Text is markup if it contains HTML sequences `<.../>` or `>...</`.  
Markup indicates text where the escape of HTML symbols is not required.  
At the end a key with a text value is always expected.  

Properties are used for data and statics.    
Data uses all data as a structured map.  
Statics used only non-structured keys, without dot and list index.  

Key with dot is an indicator for structured data.  
Each dot in the key creates/uses a sub-map for the structured data map.

Keys and partial keys ending with [n] create/use a list with a (sub-)map.

```
report.data[0].value = Value 1
report.data[1].value = Value 2
report.data[2].value = Value 3
```

## Template API
TODO:


## Generator API
TODO:


## PDF-Tools

The command line tools include helpers that focus on the design process and
testing outside and independent of projects.  
Includes: Compare, Designer, Preview  

### Download

The download includes a Java archive that contains all required libraries.  
The current version can be found here:  
https://github.com/seanox/pdf-service/raw/master/releases

### Compare

Compares two PDFs pixel- and color-based with difference image generation.

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Compare <master> <compare>
```

During the comparison, differences are searched for page by page, pixel by pixel
and color by color.  
If a difference is found between master and compare on the same page, a
difference image in the path of compare is created.  
The image is based on the grayscale image of the master.  
The differences, which compare causes, are marked in red.  
If there are discrepancies in resolution or image mass, overlaps occur, which
are displayed in blue (only in compare) and green (only in master).

### Preview

Creates a PDF preview of all templates with the annotation `@Resources` in the
ClassPath and of all files found for the specified paths, filters and globs.

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Preview <paths/filters/globs> ...
```

The preview is based on the mock-up datas in the properties files for the
templates.

### Designer

Command line Deamon, which permanently searches for templates in the ClassPath
for the annotation `@Resources` and in the file system for templates for paths,
filters and globs and creates PDF previews like the preview tool.  
The previews are updated if there are changes to the templates. 

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Designer <paths/filters/globs> ...
```

The preview is based on the mock-up datas in the properties files for the
templates.
