<p>
  <a href="https://github.com/seanox/pdf-service/pulls">
    <img src="https://img.shields.io/badge/development-active-green?style=for-the-badge">
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


# Features
- Built-in markup generator   
very simple syntax, supports placeholders and structures
- Built-in preview and designer with mock-up support  
simplifies the design process by allowing previewing without the target application  
- Independent header, data and footer areas in one template (without magic)  
Header, footer and data area are merged by overlaysand can therefore be formatted independently of the data area  
e.g. margins of the document can be used by the header and footer
- Native support of page numbers
- I18n support  
The language setting is also transferred to the template and thus fonts matching the language can be used
- Creation of markup as preview e.g. for the frontend
- API for other markup renderers and generators  
Abstract templates for individual generators and renderers


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
Coming soon


# Changes (Change Log)
## 3.4.0 20200301 (summary of the current version)  
BF: Service: Optimization of the overlay technique for header and footer  
BF: Preview: Correction in the creation of MockUp lists  
CR: Project: Migration to Maven  
CR: Project: Migration to GitHub  
CR: License: Migration to Apache License 2.0  
CR: Designer: Added support for pure markup files  
CR: Preview: Added support for pure markup files  
CR: Examples: Added various examples  
CR: Preview: Preview data of properties are now also available in header and footer  
CR: Preview: Properties of the preview data are now always searched at the template location  
CR: Template Generate: Change from placeholder 'data' to 'dataset'  
CR: Preview: General optimization of the preview creation.  
CR: Designer: General optimization of the preview creation.  
CR: Designer: Harmonisation of data storage / PDF output  

[Read more](https://raw.githubusercontent.com/seanox/pdf-service/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/aspect-js-tutorial/issues)  
[Requests](https://github.com/seanox/aspect-js-tutorial/pulls)  
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
