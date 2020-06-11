# Introduction


## What is Seanox pdf-service?
PDF service for generating/rendering PDFs based on
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
TODO:


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
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.fasterxml.jackson.databind.ObjectMapper;
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

TODO:

### Markup
TODO:

### Test
TODO:

### Generator / Render API
TODO:


### Mock-Up
TODO:


## PDF-Tools
TODO:

### Compare
TODO:

### Preview
TODO:

### Designer
TODO: