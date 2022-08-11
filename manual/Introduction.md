# Introduction


## What is Seanox PDF-Service?

Seanox PDF-Service for generating/rendering PDFs based on
[Open HTML to PDF](https://github.com/danfickle/openhtmltopdf).

The static service contains an abstraction of templates, an API for templates,
renderer and markup generators, a built-in markup generator, and a preview
function with mock-up data support.

Templates support includes, locale-dependent CSS, native page numbers and total
page numbers, as well independent areas for header, content and footer, which
are assembled by overlay for each page. Header and footer are borderless and
can therefore also use the border area of the content.

Locale dependent CSS and native page numbers and total page number are
supported.


# Features

- Built-in markup generator  
very simple syntax, supports placeholders, structures and includes
- Built-in preview and designer with mock-up support  
simplifies the design process by allowing previewing without the target
application  
- Independent header, data and footer areas in one template (without magic)  
header, footer and data area are merged by overlays and can therefore be
formatted independently of the data area  
e.g. margins of the document can be used by the header and footer
- Native support of page numbers
- I18n support  
language setting is also transferred to the template and thus fonts matching the
language can be used
- Creation of markup as preview e.g. for the frontend
- API for templates and other renderer and markup generators 
abstract templates for individual renderer and generators
- PDF and image comparison for test automation (e.g. JUnit)  
pixel- and color-based with difference image generation
- PDF Tools as standalone Java applications  
includes Compare, Designer, Preview as command line applications  
designing and testing outside and independent of projects


## Contents Overview

* [Getting Started](#getting-started)
  * [Integration](#integration)
  * [Implementation](#implementation)
  * [Markup](#markup)
  * [Meta-Tags](#meta-tags)
    * [#include](#include)
  * [Placeholder](#placeholder)
    * [Value Placeholder](#value-placeholder)
    * [Structure Placeholder](#structure-placeholder)
    * [Disposable Structure Placeholder](#disposable-structure-placeholder)
    * [Disposable Value Placeholder](#disposable-value-placeholder)
    * [Exists Placeholders](#exists-placeholder)
    * [Escaped Placeholders](#escaped-placeholders)
    * [Runtime Placeholder](#runtime-placeholder)
    * [Static Placeholder](#static-placeholder)
* [Test](#test)
* [Mock-Up](#mock-up)
* [Template API](#template-api)
  * [Template](#template)
  * [Resources](#resources)
  * [Multiplex](#multiplex)
  * [Type](#type)
  * [TemplateException](#templateexception)
  * [TemplateResourceException](#templateresourceexception)
  * [TemplateResourceNotFoundException](#templateresourcenotfoundexception)
  * [TemplateRecursionException](#templaterecursionexception)
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

final var data = Service.render(..., ...);
Files.write(Paths.get("example.pdf"), data, StandardOpenOption.CREATE);
```

The data is passed to the renderer as a meta-object. The meta-object provides
various constructors, getters, and setters for this.

```java
import com.seanox.pdf.Service;
import ...

final var meta = new Meta(...);
meta.setData(...);
meta.setStatics(...);

final var data = Service.render(..., meta);
Files.write(Paths.get("example.pdf"), data, StandardOpenOption.CREATE);
```

There are two types of data. Structured data and static texts. Both are based
on key-value dictionaries in the form of a Map.

For structured data (`Map<String, Object>`), values of the data type Collection
are interpreted as a list that can be iterated over, and values of the data
type Map are interpreted as a branch that can be processed recursively. All
other data types are considered as output value and converted by
`String.valueOf(value).getBytes()` if required.

The creation of the structured data can be done by yourself or using frameworks
like ObjectMapper.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

// Data access is only hinted at here.
final var data = new HashMap<String, Object>() {{
    put("outlet", new ObjectMapper().convertValue(..., Map.class));
    put("articles", ...stream()
        .map(entity -> new ObjectMapper().convertValue(entity, Map.class))
        .collect(Collectors.toList()));
    }};
        
final var meta = new Meta(Locale.GERMANY);
meta.setData(data);
meta.setStatics(...);

final var data = Service.render(..., meta);
Files.write(Paths.get("example.pdf"), data, StandardOpenOption.CREATE);
```

Static texts use a strictly flat and string-based key-value pairs
(`Map<String, String>`) without the support of data types Collection and Map.
This data is practical because the key-value pairs dictionary has no levels and
the keys can be used in any level of the template structures.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

// Data access is only hinted at here.
final var data = new HashMap<String, Object>() {{
    put("outlet", new ObjectMapper().convertValue(..., Map.class));
    put("articles", ...stream()
        .map(entity -> new ObjectMapper().convertValue(entity, Map.class))
        .collect(Collectors.toList()));
    }};

final var statics = new HashMap<String, String>() {{
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

final var meta = new Meta(Locale.GERMANY);
meta.setData(data);
meta.setStatics(statics);

final var data = Service.render(..., meta);
Files.write(Paths.get("example.pdf"), data, StandardOpenOption.CREATE);
```

The render method needs an implemented template, in the best case. But you can
also implement a universal template, which uses simple strings for the paths to
the templates.

Why as a template implementation?

The implementation makes the template usage traceable for the compiler and
protects against errors. Another point is the Template API, whose
implementation is defined by the templates. The service only uses the API and
has no own template and markup generator. Only the implemented standard
template defines a built-in generator.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import ...

// Data access is only hinted at here.
final var data = new HashMap<String, Object>() {{
    put("outlet", new ObjectMapper().convertValue(..., Map.class));
    put("articles", ...stream()
        .map(entity -> new ObjectMapper().convertValue(entity, Map.class))
        .collect(Collectors.toList()));
    }};

final var statics = new HashMap<String, String>() {{
    put("ARTICLE_NUMBER", "Article Number");
    put("ARTICLE_PRICE", "Price");
    put("ADDRESS_TEL", "Tel");
    put("ADDRESS_FAX", "Fax");
    put("ADDRESS_E_MAIL", "E-Mail");
    put("ADDRESS_WEB", "Web");
}};

final var meta = new Meta(Locale.GERMANY);
meta.setData(data);
meta.setStatics(statics);

final var data = Service.render(ExampleTemplate.class, meta);
Files.write(Paths.get("example.pdf"), data, StandardOpenOption.CREATE);
```

```java
import com.seanox.pdf.Template;

class ExampleTemplate extends Template {
}
```

Template need the annotation `@Resources`. This annotation defines where in the
ClassPath the resources (CSS, images, fonts, ...) for the template are located
and, if necessary, defines the file name of the template if this cannot be
derived from the Java class.

```java
import com.seanox.pdf.Template;

@Resources(base="/pdf")
class ExampleTemplate extends Template {
}
```

The complete example can be found here:  
https://github.com/seanox/pdf-service/blob/master/src/test/java/com/seanox/pdf/example/UsageTemplate.java

### Markup

The template is a pure (X)HTML document with CSS support and meta-tags and
placeholder for the generator.

```html
<html>
  <head>
    <style>
      @page {
        margin: 36mm 18mm 40mm 18mm;
        size: A4 portrait;
      }
      ...
    </style>
  </head>
  <body>
  </body>
</html>
```

The renderer recognizes three parts in the template: header, content and
footer. Content is what is not `BODY > HEADER` and not `BODY > FOOTER`. Headers
and footers are extracted as independent and borderless templates. The CSS is
completely taken over and the borderless layout is extended. All documents are
rendered separately and merged into one PDF file via page overlay. 

```html
<html>
  <head>
    <style>
      @page {
        margin: 36mm 18mm 40mm 18mm;
        size: A4 portrait;
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
    <style>
      @page {
        margin: 36mm 18mm 40mm 18mm;
        size: A4 portrait;
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
https://github.com/seanox/pdf-service/blob/master/src/test/resources/com/seanox/pdf/example/UsageTemplate%24ExampleTemplate.html  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf

### Meta-Tags

Meta-tags are a part of the built-in generator and are not an HTML standard.
They are additional instructions for the generator that start with a hash and
work exclusive line-based.

#### #include

Inserts template fragments from other resources. The path of the resources is
always relative to the ClassPath of the using resource.

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
https://github.com/seanox/pdf-service/blob/master/src/test/resources/pdf/articleIncludeX.html  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf

### Placeholder

This section and description depend on the used built-in template generator.

Placeholders support values, structures and static texts. The identifier is
case-insensitive and based on the conventions of Java variables. Thus, the
identifier begins with one of the following characters: a-z A-Z _ $ and ends on
a word character: 0-9 a-z A-Z _ or $. In between, all word characters 0-9 a-z
A-Z _ as well as the currency symbol ($) and the minus sign can be used. 

#### Value Placeholder

Placeholders represent a value to the corresponding key of a level of a
structured or branched dictionary with key-value pairs. If to the identifier a
structure with the same name exists, this is applied to the value.

```
#[IDENTIFIER]
```

Basically the value is used as string and if necessary converted with
`String.valueOf(value).getBytes()`. Exceptions are the data types Collection
and Map. Collections are used iteratively and maps recursively. In both cases
the placeholder is preserved and can be used to build lists and deep
structures.

#### Structure Placeholder

Structures are complex nested constructs for the output of values, nested data
structures as well as lists and function like templates. Structures are defined
once and can then be reused anywhere with simple placeholders of the same
identifier. They are rendered only if the key-value dictionary at the
appropriate level contains a key matching the identifier. For the data type
Collection and Map, the placeholders remain after rendering and can thus be
(re)used iteratively for lists or recursive for complex nested outputs.

```
#[IDENTIFIER[[...]]]
```

- __Structural placeholders are unique.__
- __During parsing of the markup all placeholders are determined.__
- __If structural placeholders are defined more than once, they overwrite__
  __those already determined.__

The placeholder syntax has no syntax for navigating the branched data structure
from the key-value dictionary. For that the nesting in the structure
placeholder is used.

```
#[A[[
  #[B[[
    #[C]
  ]]]
]]]
```

__For a basic understanding of the structured placeholders it is important to
understand that they do not represent a real structure or branching. They are
templates for placeholders and these templates may contain other templates than
placeholders. Therefore, the placeholders can also be defined at that location.
They are simply used in the structure.__

The example uses in the map the value of: `A -> B -> C`

```java
final var data = new HashMap<>() {{
    put("A", new HashMap<>() {{
        put("B", new HashMap<>() {{
            put("C", "Value");
        }});
    }});
}};
```

Structure placeholders are retained for iterative use. The value is inserted
before the placeholder. They are only removed when the generation is complete.
Structure placeholders work like independent sub-template and can contain
markup and other placeholders.

#### Disposable Structure Placeholder

Disposable structures are bound to their place and, unlike a normal structure,
can be defined differently multiple times, but cannot be reused or extracted
based on their identifier.

```
#[IDENTIFIER{{...}}]
```

Because of the conditional output, disposable structures can be used as a
replacement for [exists-placeholder](#exists-placeholder) and then does not
need a CSS hack.

```html
<article id="outlet">
  <h1>#[name]</h1>
  <div exists="#[address-exists]">
    #[address[[
      <p exists="#[street-exists]">#[street]</p>
      ...
    ]]]
  </div>
</article>

<article id="outlet">
  <h1>#[name]</h1>
  #[address{{
    <div>
      #[street{{<p>#[#]</p>}}]
      ...
    </div>
  }}]
</article>
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf/ArticleMultiTemplate.html

#### Disposable Value Placeholder

The [disposable structure](#disposable-structure-placeholder) can also be used
for a value, which is then represented by the placeholder `#[#]`.

```
#[IDENTIFIER{{... #[#] ...}}]
```

Also, a conditional output of text instead of the value is possible.

```
#[IDENTIFIER{{...}}]
```

Or the combination with static placeholders is also supported.

```
#[IDENTIFIER{{... #[#] ... ![IDENTIFIER] ...}}]
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf/ArticleMultiTemplate.html

#### Exists Placeholder

As part of the [runtime placeholders](#runtime-placeholder) the exits
placeholder is generated automatically based on the keys and values. __In the
meantime this function is deprecated, because this is replaced by [disposable
structures](#disposable-structure-placeholder).__

For each key and placeholder an exists-placeholder is provided. This can be
used in the markup and in combination with CSS to output/display markup
depending on the existence of values in meta-data. The Exists placeholder
contains the value `exists` if the value is not `null`, not empty and not
blank.

```html
<article id="outlet">
  <h1>#[name]</h1>
  <div exists="#[address-exists]">
    #[address[[
      <p exists="#[street-exists]">#[street]</p>
      ...
    ]]]
  </div>
</article>

<article id="outlet">
  <h1>#[name]</h1>
  #[address{{
    <div>
      #[street{{<p>#[#]</p>}}]
      ...
    </div>
  }}]
</article>
```

The complete example and more can be found here:  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf/ArticleMultiTemplate.html

#### Escaped Placeholders

For the output of special and control characters a hexadecimal escape sequence
can be used, for which the identifier from the placeholder must start with `0x`.

```
#[0x0A]
#[0x4578616D706C6521]
```

#### Runtime Placeholder

Runtime placeholders are additional automatically generated placeholders based
on the keys and values in the key-value dictionary.

The placeholder `#[locale]` is provided from the meta-locale and can be used
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

For each key and placeholder an exists-placeholder is provided. This can be
used in the markup and in combination with CSS to output/display markup
depending on the existence of values in meta-data. The Exists placeholder
contains the value `exists` if the value is not `null`, not empty and not
blank.

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
https://github.com/seanox/pdf-service/blob/master/src/test/resources/pdf/articleA.html  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf

__The exists-placeholder is supported for backward compatibility, but is
deprecated and is replaced by the [disposable
structure](#disposable-structure-placeholder).__

#### Static Placeholder

For the output of static texts from meta-statics, which uses a strictly flat
and string-based key-value map without collections and branching. If no value
exists for a placeholder, it is removed.

```
![IDENTIFIER]
```

The static texts are practical, then key-value dictionary has no levels and the
keys can be used in any level of the template structures.


## Test

In addition to PDF creation, PDF Service is also focused on the design process
and testing. For this purpose, a pixel- and color-based comparison of PDF files
is included, which can be used as a command line tool and also directly in code
and thus also in JUnit for test automation. Any differences are visualized in
the form of delta images.

```java
import com.seanox.pdf.Compare;

Compare.compare(new File("master.pdf"), new File("compare.pdf"));
```

During the comparison, differences are searched for page by page, pixel by
pixel and color by color. If a difference is found between master and compare
on the same page, a difference image in the path of compare is created. The
image is based on the grayscale image of the master. The differences, which
compare causes, are marked in red. If there are discrepancies in resolution or
image mass, overlaps occur, which are displayed in blue (only in compare) and
green (only in master).

The return value of the compare method is an array with paths to any created
difference images. If no differences were found, the return value is `null`.


## Mock-Up

Mock-up is part of the preview and design process to design and test the markup
of the PDFs independently of the project. For this purpose, a property file
with the same name can be provided parallel to the template file.

The properties creates a branched map structure for a data object, comparable to
JSON. The branching is based on the dot as separator in the key.

The data structure supports the data types: Collection, Map, Markup, Text.
Collection and Map are only used for branching. Text is markup if it contains
HTML sequences `<.../>` or `>...</`. Markup indicates text where the escape of
HTML symbols is not required.

At the end a key with a text value is always expected.

- Properties are used for data and statics.
- Data uses all data as a structured map.
- Statics used only non-structured keys, without dot and list index.

Key with dot is an indicator for structured data. Each dot in the key
creates/uses a sub-map for the structured data map. Keys and partial keys
ending with `[n]` create/use a list with a (sub-)map.

```
report.data[0].value = Value 1
report.data[1].value = Value 2
report.data[2].value = Value 3
```

Examples and more can be found here:  
https://github.com/seanox/pdf-service/tree/master/src/test/resources/pdf


## Template API

### Template

Abstract class for implementing templates. The implementation defines the
resource management, the preparation of markup, the generation of markup and
can optionally also define the PDF rendering. The service only uses the API and
has no own template and markup generator. Only the implemented standard
template defines a built-in generator.

### Resources

Templates are based on an implementation of [Template](#template) and the
annotation [Resources](#resources), which with `base` and `template` contains
information about the base directory of the resources (CSS, images, fonts,
...), as well the path of the markup template with the same name.

### Multiplex

Templates consist of three fragments: header, content and footer. The content
can be created in one step because it is embedded in a page that can contain
margins into which the headers and footers are inserted. Headers and footers
are subsequently inserted as a one-page overlay on each page. Since headers and
footers are created separately, they can use custom and additional placeholders
such as `#[page]` (current page number) and `#[pages]` (total number).

The multiplexer separates the markup for the fragments: header, content, footer
into completely separate (X)HTML documents. For this purpose, all fragments use
the complete (X)HTML document, but for each fragment the content of the body
element is reduced to the respective fragment.

#### Header

The complete (X)HTML document with `BODY > HEADER` only.

#### Content

The complete (X)HTMl document without `BODY > HEADER` and without
`BODY > FOOTER`.

#### Footer

The complete (X)HTML document with `BODY > FOOTER` only.

### Type

Templates consist of three fragments: Header, Content and Footer. The markup is
rendered using overlays, to which Type provides the corresponding constants as
an enumeration, which can then be used by the generator implementation as an
orientation to be able to react specifically to the fragments.

### TemplateException

General exception in the context of the templates.

### TemplateResourceException

Exception when accessing and using template resources.

### TemplateResourceNotFoundException

Exception if template resources are not found.

### TemplateRecursionException

Exception for endless recursions in the context of the templates.


## PDF-Tools

The command line tools include helpers that focus on the design process and
testing outside and independent of projects.  
Includes: Compare, Designer, Preview  

### Download

The download includes a Java archive that contains all required libraries.  
The current version can be found here:  
https://github.com/seanox/pdf-service/releases

### Compare

Command line tool for pixel and color based comparison of two PDFs that
generates an image with the differences.

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Compare <master> <compare>
```

During the comparison, differences are searched for page by page, pixel by
pixel and color by color. If a difference is found between master and compare
on the same page, a delta image in the path of compare is created. The image is
based on the grayscale image of the master. The differences, which compare
causes, are marked in red. If there are discrepancies in resolution or image
mass, overlaps occur, which are displayed in blue (only in compare) and green
(only in master).

### Preview

Command line tool that creates a PDF preview of all templates with the
annotation `@Resources` in the ClassPath and of all files found for the
specified paths, filters and globs.

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Preview <paths/filters/globs> ...
```

The preview is based on the mock-up data in the properties files for the
templates.

### Designer

Command line daemon which continuously creates a PDF preview of all templates
with the annotation `@Resources` in the ClassPath and of all files found for
the specified paths, filters and globs. The previews are updated when there are
changes to the templates.

```
java -cp seanox-pdf-tools.jar com.seanox.pdf.Designer <paths/filters/globs> ...
```

The preview is based on the mock-up data in the properties files for the
templates.
