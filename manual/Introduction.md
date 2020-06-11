# Introduction


## What is Seanox PDF-Service?

Seanox PDF-Service for generating/rendering PDFs based on
[Open HTML to PDF](https://github.com/danfickle/openhtmltopdf).

The static service contains an abstraction of templates, an API for markup
generators/renderers, a markup generator with preview function and mockup data
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
- API for other markup generators and renderers  
abstract templates for individual generators and renderers
- PDF comparison for test automation  
pixel-based and difference image generation
- PDF Tools as standalone Java applications  
includes Compare, Designer, Preview as command line applications  
designing and testing outside and independent of projects


## Contents Overview

* [Getting Started](#getting-started)
  * [Integration](#integration)
  * [Implementation](#implementation)
  * [Markup](#markup)
  * [Placeholder](#placeholder)
    * [Static Placeholder](#static-placeholder)
    * [Data Value Placeholder](#data-structure-placeholder)
    * [Data Structure Placeholder](#data-structure-placeholder)
    * [Escaped Placeholders](#escaped-placeholders)
* [Mock-Up](#mock-up)
* [Test](#test)
* [Generator / Render API](#generator-render-api)
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
    put("ARTICLE_NUMBER", "Article number");
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
Another point is the Generator/Render API, whose implementation is defined by  
the templates. The service only uses the API and does not have its own
generator/render.

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
This annotation determines where in the ClassPath the resources for the template
are located and, if necessary, determines the file name of the template if this
cannot be derived from the Java class.

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

The template is a pure (X)HTML document with CSS support and meta commands and
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


## Test

PDF Service is primarily optimized for the design process.  
Therefore the test of PDFs is pixel-based.
For automated tests the Compare with the static method of the same name is
available.

```java
import com.seanox.pdf.Compare;

Compare.compare(new File("master.pdf"), new File("compare.pdf"));
```

During the comparison, differences are searched for page by page, pixel by pixel
and color by color.  
If a difference is found between master and compare on the same page, a
difference image is created.  
The image is based on the grayscale image of the master.  
The differences, which compare causes, are marked in red.  
If there are discrepancies in resolution or image mass, overlaps occur, which
are displayed in blue (only in compare) and green (only in master).

The return value of the compare method is an array with paths to any created
difference images.  
If no differences were found, the return value is `null`.


## Generator / Render API
TODO:


## Mock-Up
TODO:


## PDF-Tools
TODO:

### Compare
TODO:

### Preview
TODO:

### Designer
TODO:
