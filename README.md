<p>
  <a href="https://github.com/seanox/pdf-service/pulls"
      title="Development is waiting for new issues / requests / ideas">
    <img src="https://img.shields.io/badge/development-passive-blue?style=for-the-badge">
  </a>  
  <a href="https://github.com/seanox/pdf-service/issues">
    <img src="https://img.shields.io/badge/maintenance-active-green?style=for-the-badge">
  </a>
  <a href="http://seanox.de/contact">
    <img src="https://img.shields.io/badge/support-active-green?style=for-the-badge">
  </a>
</p>

# Description
Seanox PDF-Service for generating/rendering PDFs based on
[Open HTML to PDF](https://github.com/danfickle/openhtmltopdf).

The static service contains an abstraction of templates, an API for templates,
renderer and markup generators, a built-in markup generator, and a preview
function with mock-up data support.

The templates supports includes and independent areas for header, content and
footer, which are assembled by overlay for each page. Header and footer are
borderless overlays and can therefore also use the border area of the content.

Locale dependent CSS and native page numbers and total page number are
supported.


# Features
- Built-in markup generator   
Very simple syntax, supports placeholders, structures and includes.
- Built-in preview and designer with mock-up support  
Simplifies the design process by allowing previewing without the target 
application  .
- Independent header, data and footer areas in one template (without magic)  
Header, footer and data area are merged by overlays and can therefore be
formatted independently of the data area e.g. margins of the document can be
used by the header and footer.
- Native support of page numbers
- I18n support  
Language setting is also transferred to the template and thus fonts matching
the language can be used.
- Creation of markup as preview e.g. for the frontend
- API for templates and other renderer and markup generators  
Abstract templates for individual renderer and generators.
- PDF comparison for test automation  
Pixel- and color-based with difference image generation.
- PDF Tools as standalone Java applications  
Includes Compare, Designer, Preview as command line applications designing and
testing outside and independent of projects.


# Licence Agreement
LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 
Diese Software unterliegt der Version 2 der Apache License.

Copyright (C) 2022 Seanox Software Solutions

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.


# System Requirement
Java 11 or higher


# Downloads
https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service/4.1.2  
https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service
```xml
<dependency>
    <groupId>com.seanox</groupId>
    <artifactId>seanox-pdf-service</artifactId>
    <version>4.1.2</version>
</dependency>
```

[Seanox PDF-Tools 4.1.2](https://github.com/seanox/pdf-service/releases/download/4.1.2/seanox-pdf-tools-4.1.2.jar)


# Manuals
- [Introduction](https://github.com/seanox/pdf-service/blob/master/manual/Introduction.md)


# Changes (Change Log)
## 4.1.2 20220713 (summary of the current version)  
BF: Build: Optimization of the release info process  
BF: Maven: Update of dependencies  
BF: Template: Correction for handling unexpected data type in data map  

[Read more](https://raw.githubusercontent.com/seanox/pdf-service/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/pdf-service/issues)  
[Requests](https://github.com/seanox/pdf-service/pulls)  
[Mail](http://seanox.de/contact)  
