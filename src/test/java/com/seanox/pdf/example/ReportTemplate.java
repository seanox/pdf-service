/**
 * PDF Service
 * Copyright (C) 2026 Seanox Software Solutions
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
package com.seanox.pdf.example;

import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Template;

/** 
 * Example of using the PDF service.
 * Template and preview data are in the same package.
 * The resources (CSS, images, fonts, ...) are in the ClassPath /pdf/... and are
 * used in the template relative.
 */
@Resources(base="/pdf", template="/pdf/report.html")
class ReportTemplate extends Template {
}