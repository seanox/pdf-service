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
PDF service for generating/rendering PDFs based on
[Open HTML to PDF](https://github.com/danfickle/openhtmltopdf).

The static service contains an abstraction of templates, an API for markup
renderers/generators, a markup generator and a preview function including mockup
data support.  
The templates supports includes and independent areas for header, content and
footer, which are assembled by overlay for each page. Header and footer are
borderless overlays and can therefore also use the border area of the content.  
Locale dependent CSS and native page numbers and total page number are
supported.


# Features
- Built-in markup generator   
very simple syntax, supports placeholders, structures and includes
- Built-in preview and designer with mock-up support  
simplifies the design process by allowing previewing without the target application  
- Independent header, data and footer areas in one template (without magic)  
header, footer and data area are merged by overlaysand can therefore be formatted independently of the data area  
e.g. margins of the document can be used by the header and footer
- Native support of page numbers
- I18n support  
language setting is also transferred to the template and thus fonts matching the language can be used
- Creation of markup as preview e.g. for the frontend
- API for other markup renderers and generators  
abstract templates for individual generators and renderers


# Licence Agreement
LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 
Diese Software unterliegt der Version 2 der Apache License.

Copyright (C) 2020 Seanox Software Solutions

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.


# System Requirement
Java 8 or higher


# Downloads
https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service
```xml
<dependency>
    <groupId>com.seanox</groupId>
    <artifactId>seanox-pdf-service</artifactId>
    <version>3.7.2</version>
</dependency>
```


# Changes (Change Log)
## 3.7.2 20200530 (summary of the current version)  
BF: Template: Optimization of the statics for case insensitivity  
BF: Template: Correction/optimization for markup detection and escape  
BF: Test: Correction of JUnit5  
CR: Maven: Update of dependencies  
CR: Generator: Optimization of the code  

[Read more](https://raw.githubusercontent.com/seanox/pdf-service/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/pdf-service/issues)  
[Requests](https://github.com/seanox/pdf-service/pulls)  
[Mail](http://seanox.de/contact)  


# Thanks!
<img src="https://raw.githubusercontent.com/seanox/seanox/master/sources/resources/images/thanks.png">

[JetBrains](https://www.jetbrains.com/?from=seanox)  
Sven Lorenz  
Andreas Mitterhofer  
[novaObjects GmbH](https://www.novaobjects.de)  
Leo Pelillo  
Gunter Pfannm&uuml;ller  
Annette und Steffen Pokel  
Edgar R&ouml;stle  
Michael S&auml;mann  
Markus Schlosneck  
[T-Systems International GmbH](https://www.t-systems.com)
